package main.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy {

    public enum State { IDLE, MOVING }
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    
    public static final double SIZE = 48; 
    private static final double MOVE_SPEED = 135.0; // Velocità corretta per il DeltaTime
    private static final int CHASE_RADIUS = 5; 

    private double x, y; 
    private int col, row; 
    private double targetX, targetY; 
    @SuppressWarnings("FieldMayBeFinal")
    private double offset; 
    private State state = State.IDLE;
    private Direction currentDirection; 
    @SuppressWarnings("FieldMayBeFinal")
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

    public void update(GameMap map, boolean[][] dangerMap, List<Bomb> bombs, Player player, double deltaTime) {
        if (state == State.MOVING) {
            moveToTarget(deltaTime);
            return;
        }
        if (state == State.IDLE) {
            decideNextMove(map, dangerMap, bombs, player);
        }
    }

    private void decideNextMove(GameMap map, boolean[][] dangerMap, List<Bomb> bombs, Player player) {
        // 1. PRIORITÀ ALTA: Fuga dal pericolo (Bomba vicina)
        if (dangerMap[row][col]) {
            Direction safeDir = findSafestMove(map, dangerMap, bombs);
            if (safeDir != null) {
                currentDirection = safeDir; 
                applyMove(currentDirection);
                return;
            }
        }
        
        // 2. PRIORITÀ MEDIA: Inseguimento Giocatore
        if (player != null && isPlayerInRadius(player)) {
            Direction chaseDir = getChaseDirection(map, dangerMap, bombs, player);
            if (chaseDir != null) {
                currentDirection = chaseDir;
            }
        }
        
        // Controllo se la direzione corrente è bloccata (muro o BOMBA)
        int targetCol = col;
        int targetRow = row;
        switch (currentDirection) {
            case UP: targetRow--; break;
            case DOWN: targetRow++; break;
            case LEFT: targetCol--; break;
            case RIGHT: targetCol++; break;
        }
        
        // Se la direzione scelta è bloccata, cerchiamo un'alternativa
        if (isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            Direction altDir = getAlternativeDirection(map, dangerMap, bombs);
            
            if (altDir != null) {
                // Trovata via libera: muoviti
                currentDirection = altDir;
                applyMove(currentDirection);
            } else {
                // BUG FIX: Se altDir è null, significa che siamo COMPLETAMENTE circondati.
                // In questo caso, NON chiamare applyMove. Resta fermo e prova una nuova direzione a caso.
                currentDirection = Direction.values()[rand.nextInt(Direction.values().length)];
            }
        } else {
            // La direzione corrente è libera, procedi
            applyMove(currentDirection);
        }
    }
    
    private Direction getAlternativeDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        // Prima prova a trovare una direzione sicura (no bombe, no muri)
        Direction safe = findSafestMove(map, dangerMap, bombs);
        if (safe != null) return safe;
        
        // Se non ci sono direzioni sicure, cerca qualsiasi casella che non sia un muro/bomba (anche se pericolosa)
        List<Direction> panicMoves = new ArrayList<>();
        if (!isHardBlocked(col, row-1, map, bombs)) panicMoves.add(Direction.UP);
        if (!isHardBlocked(col, row+1, map, bombs)) panicMoves.add(Direction.DOWN);
        if (!isHardBlocked(col-1, row, map, bombs)) panicMoves.add(Direction.LEFT);
        if (!isHardBlocked(col+1, row, map, bombs)) panicMoves.add(Direction.RIGHT);
        
        if (!panicMoves.isEmpty()) {
            return panicMoves.get(rand.nextInt(panicMoves.size()));
        }
        
        // BUG FIX: Se panicMoves è vuota, significa che siamo murati vivi. 
        // Ritorniamo null invece di forzare il movimento.
        return null;
    }
    
    private Direction getOppositeDirection(Direction dir) {
        switch(dir) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            default: return Direction.DOWN;
        }
    }

    private boolean isPlayerInRadius(Player p) {
        int dx = Math.abs(p.getCol() - this.col);
        int dy = Math.abs(p.getRow() - this.row);
        return dx <= CHASE_RADIUS && dy <= CHASE_RADIUS;
    }

    private Direction getChaseDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs, Player p) {
        int pCol = p.getCol(); int pRow = p.getRow();
        List<Direction> possibleMoves = new ArrayList<>();
        if (pRow < this.row && !isBlocked(col, row - 1, map, dangerMap, bombs)) possibleMoves.add(Direction.UP);
        if (pRow > this.row && !isBlocked(col, row + 1, map, dangerMap, bombs)) possibleMoves.add(Direction.DOWN);
        if (pCol < this.col && !isBlocked(col - 1, row, map, dangerMap, bombs)) possibleMoves.add(Direction.LEFT);
        if (pCol > this.col && !isBlocked(col + 1, row, map, dangerMap, bombs)) possibleMoves.add(Direction.RIGHT);
        if (!possibleMoves.isEmpty()) return possibleMoves.get(rand.nextInt(possibleMoves.size()));
        return null;
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
    
    private boolean isHardBlocked(int c, int r, GameMap map, List<Bomb> bombs) {
        if (r < 0 || c < 0) return true; 
        return map.isTileSolid(c, r) || isBombAt(c, r, bombs);
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
        if (!safeDirections.isEmpty()) return safeDirections.get(rand.nextInt(safeDirections.size())); 
        return null; 
    }
    
    private void moveToTarget(double deltaTime) {
        double step = MOVE_SPEED * deltaTime;
        
        if (x < targetX) { x += step; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= step; if (x <= targetX) x = targetX; }
        if (y < targetY) { y += step; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= step; if (y <= targetY) y = targetY; }

        if (Math.abs(x - targetX) < 1.0 && Math.abs(y - targetY) < 1.0) {
             x = targetX; y = targetY; state = State.IDLE; 
        }
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCol() { return col; }
    public int getRow() { return row; }
}