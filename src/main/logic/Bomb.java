package main.logic;

public class Bomb {
    private static final double FUSE_TIME = 2.0; 
    private int col, row;
    private double timer;
    private double currentRadius;
    private boolean growing = true;
    private boolean isRemote; 

    public Bomb(int col, int row, boolean isRemote) {
        this.col = col;
        this.row = row;
        this.isRemote = isRemote;
        this.timer = FUSE_TIME;
        this.currentRadius = GameMap.TILE_SIZE * 0.7; 
    }

    // NUOVO UPDATE
    public void update(double deltaTime) {
        if (!isRemote) timer -= deltaTime;
        
        // Animazione pulsazione (adattata al tempo)
        double pulseSpeed = 6.0 * deltaTime; // Circa 0.2 * 30
        
        if (growing) {
            currentRadius += pulseSpeed;
            if (currentRadius >= GameMap.TILE_SIZE * 0.85) growing = false;
        } else {
            currentRadius -= pulseSpeed;
            if (currentRadius <= GameMap.TILE_SIZE * 0.6) growing = true;
        }
    }

    public void triggerExplosion() { this.timer = 0; }
    public boolean isExploded() { return timer <= 0; }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public double getCurrentRadius() { return currentRadius; }
    public boolean isRemote() { return isRemote; }

    // Per il renderer, convertiamo in un valore intero fittizio
    public int getTimerFrames() { return (int)(timer * 30); }
}