package main.model;

import javafx.scene.paint.Color;

// Enumerazione che definisce i tipi di potenziamenti e il loro colore base.
public enum PowerUpType {
    // Aumenta il numero massimo di bombe piazzabili
    BOMB_UP(Color.BLACK),

    // Aumenta il raggio dell'esplosione (Fire)
    FIRE_UP(Color.ORANGERED),

    // Aumenta la velocità di movimento del giocatore
    SPEED_UP(Color.CYAN),

    // Abilità: Permette di calciare le bombe (non implementato nella logica di collisione)
    KICK(Color.PURPLE),

    // Abilità: Permette di lanciare le bombe (non implementato nella logica di collisione)
    PUNCH(Color.YELLOW),
    
    // Abilità: Permette la detonazione remota (tasto X)
    REMOTE(Color.LIGHTGREEN);

    private final Color color; // Colore associato al tipo di Power-Up

    // Costruttore: associa un colore a ciascun tipo.
    PowerUpType(Color color) {
        this.color = color;
    }

    // Restituisce il colore associato al Power-Up.
    public Color getColor() {
        return color;
    }
}