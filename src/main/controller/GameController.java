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

import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.List; 
import java.util.Random;

// Classe principale che gestisce la logica del gioco e il rendering.
public class GameController {

    @FXML private Canvas gameCanvas; // Canvas JavaFX iniettato dal file FXML
    private GraphicsContext gc;      // Contesto grafico per disegnare sul Canvas
    private Player player;           // Istanza del giocatore
    private GameMap gameMap;         // Istanza della mappa
    private Main mainApp;            // Riferimento alla classe principale (per cambiare scena)
    private AnimationTimer gameLoop;  // Il loop principale di gioco a passo fisso

    // Stati del Gioco per gestire le schermate di intermezzo e fine partita.
    private enum GameState {
        PLAYING,    // Gioco normale
        RESPAWNING, // Schermata nera "Vite rimaste" dopo la morte
        GAME_OVER,  // Partita terminata
        VICTORY,    // Obiettivi completati
        PAUSED      // Gioco in pausa
    }
    
    private GameState currentState = GameState.PLAYING; // Stato attuale del gioco
    private double stateTimer = 0; // Timer usato per le transizioni (Respawn, Victory)

    // Liste di tutte le entità attive sulla mappa
    private List<Bomb> bombs = new ArrayList<>();       // Bombe piazzate
    private List<Enemy> enemies = new ArrayList<>();    // Nemici attivi
    private List<PowerUp> powerUps = new ArrayList<>(); // Power-up a terra
    private List<Objective> objectives = new ArrayList<>(); // Obiettivi da raccogliere
    private List<Explosion> explosions = new ArrayList<>(); // Lista delle fiamme attive (animazione)
    
    // Flag per il tracciamento della pressione dei tasti
    private boolean bombKeyPressed = false; 
    private boolean remoteKeyPressed = false; 
    private boolean pauseKeyPressed = false;

    private double enemyOffset = 0; 
    private boolean[][] dangerMap; // Mappa booleana per l'IA dei nemici (dove c'è pericolo)

    // Variabili per il Game Loop a Passo Fisso (30 FPS)
    private static final double TARGET_FPS = 30.0;
    private static final double TARGET_NANO_PER_FRAME = 1_000_000_000.0 / TARGET_FPS;
    private long lastFrameTime = 0; 
    
    private static final int MAX_PLAYER_POWERUPS = 2; // Limite ai power-up unici

    // --- VARIABILI DI LAYOUT E DIMENSIONI FISSE ---
    private static final int MAP_COLUMNS = 13;
    private static final int MAP_ROWS = 11;
    private static final double HUD_HEIGHT = 80; 
    private static final double WINDOW_WIDTH = 1024;
    private static final double WINDOW_HEIGHT = 768;
    
    // Calcolo degli offset per centrare la mappa
    private static final double MAP_OFFSET_X = (WINDOW_WIDTH - (MAP_COLUMNS * GameMap.TILE_SIZE)) / 2;
    private static final double MAP_OFFSET_Y = HUD_HEIGHT + ((WINDOW_HEIGHT - HUD_HEIGHT - (MAP_ROWS * GameMap.TILE_SIZE)) / 2);

    // Variabili di stato del gioco (HUD)
    private int lives = 3;             // Vite rimanenti
    private int score = 0;             // Punteggio attuale
    private int enemiesKilled = 0;     // Contatore nemici uccisi
    private double timeLeft = 240.0;   // Tempo rimanente (4 minuti)

    // Classe interna per gestire le coordinate (usata nello spawning)
    private static class Coord {
        int x, y;
        Coord(int x, int y) { this.x = x; this.y = y; }
    }

    // Inizializzazione (chiamata una sola volta dal loader FXML)
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();

        gameMap = new GameMap(MAP_COLUMNS, MAP_ROWS);
        this.dangerMap = new boolean[MAP_ROWS][MAP_COLUMNS];

        // Inizializzazione Player
        double playerOffset = (GameMap.TILE_SIZE - Player.SIZE) / 2.0;
        player = new Player(1, 1, playerOffset); 

        // Inizializzazione Nemici e Obiettivi
        this.enemyOffset = (GameMap.TILE_SIZE - Enemy.SIZE) / 2.0;
        spawnEnemies(MAP_COLUMNS, MAP_ROWS);
        spawnObjectives(MAP_COLUMNS, MAP_ROWS);

        lastFrameTime = System.nanoTime();
        
        // Setup del Game Loop a Passo Fisso
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
    
    // Posiziona i nemici nelle posizioni iniziali predefinite (angoli)
    private void spawnEnemies(int columns, int rows) {
        enemies.clear(); 
        if (columns > 2 && rows > 2) {
            enemies.add(new Enemy(columns - 2, 1, enemyOffset)); 
            enemies.add(new Enemy(1, rows - 2, enemyOffset));     
            enemies.add(new Enemy(columns - 2, rows - 2, enemyOffset)); 
        }
    }
    
    // Posiziona i 3 obiettivi (perle) in celle vuote casuali all'inizio
    private void spawnObjectives(int cols, int rows) {
        objectives.clear();
        List<Coord> emptyLocations = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c == 1 && r == 1) continue; // Salta la posizione di spawn del player
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

    // Gestione della pressione dei tasti (Input Handler)
    public void onKeyPressed(KeyCode code) {
        // Gestione Pausa (tasto ENTER)
        if (code == KeyCode.ENTER) {
            if (currentState != GameState.GAME_OVER && currentState != GameState.VICTORY) {
                if (!pauseKeyPressed) {
                    pauseKeyPressed = true;
                    togglePause();
                }
                return; 
            }
        }
        // Uscita rapida da Game Over o Victory
        if (currentState == GameState.GAME_OVER || currentState == GameState.VICTORY) {
            if (code == KeyCode.ENTER || code == KeyCode.ESCAPE) {
                if (mainApp != null) mainApp.showMenuScreen();
            }
            return;
        }

        // Blocca l'input durante Pausa e Respawn
        if (currentState == GameState.PAUSED || currentState == GameState.RESPAWNING) return;

        // Movimento del giocatore (solo se Idle)
        if (player.isIdle()) {
            int targetCol = player.getCol();
            int targetRow = player.getRow();
            switch (code) {
                case UP: targetRow--; break;
                case DOWN: targetRow++; break;
                case LEFT: targetCol--; break;
                case RIGHT: targetCol++; break;
                default: break;
            }
            if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                if (!isBombAt(targetCol, targetRow)) player.moveTo(targetCol, targetRow, gameMap);
            }
        }
        
        // Azioni
        if (code == KeyCode.Z && !bombKeyPressed) { bombKeyPressed = true; placeBomb(); }
        if (code == KeyCode.X && !remoteKeyPressed) { remoteKeyPressed = true; triggerRemoteBomb(); }
        if (code == KeyCode.ESCAPE) { if (mainApp != null) mainApp.showMenuScreen(); }
    }
    
    // Gestione del rilascio dei tasti (per i flag)
    public void onKeyReleased(KeyCode code) {
        if (code == KeyCode.Z) bombKeyPressed = false;
        if (code == KeyCode.X) remoteKeyPressed = false;
        if (code == KeyCode.ENTER) pauseKeyPressed = false;
    }
    
    // Attiva/disattiva lo stato di pausa.
    private void togglePause() {
        if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
        else if (currentState == GameState.PAUSED) currentState = GameState.PLAYING;
    }

    // Imposta il riferimento all'applicazione Main.
    public void setMainApp(Main mainApp) { this.mainApp = mainApp; }

    // Avvia la partita: inizializza tutti gli stati e avvia il loop.
    public void startGame() {
        // AudioManager.getInstance().playMusic("/music/game_theme.mp3");
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

    // Ferma la partita: ferma il loop e pulisce le liste.
    public void stopGame() {
        // AudioManager.getInstance().stopMusic();
        gameLoop.stop();
        bombs.clear(); enemies.clear(); powerUps.clear(); objectives.clear(); explosions.clear();
    }

    // --- CICLO DI AGGIORNAMENTO (GAME LOOP) ---
    // Aggiorna lo stato del gioco basato sul tempo trascorso (deltaTimeSeconds).
    private void update(double deltaTime) {
        if (currentState == GameState.PAUSED) return;

        // Gestione Stato RESPAWNING
        if (currentState == GameState.RESPAWNING) {
            stateTimer -= deltaTime;
            if (stateTimer <= 0) finishRespawn();
            return; 
        }
        // Gestione Stato VICTORY
        if (currentState == GameState.VICTORY) {
            stateTimer -= deltaTime;
            if (stateTimer <= 0) { if (mainApp != null) mainApp.showMenuScreen(); }
            return;
        }
        // Gestione Stato GAME OVER
        if (currentState == GameState.GAME_OVER) return; 

        // 1. Aggiornamento Timer di Livello
        timeLeft -= deltaTime;
        if (timeLeft <= 0) { timeLeft = 0; handleDeath(); }

        // 2. Logica di Gioco Principale
        updateDangerMap();      // Ricalcola le aree di pericolo per l'IA
        updateExplosions();     // Gestisce le animazioni del fuoco e le collisioni
        player.update();        // Aggiorna lo scivolamento del giocatore
        updateBombs();          // Gestisce i timer delle bombe
        
        for (Objective obj : objectives) obj.update(); // Aggiorna animazione delle perle
        checkPowerUpCollection(); 
        checkObjectiveCollection(); 

        // Aggiorna il movimento dei nemici
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(gameMap, dangerMap, bombs); 
        }
        
        checkPlayerCollisions(); // Controlla la collisione Giocatore vs Nemico
    }
    
    // Gestisce la perdita di una vita.
    private void handleDeath() {
        if (currentState != GameState.PLAYING) return; // Protezione da doppie chiamate
        lives--;
        if (lives > 0) {
            currentState = GameState.RESPAWNING;
            stateTimer = 3.0; // 3 secondi di schermata nera
        } else {
            currentState = GameState.GAME_OVER;
        }
    }
    
    // Riporta il gioco allo stato PLAYING dopo il respawn.
    private void finishRespawn() {
        double playerOffset = (GameMap.TILE_SIZE - Player.SIZE) / 2.0;
        player = new Player(1, 1, playerOffset); // Riposiziona
        
        player.activateImmunity(60); // 2 secondi di immunità
        for (Enemy enemy : enemies) enemy.resetPosition(); // Sposta i nemici
        bombs.clear(); explosions.clear(); // Pulisce bombe e fuoco
        currentState = GameState.PLAYING;
    }

    // --- COLLISIONI E PIAZZAMENTO ---

    // Aggiorna lo stato delle esplosioni e verifica le collisioni con il fuoco.
    private void updateExplosions() {
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion e = it.next();
            e.update();
            if (e.isFinished()) {
                it.remove();
            }
        }
        checkFireCollisions(); // Controlla se qualcuno è morto nel fuoco
    }

    // Verifica se il giocatore o i nemici sono entrati in contatto con le fiamme.
    private void checkFireCollisions() {
        // 1. Giocatore vs Fuoco
        if (!player.isImmune()) {
            for (Explosion e : explosions) {
                if (player.getCol() == e.getCol() && player.getRow() == e.getRow()) {
                    handleDeath();
                    break;
                }
            }
        }
        // 2. Nemici vs Fuoco
        enemies.removeIf(enemy -> {
            for (Explosion e : explosions) {
                if (enemy.getCol() == e.getCol() && enemy.getRow() == e.getRow()) {
                    score += 200; enemiesKilled++; return true;
                }
            }
            return false;
        });
    }

    // Controlla se il giocatore ha raccolto un obiettivo.
    private void checkObjectiveCollection() {
        Iterator<Objective> it = objectives.iterator();
        while (it.hasNext()) {
            Objective obj = it.next();
            if (obj.getCol() == player.getCol() && obj.getRow() == player.getRow()) {
                player.collectObjective();
                it.remove();
                score += 1000; 
                if (player.hasWon()) { currentState = GameState.VICTORY; stateTimer = 10.0; }
            }
        }
    }

    // Controlla se il giocatore ha raccolto un Power-Up.
    private void checkPowerUpCollection() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp p = it.next();
            if (p.getCol() == player.getCol() && p.getRow() == player.getRow()) {
                if (player.addPowerUp(p.getType())) it.remove();
            }
        }
    }

    // Piazzamento della bomba (tasto Z).
    private void placeBomb() {
        if (bombs.size() >= player.getMaxBombs()) return;
        if (isBombAt(player.getCol(), player.getRow())) return;
        if (!gameMap.isTileSolid(player.getCol(), player.getRow())) {
            bombs.add(new Bomb(player.getCol(), player.getRow(), player.hasRemote()));
        }
    }

    // Attivazione remota della bomba (tasto X, se potenziamento attivo).
    private void triggerRemoteBomb() {
        if (!bombs.isEmpty() && player.hasRemote()) bombs.get(0).triggerExplosion();
    }

    // Verifica la presenza di una bomba nella cella data.
    private boolean isBombAt(int col, int row) {
        for (Bomb bomb : bombs) { if (bomb.getCol() == col && bomb.getRow() == row) return true; }
        return false;
    }

    // Aggiorna il timer di tutte le bombe.
    private void updateBombs() {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.update();
            if (bomb.isExploded()) { iterator.remove(); explodeBomb(bomb); }
        }
    }

    // Calcola e innesca l'esplosione a croce.
    private void explodeBomb(Bomb bomb) {
        int r = bomb.getRow(); int c = bomb.getCol();
        int radius = player.getExplosionRadius();
        
        // La cella centrale e le 4 direzioni (fermandosi al muro)
        fireAt(r, c); 
        for (int i = 1; i <= radius; i++) { if(!fireAt(r - i, c)) break; } // SU
        for (int i = 1; i <= radius; i++) { if(!fireAt(r + i, c)) break; } // GIÙ
        for (int i = 1; i <= radius; i++) { if(!fireAt(r, c - i)) break; } // SINISTRA
        for (int i = 1; i <= radius; i++) { if(!fireAt(r, c + i)) break; } // DESTRA
    }

    // Gestisce l'effetto del fuoco su una singola cella (r, c).
    private boolean fireAt(int r, int c) {
        TileType type = gameMap.getTile(c, r);
        if (type == TileType.WALL) return false; // Il muro blocca il fuoco

        // Crea l'oggetto esplosione per l'animazione e la collisione temporanea
        explosions.add(new Explosion(c, r));

        if (gameMap.destroyTile(r, c)) spawnRandomPowerUp(c, r); // Distrugge mattone e spawna power-up
        
        // Il fuoco si ferma se colpisce un mattone
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
        if (removed) { score += 200; enemiesKilled++; }
    }
    
    // Controlla la collisione tra giocatore e nemico.
    private void checkPlayerCollisions() {
        if (currentState != GameState.PLAYING) return;
        if (player.isImmune()) return;

        for (Enemy enemy : enemies) {
            if (enemy.getCol() == player.getCol() && enemy.getRow() == player.getRow()) {
                handleDeath();
                break;
            }
        }
    }
    
    // --- DANGER MAP (IA Nemici) ---
    // Aggiorna la mappa del pericolo (aree coperte da bombe) per l'IA dei nemici.
    private void updateDangerMap() {
        // Pulisce la mappa del pericolo
        for (int r = 0; r < dangerMap.length; r++) {
            for (int c = 0; c < dangerMap[r].length; c++) dangerMap[r][c] = false;
        }
        int radius = player.getExplosionRadius(); 
        
        // Segna tutte le celle nel raggio delle bombe attive come pericolose
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

    // --- DRAWING ---
    // Metodo principale di disegno (chiamato 30 volte al secondo).
    private void draw() {
        // Disegna schermate di intermezzo
        if (currentState == GameState.RESPAWNING) { drawRespawnScreen(); return; }
        if (currentState == GameState.VICTORY) { drawVictoryScreen(); return; }

        // Sfondo base
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Disegno HUD e cornice
        drawDecorativeBackground();
        drawHUD();

        // Traslazione per centrare la mappa e spostarla sotto l'HUD
        gc.save();
        gc.translate(MAP_OFFSET_X, MAP_OFFSET_Y);

        // Disegno Entità
        gameMap.draw(gc); 
        for (PowerUp p : powerUps) p.draw(gc);
        for (Objective obj : objectives) obj.draw(gc); 
        for (Bomb bomb : bombs) bomb.draw(gc);
        for (Enemy enemy : enemies) enemy.draw(gc);
        
        for (Explosion e : explosions) e.draw(gc); // Disegna le fiamme!

        player.draw(gc);
        
        gc.restore(); 

        // Disegno overlay finali (Game Over / Pausa)
        if (currentState == GameState.GAME_OVER) { drawOverlay("GAME OVER", Color.RED); } 
        else if (currentState == GameState.PAUSED) { drawOverlay("PAUSA", Color.LIGHTBLUE); }
    }
    
    // Disegna la schermata di intermezzo dopo aver perso una vita.
    private void drawRespawnScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 50));
        gc.setTextAlign(TextAlignment.CENTER);
        
        gc.setFill(Color.WHITE);
        gc.fillText("VITE RIMASTE: " + lives, gameCanvas.getWidth()/2, gameCanvas.getHeight()/2);
        
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // Disegna la schermata di Vittoria.
    private void drawVictoryScreen() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 80));
        gc.setFill(Color.GOLD);
        gc.fillText("VICTORY", gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 - 50);
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        gc.setFill(Color.WHITE);
        gc.fillText("Punteggio Finale: " + score, gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 20);
        
        gc.setFont(Font.font("Monospaced", 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Menu in " + (int)Math.ceil(stateTimer) + "...", gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 80);
        
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    // Disegna lo sfondo decorativo attorno alla mappa (cornice).
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

    // Disegna l'HUD (Heads-Up Display) in alto.
    private void drawHUD() {
        gc.setFill(Color.web("#008000")); // Sfondo verde brillante
        gc.fillRect(0, 0, WINDOW_WIDTH, HUD_HEIGHT);
        
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(4);
        gc.strokeRect(2, 2, WINDOW_WIDTH-4, HUD_HEIGHT-4); // Bordo arancione
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setTextAlign(TextAlignment.LEFT);

        double centerY = HUD_HEIGHT / 2;
        double startX = 40;

        // 1. Testa del Giocatore e Vite
        drawMiniPlayerHead(startX, centerY - 15);
        startX += 45; 

        double heartSize = 1.5;
        double heartX = startX;
        double heartY = centerY - 20; 
        
        gc.save();
        gc.translate(heartX, heartY);
        gc.scale(heartSize, heartSize);
        
        gc.setFill(Color.RED);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        String heartPath = "M 12,4 Q 4,4 4,10 Q 4,18 12,24 Q 20,18 20,10 Q 20,4 12,4 z";
        gc.beginPath();
        gc.appendSVGPath(heartPath); // Disegna il cuore
        gc.fill();
        gc.stroke();
        
        gc.restore();

        // Numero Vite (centrato nel cuore)
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        
        double textX = heartX + (24 * heartSize) / 2; 
        double textY = centerY + 8; 
        
        gc.fillText(String.valueOf(lives), textX, textY); 
        gc.strokeText(String.valueOf(lives), textX, textY);
        gc.setTextAlign(TextAlignment.LEFT);

        // 2. Nemici Uccisi
        startX += 90; 
        double textBaselineY = centerY + 10; 

        gc.setFill(Color.RED);
        gc.fillRect(startX, centerY - 12, 24, 24); 
        gc.setFill(Color.WHITE); 
        gc.fillRect(startX + 4, centerY - 8, 6, 6); 
        gc.fillRect(startX + 14, centerY - 8, 6, 6); 
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(enemiesKilled), startX + 35, textBaselineY);

        // 3. Punteggio
        startX += 120;

        gc.setFill(Color.GOLD);
        gc.fillOval(startX, centerY - 12, 24, 24);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(startX + 5, centerY - 7, 14, 14);
        
        String scoreStr = String.format("%02d", score);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.fillText(scoreStr, startX + 35, textBaselineY);
        gc.strokeText(scoreStr, startX + 35, textBaselineY);

        // 4. Timer
        double timerWidth = 160;
        double timerHeight = 40;
        double timerX = WINDOW_WIDTH - 220;
        double timerY = centerY - timerHeight/2; 
        gc.setFill(Color.BLACK);
        gc.fillRect(timerX, timerY, timerWidth, timerHeight);
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(timerX, timerY, timerWidth, timerHeight);

        int minutes = (int) timeLeft / 60;
        int seconds = (int) timeLeft % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        
        gc.setFill(timeLeft < 30 ? Color.RED : Color.WHITE);
        gc.fillText(timeString, timerX + 40, textBaselineY);
        
        // 5. Power-Up attivi
        double iconX = startX + 150; 
        List<PowerUpType> activeAbs = player.getActivePowerUps();
        
        for (PowerUpType p : activeAbs) {
            drawMiniPowerUp(p, iconX, centerY - 15, gc);
            
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            gc.setFill(Color.WHITE);
            String name = p.name().replace("_UP", "");
            gc.fillText(name, iconX + 35, centerY + 5);
            
            iconX += 80; 
        }
    }
    
    // Disegna la mini icona del Power-Up (helper per drawHUD).
    private void drawMiniPowerUp(PowerUpType type, double x, double y, GraphicsContext gc) {
        double size = 30;
        gc.setFill(type.getColor());
        gc.fillRect(x, y, size, size);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size);
        
        gc.setFill(Color.WHITE);
        gc.fillRect(x + 8, y + 8, size - 16, size - 16);
    }
    
    // Disegna la testa del giocatore nell'HUD (helper per drawHUD).
    private void drawMiniPlayerHead(double x, double y) {
        double size = 30;
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, size, size - 4); 
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size - 4);
        gc.setFill(Color.BLACK);
        gc.fillRect(x + 8, y + 8, 4, 8);
        gc.fillRect(x + 18, y + 8, 4, 8);
        gc.setFill(Color.MAGENTA);
        gc.fillRect(x + 10, y - 6, 10, 6);
        gc.strokeRect(x + 10, y - 6, 10, 6);
    }

    // Disegna l'overlay scuro per Pausa/Game Over.
    private void drawOverlay(String title, Color color) {
        gc.setFill(new Color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gc.setFill(color);
        gc.setFont(new Font("Arial", 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(title, gameCanvas.getWidth()/2, gameCanvas.getHeight()/2);
        gc.setFont(new Font("Arial", 20));
        
        if (title.equals("PAUSA")) {
            gc.setFill(Color.WHITE);
            gc.fillText("Premi INVIO per riprendere", gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 50);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillText("Premi ENTER per tornare al menu", gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 50);
        }
        
        if (currentState == GameState.VICTORY) {
            gc.fillText("Punteggio Finale: " + score, gameCanvas.getWidth()/2, gameCanvas.getHeight()/2 + 90);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }
}