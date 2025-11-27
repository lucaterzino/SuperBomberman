package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

// Rappresenta il giocatore principale (Bomberman).
public class Player {
    
    // Stati del movimento del giocatore.
    public enum State { IDLE, MOVING }

    public static final double SIZE = 48; // Dimensione del personaggio in pixel
    
    // Statistiche potenziabili
    private double moveSpeed = 6.0;         // Velocità di scivolamento
    private int maxBombs = 1;               // Numero massimo di bombe piazzabili contemporaneamente
    private int explosionRadius = 2;        // Raggio dell'esplosione
    
    // Limiti massimi assoluti
    private static final int ABSOLUTE_MAX_BOMBS = 5; 
    private static final double MAX_SPEED = 12.0; 
    
    // Flag per le abilità speciali (non ancora usate nella logica di collisione)
    private boolean hasKick = false;
    private boolean hasPunch = false;
    private boolean hasRemote = false;
    
    // Tracciamento dei power-up unici attivi
    private List<PowerUpType> activePowerUps = new ArrayList<>();
    private static final int MAX_TOTAL_POWERUPS = 2; // Limite ai power-up unici

    // Obiettivi e condizione di vittoria
    private int objectivesCollected = 0;
    private static final int TOTAL_OBJECTIVES_TO_WIN = 3;

    private double x, y; // Posizione attuale in pixel
    private int col, row; // Posizione attuale nella griglia
    private double targetX, targetY; // Posizione pixel di destinazione
    private double offset; // Offset per centrare il giocatore nel tile
    private State state = State.IDLE; // Stato attuale
    
    private int immunityFrames = 0; // Timer per l'immunità dopo aver perso una vita

    // Costruttore: inizializza il giocatore in una cella (col, row).
    public Player(int startCol, int startRow, double offset) {
        this.col = startCol;
        this.row = startRow;
        this.offset = offset;
        // Calcola la posizione pixel iniziale basata sulla griglia
        this.x = col * GameMap.TILE_SIZE + offset;
        this.y = row * GameMap.TILE_SIZE + offset;
        this.targetX = x;
        this.targetY = y;
    }

    // Aggiorna lo stato, chiamato ad ogni frame.
    public void update() {
        // Gestione Timer Immunità
        if (immunityFrames > 0) {
            immunityFrames--;
        }

        // Se fermo, non fare nulla
        if (state == State.IDLE) return;

        // Logica di scivolamento (muove x verso targetX)
        if (x < targetX) { x += moveSpeed; if (x >= targetX) x = targetX; }
        else if (x > targetX) { x -= moveSpeed; if (x <= targetX) x = targetX; }

        // Logica di scivolamento (muove y verso targetY)
        if (y < targetY) { y += moveSpeed; if (y >= targetY) y = targetY; }
        else if (y > targetY) { y -= moveSpeed; if (y <= targetY) y = targetY; }

        // Controllo raggiungimento target (usando abs per evitare salti)
        if (Math.abs(x - targetX) < moveSpeed && Math.abs(y - targetY) < moveSpeed) {
            x = targetX;
            y = targetY;
            state = State.IDLE; // Raggiunto, torna fermo
        }
    }
    
    // Attiva il timer di immunità.
    public void activateImmunity(int frames) {
        this.immunityFrames = frames;
    }
    
    // Controlla se il giocatore è attualmente immune.
    public boolean isImmune() {
        return immunityFrames > 0;
    }

    // Disegna il giocatore (Bomberman) in stile Pixel Art.
    public void draw(GraphicsContext gc) {
        // Effetto lampeggio: se immune, non disegnare in alcuni frame
        if (immunityFrames > 0 && (immunityFrames / 4) % 2 == 0) return;

        double cx = x + SIZE / 2; // Centro X

        // Ombra sotto i piedi
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillOval(x + 8, y + SIZE - 8, SIZE - 16, 8);

        // Corpo (Blu)
        gc.setFill(Color.BLUE);
        gc.fillRect(x + 14, y + 24, 20, 14);
        
        // Dettagli corpo (Cintura, Stivali/Piedi)
        gc.setFill(Color.BLACK);
        gc.fillRect(x + 14, y + 34, 20, 4);
        gc.setFill(Color.GOLD);
        gc.fillRect(x + 22, y + 34, 4, 4);

        // Testa (Bianca)
        gc.setFill(Color.WHITE);
        gc.fillRect(x + 10, y + 4, 28, 26); 
        
        // Contorno testa
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x + 10, y + 4, 28, 26);

        // Occhi (Neri)
        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 6, y + 14, 4, 8); 
        gc.fillRect(cx + 2, y + 14, 4, 8); 
        
        // Sopracciglia
        gc.strokeLine(cx - 8, y + 12, cx - 2, y + 16);
        gc.strokeLine(cx + 8, y + 12, cx + 2, y + 16);

        // Mani/Guanti (Magenta)
        gc.setFill(Color.MAGENTA);
        gc.fillOval(x + 4, y + 24, 10, 10); 
        gc.fillOval(x + 34, y + 24, 10, 10); 

        // Piedi (Magenta)
        gc.fillOval(x + 10, y + 38, 12, 10); 
        gc.fillOval(x + 26, y + 38, 12, 10); 
        
        // Antenna (Magenta)
        gc.setFill(Color.MAGENTA);
        gc.fillRect(cx - 4, y - 4, 8, 8);
    }

    // Inizia un movimento verso una nuova cella della griglia.
    public void moveTo(int targetCol, int targetRow, GameMap map) {
        if (state != State.IDLE) return; // Muovi solo se fermo
        if (map.isTileSolid(targetCol, targetRow)) return; // Controlla collisione

        this.state = State.MOVING;
        this.col = targetCol;
        this.row = targetRow;
        // Imposta la destinazione precisa in pixel
        this.targetX = targetCol * GameMap.TILE_SIZE + offset;
        this.targetY = targetRow * GameMap.TILE_SIZE + offset;
    }

    // Tenta di aggiungere un power-up all'inventario (Max 2, non ripetibili).
    public boolean addPowerUp(PowerUpType type) {
        if (activePowerUps.size() >= MAX_TOTAL_POWERUPS) return false; 
        if (activePowerUps.contains(type)) return false; 

        activePowerUps.add(type);
        applyPowerUpEffect(type);
        return true;
    }

    // Applica l'effetto del power-up alle statistiche.
    private void applyPowerUpEffect(PowerUpType type) {
        switch (type) {
            case BOMB_UP: increaseBombs(); break;
            case FIRE_UP: increaseRadius(); break;
            case SPEED_UP: increaseSpeed(); break;
            case KICK: hasKick = true; break;
            case PUNCH: hasPunch = true; break;
            case REMOTE: hasRemote = true; break;
        }
    }

    // Incrementa il limite di bombe (fino a 5).
    private void increaseBombs() { if (maxBombs < ABSOLUTE_MAX_BOMBS) maxBombs++; }
    // Incrementa il raggio di esplosione di 1.
    private void increaseRadius() { explosionRadius++; }
    // Incrementa la velocità di 1.0 (fino al MAX_SPEED).
    private void increaseSpeed() { if (moveSpeed < MAX_SPEED) moveSpeed += 1.0; }

    // --- Getters e Status ---
    public int getMaxBombs() { return maxBombs; }
    public int getExplosionRadius() { return explosionRadius; }
    public double getMoveSpeed() { return moveSpeed; }
    public boolean hasRemote() { return hasRemote; }
    public boolean canKick() { return hasKick; }
    public boolean canPunch() { return hasPunch; }
    
    public List<PowerUpType> getActivePowerUps() { return activePowerUps; }
    public int getObjectivesCollected() { return objectivesCollected; }
    public int getTotalObjectivesToWin() { return TOTAL_OBJECTIVES_TO_WIN; }
    public void collectObjective() { objectivesCollected++; }
    public boolean hasWon() { return objectivesCollected >= TOTAL_OBJECTIVES_TO_WIN; }
    
    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isIdle() { return state == State.IDLE; }
    public double getX() { return x; }
    public double getY() { return y; }
}