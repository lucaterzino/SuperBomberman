package main.logic;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public enum State { IDLE, MOVING }
    public enum Direction { UP, DOWN, LEFT, RIGHT }
    
    public static final double SIZE = 48; 
    
    private double x, y; 
    private int col, row; 
    private double targetX, targetY; 
    private double offset; 
    private State state = State.IDLE;
    private Direction direction = Direction.DOWN;
    
    // CAMBIO: Immunità in secondi (double) invece che frame
    private double immunityTime = 0;
    
    // CAMBIO: Velocità in Pixel al Secondo (6 px * 30 fps = 180)
    private double moveSpeed = 180.0; 
    private static final double MAX_SPEED = 360.0; // 12 * 30

    private int maxBombs = 1;     
    private int explosionRadius = 2; 
    private static final int ABSOLUTE_MAX_BOMBS = 5; 

    private boolean hasRemote = false;
    
    private List<PowerUpType> activePowerUps = new ArrayList<>();
    private static final int MAX_TOTAL_POWERUPS = 2; 
    private int objectivesCollected = 0;
    private int totalObjectivesToWin = 3;

    public Player(int startCol, int startRow, double offset) {
        this.col = startCol;
        this.row = startRow;
        this.offset = offset;
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
    }

    public void resetAfterDeath(int startCol, int startRow) {
        this.col = startCol;
        this.row = startRow;
        this.x = startCol * GameMap.TILE_SIZE + offset;
        this.y = startRow * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
        this.state = State.IDLE;
        this.direction = Direction.DOWN;
        
        this.activePowerUps.clear();
        this.moveSpeed = 180.0; // Reset a 180
        this.maxBombs = 1;
        this.explosionRadius = 2;
        this.hasRemote = false;
    }

    // NUOVO UPDATE: Accetta deltaTime
    public void update(double deltaTime) {
        if (immunityTime > 0) immunityTime -= deltaTime;
        if (state == State.IDLE) return;

        // Calcola lo spostamento per questo frame
        double step = moveSpeed * deltaTime;

        if (x < targetX) { x += step; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= step; if (x <= targetX) x = targetX; }
        if (y < targetY) { y += step; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= step; if (y <= targetY) y = targetY; }

        if (Math.abs(x - targetX) < step && Math.abs(y - targetY) < step) {
            x = targetX; y = targetY; state = State.IDLE; 
        }
    }

    public void activateImmunity(double seconds) { immunityTime = seconds; }
    public boolean isImmune() { return immunityTime > 0; }
    
    // Metodo helper per il renderer (converte in "frame fittizi" per il lampeggio)
    public int getImmunityFrames() { return (int)(immunityTime * 30); }

    public void setDirection(Direction dir) { this.direction = dir; }
    public Direction getDirection() { return direction; }

    public void moveTo(int targetCol, int targetRow, GameMap map) {
        if (state != State.IDLE) return; 
        if (map.isTileSolid(targetCol, targetRow)) return;
        this.state = State.MOVING;
        this.col = targetCol;
        this.row = targetRow;
        this.targetX = targetCol * GameMap.TILE_SIZE + offset;
        this.targetY = targetRow * GameMap.TILE_SIZE + offset;
    }

    public boolean addPowerUp(PowerUpType type) {
        if (activePowerUps.size() >= MAX_TOTAL_POWERUPS) return false; 
        if (activePowerUps.contains(type)) return false; 
        activePowerUps.add(type);
        applyPowerUpEffect(type);
        return true;
    }

    private void applyPowerUpEffect(PowerUpType type) {
        switch (type) {
            case BOMB_UP: if (maxBombs < ABSOLUTE_MAX_BOMBS) maxBombs++; break;
            case FIRE_UP: explosionRadius++; break;
            case SPEED_UP: if (moveSpeed < MAX_SPEED) moveSpeed += 30.0; break; // +30 px/s
            case REMOTE: hasRemote = true; break;
        }
    }

    public int getMaxBombs() { return maxBombs; }
    public int getExplosionRadius() { return explosionRadius; }
    public double getMoveSpeed() { return moveSpeed; }
    public boolean hasRemote() { return hasRemote; }
    public List<PowerUpType> getActivePowerUps() { return activePowerUps; }
    public void setTotalObjectivesToWin(int count) { this.totalObjectivesToWin = count; }
    public int getTotalObjectivesToWin() { return totalObjectivesToWin; }
    public void collectObjective() { objectivesCollected++; }
    public boolean hasWon() { return objectivesCollected >= totalObjectivesToWin; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isIdle() { return state == State.IDLE; }
}