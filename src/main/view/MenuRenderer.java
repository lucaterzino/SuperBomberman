package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class MenuRenderer {
    private GraphicsContext gc;
    private double w, h;

    public MenuRenderer(GraphicsContext gc, double w, double h) {
        this.gc = gc; this.w = w; this.h = h;
    }

    public void drawMenu(double time, double cloudX, String[] options, int selected) {
        gc.setFill(Color.web("#0000AA")); gc.fillRect(0,0,w,h/2);
        gc.setFill(Color.web("#4444FF")); gc.fillRect(0,h/2,w,h/2);
        
        gc.setFill(Color.ORANGE); gc.setFont(Font.font("Impact", 100));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SUPER BOMBERMAN 2", w/2, 200);
        
        if ((int)(time * 1.0) % 2 == 0) {
             gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
             gc.setFill(Color.ORANGE); gc.fillText("PUSH START BUTTON!", w/2, 460);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        for(int i=0; i<options.length; i++) {
            gc.setFill(i == selected ? Color.CYAN : Color.WHITE);
            gc.fillText(options[i], w/2, 530 + i*50);
            if (i == selected) {
                 gc.setFill(Color.WHITE); gc.fillRect(w/2 - 200, 515 + i*50, 30, 30); // Cursore
            }
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }
}