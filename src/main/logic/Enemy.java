package main.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy {

    public enum State { IDLE, MOVING }
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    
    public static final double SIZE = 48; 
    private static final double MOVE_SPEED = 4.5; 
    private static final int CHASE_RADIUS = 5; // Raggio di inseguimento (in caselle)

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

    // Aggiornato per ricevere il Player
    public void update(GameMap map, boolean[][] dangerMap, List<Bomb> bombs, Player player) {
        if (state == State.MOVING) {
            moveToTarget();
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
                if (applyMove(currentDirection)) return;
            }
        }
        
        // 2. PRIORITÀ MEDIA: Inseguimento Giocatore
        if (player != null && isPlayerInRadius(player)) {
            Direction chaseDir = getChaseDirection(map, dangerMap, bombs, player);
            if (chaseDir != null) {
                currentDirection = chaseDir;
                // Continua sotto per applicare il movimento con i controlli standard
            }
        }
        
        // 3. MOVIMENTO STANDARD (Casuale o continuativo)
        int targetCol = col;
        int targetRow = row;
        
        // Prova a mantenere la direzione corrente (inclusa quella di inseguimento appena calcolata)
        switch (currentDirection) {
            case UP: targetRow--; break;
            case DOWN: targetRow++; break;
            case LEFT: targetCol--; break;
            case RIGHT: targetCol++; break;
        }
        
        // Se la direzione è bloccata, ne sceglie una nuova valida a caso
        if (isBlocked(targetCol, targetRow, map, dangerMap, bombs)) {
            currentDirection = getRandomValidDirection(map, dangerMap, bombs);
        }
        
        // Applica il movimento finale
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
    
    // Calcola se il giocatore è nel raggio di "vista"
    private boolean isPlayerInRadius(Player p) {
        int dx = Math.abs(p.getCol() - this.col);
        int dy = Math.abs(p.getRow() - this.row);
        return dx <= CHASE_RADIUS && dy <= CHASE_RADIUS;
    }

    // Determina la direzione migliore per avvicinarsi al giocatore
    private Direction getChaseDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs, Player p) {
        int pCol = p.getCol();
        int pRow = p.getRow();
        
        List<Direction> possibleMoves = new ArrayList<>();
        
        // Aggiungi le direzioni che avvicinano al giocatore E non sono bloccate
        if (pRow < this.row && !isBlocked(col, row - 1, map, dangerMap, bombs)) possibleMoves.add(Direction.UP);
        if (pRow > this.row && !isBlocked(col, row + 1, map, dangerMap, bombs)) possibleMoves.add(Direction.DOWN);
        if (pCol < this.col && !isBlocked(col - 1, row, map, dangerMap, bombs)) possibleMoves.add(Direction.LEFT);
        if (pCol > this.col && !isBlocked(col + 1, row, map, dangerMap, bombs)) possibleMoves.add(Direction.RIGHT);
        
        if (!possibleMoves.isEmpty()) {
            // Scegli a caso tra le direzioni che avvicinano (per non farlo sembrare troppo robotico)
            return possibleMoves.get(rand.nextInt(possibleMoves.size()));
        }
        
        return null; // Nessuna mossa diretta valida verso il giocatore
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
        // Il nemico evita i muri, i mattoni, le zone di pericolo (esplosioni future) e le bombe stesse
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
            return safeDirections.get(rand.nextInt(safeDirections.size())); 
        }
        return null; 
    }
    
    private Direction getRandomValidDirection(GameMap map, boolean[][] dangerMap, List<Bomb> bombs) {
        Direction safest = findSafestMove(map, dangerMap, bombs);
        if (safest != null) return safest; 
        
        // Fallback: cerca celle non solide (anche se pericolose) se è in trappola
        List<Direction> validDirections = new ArrayList<>();
        if (!map.isTileSolid(col, row-1) && !isBombAt(col, row-1, bombs)) validDirections.add(Direction.UP);
        if (!map.isTileSolid(col, row+1) && !isBombAt(col, row+1, bombs)) validDirections.add(Direction.DOWN);
        if (!map.isTileSolid(col-1, row) && !isBombAt(col-1, row, bombs)) validDirections.add(Direction.LEFT);
        if (!map.isTileSolid(col+1, row) && !isBombAt(col+1, row, bombs)) validDirections.add(Direction.RIGHT);

        if (!validDirections.isEmpty()) { 
            return validDirections.get(rand.nextInt(validDirections.size())); 
        }
        return currentDirection; 
    }

    private void moveToTarget() {
        if (x < targetX) { x += MOVE_SPEED; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= MOVE_SPEED; if (x <= targetX) x = targetX; }

        if (y < targetY) { y += MOVE_SPEED; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= MOVE_SPEED; if (y <= targetY) y = targetY; }
        
        if (Math.abs(x - targetX) < 1.0 && Math.abs(y - targetY) < 1.0) {
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