package main.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
//import main.AudioManager; 
import main.Main;
import main.model.*; 

import java.awt.Point; 
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.List; 
import java.util.Random;

public class GameController {

    @FXML private Canvas gameCanvas;
    private GraphicsContext gc;
    private Player player;
    private GameMap gameMap; 
    private Main mainApp; 
    private AnimationTimer gameLoop;

    private List<Bomb> bombs = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>(); 
    private List<Objective> objectives = new ArrayList<>(); 
    
    private boolean gameWon = false; 
    private boolean gameOver = false;

    private boolean bombKeyPressed = false; 
    private boolean remoteKeyPressed = false; 

    private double enemyOffset = 0; 
    private boolean[][] dangerMap; 

    private static final double TARGET_FPS = 30.0;
    private static final double TARGET_NANO_PER_FRAME = 1_000_000_000.0 / TARGET_FPS;
    private long lastFrameTime = 0; 
    
    private static final int MAX_PLAYER_POWERUPS = 2;
    
    // --- DIMENSIONI E OFFSET ---
    private static final int MAP_COLUMNS = 13;
    private static final int MAP_ROWS = 11;
    
    private static final double HUD_HEIGHT = 80; 
    private static final double WINDOW_WIDTH = 1024;
    private static final double WINDOW_HEIGHT = 768;
    
    // Offset per centrare la mappa
    private static final double MAP_OFFSET_X = (WINDOW_WIDTH - (MAP_COLUMNS * GameMap.TILE_SIZE)) / 2;
    private static final double MAP_OFFSET_Y = HUD_HEIGHT + ((WINDOW_HEIGHT - HUD_HEIGHT - (MAP_ROWS * GameMap.TILE_SIZE)) / 2);

    private int lives = 3;
    private int score = 0;
    private int enemiesKilled = 0;
    private double timeLeft = 240.0; 

    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();

        // Inizializza Mappa Fissa 13x11
        gameMap = new GameMap(MAP_COLUMNS, MAP_ROWS);
        this.dangerMap = new boolean[MAP_ROWS][MAP_COLUMNS];

        double playerOffset = (GameMap.TILE_SIZE - Player.SIZE) / 2.0;
        player = new Player(1, 1, playerOffset); 

        this.enemyOffset = (GameMap.TILE_SIZE - Enemy.SIZE) / 2.0;
        spawnEnemies(MAP_COLUMNS, MAP_ROWS);
        spawnObjectives(MAP_COLUMNS, MAP_ROWS);

        lastFrameTime = System.nanoTime();
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaNano = now - lastFrameTime;
                if (deltaNano >= TARGET_NANO_PER_FRAME) {
                    double deltaTimeSeconds = deltaNano / 1_000_000_000.0;
                    lastFrameTime = now - (long)(deltaNano % TARGET_NANO_PER_FRAME);
                    
                    update(deltaTimeSeconds);
                    draw();
                }
            }
        };
        gameCanvas.setFocusTraversable(true);
    }
    
    private void spawnEnemies(int columns, int rows) {
        enemies.clear(); 
        if (columns > 2 && rows > 2) {
            enemies.add(new Enemy(columns - 2, 1, enemyOffset)); 
            enemies.add(new Enemy(1, rows - 2, enemyOffset));     
            enemies.add(new Enemy(columns - 2, rows - 2, enemyOffset)); 
        }
    }
    
    private void spawnObjectives(int cols, int rows) {
        objectives.clear();
        gameWon = false;
        List<Point> emptyLocations = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c == 1 && r == 1) continue; 
                if (gameMap.getTile(c, r) == TileType.EMPTY) {
                    emptyLocations.add(new Point(c, r));
                }
            }
        }
        Random rand = new Random();
        int objectivesToSpawn = 3;
        if (emptyLocations.size() < objectivesToSpawn) objectivesToSpawn = emptyLocations.size();
        for (int i = 0; i < objectivesToSpawn; i++) {
            if (emptyLocations.isEmpty()) break;
            int index = rand.nextInt(emptyLocations.size());
            Point p = emptyLocations.remove(index);
            objectives.add(new Objective(p.x, p.y));
        }
    }

    public void onKeyPressed(KeyCode code) {
        if (gameWon || gameOver) {
            if (code == KeyCode.ENTER || code == KeyCode.ESCAPE) {
                if (mainApp != null) mainApp.showMenuScreen();
            }
            return;
        }

        if (player.isIdle()) {
            int targetCol = player.getCol();
            int targetRow = player.getRow();
            switch (code) {
                case UP:    targetRow--; break;
                case DOWN:  targetRow++; break;
                case LEFT:  targetCol--; break;
                case RIGHT: targetCol++; break;
                default: break;
            }
            if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                if (!isBombAt(targetCol, targetRow)) {
                    player.moveTo(targetCol, targetRow, gameMap);
                }
            }
        }
        
        if (code == KeyCode.Z && !bombKeyPressed) {
            bombKeyPressed = true;
            placeBomb();
        }

        if (code == KeyCode.X && !remoteKeyPressed) {
            remoteKeyPressed = true;
            triggerRemoteBomb();
        }

        if (code == KeyCode.ESCAPE) {
            if (mainApp != null) mainApp.showMenuScreen();
        }
    }
    
    public void onKeyReleased(KeyCode code) {
        if (code == KeyCode.Z) bombKeyPressed = false;
        if (code == KeyCode.X) remoteKeyPressed = false;
    }

    public void setMainApp(Main mainApp) { this.mainApp = mainApp; }

    public void startGame() {
        AudioManager.getInstance().playMusic("/music/game_theme.mp3");
        
        gameMap = new GameMap(MAP_COLUMNS, MAP_ROWS); 
        player = new Player(1, 1, (GameMap.TILE_SIZE - Player.SIZE) / 2.0); 
        spawnEnemies(MAP_COLUMNS, MAP_ROWS);
        spawnObjectives(MAP_COLUMNS, MAP_ROWS);
        
        powerUps.clear(); 
        bombs.clear();
        
        lives = 3;
        score = 0;
        enemiesKilled = 0;
        timeLeft = 240.0; 
        gameWon = false;
        gameOver = false;
        
        lastFrameTime = System.nanoTime();
        gameLoop.start();
        gameCanvas.requestFocus(); 
    }

    public void stopGame() {
        AudioManager.getInstance().stopMusic();
        gameLoop.stop();
        bombs.clear();
        enemies.clear(); 
        powerUps.clear();
        objectives.clear();
    }

    private void update(double deltaTime) {
        if (gameWon || gameOver) return; 

        timeLeft -= deltaTime;
        if (timeLeft <= 0) {
            timeLeft = 0;
            handleDeath(); 
        }

        updateDangerMap();
        player.update();
        updateBombs(); 
        
        for (Objective obj : objectives) obj.update();

        checkPowerUpCollection(); 
        checkObjectiveCollection(); 

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(gameMap, dangerMap, bombs); 
        }
        
        checkPlayerCollisions();
    }
    
    private void handleDeath() {
        lives--;
        if (lives > 0) {
            player = new Player(1, 1, (GameMap.TILE_SIZE - Player.SIZE) / 2.0);
        } else {
            gameOver = true;
        }
    }

    private void checkObjectiveCollection() {
        Iterator<Objective> it = objectives.iterator();
        while (it.hasNext()) {
            Objective obj = it.next();
            if (obj.getCol() == player.getCol() && obj.getRow() == player.getRow()) {
                player.collectObjective();
                it.remove();
                score += 1000; 
                if (player.hasWon()) gameWon = true;
            }
        }
    }

    private void checkPowerUpCollection() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            if (p.getCol() == player.getCol() && p.getRow() == player.getRow()) {
                boolean collected = player.addPowerUp(p.getType());
                if (collected) {
                    it.remove(); 
                }
            }
        }
    }

    private void placeBomb() {
        if (bombs.size() >= player.getMaxBombs()) return;
        int col = player.getCol();
        int row = player.getRow();
        if (isBombAt(col, row)) return;
        if (!gameMap.isTileSolid(col, row)) {
            bombs.add(new Bomb(col, row, player.hasRemote()));
        }
    }

    private void triggerRemoteBomb() {
        if (!bombs.isEmpty()) {
            Bomb b = bombs.get(0);
            if (player.hasRemote()) b.triggerExplosion();
        }
    }

    private boolean isBombAt(int col, int row) {
        for (Bomb bomb : bombs) {
            if (bomb.getCol() == col && bomb.getRow() == row) return true;
        }
        return false;
    }

    private void updateBombs() {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.update();
            if (bomb.isExploded()) {
                iterator.remove(); 
                explodeBomb(bomb); 
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        int r = bomb.getRow();
        int c = bomb.getCol();
        int radius = player.getExplosionRadius();

        fireAt(r, c); 
        for (int i = 1; i <= radius; i++) { if(!fireAt(r - i, c)) break; }
        for (int i = 1; i <= radius; i++) { if(!fireAt(r + i, c)) break; }
        for (int i = 1; i <= radius; i++) { if(!fireAt(r, c - i)) break; }
        for (int i = 1; i <= radius; i++) { if(!fireAt(r, c + i)) break; }
    }

    private boolean fireAt(int r, int c) {
        TileType type = gameMap.getTile(c, r);
        if (type == TileType.WALL) return false;

        if (gameMap.destroyTile(r, c)) {
            spawnRandomPowerUp(c, r);
        }
        
        checkEnemyHit(c, r);
        return type != TileType.BRICK; 
    }

    private void spawnRandomPowerUp(int col, int row) {
        if (player.getActivePowerUps().size() >= MAX_PLAYER_POWERUPS) return;

        Random rand = new Random();
        if (rand.nextDouble() < 0.15) {
            PowerUpType[] types = PowerUpType.values();
            List<PowerUpType> availableTypes = new ArrayList<>();
            List<PowerUpType> currentPowerUps = player.getActivePowerUps();
            for (PowerUpType t : types) {
                if (!currentPowerUps.contains(t)) availableTypes.add(t);
            }
            if (!availableTypes.isEmpty()) {
                PowerUpType randomType = availableTypes.get(rand.nextInt(availableTypes.size()));
                powerUps.add(new PowerUp(col, row, randomType));
            }
        }
    }
    
    private void checkEnemyHit(int col, int row) {
        boolean removed = enemies.removeIf(enemy -> enemy.getCol() == col && enemy.getRow() == row);
        if (removed) {
            score += 200; 
            enemiesKilled++;
        }
    }
    
    private void checkPlayerCollisions() {
        for (Enemy enemy : enemies) {
            if (enemy.getCol() == player.getCol() && enemy.getRow() == player.getRow()) {
                handleDeath();
            }
        }
    }
    
    private void updateDangerMap() {
        for (int r = 0; r < dangerMap.length; r++) {
            for (int c = 0; c < dangerMap[r].length; c++) dangerMap[r][c] = false;
        }
        int radius = player.getExplosionRadius(); 
        
        for (Bomb bomb : bombs) {
            int r = bomb.getRow();
            int c = bomb.getCol();
            markDanger(r, c); 
            for (int i = 1; i <= radius; i++) { if(!markDangerDir(r - i, c)) break; }
            for (int i = 1; i <= radius; i++) { if(!markDangerDir(r + i, c)) break; }
            for (int i = 1; i <= radius; i++) { if(!markDangerDir(r, c - i)) break; }
            for (int i = 1; i <= radius; i++) { if(!markDangerDir(r, c + i)) break; }
        }
    }
    
    private boolean markDangerDir(int r, int c) {
        if (r < 0 || r >= dangerMap.length || c < 0 || c >= dangerMap[0].length) return false;
        TileType type = gameMap.getTile(c, r);
        if (type == TileType.WALL) return false;
        dangerMap[r][c] = true;
        return type != TileType.BRICK; 
    }
    
    private void markDanger(int r, int c) {
        if (r >= 0 && r < dangerMap.length && c >= 0 && c < dangerMap[0].length) dangerMap[r][c] = true;
    }

    private void draw() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        drawDecorativeBackground();
        drawHUD();

        gc.save();
        gc.translate(MAP_OFFSET_X, MAP_OFFSET_Y);

        gameMap.draw(gc); 
        
        for (PowerUp p : powerUps) p.draw(gc);
        for (Objective obj : objectives) obj.draw(gc); 
        
        for (Bomb bomb : bombs) bomb.draw(gc);
        for (Enemy enemy : enemies) enemy.draw(gc);
        
        player.draw(gc);
        
        gc.restore(); 

        if (gameWon) {
            drawOverlay("VITTORIA!", Color.GOLD);
        } else if (gameOver) {
            drawOverlay("GAME OVER", Color.RED);
        }
    }
    
    private void drawDecorativeBackground() {
        double tileSize = 64; 
        for (double y = HUD_HEIGHT; y < WINDOW_HEIGHT; y += tileSize) {
            for (double x = 0; x < WINDOW_WIDTH; x += tileSize) {
                gc.setFill(Color.web("#204020"));
                gc.fillRect(x, y, tileSize, tileSize);
                gc.setStroke(Color.web("#103010"));
                gc.setLineWidth(2);
                gc.strokeRect(x, y, tileSize, tileSize);
                gc.setFill(Color.web("#306030"));
                gc.fillOval(x+10, y+10, 20, 10);
                gc.fillOval(x+30, y+40, 10, 20);
            }
        }
        gc.setFill(Color.rgb(0,0,0,0.5));
        gc.fillRect(MAP_OFFSET_X + 10, MAP_OFFSET_Y + 10, MAP_COLUMNS * GameMap.TILE_SIZE, MAP_ROWS * GameMap.TILE_SIZE);
    }

    private void drawHUD() {
        // Sfondo HUD
        gc.setFill(Color.web("#008000")); 
        gc.fillRect(0, 0, WINDOW_WIDTH, HUD_HEIGHT);
        
        // Bordo Dorato
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(4);
        gc.strokeRect(2, 2, WINDOW_WIDTH-4, HUD_HEIGHT-4);
        
        // Font stile Pixel (Monospaced)
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setTextAlign(TextAlignment.LEFT);

        // Centriamo verticalmente tutti gli elementi sulla stessa linea
        double centerY = HUD_HEIGHT / 2;
        double startX = 40;

        // --- TESTA DEL PERSONAGGIO ---
        // Disegna centrata rispetto a centerY
        drawMiniPlayerHead(startX, centerY - 15);
        
        startX += 45; // Spazio tra testa e cuore

        // --- CUORE ROSSO CON VITE ---
        // Centrato verticalmente rispetto a centerY
        double heartSize = 1.5;
        double heartX = startX;
        // Il cuore originale è alto circa 25px, scalato 1.5 diventa 37.5px
        // Per centrarlo su centerY, dobbiamo traslare Y di - (altezza/2)
        double heartY = centerY - 20; 
        
        gc.save();
        gc.translate(heartX, heartY);
        gc.scale(heartSize, heartSize);
        
        // Disegno Cuore (SVG Path migliorato e simmetrico)
        gc.setFill(Color.RED);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        String heartPath = "M 12,4 Q 4,4 4,10 Q 4,18 12,24 Q 20,18 20,10 Q 20,4 12,4 z";
        gc.beginPath();
        gc.appendSVGPath(heartPath);
        gc.fill();
        gc.stroke();
        
        gc.restore();

        // Numero vite dentro il cuore (Centrato perfettamente)
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        
        // Coordinate del testo: centro orizzontale del cuore + offset verticale per baseline
        double textX = heartX + (24 * heartSize) / 2; 
        double textY = centerY + 8; // Aggiustamento fine per centratura verticale del font
        
        gc.fillText(String.valueOf(lives), textX, textY); 
        gc.strokeText(String.valueOf(lives), textX, textY);
        gc.setTextAlign(TextAlignment.LEFT);

        // --- Resto dell'HUD (Allineato sulla stessa riga Y) ---
        startX += 90; 
        double textBaselineY = centerY + 10; // Baseline comune per tutto il testo

        // Nemici
        // Icona nemico centrata su centerY
        gc.setFill(Color.RED);
        gc.fillRect(startX, centerY - 12, 24, 24); // Quadrato rosso base
        gc.setFill(Color.WHITE); 
        gc.fillRect(startX + 4, centerY - 8, 6, 6); // Occhio sx
        gc.fillRect(startX + 14, centerY - 8, 6, 6); // Occhio dx
        
        // Testo nemici
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(enemiesKilled), startX + 35, textBaselineY);

        startX += 120;

        // Punteggio
        // Icona moneta centrata
        gc.setFill(Color.GOLD);
        gc.fillOval(startX, centerY - 12, 24, 24);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(startX + 5, centerY - 7, 14, 14);
        
        // Testo Punteggio
        String scoreStr = String.format("%02d", score);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.fillText(scoreStr, startX + 35, textBaselineY);
        gc.strokeText(scoreStr, startX + 35, textBaselineY);

        // Timer (A destra, allineato)
        double timerWidth = 160;
        double timerHeight = 40;
        double timerX = WINDOW_WIDTH - 220;
        double timerY = centerY - timerHeight/2; // Centrato verticalmente
        
        gc.setFill(Color.BLACK);
        gc.fillRect(timerX, timerY, timerWidth, timerHeight);
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(timerX, timerY, timerWidth, timerHeight);

        int minutes = (int) timeLeft / 60;
        int seconds = (int) timeLeft % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        
        gc.setFill(timeLeft < 30 ? Color.RED : Color.WHITE);
        // Testo timer centrato nel box
        gc.fillText(timeString, timerX + 40, textBaselineY);
        
        // PowerUps (Icone piccole, sotto il timer o di lato)
        double iconX = startX + 150; // Dopo il punteggio
        List<PowerUpType> activeAbs = player.getActivePowerUps();
        for (PowerUpType p : activeAbs) {
            gc.setFill(p.getColor());
            gc.fillRect(iconX, centerY - 12, 24, 24);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(iconX, centerY - 12, 24, 24);
            iconX += 35;
        }
    }
    
    private void drawMiniPlayerHead(double x, double y) {
        double size = 30;
        
        // Casco/Testa Bianca (Pixel Style)
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, size, size - 4); 
        
        // Contorno nero
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size - 4);
        
        // Occhi (linee verticali)
        gc.setFill(Color.BLACK);
        gc.fillRect(x + 8, y + 8, 4, 8);
        gc.fillRect(x + 18, y + 8, 4, 8);
        
        // Pom-pom (Antenna)
        gc.setFill(Color.MAGENTA);
        gc.fillRect(x + 10, y - 6, 10, 6);
        gc.strokeRect(x + 10, y - 6, 10, 6);
    }

    private void drawOverlay(String title, Color color) {
        gc.setFill(new Color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        gc.setFill(color);
        gc.setFont(new Font("Arial", 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, gameCanvas.getWidth()/2, gameCanvas.getHeight()/2);
        
        gc.setFont(new Font("Arial", 20));
        gc.setFill(Color.WHITE);
        gc.fillText("Premi ENTER per tornare al menu", gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 50);
        
        if (gameWon) {
             gc.fillText("Punteggio Finale: " + score, gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 90);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }
}