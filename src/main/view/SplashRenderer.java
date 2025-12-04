package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class SplashRenderer {
    private GraphicsContext gc;
    private double w, h;

    public SplashRenderer(GraphicsContext gc, double w, double h) {
        this.gc = gc; this.w = w; this.h = h;
    }
    
    public void clear() {
        gc.clearRect(0, 0, w, h);
    }
    
    public void drawBackground() {
        gc.setFill(Color.web("#808080")); // Sfondo Grigio
        gc.fillRect(0, 0, w, h);
    }

    public void drawHudsonLogo() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        Color hudsonBlue = Color.web("#0000EE");
        gc.setFill(hudsonBlue);

        double centerX = w / 2;
        double centerY = h / 2;

        Font bigFont = Font.font("Impact", 110);
        gc.setFont(bigFont);
        
        String textSoft = "HUDSON SOFT";
        Text tempText = new Text(textSoft);
        tempText.setFont(bigFont);
        double widthSoft = tempText.getLayoutBounds().getWidth();
        double heightSoft = tempText.getLayoutBounds().getHeight();
        
        double xStart = centerX - (widthSoft / 2);
        double ySoft = centerY + 40;
        
        gc.fillText(textSoft, xStart, ySoft);
        
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("®", xStart + widthSoft + 10, ySoft - heightSoft/2);

        Font smallFont = Font.font("Arial", FontWeight.BOLD, 24);
        gc.setFont(smallFont);
        
        String textGroup = "HUDSON GROUP";
        Text tempTextGroup = new Text(textGroup);
        tempTextGroup.setFont(smallFont);
        double widthGroup = tempTextGroup.getLayoutBounds().getWidth();
        
        double yGroup = ySoft - heightSoft + 20; 
        gc.fillText(textGroup, xStart, yGroup);

        double barStartX = xStart + widthGroup + 15; 
        double barWidth = (xStart + widthSoft) - barStartX;
        double barHeight = 18; 
        
        gc.fillRect(barStartX, yGroup - 18, barWidth, barHeight);
        
        // Dettagli taglio lettere
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double[] cuts = {0.12, 0.25, 0.38, 0.5, 0.62, 0.75, 0.88}; 
        for (double ratio : cuts) {
            double cutX = xStart + (widthSoft * ratio);
            gc.strokeLine(cutX, ySoft - heightSoft + 30, cutX, ySoft + 5);
        }
    }
    
    public void drawTitleScreen(double time, double cloudX, String[] options, int selectedIndex) {
        // Sfondo Cielo
        gc.setFill(Color.web("#0000AA")); gc.fillRect(0,0,w,h/2);
        gc.setFill(Color.web("#4444FF")); gc.fillRect(0,h/2,w,h/2);
        
        // Nuvole
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.7);
        drawCloud(100 + cloudX, 100, 120);
        drawCloud(400 + cloudX, 50, 150);
        drawCloud(800 + cloudX, 120, 100);
        drawCloud(100 + cloudX + w, 80, 130); 
        drawCloud(400 + cloudX + w, 40, 160);
        gc.setGlobalAlpha(1.0);
        
        // Montagne
        gc.setFill(Color.web("#228B22")); 
        gc.beginPath();
        gc.moveTo(0, h); gc.lineTo(0, h-100);
        gc.lineTo(200, h-200); gc.lineTo(500, h-250); gc.lineTo(800, h-150);
        gc.lineTo(1024, h-100); gc.lineTo(1024, h);
        gc.closePath(); gc.fill();
        gc.setStroke(Color.web("#006400")); gc.setLineWidth(5); gc.stroke();

        // Logo
        drawGameLogo(w/2, 220);
        
        // Scritta Lampeggiante
        if ((int)(time * 0.05) % 2 == 0) {
             gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
             gc.setTextAlign(TextAlignment.CENTER);
             gc.setFill(Color.ORANGE); gc.setStroke(Color.BLACK); gc.setLineWidth(2);
             gc.fillText("PUSH START BUTTON!", w/2, 460);
             gc.strokeText("PUSH START BUTTON!", w/2, 460);
        }
        
        // Menu Opzioni
        drawMenuOptions(w/2, 530, options, selectedIndex);
        
        // Copyright
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE); gc.setStroke(Color.BLACK); gc.setLineWidth(1);
        gc.fillText("© 1995 HUDSON SOFT", w/2, h - 30);
        gc.strokeText("© 1995 HUDSON SOFT", w/2, h - 30);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawCloud(double x, double y, double width) {
        gc.fillOval(x, y, width, width * 0.6);
        gc.fillOval(x + width*0.4, y - width*0.2, width*0.8, width * 0.6);
        gc.fillOval(x + width*0.7, y + width*0.1, width*0.6, width * 0.5);
    }

    private void drawGameLogo(double x, double y) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.BLACK); gc.fillOval(x - 130, y - 90, 260, 260);
        gc.setFill(Color.web("#4B0082")); gc.fillOval(x - 120, y - 80, 240, 240);
        gc.setFill(Color.WHITE); gc.setGlobalAlpha(0.4); gc.fillOval(x - 80, y - 50, 80, 80); gc.setGlobalAlpha(1.0);
        gc.setFill(Color.GRAY); gc.fillRect(x - 20, y - 100, 40, 30);
        
        gc.setFont(Font.font("Impact", 70));
        gc.setFill(Color.YELLOW); gc.setStroke(Color.BLACK); gc.setLineWidth(3);
        gc.fillText("SUPER", x, y - 80); gc.strokeText("SUPER", x, y - 80);

        gc.setFont(Font.font("Impact", 110));
        gc.setFill(Color.ORANGERED); gc.setStroke(Color.WHITE); gc.setLineWidth(2);
        gc.fillText("BOMBERMAN", x, y + 60); gc.strokeText("BOMBERMAN", x, y + 60);
        
        gc.setFont(Font.font("Impact", 160));
        gc.setFill(Color.LIMEGREEN); gc.setStroke(Color.BLACK); gc.setLineWidth(4);
        double numX = x + 380; double numY = y + 60;
        gc.setFill(Color.BLACK); gc.fillText("2", numX + 10, numY + 10);
        gc.setFill(Color.web("#32CD32")); gc.fillText("2", numX, numY); gc.strokeText("2", numX, numY);
    }

    private void drawMenuOptions(double x, double y, String[] options, int selectedIndex) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        for (int i = 0; i < options.length; i++) {
            double textY = y + (i * 60);
            gc.setFill(Color.BLACK); gc.fillText(options[i], x + 4, textY + 4);
            if (i == selectedIndex) {
                gc.setFill(Color.CYAN); gc.setStroke(Color.BLUE); gc.setLineWidth(1);
                gc.strokeText(options[i], x, textY);
                drawCursor(x - 200, textY - 15);
            } else {
                gc.setFill(Color.WHITE); 
            }
            gc.fillText(options[i], x, textY);
        }
    }
    
    private void drawCursor(double x, double y) {
        double size = 30;
        gc.setFill(Color.WHITE); gc.fillRect(x, y - 15, size, size);
        gc.setStroke(Color.BLACK); gc.setLineWidth(2); gc.strokeRect(x, y - 15, size, size);
        gc.setFill(Color.BLACK); gc.fillRect(x + 8, y - 7, 4, 10); gc.fillRect(x + 18, y - 7, 4, 10);
        gc.setFill(Color.MAGENTA); gc.fillRect(x + 10, y - 21, 10, 6); gc.strokeRect(x + 10, y - 21, 10, 6);
    }
}