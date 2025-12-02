package main.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy {

    public enum State { IDLE, MOVING }
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    
    public static final double SIZE = 48; 
    private static final double MOVE_SPEED = 4.75; 

    private double x, y; 
    private int col, row; 
    private double targetX, targetY; 
    private double offset; 
    private State state = State.IDLE;
    private Direction currentDirection; 
    private Random rand = new Random();
    
    private int startCol, startRow;

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
        this.currentDirection = Direction.values()[rand.nextInt(Direction.values().length)];
    }

    // Metodo per resettare il nemico alla posizione originale
    public void resetPosition() {
        this.col = startCol;
        this.row = startRow;
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
        this.state = State.IDLE;
        this.currentDirection = Direction.values()[rand.nextInt(Direction.values().length)];
    }

    public void update(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        if (state == State.MOVING) {
            moveToTarget();
            return;
        }
        if (state == State.IDLE) {
            decideNextMove(map, dangerMap, bombs);
        }
    }

    private void decideNextMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        // 1. Se la casella attuale è pericolosa, cerca via di fuga
        if (dangerMap[row][col]) {
            Direction safeDir = findSafestMove(map, dangerMap, bombs);
            if (safeDir != null) {
                currentDirection = safeDir; 
                if (applyMove(currentDirection)) return;
            }
        }
        
        // 2. Calcola la prossima mossa basata sulla direzione corrente
        int targetCol = col;
        int targetRow = row;
        switch (currentDirection) {
            case UP: targetRow--; break;
            case DOWN: targetRow++; break;
            case LEFT: targetCol--; break;
            case RIGHT: targetCol++; break;
        }
        
        // 3. Se bloccato o in pericolo, cambia direzione
        if (isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            currentDirection = getRandomValidDirection(map, dangerMap, bombs);
        }
        
        // 4. Applica il movimento se la direzione (nuova o vecchia) è valida
        // Nota: usiamo una logica inversa qui per sicurezza
        int checkCol = col;
        int checkRow = row;
        switch (currentDirection) {
            case UP: checkRow--; break;
            case DOWN: checkRow++; break;
            case LEFT: checkCol--; break;
            case RIGHT: checkCol++; break;
        }
        
        if (!isBlocked(checkCol, checkRow, map, dangerMap, bombs)) {
            applyMove(currentDirection);
        }
    }
    
    private boolean applyMove(Direction dir) {
        this.state = State.MOVING;
        switch(dir) {
            case UP: this.row--; break;
            case DOWN: this.row++; break;
            case LEFT: this.col--; break;
            case RIGHT: this.col++; break;
        }
        this.targetX = this.col * GameMap.TILE_SIZE + offset;
        this.targetY = this.row * GameMap.TILE_SIZE + offset;
        return true;
    }
    
    private boolean isBlocked(int c, int r, GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        if (r < 0 || r >= dangerMap.length || c < 0 || c >= dangerMap[0].length) return true;
        return map.isTileSolid(c, r) || dangerMap[r][c] || isBombAt(c, r, bombs);
    }

    private boolean isBombAt(int c, int r, List<Bomb> bombs) {
        for (Bomb b : bombs) { if (b.getCol() == c && b.getRow() == r) return true; }
        return false;
    }
    
    private Direction findSafestMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        List<Direction> safeDirections = new ArrayList<>();
        if (!isBlocked(col, row - 1, map, dangerMap, bombs)) safeDirections.add(Direction.UP);
        if (!isBlocked(col, row + 1, map, dangerMap, bombs)) safeDirections.add(Direction.DOWN);
        if (!isBlocked(col - 1, row, map, dangerMap, bombs)) safeDirections.add(Direction.LEFT);
        if (!isBlocked(col + 1, row, map, dangerMap, bombs)) safeDirections.add(Direction.RIGHT);
        if (!safeDirections.isEmpty()) { 
            return safeDirections.get(new Random().nextInt(safeDirections.size())); 
        }
        return null; 
    }
    
    private Direction getRandomValidDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        Direction safest = findSafestMove(map, dangerMap, bombs);
        if (safest != null) return safest; 
        
        // Fallback: cerca celle non solide (anche se pericolose)
        List<Direction> validDirections = new ArrayList<>();
        if (!map.isTileSolid(col, row-1) && !isBombAt(col, row-1, bombs)) validDirections.add(Direction.UP);
        if (!map.isTileSolid(col, row+1) && !isBombAt(col, row+1, bombs)) validDirections.add(Direction.DOWN);
        if (!map.isTileSolid(col-1, row) && !isBombAt(col-1, row, bombs)) validDirections.add(Direction.LEFT);
        if (!map.isTileSolid(col+1, row) && !isBombAt(col+1, row, bombs)) validDirections.add(Direction.RIGHT);

        if (!validDirections.isEmpty()) { 
            return validDirections.get(new Random().nextInt(validDirections.size())); 
        }
        return currentDirection; 
    }

    // --- CORREZIONE CRITICA ---
    private void moveToTarget() {
        if (x < targetX) { 
            x += MOVE_SPEED; 
            if (x >= targetX) x = targetX; 
        } else if (x > targetX) { 
            x -= MOVE_SPEED; 
            if (x <= targetX) x = targetX; 
        }

        if (y < targetY) { 
            y += MOVE_SPEED; 
            if (y >= targetY) y = targetY; 
        } else if (y > targetY) { 
            y -= MOVE_SPEED; 
            if (y <= targetY) y = targetY; 
        }
        
        // Usa una tolleranza o confronto diretto sicuro
        // Poiché sopra facciamo il "clamp" (if x >= targetX then x = targetX), 
        // l'uguaglianza esatta ora dovrebbe funzionare, ma usiamo Math.abs per sicurezza
        if (Math.abs(x - targetX) < 0.1 && Math.abs(y - targetY) < 0.1) {
             x = targetX;
             y = targetY;
             state = State.IDLE; 
        }
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCol() { return col; }
    public int getRow() { return row; }
}