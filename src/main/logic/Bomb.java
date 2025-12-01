package main.logic;

public class Bomb {
    private static final int FUSE_TIME_FRAMES = 60; 
    private int col, row;
    private int timerFrames;
    private double currentRadius;
    private boolean growing = true;
    private boolean isRemote; 

    public Bomb(int col, int row, boolean isRemote) {
        this.col = col;
        this.row = row;
        this.isRemote = isRemote;
        this.timerFrames = FUSE_TIME_FRAMES;
        this.currentRadius = GameMap.TILE_SIZE * 0.7; 
    }

    public void update() {
        if (!isRemote) timerFrames--;
        double pulseSpeed = 0.2; 
        if (growing) {
            currentRadius += pulseSpeed;
            if (currentRadius >= GameMap.TILE_SIZE * 0.85) growing = false;
        } else {
            currentRadius -= pulseSpeed;
            if (currentRadius <= GameMap.TILE_SIZE * 0.6) growing = true;
        }
    }

    public void triggerExplosion() { this.timerFrames = 0; }
    public boolean isExploded() { return timerFrames <= 0; }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public double getCurrentRadius() { return currentRadius; }
    public boolean isRemote() { return isRemote; }
    public int getTimerFrames() { return timerFrames; }
}