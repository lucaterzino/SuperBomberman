package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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

    public void draw(GraphicsContext gc) {
        if (collected) return; 

        double centerX = col * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2.0;
        double centerY = row * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2.0;
        
        double currentSize = SIZE * scale;
        double x = centerX - currentSize / 2.0;
        double y = centerY - currentSize / 2.0;

        // Ombra
        gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.4));
        gc.fillOval(x + 4, y + 4, currentSize, currentSize);

        // Corpo Perla
        gc.setFill(Color.web("#00CED1")); // Dark Turquoise
        gc.fillOval(x, y, currentSize, currentSize);
        
        // Bordo
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeOval(x, y, currentSize, currentSize);

        // Riflesso
        gc.setFill(Color.WHITE);
        gc.fillOval(x + currentSize * 0.2, y + currentSize * 0.2, currentSize * 0.25, currentSize * 0.25);
        
        // Scintillio
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        double cx = x + currentSize * 0.3;
        double cy = y + currentSize * 0.3;
        gc.strokeLine(cx - 2, cy, cx + 2, cy);
        gc.strokeLine(cx, cy - 2, cx, cy + 2);
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}