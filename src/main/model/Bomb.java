package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bomb {

    private static final int FUSE_TIME_FRAMES = 60; // 2 secondi a 30fps
    
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
        
        // Animazione pulsante
        double pulseSpeed = 0.2; 
        if (growing) {
            currentRadius += pulseSpeed;
            if (currentRadius >= GameMap.TILE_SIZE * 0.85) growing = false;
        } else {
            currentRadius -= pulseSpeed;
            if (currentRadius <= GameMap.TILE_SIZE * 0.6) growing = true;
        }
    }

    public void triggerExplosion() {
        this.timerFrames = 0;
    }

    public void draw(GraphicsContext gc) {
        double pixelX = col * GameMap.TILE_SIZE;
        double pixelY = row * GameMap.TILE_SIZE;
        double offset = (GameMap.TILE_SIZE - currentRadius) / 2;

        // --- PIXEL ART BOMB ---
        
        // Corpo (Nero/Blu scuro)
        gc.setFill(isRemote ? Color.DARKRED : Color.web("#111111"));
        gc.fillOval(pixelX + offset, pixelY + offset, currentRadius, currentRadius);
        
        // Riflesso (Bianco, a sinistra)
        gc.setFill(Color.WHITE);
        gc.fillOval(pixelX + offset + currentRadius*0.2, pixelY + offset + currentRadius*0.2, currentRadius*0.25, currentRadius*0.25);

        // Tappo superiore (Giallo)
        gc.setFill(Color.GOLD);
        gc.fillRect(pixelX + GameMap.TILE_SIZE/2.0 - 6, pixelY + offset - 6, 12, 8);
        
        // Miccia (Curva animata)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double micciaX = pixelX + GameMap.TILE_SIZE/2.0;
        double micciaY = pixelY + offset - 6;
        
        if (timerFrames % 20 < 10) {
            // Miccia dritta
            gc.strokeLine(micciaX, micciaY, micciaX, micciaY - 8);
        } else {
            // Miccia curva
            gc.strokeLine(micciaX, micciaY, micciaX + 4, micciaY - 8);
        }
        
        // Scintilla (Rosso/Arancio alternati)
        if (!isRemote) {
            gc.setFill((timerFrames / 5) % 2 == 0 ? Color.RED : Color.YELLOW);
            gc.fillOval(micciaX - 3, micciaY - 12, 6, 6);
        }
    }

    public boolean isExploded() {
        return timerFrames <= 0;
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
}