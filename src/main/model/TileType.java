package main.model;

import javafx.scene.paint.Color;

 // Definisce i tipi di blocchi sulla mappa e il loro colore.
 
public enum TileType {
    EMPTY(Color.web("#528C52")), // Un verde per il pavimento
    WALL(Color.web("#606060")),  // Muro indistruttibile (grigio scuro)
    BRICK(Color.web("#AF6D39")), // Mattone distruttibile (marrone/arancio)
    BOMB(Color.BLACK);           // Una bomba piazzata (colore non usato nel draw, ma per logica)

    private final Color color;

    TileType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}