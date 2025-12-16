package main.logic;

public class PowerUp {
    
    public static final double SIZE = 40; 
    
    private int col, row;
    private PowerUpType type;

    public PowerUp(int col, int row, PowerUpType type) {
        this.col = col;
        this.row = row;
        this.type = type;
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public PowerUpType getType() { return type; }
}