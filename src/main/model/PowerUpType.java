package main.model;

import javafx.scene.paint.Color;

public enum PowerUpType {
    BOMB_UP(Color.BLACK),
    FIRE_UP(Color.ORANGERED),
    SPEED_UP(Color.CYAN),
    KICK(Color.PURPLE),
    PUNCH(Color.YELLOW),
    REMOTE(Color.LIGHTGREEN);

    private final Color color;

    PowerUpType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}