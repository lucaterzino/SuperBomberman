package main.logic;

public class Objective {

    public static final double SIZE = 44; 
    private int col, row;
    private boolean collected = false;
    private double scale = 1.0;
    private boolean growing = true;

    public Objective(int col, int row) {
        this.col = col;
        this.row = row;
    }
    
    public void update() {
        if (growing) {
            scale += 0.01;
            if (scale > 1.1) growing = false;
        } else {
            scale -= 0.01;
            if (scale < 0.9) growing = true;
        }
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
    public double getScale() { return scale; }
}