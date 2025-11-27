package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Rappresenta l'obiettivo collezionabile (la Perla) sulla mappa.
public class Objective {

    public static final double SIZE = 44; // Dimensione base dell'oggetto
    private int col, row;                 // Posizione nella griglia
    private boolean collected = false;    // Stato: true se raccolto dal giocatore
    
    // Variabili per l'animazione pulsante
    private double scale = 1.0;           // Fattore di scala attuale (per pulsazione)
    private boolean growing = true;       // Direzione dell'animazione (cresce o rimpicciolisce)

    // Costruttore: imposta la posizione di spawn.
    public Objective(int col, int row) {
        this.col = col;
        this.row = row;
    }
    
    // Aggiorna lo stato, principalmente per gestire l'animazione di pulsazione.
    public void update() {
        if (growing) {
            scale += 0.01; // Aumenta la scala
            if (scale > 1.1) growing = false; // Raggiunto il massimo, inizia a rimpicciolire
        } else {
            scale -= 0.01; // Diminuisce la scala
            if (scale < 0.9) growing = true; // Raggiunto il minimo, inizia a crescere
        }
    }

    // Disegna la Perla sul contesto grafico.
    public void draw(GraphicsContext gc) {
        // Non disegnare se è stata raccolta
        if (collected) return; 

        // Calcola il centro esatto della cella (per centrare la perla)
        double centerX = col * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2.0;
        double centerY = row * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2.0;
        
        // Calcola la dimensione attuale basata sull'animazione (scala)
        double currentSize = SIZE * scale;
        double x = centerX - currentSize / 2.0;
        double y = centerY - currentSize / 2.0;

        // 1. Ombra della perla (per effetto 3D)
        gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.4));
        gc.fillOval(x + 4, y + 4, currentSize, currentSize);

        // 2. Corpo principale della perla (Azzurro ciano)
        gc.setFill(Color.web("#00CED1")); 
        gc.fillOval(x, y, currentSize, currentSize);
        
        // 3. Bordo dorato (per farla risaltare)
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeOval(x, y, currentSize, currentSize);

        // 4. Riflesso di luce (punto bianco)
        gc.setFill(Color.WHITE);
        gc.fillOval(x + currentSize * 0.2, y + currentSize * 0.2, currentSize * 0.25, currentSize * 0.25);
        
        // 5. Linee di brillantezza (simulano il luccichio)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        double cx = x + currentSize * 0.3;
        double cy = y + currentSize * 0.3;
        gc.strokeLine(cx - 2, cy, cx + 2, cy); // Linea orizzontale
        gc.strokeLine(cx, cy - 2, cx, cy + 2); // Linea verticale
    }

    // Restituisce la colonna attuale.
    public int getCol() { return col; }
    // Restituisce la riga attuale.
    public int getRow() { return row; }
    // Controlla se la perla è stata raccolta.
    public boolean isCollected() { return collected; }
    // Imposta lo stato di raccolta.
    public void setCollected(boolean collected) { this.collected = collected; }
}