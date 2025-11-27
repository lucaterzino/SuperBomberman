package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collections;
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

    // Metodo per resettare il nemico alla posizione originale
    public void resetPosition() {
        this.col = startCol;
        this.row = startRow;
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
        this.state = State.IDLE;
        // Opzionale: cambia direzione casuale al reset
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

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.ORANGERED);
        gc.fillRect(x + 8, y + 8, SIZE - 16, SIZE - 16);
        gc.fillRect(x + 4, y + 12, 4, SIZE - 24);
        gc.fillRect(x + SIZE - 8, y + 12, 4, SIZE - 24);
        gc.fillRect(x + 12, y + 4, SIZE - 24, 4);
        gc.fillRect(x + 12, y + SIZE - 8, SIZE - 24, 4);
        
        double cx = x + SIZE/2;
        double cy = y + SIZE/2;
        
        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 10, cy - 6, 4, 8); 
        gc.fillRect(cx + 6, cy - 6, 4, 8);  
        
        gc.setFill(Color.WHITE);
        gc.fillRect(cx - 10, cy - 6, 2, 2);
        gc.fillRect(cx + 6, cy - 6, 2, 2);
        
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeLine(cx - 8, cy + 8, cx - 4, cy + 12);
        gc.strokeLine(cx - 4, cy + 12, cx, cy + 8);
        gc.strokeLine(cx, cy + 8, cx + 4, cy + 12);
        gc.strokeLine(cx + 4, cy + 12, cx + 8, cy + 8);
    }

    private void decideNextMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
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
                return; 
            }
            return; 
        }
        int targetCol = col;
        int targetRow = row;
        switch (currentDirection) {
            case UP: targetRow--; break; case DOWN: targetRow++; break;
            case LEFT: targetCol--; break; case RIGHT: targetCol++; break;
        }
        if (isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            currentDirection = getRandomValidDirection(map, dangerMap, bombs);
            targetCol = col;
            targetRow = row;
            switch (currentDirection) {
                case UP: targetRow--; break; case DOWN: targetRow++; break;
                case LEFT: targetCol--; break; case RIGHT: targetCol++; break;
            }
        }
        
        if (!isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            moveTo(targetCol, targetRow);
        }
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
        if (!safeDirections.isEmpty()) { Collections.shuffle(safeDirections); return safeDirections.get(0); }
        return null; 
    }
    
    private Direction getRandomValidDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        Direction safest = findSafestMove(map, dangerMap, bombs);
        if (safest != null) return safest; 
        List<Direction> validDirections = new ArrayList<>();
        if (!map.isTileSolid(col, row-1) && !isBombAt(col, row-1, bombs)) validDirections.add(Direction.UP);
        if (!map.isTileSolid(col, row+1) && !isBombAt(col, row+1, bombs)) validDirections.add(Direction.DOWN);
        if (!map.isTileSolid(col-1, row) && !isBombAt(col-1, row, bombs)) validDirections.add(Direction.LEFT);
        if (!map.isTileSolid(col+1, row) && !isBombAt(col+1, row, bombs)) validDirections.add(Direction.RIGHT);
        if (!validDirections.isEmpty()) { Collections.shuffle(validDirections); return validDirections.get(0); }
        return currentDirection; 
    }

    private void moveTo(int targetCol, int targetRow) {
        this.state = State.MOVING;
        this.col = targetCol;
        this.row = targetRow;
        this.targetX = targetCol * GameMap.TILE_SIZE + offset;
        this.targetY = targetRow * GameMap.TILE_SIZE + offset;
    }
    
    private void moveToTarget() {
        if (x < targetX) { x += MOVE_SPEED; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= MOVE_SPEED; if (x <= targetX) x = targetX; }
        if (y < targetY) { y += MOVE_SPEED; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= MOVE_SPEED; if (y <= targetY) y = targetY; }
        if (x == targetX && y == targetY) state = State.IDLE; 
    }
    
    public int getCol() { return col; }
    public int getRow() { return row; }
}