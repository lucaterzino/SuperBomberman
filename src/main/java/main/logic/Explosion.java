package main.logic;

public class Explosion {
    private static final double DURATION = 1.0; // 1 Secondo
    private int col, row;
    private double timer;

    public Explosion(int col, int row) {
        this.col = col;
        this.row = row;
        this.timer = DURATION;
    }

    public void update(double deltaTime) { timer -= deltaTime; }
    public boolean isFinished() { return timer <= 0; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    
    // Per il renderer
    public int getTimer() { return (int)(timer * 30); }
}