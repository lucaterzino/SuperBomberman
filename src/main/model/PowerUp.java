package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PowerUp {
    
    public static final double SIZE = 40; 
    
    private int col, row;
    private PowerUpType type;

    public PowerUp(int col, int row, PowerUpType type) {
        this.col = col;
        this.row = row;
        this.type = type;
    }

    public void draw(GraphicsContext gc) {
        double x = col * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - SIZE) / 2.0;
        double y = row * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - SIZE) / 2.0;

        gc.setFill(type.getColor());
        gc.fillRect(x, y, SIZE, SIZE);
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, SIZE, SIZE);
        
        double cx = x + SIZE/2;
        double cy = y + SIZE/2;

        switch(type) {
            case BOMB_UP: 
                gc.setFill(Color.BLACK);
                gc.fillOval(cx - 8, cy - 6, 16, 16);
                gc.setFill(Color.WHITE); 
                gc.fillOval(cx - 4, cy - 4, 4, 4);
                break;
            case FIRE_UP: 
                gc.setFill(Color.YELLOW);
                gc.fillOval(cx - 6, cy - 4, 12, 12);
                gc.fillPolygon(new double[]{cx-6, cx, cx+6}, new double[]{cy, cy-12, cy}, 3);
                gc.setFill(Color.RED);
                gc.fillOval(cx - 3, cy - 1, 6, 6);
                break;
            case SPEED_UP: 
                gc.setFill(Color.RED);
                gc.fillOval(cx - 10, cy, 20, 8); 
                gc.fillRect(cx - 6, cy - 8, 12, 8); 
                gc.setFill(Color.WHITE); 
                gc.fillOval(cx - 8, cy + 6, 4, 4);
                gc.fillOval(cx + 4, cy + 6, 4, 4);
                break;
            case KICK: 
                gc.setFill(Color.PURPLE);
                gc.fillRect(cx - 6, cy - 8, 8, 12); 
                gc.fillRect(cx - 6, cy + 4, 14, 6); 
                break;
            case PUNCH: 
                gc.setFill(Color.RED);
                gc.fillOval(cx - 8, cy - 8, 16, 16);
                break;
            case REMOTE: 
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(cx - 6, cy - 8, 12, 16);
                gc.setFill(Color.RED); 
                gc.fillOval(cx - 2, cy - 4, 4, 4);
                break;
        }
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public PowerUpType getType() { return type; }
}