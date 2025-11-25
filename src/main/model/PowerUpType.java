package main.model;

import javafx.scene.paint.Color;

public enum PowerUpType {
    BOMB_UP(Color.BLACK),        // Aumenta numero bombe
    FIRE_UP(Color.ORANGERED),    // Aumenta raggio esplosione
    SPEED_UP(Color.CYAN),        // Aumenta velocità
    KICK(Color.PURPLE),          // Calcia bombe --- (Implementazione futura)
    PUNCH(Color.YELLOW),         // Lancia bombe ---(Implementazione futura)
    REMOTE(Color.LIGHTGREEN);    // Detonatore remoto

    private final Color color;

    PowerUpType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}