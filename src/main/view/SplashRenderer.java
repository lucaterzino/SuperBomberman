package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class SplashRenderer {
    private GraphicsContext gc;
    private double w, h;

    public SplashRenderer(GraphicsContext gc, double w, double h) {
        this.gc = gc; this.w = w; this.h = h;
    }

    public void drawHudsonLogo() {
        gc.setFill(Color.WHITE); gc.fillRect(0,0,w,h);
        gc.setFill(Color.BLUE); 
        gc.setFont(Font.font("Impact", 90));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HUDSON SOFT", w/2, h/2);
        gc.setTextAlign(TextAlignment.LEFT);
    }
}