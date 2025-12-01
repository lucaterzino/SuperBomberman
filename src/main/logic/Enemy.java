package main.logic;

import java.util.List;
import java.util.Random;

public class Enemy {
    public enum State { IDLE, MOVING }
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    public static final double SIZE = 48; 
    private static final double MOVE_SPEED = 4.5; 

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
        if (dangerMap[row][col]) {
            Direction safeDir = findSafestMove(map, dangerMap, bombs);
            if (safeDir != null) {
                currentDirection = safeDir; 
                setMoveTarget(currentDirection);
                return; 
            }
            return; 
        }
        
        setMoveTarget(currentDirection); // Calcola target ipotetico
        if (isBlocked(col, row, currentDirection, map, dangerMap, bombs)) {
            currentDirection = getRandomValidDirection(map, dangerMap, bombs);
            setMoveTarget(currentDirection);
        }
        
        if (!isBlocked(col, row, currentDirection, map, dangerMap, bombs)) {
             // Avvia movimento
             state = State.MOVING;
        }
    }
    
    private void setMoveTarget(Direction dir) {
         int tc = col; int tr = row;
         switch(dir) { case UP: tr--; break; case DOWN: tr++; break; case LEFT: tc--; break; case RIGHT: tc++; break; }
         // Qui stiamo solo calcolando, l'aggiornamento vero avviene se non è bloccato
         // ... logica semplificata per brevità, nel rendering MVC questo è logica pura
         if (state == State.MOVING) {
             this.col = tc; this.row = tr;
             this.targetX = tc * GameMap.TILE_SIZE + offset;
             this.targetY = tr * GameMap.TILE_SIZE + offset;
         }
    }

    
    private boolean isBlocked(int c, int r, Direction dir, GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
         int tc = c; int tr = r;
         switch(dir) { case UP: tr--; break; case DOWN: tr++; break; case LEFT: tc--; break; case RIGHT: tc++; break; }
         if (tr < 0 || tr >= dangerMap.length || tc < 0 || tc >= dangerMap[0].length) return true;
         return map.isTileSolid(tc, tr) || dangerMap[tr][tc] || isBombAt(tc, tr, bombs);
    }
    // ...

    private void moveToTarget() {
        if (x < targetX) { x += MOVE_SPEED; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= MOVE_SPEED; if (x <= targetX) x = targetX; }
        if (y < targetY) { y += MOVE_SPEED; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= MOVE_SPEED; if (y <= targetY) y = targetY; }
        if (x == targetX && y == targetY) state = State.IDLE; 
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    
    // ... Helper methods ...
    private boolean isBombAt(int c, int r, List<Bomb> bombs) { for (Bomb b : bombs) if (b.getCol() == c && b.getRow() == r) return true; return false; }
    private Direction findSafestMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) { /* ... */ return null; }
    private Direction getRandomValidDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) { /* ... */ return currentDirection; }
}