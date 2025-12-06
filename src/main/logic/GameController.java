package main.logic;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import main.Gioco;
import main.view.GameRenderer;

import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.List; 
import java.util.Random;

public class GameController {

    @FXML private Canvas gameCanvas;
    private GameRenderer renderer;
    
    private Player player;
    private GameMap gameMap; 
    private Gioco mainApp; 
    private AnimationTimer gameLoop;

    public enum GameState {
        PLAYING, RESPAWNING, GAME_OVER, VICTORY, PAUSED
    }
    
    private GameState currentState = GameState.PLAYING;
    private double stateTimer = 0; 

    private List<Bomb> bombs = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>(); 
    private List<Objective> objectives = new ArrayList<>(); 
    private List<Explosion> explosions = new ArrayList<>();
    
    private boolean bombKeyPressed = false; 
    private boolean remoteKeyPressed = false; 
    private boolean pauseKeyPressed = false; // Ora utilizzato correttamente

    // --- MENU PAUSA ---
    private String[] pauseOptions = {"OPTIONS", "EXIT"};
    private int pauseIndex = 0;

    private double enemyOffset = 0; 
    private boolean[][] dangerMap; 

    private static final double TARGET_FPS = 30.0;
    private static final double TARGET_NANO_PER_FRAME = 1_000_000_000.0 / TARGET_FPS;
    private long lastFrameTime = 0; 
    
    private static final int MAX_PLAYER_POWERUPS = 2;
    
    private static final int MAP_COLUMNS = 13;
    private static final int MAP_ROWS = 11;
    private static final double WINDOW_WIDTH = 1024;
    private static final double WINDOW_HEIGHT = 768;
    private static final double HUD_HEIGHT = 80;
    
    private static final double MAP_OFFSET_X = (WINDOW_WIDTH - (MAP_COLUMNS * GameMap.TILE_SIZE)) / 2;
    private static final double MAP_OFFSET_Y = HUD_HEIGHT + ((WINDOW_HEIGHT - HUD_HEIGHT - (MAP_ROWS * GameMap.TILE_SIZE)) / 2);

    private int lives = 3;
    private int score = 0;
    private int enemiesKilled = 0;
    private double timeLeft = 240.0; 

    private static class Coord {
        int x, y;
        Coord(int x, int y) { this.x = x; this.y = y; }
    }

    public void initialize() {
        renderer = new GameRenderer(gameCanvas.getGraphicsContext2D(), WINDOW_WIDTH, WINDOW_HEIGHT);
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
    
    public void setMainApp(Gioco mainApp) { 
        this.mainApp = mainApp; 
    }
    
    private void draw() {
        renderer.clear();
        
        if (currentState == GameState.RESPAWNING) {
            renderer.drawRespawnScreen(lives);
            return;
        }
        
        if (currentState == GameState.VICTORY) {
            renderer.drawVictoryScreen(score, stateTimer);
            return;
        }

        renderer.drawHUD(lives, score, enemiesKilled, timeLeft, player.getActivePowerUps());
        renderer.drawGameScene(
            gameMap, player, enemies, bombs, explosions, powerUps, objectives, 
            MAP_OFFSET_X, MAP_OFFSET_Y
        );

        if (currentState == GameState.GAME_OVER) {
            renderer.drawGameOverOverlay();
        } else if (currentState == GameState.PAUSED) {
            renderer.drawPauseMenu(pauseOptions, pauseIndex);
        }
    }

    private void update(double deltaTime) {
        if (currentState == GameState.PAUSED) return;

        if (currentState == GameState.RESPAWNING) {
            stateTimer -= deltaTime;
            if (stateTimer <= 0) {
                finishRespawn();
            }
            return; 
        }
        
        if (currentState == GameState.VICTORY) {
            stateTimer -= deltaTime;
            if (stateTimer <= 0) {
                if (mainApp != null) mainApp.showMenuScreen();
            }
            return;
        }
        
        if (currentState == GameState.GAME_OVER) return; 

        timeLeft -= deltaTime;
        if (timeLeft <= 0) {
            timeLeft = 0;
            handleDeath(); 
        }

        updateDangerMap();
        updateExplosions(); 
        player.update();
        updateBombs(); 
        
        for (Objective obj : objectives) obj.update();
        checkPowerUpCollection(); 
        checkObjectiveCollection(); 

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(gameMap, dangerMap, bombs, player); 
        }
        
        checkPlayerCollisions();
    }

    public void onKeyPressed(KeyCode code) {
        // GESTIONE PAUSA (ESC) - Con controllo pressione ripetuta
        if (code == KeyCode.ESCAPE) {
            if (!pauseKeyPressed) {
                pauseKeyPressed = true;
                if (currentState == GameState.PLAYING) {
                    togglePause();
                } else if (currentState == GameState.PAUSED) {
                    togglePause();
                } else if (currentState == GameState.GAME_OVER || currentState == GameState.VICTORY) {
                    if (mainApp != null) mainApp.showMenuScreen();
                }
            }
            return;
        }

        // GESTIONE INPUT DURANTE LA PAUSA
        if (currentState == GameState.PAUSED) {
            if (code == KeyCode.UP) {
                pauseIndex--;
                if (pauseIndex < 0) pauseIndex = pauseOptions.length - 1;
            } else if (code == KeyCode.DOWN) {
                pauseIndex++;
                if (pauseIndex >= pauseOptions.length) pauseIndex = 0;
            } else if (code == KeyCode.ENTER || code == KeyCode.Z || code == KeyCode.SPACE) {
                executePauseOption();
            }
            return; // Blocca altri input di gioco
        }

        if (currentState == GameState.GAME_OVER || currentState == GameState.VICTORY) {
            if (code == KeyCode.ENTER) {
                if (mainApp != null) mainApp.showMenuScreen();
            }
            return;
        }

        if (currentState == GameState.RESPAWNING) return;

        // INPUT GIOCO NORMALE
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
    }
    
    public void onKeyReleased(KeyCode code) {
        if (code == KeyCode.Z) bombKeyPressed = false;
        if (code == KeyCode.X) remoteKeyPressed = false;
        if (code == KeyCode.ESCAPE) pauseKeyPressed = false; // Reset flag pausa
    }
    
    private void togglePause() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
            pauseIndex = 0;
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
        }
    }
    
    private void executePauseOption() {
        if (pauseIndex == 0) { 
            // OPTIONS
            System.out.println("Opzioni non ancora implementate");
        } else if (pauseIndex == 1) { 
            // EXIT
            if (mainApp != null) mainApp.showMenuScreen();
        }
    }
    
    // ... Metodi di logica standard (spawn, update, collisioni) ...
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
        List<Coord> emptyLocations = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c == 1 && r == 1) continue; 
                if (gameMap.getTile(c, r) == TileType.EMPTY) emptyLocations.add(new Coord(c, r));
            }
        }
        Random rand = new Random();
        int objectivesToSpawn = 3;
        if (emptyLocations.size() < objectivesToSpawn) objectivesToSpawn = emptyLocations.size();
        for (int i = 0; i < objectivesToSpawn; i++) {
            if (emptyLocations.isEmpty()) break;
            int index = rand.nextInt(emptyLocations.size());
            Coord p = emptyLocations.remove(index);
            objectives.add(new Objective(p.x, p.y));
        }
    }
    public void startGame() {
        gameMap = new GameMap(MAP_COLUMNS, MAP_ROWS); 
        player = new Player(1, 1, (GameMap.TILE_SIZE - Player.SIZE) / 2.0); 
        spawnEnemies(MAP_COLUMNS, MAP_ROWS);
        spawnObjectives(MAP_COLUMNS, MAP_ROWS);
        powerUps.clear(); bombs.clear(); explosions.clear(); 
        lives = 3; score = 0; enemiesKilled = 0; timeLeft = 240.0; 
        currentState = GameState.PLAYING;
        lastFrameTime = System.nanoTime();
        gameLoop.start();
        gameCanvas.requestFocus(); 
    }
    public void stopGame() {
        gameLoop.stop();
        bombs.clear(); enemies.clear(); powerUps.clear(); objectives.clear(); explosions.clear();
    }
    private void updateExplosions() {
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion e = it.next(); e.update();
            if (e.isFinished()) it.remove();
        }
        checkFireCollisions();
    }
    private void checkFireCollisions() {
        enemies.removeIf(enemy -> {
            for (Explosion e : explosions) {
                if (enemy.getCol() == e.getCol() && enemy.getRow() == e.getRow()) {
                    score += 200; enemiesKilled++; return true;
                }
            }
            return false;
        });
        if (!player.isImmune()) {
            for (Explosion e : explosions) {
                if (player.getCol() == e.getCol() && player.getRow() == e.getRow()) {
                    handleDeath(); break;
                }
            }
        }
    }
    private void handleDeath() {
        if (currentState != GameState.PLAYING) return; 
        lives--;
        if (lives > 0) { currentState = GameState.RESPAWNING; stateTimer = 3.0; } 
        else { currentState = GameState.GAME_OVER; }
    }
    private void finishRespawn() {
        double playerOffset = (GameMap.TILE_SIZE - Player.SIZE) / 2.0;
        player = new Player(1, 1, playerOffset);
        player.activateImmunity(60);
        for (Enemy enemy : enemies) enemy.resetPosition();
        bombs.clear(); explosions.clear(); 
        currentState = GameState.PLAYING;
    }
    private void checkObjectiveCollection() {
        Iterator<Objective> it = objectives.iterator();
        while (it.hasNext()) {
            Objective obj = it.next();
            if (obj.getCol() == player.getCol() && obj.getRow() == player.getRow()) {
                player.collectObjective(); it.remove(); score += 1000; 
                if (player.hasWon()) { currentState = GameState.VICTORY; stateTimer = 10.0; }
            }
        }
    }
    private void checkPowerUpCollection() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            if (p.getCol() == player.getCol() && p.getRow() == player.getRow()) {
                if (player.addPowerUp(p.getType())) it.remove(); 
            }
        }
    }
    private void placeBomb() {
        if (bombs.size() >= player.getMaxBombs()) return;
        if (isBombAt(player.getCol(), player.getRow())) return;
        if (!gameMap.isTileSolid(player.getCol(), player.getRow())) bombs.add(new Bomb(player.getCol(), player.getRow(), player.hasRemote()));
    }
    private void triggerRemoteBomb() { if (!bombs.isEmpty() && player.hasRemote()) bombs.get(0).triggerExplosion(); }
    private boolean isBombAt(int col, int row) { for (Bomb bomb : bombs) if (bomb.getCol() == col && bomb.getRow() == row) return true; return false; }
    private void updateBombs() {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next(); bomb.update();
            if (bomb.isExploded()) { iterator.remove(); explodeBomb(bomb); }
        }
    }
    private void explodeBomb(Bomb bomb) {
        int r = bomb.getRow(); int c = bomb.getCol(); int radius = player.getExplosionRadius();
        fireAt(r, c); 
        for (int i = 1; i <= radius; i++) { if(!fireAt(r - i, c)) break; }
        for (int i = 1; i <= radius; i++) { if(!fireAt(r + i, c)) break; }
        for (int i = 1; i <= radius; i++) { if(!fireAt(r, c - i)) break; }
        for (int i = 1; i <= radius; i++) { if(!fireAt(r, c + i)) break; }
    }
    private boolean fireAt(int r, int c) {
        TileType type = gameMap.getTile(c, r);
        if (type == TileType.WALL) return false;
        explosions.add(new Explosion(c, r));
        if (gameMap.destroyTile(r, c)) spawnRandomPowerUp(c, r);
        return type != TileType.BRICK; 
    }
    private void spawnRandomPowerUp(int col, int row) {
        if (player.getActivePowerUps().size() >= MAX_PLAYER_POWERUPS) return;
        Random rand = new Random();
        if (rand.nextDouble() < 0.15) {
            PowerUpType[] types = PowerUpType.values();
            List<PowerUpType> availableTypes = new ArrayList<>();
            List<PowerUpType> currentPowerUps = player.getActivePowerUps();
            for (PowerUpType t : types) { if (!currentPowerUps.contains(t)) availableTypes.add(t); }
            if (!availableTypes.isEmpty()) {
                PowerUpType randomType = availableTypes.get(rand.nextInt(availableTypes.size()));
                powerUps.add(new PowerUp(col, row, randomType));
            }
        }
    }
    private void checkPlayerCollisions() {
        if (currentState != GameState.PLAYING) return;
        if (player.isImmune()) return;
        for (Enemy enemy : enemies) {
            if (enemy.getCol() == player.getCol() && enemy.getRow() == player.getRow()) {
                handleDeath(); break;
            }
        }
    }
    private void updateDangerMap() {
        for (int r = 0; r < dangerMap.length; r++) { for (int c = 0; c < dangerMap[r].length; c++) dangerMap[r][c] = false; }
        int radius = player.getExplosionRadius(); 
        for (Bomb bomb : bombs) {
            int r = bomb.getRow(); int c = bomb.getCol();
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
}