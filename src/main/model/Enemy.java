package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Rappresenta un nemico sulla mappa. Gestisce il movimento a griglia e l'IA di base.
public class Enemy {

    // Stati del movimento del nemico
    public enum State { IDLE, MOVING }
    // Direzioni possibili di movimento
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    
    public static final double SIZE = 48; // Dimensione in pixel (per il disegno)
    private static final double MOVE_SPEED = 4.5; // Velocità di scivolamento tra le celle

    private double x, y; // Posizione attuale in pixel
    private int col, row; // Posizione attuale nella griglia
    private double targetX, targetY; // Posizione pixel di destinazione
    private double offset; // Offset per centrare il nemico nel tile
    private State state = State.IDLE; // Stato attuale del nemico
    private Direction currentDirection; // Ultima direzione scelta
    private Random rand = new Random();
    
    private int startCol, startRow; // Posizione di spawn iniziale (per il reset)

    // Costruttore: imposta la posizione iniziale e le variabili di start.
    public Enemy(int startCol, int startRow, double offset) {
        this.startCol = startCol;
        this.startRow = startRow;
        this.col = startCol;
        this.row = startRow;
        this.offset = offset;
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
        // Sceglie una direzione iniziale casuale
        this.currentDirection = Direction.values()[rand.nextInt(Direction.values().length)];
    }

    // Metodo per resettare il nemico alla posizione originale di spawn.
    public void resetPosition() {
        this.col = startCol;
        this.row = startRow;
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
        this.state = State.IDLE;
        // Cambia direzione casuale al reset per una nuova rotta
        this.currentDirection = Direction.values()[rand.nextInt(Direction.values().length)];
    }

    // Metodo principale di aggiornamento (chiamato ad ogni frame).
    public void update(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        // 1. Se si sta già muovendo verso un target, continua lo scivolamento.
        if (state == State.MOVING) {
            moveToTarget();
            return;
        }
        // 2. Se è fermo (IDLE), decide la prossima mossa.
        if (state == State.IDLE) {
            decideNextMove(map, dangerMap, bombs);
        }
    }

    // Disegna il nemico in stile Pixel Art (simulazione del "Balloom").
    public void draw(GraphicsContext gc) {
        // Corpo Arancione (Base e bordi squadrati)
        gc.setFill(Color.ORANGERED);
        gc.fillRect(x + 8, y + 8, SIZE - 16, SIZE - 16);
        gc.fillRect(x + 4, y + 12, 4, SIZE - 24);
        gc.fillRect(x + SIZE - 8, y + 12, 4, SIZE - 24);
        gc.fillRect(x + 12, y + 4, SIZE - 24, 4);
        gc.fillRect(x + 12, y + SIZE - 8, SIZE - 24, 4);
        
        double cx = x + SIZE/2;
        double cy = y + SIZE/2;
        
        // Occhi (Nero)
        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 10, cy - 6, 4, 8); 
        gc.fillRect(cx + 6, cy - 6, 4, 8);  
        
        // Riflesso occhi (Bianco)
        gc.setFill(Color.WHITE);
        gc.fillRect(cx - 10, cy - 6, 2, 2);
        gc.fillRect(cx + 6, cy - 6, 2, 2);
        
        // Bocca (simulazione di una bocca)
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeLine(cx - 8, cy + 8, cx - 4, cy + 12);
        gc.strokeLine(cx - 4, cy + 12, cx, cy + 8);
        gc.strokeLine(cx, cy + 8, cx + 4, cy + 12);
        gc.strokeLine(cx + 4, cy + 12, cx + 8, cy + 8);
    }

    // Logica di Intelligenza Artificiale (IA) del nemico.
    private void decideNextMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        // PRIORITÀ 1: Fuga dal pericolo (DangerMap)
        if (dangerMap[row][col]) {
            Direction safeDir = findSafestMove(map, dangerMap, bombs);
            if (safeDir != null) {
                currentDirection = safeDir; 
                int targetCol = col;
                int targetRow = row;
                switch (currentDirection) {
                    case UP: targetRow--; break; case DOWN: targetRow++; break;
                    case LEFT: targetCol--; break; case RIGHT: targetCol++; break;
                }
                moveTo(targetCol, targetRow);
                return; // Mossa eseguita (fuga)
            }
            return; // Impossibile fuggire (rimane fermo)
        }
        
        // PRIORITÀ 2: Movimento casuale o continuo
        int targetCol = col;
        int targetRow = row;
        
        // Calcola la posizione target basata sulla direzione corrente
        switch (currentDirection) {
            case UP: targetRow--; break; case DOWN: targetRow++; break;
            case LEFT: targetCol--; break; case RIGHT: targetCol++; break;
        }
        
        // Se la mossa nella direzione corrente è bloccata, scegli una nuova direzione casuale.
        if (isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            currentDirection = getRandomValidDirection(map, dangerMap, bombs);
            
            // Ricalcola il target con la nuova direzione
            targetCol = col;
            targetRow = row;
            switch (currentDirection) {
                case UP: targetRow--; break; case DOWN: targetRow++; break;
                case LEFT: targetCol--; break; case RIGHT: targetCol++; break;
            }
        }
        
        // Esegue la mossa finale (se non bloccata dopo il ricalcolo)
        if (!isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            moveTo(targetCol, targetRow);
        }
    }
    
    // Controlla se la casella target è bloccata da muro, mattone, bomba o pericolo imminente.
    private boolean isBlocked(int c, int r, GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        // Controllo limiti della griglia
        if (r < 0 || r >= dangerMap.length || c < 0 || c >= dangerMap[0].length) return true;
        
        // Bloccato da un blocco solido (Muro/Mattone), dalla DangerMap o da una Bomba
        return map.isTileSolid(c, r) || dangerMap[r][c] || isBombAt(c, r, bombs);
    }

    // Controlla se una bomba è già presente in una specifica cella.
    private boolean isBombAt(int c, int r, List<Bomb> bombs) {
        for (Bomb b : bombs) { if (b.getCol() == c && b.getRow() == r) return true; }
        return false;
    }
    
    // Trova la direzione più sicura (non bloccata e non pericolosa) per fuggire.
    private Direction findSafestMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        List<Direction> safeDirections = new ArrayList<>();
        if (!isBlocked(col, row - 1, map, dangerMap, bombs)) safeDirections.add(Direction.UP);
        if (!isBlocked(col, row + 1, map, dangerMap, bombs)) safeDirections.add(Direction.DOWN);
        if (!isBlocked(col - 1, row, map, dangerMap, bombs)) safeDirections.add(Direction.LEFT);
        if (!isBlocked(col + 1, row, map, dangerMap, bombs)) safeDirections.add(Direction.RIGHT);
        
        // Se ci sono opzioni sicure, ne sceglie una a caso.
        if (!safeDirections.isEmpty()) { Collections.shuffle(safeDirections); return safeDirections.get(0); }
        return null; // Nessuna via di fuga
    }
    
    // Trova una direzione valida (non solo per fuggire, ma anche per il movimento base).
    private Direction getRandomValidDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        // Tenta prima di trovare una direzione completamente sicura (fuori da DangerMap)
        Direction safest = findSafestMove(map, dangerMap, bombs);
        if (safest != null) return safest; 
        
        // Se è in un vicolo cieco pericoloso, cerca qualsiasi direzione non bloccata da Muri/Mattoni/Bombe.
        List<Direction> validDirections = new ArrayList<>();
        if (!map.isTileSolid(col, row-1) && !isBombAt(col, row-1, bombs)) validDirections.add(Direction.UP);
        if (!map.isTileSolid(col, row+1) && !isBombAt(col, row+1, bombs)) validDirections.add(Direction.DOWN);
        if (!map.isTileSolid(col-1, row) && !isBombAt(col-1, row, bombs)) validDirections.add(Direction.LEFT);
        if (!map.isTileSolid(col+1, row) && !isBombAt(col+1, row, bombs)) validDirections.add(Direction.RIGHT);
        
        if (!validDirections.isEmpty()) { Collections.shuffle(validDirections); return validDirections.get(0); }
        return currentDirection; // Se non c'è via d'uscita, mantiene la direzione corrente
    }

    // Inizia il movimento verso una nuova cella (imposta il target in pixel).
    private void moveTo(int targetCol, int targetRow) {
        this.state = State.MOVING;
        this.col = targetCol;
        this.row = targetRow;
        this.targetX = targetCol * GameMap.TILE_SIZE + offset;
        this.targetY = targetRow * GameMap.TILE_SIZE + offset;
    }
    
    // Gestisce lo scivolamento del nemico verso la sua destinazione pixel per pixel.
    private void moveToTarget() {
        if (x < targetX) { x += MOVE_SPEED; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= MOVE_SPEED; if (x <= targetX) x = targetX; }
        if (y < targetY) { y += MOVE_SPEED; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= MOVE_SPEED; if (y <= targetY) y = targetY; }
        
        // Quando il target è raggiunto, torna allo stato IDLE.
        if (x == targetX && y == targetY) state = State.IDLE; 
    }
    
    // Restituisce la colonna attuale.
    public int getCol() { return col; }
    // Restituisce la riga attuale.
    public int getRow() { return row; }
}