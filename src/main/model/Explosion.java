package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Explosion {
    
    private static final int DURATION = 30; // Durata dell'esplosione in frame (1 secondo @ 30fps)
    private int col, row;
    private int timer;

    public Explosion(int col, int row) {
        this.col = col;
        this.row = row;
        this.timer = DURATION;
    }

    public void update() {
        timer--;
    }

    public boolean isFinished() {
        return timer <= 0;
    }

    public void draw(GraphicsContext gc) {
        double x = col * GameMap.TILE_SIZE;
        double y = row * GameMap.TILE_SIZE;
        double s = GameMap.TILE_SIZE; // Grandezza tile (60px)
        
        // --- ANIMAZIONE PIXEL ART ---
        
        // 1. Base esterna (Rosso/Arancio scuro) - Forma irregolare a croce
        gc.setFill(Color.ORANGERED);
        gc.fillRect(x + 4, y + 10, s - 8, s - 20); // Orizzontale spesso
        gc.fillRect(x + 10, y + 4, s - 20, s - 8); // Verticale spesso
        
        // 2. Corpo centrale (Arancione/Giallo)
        gc.setFill(Color.ORANGE);
        gc.fillRect(x + 8, y + 8, s - 16, s - 16);
        
        // 3. Nucleo ardente (Giallo)
        gc.setFill(Color.YELLOW);
        gc.fillRect(x + 14, y + 14, s - 28, s - 28);
        
        // 4. Centro bianco (Flash) - pulsa
        if (timer > DURATION / 2) {
            gc.setFill(Color.WHITE);
            gc.fillRect(x + 20, y + 20, s - 40, s - 40);
        }
        
        // 5. Effetto "sfarfallio" sui bordi (Pixel sparsi)
        if (timer % 6 < 3) { // Lampeggia ogni pochi frame
            gc.setFill(Color.YELLOW);
            gc.fillRect(x + 2, y + 25, 4, 10); // Scintilla sx
            gc.fillRect(x + s - 6, y + 25, 4, 10); // Scintilla dx
            gc.fillRect(x + 25, y + 2, 10, 4); // Scintilla su
            gc.fillRect(x + 25, y + s - 6, 10, 4); // Scintilla giù
        }
    }

    public int getCol() { return col; }
    public int getRow() { return row; }
}