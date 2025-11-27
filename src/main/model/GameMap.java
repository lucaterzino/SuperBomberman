package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

// Gestisce la griglia di gioco, la generazione procedurale della mappa
// e il rendering di muri e mattoni in stile Pixel Art.
public class GameMap {

    // Dimensione di una singola cella (Tile) in pixel.
    public static final int TILE_SIZE = 60; 

    private TileType[][] grid; // Matrice 2D che rappresenta la mappa [riga][colonna]
    private int columns;       // Numero di colonne (larghezza)
    private int rows;          // Numero di righe (altezza)

    // Costruttore: inizializza le dimensioni della griglia e avvia la generazione.
    public GameMap(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.grid = new TileType[rows][columns];
        generateMap();
    }

    // Genera la disposizione procedurale dei blocchi: muri fissi e mattoni distruttibili.
    private void generateMap() {
        Random rand = new Random();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                
                // 1. Crea i bordi esterni indistruttibili
                if (r == 0 || c == 0 || r == rows - 1 || c == columns - 1) {
                    grid[r][c] = TileType.WALL;
                }
                // 2. Crea la griglia interna di pilastri indistruttibili (posizione pari)
                else if (r % 2 == 0 && c % 2 == 0) {
                    grid[r][c] = TileType.WALL;
                }
                // 3. Posiziona i mattoni distruttibili (BRICK)
                // C'è una probabilità del 25% (0.25) per ogni cella non muro,
                // evitando di posizionarli troppo vicino agli angoli iniziali.
                else if ((r > 2 || c > 2) && (r < rows - 3 || c < columns - 3) && rand.nextDouble() < 0.25) { 
                    grid[r][c] = TileType.BRICK;
                }
                // 4. Il resto è pavimento libero
                else {
                    grid[r][c] = TileType.EMPTY;
                }
            }
        }
        
        // --- Aree di spawn del giocatore (angolo in alto a sinistra) ---
        // Assicura che le celle (1,1), (1,2) e (2,1) siano libere per il player.
        grid[1][1] = TileType.EMPTY;
        grid[1][2] = TileType.EMPTY;
        grid[2][1] = TileType.EMPTY;

        // --- Aree di spawn per i nemici (gli altri 3 angoli) ---
        // Pulisce l'area attorno agli angoli dove i nemici verranno generati.
        if (columns > 3 && rows > 3) {
            grid[1][columns - 2] = TileType.EMPTY;
            grid[rows - 2][1] = TileType.EMPTY;
            grid[rows - 2][columns - 2] = TileType.EMPTY;
        }
    }

    // Disegna l'intera mappa sul contesto grafico in stile Pixel Art.
    public void draw(GraphicsContext gc) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                TileType type = grid[r][c];
                
                double x = c * TILE_SIZE;
                double y = r * TILE_SIZE;

                // --- 1. DISEGNO PAVIMENTO (Sfondo in erba) ---
                gc.setFill(Color.web("#2E8B57")); // Verde scuro base
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.setFill(Color.web("#3CB371")); // Dettagli erba (pixel art)
                gc.fillRect(x + 5, y + 5, 4, 4);
                gc.fillRect(x + 40, y + 15, 4, 4);
                gc.fillRect(x + 20, y + 40, 4, 4);
                gc.fillRect(x + 50, y + 50, 4, 4);

                // --- 2. DISEGNO MURI INDISTRUTTIBILI (WALL) ---
                if (type == TileType.WALL) {
                    // Base del muro (Grigio scuro)
                    gc.setFill(Color.web("#555555"));
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    
                    // Ombre e riflessi per effetto 3D (Pixel Art)
                    gc.setFill(Color.web("#AAAAAA")); // Luce in alto e a sinistra
                    gc.fillRect(x, y, TILE_SIZE, 4);
                    gc.fillRect(x, y, 4, TILE_SIZE);
                    
                    gc.setFill(Color.web("#333333")); // Ombra in basso e a destra
                    gc.fillRect(x, y + TILE_SIZE - 4, TILE_SIZE, 4);
                    gc.fillRect(x + TILE_SIZE - 4, y, 4, TILE_SIZE);
                    
                    // Dettaglio centrale del blocco (simula profondità)
                    gc.setFill(Color.web("#222222"));
                    gc.fillRect(x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                    gc.setFill(Color.web("#666666"));
                    gc.fillRect(x + 14, y + 14, TILE_SIZE - 28, TILE_SIZE - 28);
                }
                // --- 3. DISEGNO MATTONI DISTRUTTIBILI (BRICK) ---
                else if (type == TileType.BRICK) {
                    // Base del mattone (Marrone)
                    gc.setFill(Color.web("#D2691E")); 
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    
                    // Linee di giunzione (simulano il pattern di mattoni)
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeLine(x, y + 15, x + TILE_SIZE, y + 15);
                    gc.strokeLine(x, y + 30, x + TILE_SIZE, y + 30);
                    gc.strokeLine(x, y + 45, x + TILE_SIZE, y + 45);
                    gc.strokeLine(x + 30, y, x + 30, y + 15);
                    gc.strokeLine(x + 15, y + 15, x + 15, y + 30);
                    gc.strokeLine(x + 45, y + 15, x + 45, y + 30);
                    gc.strokeLine(x + 30, y + 30, x + 30, y + 45);
                    gc.strokeLine(x + 15, y + 45, x + 15, y + 60);
                    gc.strokeLine(x + 45, y + 45, x + 45, y + 60);
                    
                    // Ombra leggera in basso e a destra
                    gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.2));
                    gc.fillRect(x + TILE_SIZE - 4, y, 4, TILE_SIZE);
                    gc.fillRect(x, y + TILE_SIZE - 4, TILE_SIZE, 4);
                }
            }
        }
    }

    // Controlla se una data cella è considerata "solida" (blocca il movimento).
    public boolean isTileSolid(int col, int row) {
        // Controlla i limiti della mappa
        if (row < 0 || row >= rows || col < 0 || col >= columns) return true;
        
        TileType type = grid[row][col];
        // Bloccato se è un Muro fisso o un Mattone distruttibile
        return type == TileType.WALL || type == TileType.BRICK;
    }

    // Restituisce il tipo di mattonella in una specifica posizione.
    public TileType getTile(int col, int row) {
        // Se fuori dai limiti, si comporta come un muro
        if (row < 0 || row >= rows || col < 0 || col >= columns) return TileType.WALL;
        return grid[row][col];
    }
    
    // Tenta di distruggere un mattone in una data posizione.
    public boolean destroyTile(int row, int col) {
        // Controlla limiti
        if (row < 0 || row >= rows || col < 0 || col >= columns) return false;
        
        // Se è un mattone, lo cambia in EMPTY (pavimento libero)
        if (grid[row][col] == TileType.BRICK) {
            grid[row][col] = TileType.EMPTY;
            return true; // Distruzione avvenuta
        }
        return false; // Nessuna distruzione
    }
}