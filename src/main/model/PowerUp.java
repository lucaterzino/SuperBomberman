package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Rappresenta un Power-Up sulla mappa che il giocatore può raccogliere.
public class PowerUp {
    
    public static final double SIZE = 40; // Dimensione del Power-Up in pixel
    
    private int col, row;         // Posizione nella griglia
    private PowerUpType type;     // Tipo di potenziamento (BOMB_UP, FIRE_UP, ecc.)

    // Costruttore: imposta posizione e tipo.
    public PowerUp(int col, int row, PowerUpType type) {
        this.col = col;
        this.row = row;
        this.type = type;
    }

    // Disegna il Power-Up sul contesto grafico, includendo l'icona specifica.
    public void draw(GraphicsContext gc) {
        // Calcola la posizione pixel centrata nel tile
        double x = col * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - SIZE) / 2.0;
        double y = row * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - SIZE) / 2.0;

        // 1. Sfondo del Power-Up (colore base dal tipo)
        gc.setFill(type.getColor());
        gc.fillRect(x, y, SIZE, SIZE);
        
        // 2. Bordo bianco
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, SIZE, SIZE);
        
        // Centro per il disegno dell'icona
        double cx = x + SIZE/2;
        double cy = y + SIZE/2;

        // 3. Disegno dell'icona specifica in base al tipo (Pixel Art simulata)
        switch(type) {
            case BOMB_UP: // Icona Bomba
                gc.setFill(Color.BLACK);
                gc.fillOval(cx - 8, cy - 6, 16, 16);
                gc.setFill(Color.WHITE); 
                gc.fillOval(cx - 4, cy - 4, 4, 4);
                break;
            case FIRE_UP: // Icona Fiamma/Fuoco
                gc.setFill(Color.YELLOW);
                gc.fillOval(cx - 6, cy - 4, 12, 12);
                // Triangolo sopra per simulare la fiamma
                gc.fillPolygon(new double[]{cx-6, cx, cx+6}, new double[]{cy, cy-12, cy}, 3);
                gc.setFill(Color.RED);
                gc.fillOval(cx - 3, cy - 1, 6, 6);
                break;
            case SPEED_UP: // Icona Pattino/Ruota
                gc.setFill(Color.RED);
                gc.fillOval(cx - 10, cy, 20, 8); // Base orizzontale
                gc.fillRect(cx - 6, cy - 8, 12, 8); // Parte superiore
                gc.setFill(Color.WHITE); 
                gc.fillOval(cx - 8, cy + 6, 4, 4); // Ruota sinistra
                gc.fillOval(cx + 4, cy + 6, 4, 4); // Ruota destra
                break;
            case KICK: // Icona Calcio/Stivale
                gc.setFill(Color.PURPLE);
                gc.fillRect(cx - 6, cy - 8, 8, 12); // Gamba
                gc.fillRect(cx - 6, cy + 4, 14, 6); // Piede
                break;
            case PUNCH: // Icona Pugno/Guantone
                gc.setFill(Color.RED);
                gc.fillOval(cx - 8, cy - 8, 16, 16);
                break;
            case REMOTE: // Icona Detonatore/Bottone
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(cx - 6, cy - 8, 12, 16); // Telecomando
                gc.setFill(Color.RED); 
                gc.fillOval(cx - 2, cy - 4, 4, 4); // Bottone rosso
                break;
        }
    }

    // Restituisce la colonna attuale.
    public int getCol() { return col; }
    // Restituisce la riga attuale.
    public int getRow() { return row; }
    // Restituisce il tipo di Power-Up.
    public PowerUpType getType() { return type; }
}