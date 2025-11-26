package main.model;

import javafx.scene.paint.Color;

public enum TileType {
    EMPTY(Color.web("#528C52")), // Verde
    WALL(Color.web("#606060")),  // Grigio
    BRICK(Color.web("#AF6D39")), // Arancio
    BOMB(Color.BLACK);

    private final Color color;

    TileType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}