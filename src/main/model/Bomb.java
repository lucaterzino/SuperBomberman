package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Rappresenta la bomba piazzata dal giocatore.
public class Bomb {

    private static final int FUSE_TIME_FRAMES = 60; // Durata dell'innesco (2 secondi a 30fps)
    
    private int col, row;           // Posizione nella griglia
    private int timerFrames;        // Contatore per l'esplosione
    private double currentRadius;    // Raggio attuale per l'animazione pulsante
    private boolean growing = true; // Direzione dell'animazione (cresce/rimpicciolisce)
    private boolean isRemote;       // Indica se la bomba ha il detonatore remoto

    // Costruttore: imposta la posizione e il tipo (normale o remota).
    public Bomb(int col, int row, boolean isRemote) {
        this.col = col;
        this.row = row;
        this.isRemote = isRemote;
        this.timerFrames = FUSE_TIME_FRAMES;
        this.currentRadius = GameMap.TILE_SIZE * 0.7; // Raggio iniziale per il disegno
    }

    // Metodo di aggiornamento chiamato ad ogni frame.
    public void update() {
        // Il timer scende solo se non è una bomba remota.
        if (!isRemote) timerFrames--;
        
        // Logica per l'animazione pulsante (fa ingrandire e rimpicciolire la bomba)
        double pulseSpeed = 0.2; 
        if (growing) {
            currentRadius += pulseSpeed;
            // Se raggiunge il massimo, inverte la direzione
            if (currentRadius >= GameMap.TILE_SIZE * 0.85) growing = false;
        } else {
            currentRadius -= pulseSpeed;
            // Se raggiunge il minimo, inverte la direzione
            if (currentRadius <= GameMap.TILE_SIZE * 0.6) growing = true;
        }
    }

    // Forza l'esplosione immediata (usato per il detonatore remoto).
    public void triggerExplosion() {
        this.timerFrames = 0;
    }

    // Disegna la bomba in stile Pixel Art.
    public void draw(GraphicsContext gc) {
        double pixelX = col * GameMap.TILE_SIZE;
        double pixelY = row * GameMap.TILE_SIZE;
        // Offset per centrare l'ovale che pulsa
        double offset = (GameMap.TILE_SIZE - currentRadius) / 2;

        // 1. Corpo principale (Nero o Rosso scuro se remota)
        gc.setFill(isRemote ? Color.DARKRED : Color.web("#111111"));
        gc.fillOval(pixelX + offset, pixelY + offset, currentRadius, currentRadius);
        
        // 2. Riflesso (simula una superficie lucida)
        gc.setFill(Color.WHITE);
        gc.fillOval(pixelX + offset + currentRadius*0.2, pixelY + offset + currentRadius*0.2, currentRadius*0.25, currentRadius*0.25);

        // 3. Tappo della miccia (Oro)
        gc.setFill(Color.GOLD);
        gc.fillRect(pixelX + GameMap.TILE_SIZE/2.0 - 6, pixelY + offset - 6, 12, 8);
        
        // 4. Miccia
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double micciaX = pixelX + GameMap.TILE_SIZE/2.0;
        double micciaY = pixelY + offset - 6;
        
        // Animazione della miccia (lampeggio o movimento)
        if (timerFrames % 20 < 10) {
            // Miccia dritta
            gc.strokeLine(micciaX, micciaY, micciaX, micciaY - 8);
        } else {
            // Miccia leggermente piegata
            gc.strokeLine(micciaX, micciaY, micciaX + 4, micciaY - 8);
        }
        
        // 5. Scintilla (solo se non è una bomba remota)
        if (!isRemote) {
            // La scintilla lampeggia velocemente (Rosso/Giallo)
            gc.setFill((timerFrames / 5) % 2 == 0 ? Color.RED : Color.YELLOW);
            gc.fillOval(micciaX - 3, micciaY - 12, 6, 6);
        }
    }

    // Controlla se il timer è scaduto e la bomba deve esplodere.
    public boolean isExploded() {
        return timerFrames <= 0;
    }

    // Restituisce la colonna attuale.
    public int getCol() { return col; }
    // Restituisce la riga attuale.
    public int getRow() { return row; }
}