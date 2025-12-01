package main.logic;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public enum State { IDLE, MOVING }
    public static final double SIZE = 48; 
    
    private double x, y; 
    private int col, row; 
    private double targetX, targetY; 
    private double offset; 
    private State state = State.IDLE;
    private int immunityFrames = 0;

    private double moveSpeed = 6.0; 
    private int maxBombs = 1;     
    private int explosionRadius = 2; 
    private static final int ABSOLUTE_MAX_BOMBS = 5; 
    private static final double MAX_SPEED = 12.0; 
    private boolean hasRemote = false;
    
    private List<PowerUpType> activePowerUps = new ArrayList<>();
    private static final int MAX_TOTAL_POWERUPS = 2; 

    private int objectivesCollected = 0;
    private static final int TOTAL_OBJECTIVES_TO_WIN = 3;

    public Player(int startCol, int startRow, double offset) {
        this.col = startCol;
        this.row = startRow;
        this.offset = offset;
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
    }

    public void update() {
        if (immunityFrames > 0) immunityFrames--;
        if (state == State.IDLE) return;

        if (x < targetX) { x += moveSpeed; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= moveSpeed; if (x <= targetX) x = targetX; }
        if (y < targetY) { y += moveSpeed; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= moveSpeed; if (y <= targetY) y = targetY; }

        if (Math.abs(x - targetX) < moveSpeed && Math.abs(y - targetY) < moveSpeed) {
            x = targetX; y = targetY; state = State.IDLE; 
        }
    }

    public void activateImmunity(int frames) { immunityFrames = frames; }
    public boolean isImmune() { return immunityFrames > 0; }
    public int getImmunityFrames() { return immunityFrames; }

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
            case SPEED_UP: if (moveSpeed < MAX_SPEED) moveSpeed += 1.0; break;
            case REMOTE: hasRemote = true; break;
        }
    }

    public int getMaxBombs() { return maxBombs; }
    public int getExplosionRadius() { return explosionRadius; }
    public double getMoveSpeed() { return moveSpeed; }
    public boolean hasRemote() { return hasRemote; }
    public List<PowerUpType> getActivePowerUps() { return activePowerUps; }
    public int getObjectivesCollected() { return objectivesCollected; }
    public int getTotalObjectivesToWin() { return TOTAL_OBJECTIVES_TO_WIN; }
    public void collectObjective() { objectivesCollected++; }
    public boolean hasWon() { return objectivesCollected >= TOTAL_OBJECTIVES_TO_WIN; }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isIdle() { return state == State.IDLE; }
}