package main.logic;

import java.util.Random;

public class GameMap {
    public static final int TILE_SIZE = 60; 
    private TileType[][] grid;
    private int columns;
    private int rows;

    // Parametro per la densità dei muri (0.25 = 25%, 0.40 = 40% ecc.)
    private double wallDensity;

    // Costruttore aggiornato
    public GameMap(int columns, int rows, double wallDensity) {
        this.columns = columns;
        this.rows = rows;
        this.wallDensity = wallDensity; // Salviamo la densità
        this.grid = new TileType[rows][columns];
        generateMap();
    }

    private void generateMap() {
        Random rand = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (r == 0 || c == 0 || r == rows - 1 || c == columns - 1) grid[r][c] = TileType.WALL;
                else if (r % 2 == 0 && c % 2 == 0) grid[r][c] = TileType.WALL;
                // Usa la variabile wallDensity invece del valore fisso 0.25
                else if ((r > 2 || c > 2) && (r < rows - 3 || c < columns - 3) && rand.nextDouble() < wallDensity) grid[r][c] = TileType.BRICK;
                else grid[r][c] = TileType.EMPTY;
            }
        }
        // Zona sicura iniziale
        grid[1][1] = TileType.EMPTY; grid[1][2] = TileType.EMPTY; grid[2][1] = TileType.EMPTY;
        if (columns > 3 && rows > 3) {
            grid[1][columns - 2] = TileType.EMPTY;
            grid[rows - 2][1] = TileType.EMPTY;
            grid[rows - 2][columns - 2] = TileType.EMPTY;
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
    
    public int getColumns() { return columns; }
    public int getRows() { return rows; }
}