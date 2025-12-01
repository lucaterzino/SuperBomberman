package main.logic;

public class Explosion {
    private static final int DURATION = 30; 
    private int col, row;
    private int timer;

    public Explosion(int col, int row) {
        this.col = col;
        this.row = row;
        this.timer = DURATION;
    }

    public void update() { timer--; }
    public boolean isFinished() { return timer <= 0; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public int getTimer() { return timer; }
}