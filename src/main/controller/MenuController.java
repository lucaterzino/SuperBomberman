package main.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
//import main.AudioManager;
import main.Main;

public class MenuController {

    @FXML private Canvas menuCanvas;
    private GraphicsContext gc;
    private Main mainApp;
    private AnimationTimer menuLoop;

    // Opzioni Menu
    private String[] options = {"NORMAL GAME", "OPTION", "EXIT"};
    private int selectedIndex = 0;
    
    // Variabili per animazioni
    private double time = 0;
    private double cloudX = 0;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        gc = menuCanvas.getGraphicsContext2D();
        
        // Avvia la musica del menu se disponibile
        // AudioManager.getInstance().playMusic("/music/menu_theme.mp3"); 

        // Game Loop per il menu (per animazioni)
        menuLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        };
        menuLoop.start();
    }

    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (code == KeyCode.UP) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = options.length - 1;
            AudioManager.getInstance().playSoundEffect("cursor"); 
        } else if (code == KeyCode.DOWN) {
            selectedIndex++;
            if (selectedIndex >= options.length) selectedIndex = 0;
            AudioManager.getInstance().playSoundEffect("cursor"); 
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.Z) {
            selectOption();
        }
    }

    private void selectOption() {
        if (selectedIndex == 0) { // NORMAL GAME
            AudioManager.getInstance().playSoundEffect("confirm");
            AudioManager.getInstance().stopMusic();
            stopLoop(); 
            if (mainApp != null) {
                mainApp.showGameScreen();
            }
        } else if (selectedIndex == 2) { // EXIT
            System.exit(0);
        } else {
            // OPTION
            System.out.println("Opzione non disponibile: " + options[selectedIndex]);
        }
    }
    
    public void stopLoop() {
        if (menuLoop != null) menuLoop.stop();
    }

    private void update() {
        time += 0.05; // Incremento tempo (circa 3.0 al secondo a 60fps)
        
        // Animazione Nuvole
        cloudX -= 0.5;
        if (cloudX < -250) cloudX = 0;
    }

    private void draw() {
        double w = menuCanvas.getWidth();
        double h = menuCanvas.getHeight();
        
        // 1. SFONDO CIELO
        gc.setFill(Color.web("#0000AA")); 
        gc.fillRect(0, 0, w, h/2);
        gc.setFill(Color.web("#4444FF"));
        gc.fillRect(0, h/2, w, h/2);

        // 2. NUVOLE
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.7);
        drawCloud(100 + cloudX, 100, 120);
        drawCloud(400 + cloudX, 50, 150);
        drawCloud(800 + cloudX, 120, 100);
        drawCloud(100 + cloudX + w, 80, 130); 
        drawCloud(400 + cloudX + w, 40, 160);
        gc.setGlobalAlpha(1.0);

        // 3. MONTAGNE
        gc.setFill(Color.web("#228B22")); 
        gc.beginPath();
        gc.moveTo(0, h);
        gc.lineTo(0, h - 100);
        gc.lineTo(150, h - 200);
        gc.lineTo(300, h - 120);
        gc.lineTo(500, h - 250); 
        gc.lineTo(700, h - 150);
        gc.lineTo(900, h - 180);
        gc.lineTo(1024, h - 100);
        gc.lineTo(1024, h);
        gc.closePath();
        gc.fill();
        
        gc.setStroke(Color.web("#006400"));
        gc.setLineWidth(5);
        gc.stroke();

        // 4. LOGO "SUPER BOMBERMAN 2"
        drawLogo(w/2, 220);

        // 5. SCRITTA LAMPEGGIANTE "PUSH START BUTTON!"
        if ((int)(time * 0.35) % 2 == 0) {
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
            gc.setTextAlign(TextAlignment.CENTER);
            
            gc.setFill(Color.ORANGE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            
            gc.fillText("PUSH START BUTTON!", w/2, 460);
            gc.strokeText("PUSH START BUTTON!", w/2, 460);
        }

        // 6. MENU OPZIONI
        drawMenuOptions(w/2, 530);

        // 7. COPYRIGHT
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.fillText("© 1995 HUDSON SOFT", w/2, h - 30);
        gc.strokeText("© 1995 HUDSON SOFT", w/2, h - 30);
        
        gc.setTextAlign(TextAlignment.LEFT); 
    }
    
    private void drawCloud(double x, double y, double width) {
        gc.fillOval(x, y, width, width * 0.6);
        gc.fillOval(x + width*0.4, y - width*0.2, width*0.8, width * 0.6);
        gc.fillOval(x + width*0.7, y + width*0.1, width*0.6, width * 0.5);
    }

    private void drawLogo(double x, double y) {
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Ombra Bomba
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 130, y - 90, 260, 260);
        
        // Corpo bomba
        gc.setFill(Color.web("#4B0082")); 
        gc.fillOval(x - 120, y - 80, 240, 240);
        
        // Riflesso
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.4);
        gc.fillOval(x - 80, y - 50, 80, 80);
        gc.setGlobalAlpha(1.0);
        
        // Miccia
        gc.setFill(Color.GRAY);
        gc.fillRect(x - 20, y - 100, 40, 30);
        
        // "SUPER"
        gc.setFont(Font.font("Impact", 70));
        gc.setFill(Color.YELLOW);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.fillText("SUPER", x, y - 80);
        gc.strokeText("SUPER", x, y - 80);

        // "BOMBERMAN"
        gc.setFont(Font.font("Impact", 110));
        gc.setFill(Color.DARKRED);
        gc.fillText("BOMBERMAN", x + 5, y + 65); // Ombra
        
        gc.setFill(Color.ORANGERED);
        gc.setStroke(Color.WHITE); 
        gc.setLineWidth(2);
        gc.fillText("BOMBERMAN", x, y + 60);
        gc.strokeText("BOMBERMAN", x, y + 60);
        
        // "2" (Verde Gigante)
        gc.setFont(Font.font("Impact", 160));
        gc.setFill(Color.LIMEGREEN);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(4);
        
        double numX = x + 380;
        double numY = y + 60;
        
        gc.setFill(Color.BLACK);
        gc.fillText("2", numX + 10, numY + 10);
        
        gc.setFill(Color.web("#32CD32")); 
        gc.fillText("2", numX, numY);
        gc.strokeText("2", numX, numY);
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(numX - 40, numY - 80, numX + 60, numY - 80);
        gc.strokeLine(numX - 60, numY - 40, numX + 80, numY - 40);
        gc.strokeLine(numX - 30, numY, numX + 70, numY);
    }

    private void drawMenuOptions(double x, double y) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        
        for (int i = 0; i < options.length; i++) {
            double textY = y + (i * 50);
            
            // Ombra
            gc.setFill(Color.BLACK);
            gc.fillText(options[i], x + 4, textY + 4);
            
            if (i == selectedIndex) {
                gc.setFill(Color.CYAN); 
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(1);
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
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y - 15, size, size);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y - 15, size, size);
        
        gc.setFill(Color.BLACK);
        gc.fillRect(x + 8, y - 7, 4, 10);
        gc.fillRect(x + 18, y - 7, 4, 10);
        
        gc.setFill(Color.MAGENTA);
        gc.fillRect(x + 10, y - 21, 10, 6);
        gc.strokeRect(x + 10, y - 21, 10, 6);
    }
}