package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class EndGameRenderer {
    private GraphicsContext gc;
    private double w, h;

    public EndGameRenderer(GraphicsContext gc, double w, double h) {
        this.gc = gc; this.w = w; this.h = h;
    }

    public void drawEndScreen(double time) {
        gc.clearRect(0, 0, w, h);
        
        // Sfondo che cambia colore lentamente (effetto "Party")
        double hue = (time * 50) % 360;
        gc.setFill(Color.hsb(hue, 0.6, 0.2)); 
        gc.fillRect(0, 0, w, h);
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Titolo
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 80));
        gc.setFill(Color.YELLOW); gc.setStroke(Color.ORANGE); gc.setLineWidth(3);
        gc.fillText("CONGRATULATIONS!", w/2, 200);
        gc.strokeText("CONGRATULATIONS!", w/2, 200);

        // Sottotitolo
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 40));
        gc.setFill(Color.WHITE);
        gc.fillText("YOU CLEARED ALL LEVELS!", w/2, 300);
        
        // Crediti o Messaggio finale
        gc.setFont(Font.font("Arial", 30));
        gc.setFill(Color.LIGHTGREEN);
        gc.fillText("WORLD 1 COMPLETED", w/2, 450);
        
        // Istruzioni per uscire (lampeggianti)
        if ((int)(time * 2) % 2 == 0) {
            gc.setFont(Font.font("Monospaced", 25));
            gc.setFill(Color.WHITE);
            gc.fillText("PRESS ENTER TO RETURN TO MENU", w/2, 600);
        }
    }
}