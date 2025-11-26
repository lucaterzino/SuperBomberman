package main.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class GameMap {

    public static final int TILE_SIZE = 60; 

    private TileType[][] grid;
    private int columns;
    private int rows;

    public GameMap(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.grid = new TileType[rows][columns];
        generateMap();
    }

    private void generateMap() {
        Random rand = new Random();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (r == 0 || c == 0 || r == rows - 1 || c == columns - 1) {
                    grid[r][c] = TileType.WALL;
                }
                else if (r % 2 == 0 && c % 2 == 0) {
                    grid[r][c] = TileType.WALL;
                }
                else if ((r > 2 || c > 2) && (r < rows - 3 || c < columns - 3) && rand.nextDouble() < 0.25) { 
                    grid[r][c] = TileType.BRICK;
                }
                else {
                    grid[r][c] = TileType.EMPTY;
                }
            }
        }
        
        grid[1][1] = TileType.EMPTY;
        grid[1][2] = TileType.EMPTY;
        grid[2][1] = TileType.EMPTY;

        if (columns > 3 && rows > 3) {
            grid[1][columns - 2] = TileType.EMPTY;
            grid[rows - 2][1] = TileType.EMPTY;
            grid[rows - 2][columns - 2] = TileType.EMPTY;
        }
    }

    public void draw(GraphicsContext gc) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                TileType type = grid[r][c];
                
                double x = c * TILE_SIZE;
                double y = r * TILE_SIZE;

                // --- PAVIMENTO (Pixel Art Erba) ---
                gc.setFill(Color.web("#2E8B57")); // SeaGreen base
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                // Dettagli erba
                gc.setFill(Color.web("#3CB371")); 
                gc.fillRect(x + 5, y + 5, 4, 4);
                gc.fillRect(x + 40, y + 15, 4, 4);
                gc.fillRect(x + 20, y + 40, 4, 4);
                gc.fillRect(x + 50, y + 50, 4, 4);

                if (type == TileType.WALL) {
                    // --- MURO INDISTRUTTIBILE ---
                    gc.setFill(Color.web("#555555"));
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    gc.setFill(Color.web("#AAAAAA"));
                    gc.fillRect(x, y, TILE_SIZE, 4);
                    gc.fillRect(x, y, 4, TILE_SIZE);
                    gc.setFill(Color.web("#333333"));
                    gc.fillRect(x, y + TILE_SIZE - 4, TILE_SIZE, 4);
                    gc.fillRect(x + TILE_SIZE - 4, y, 4, TILE_SIZE);
                    gc.setFill(Color.web("#222222"));
                    gc.fillRect(x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                    gc.setFill(Color.web("#666666"));
                    gc.fillRect(x + 14, y + 14, TILE_SIZE - 28, TILE_SIZE - 28);
                }
                else if (type == TileType.BRICK) {
                    // --- MATTONE DISTRUTTIBILE ---
                    gc.setFill(Color.web("#D2691E")); 
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
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
                    gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.2));
                    gc.fillRect(x + TILE_SIZE - 4, y, 4, TILE_SIZE);
                    gc.fillRect(x, y + TILE_SIZE - 4, TILE_SIZE, 4);
                }
            }
        }
    }

    public boolean isTileSolid(int col, int row) {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return true;
        TileType type = grid[row][col];
        return type == TileType.WALL || type == TileType.BRICK;
    }

    public TileType getTile(int col, int row) {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return TileType.WALL;
        return grid[row][col];
    }
    
    public boolean destroyTile(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= columns) return false;
        if (grid[row][col] == TileType.BRICK) {
            grid[row][col] = TileType.EMPTY;
            return true; 
        }
        return false;
    }
}