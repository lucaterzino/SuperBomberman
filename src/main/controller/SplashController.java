package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.Main;

public class SplashController {

    private Main mainApp;

    @FXML private StackPane rootPane;
    @FXML private VBox loadingContainer;
    @FXML private Button playNowButton;
    @FXML private ProgressBar loadingBar;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    void handlePlayNow(ActionEvent event) {
        playNowButton.setVisible(false);
        loadingBar.setVisible(true);
        loadingBar.setProgress(0);

        // Animazione caricamento (3 secondi)
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 1))
        );
        
        // Quando finisce il caricamento, mostra il logo Hudson
        timeline.setOnFinished(e -> showHudsonLogo());
        timeline.play();
    }

    private void showHudsonLogo() {
        // 1. Nascondi il contenitore di caricamento (bottone e barra)
        loadingContainer.setVisible(false);
        
        // 2. Crea un nuovo Canvas per il logo e aggiungilo alla scena
        Canvas logoCanvas = new Canvas(1024, 768);
        rootPane.getChildren().add(logoCanvas); // Aggiungilo sopra a tutto

        // 3. Disegna il logo Hudson Soft
        drawHudsonLogo(logoCanvas.getGraphicsContext2D());

        // 4. Attendi 3 secondi e poi vai al menu
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            if (mainApp != null) {
                mainApp.showMenuScreen();
            }
        });
        pause.play();
    }

    private void drawHudsonLogo(GraphicsContext gc) {
        double w = 1024;
        double h = 768;

        // Sfondo Bianco
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        // Colore Blu Hudson (Fedele all'originale)
        Color hudsonBlue = Color.web("#0000EE");
        gc.setFill(hudsonBlue);

        double centerX = w / 2;
        double centerY = h / 2;

        // --- SCRITTA "HUDSON SOFT" (Grande, stile Pixel Art simulato) ---
        // Usiamo un font molto grande e bold per simulare i blocchi massicci
        // "Impact" o "Arial Black" rendono bene l'idea.
        Font bigFont = Font.font("Arial Black", 90); 
        gc.setFont(bigFont);
        
        String textSoft = "HUDSON SOFT";
        Text tempText = new Text(textSoft);
        tempText.setFont(bigFont);
        double widthSoft = tempText.getLayoutBounds().getWidth();
        double heightSoft = tempText.getLayoutBounds().getHeight();
        
        // Coordinate per centrare "HUDSON SOFT"
        double xStart = centerX - (widthSoft / 2);
        double ySoft = centerY + 50;
        
        gc.fillText(textSoft, xStart, ySoft);
        
        // Simbolo Registered ®
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("®", xStart + widthSoft + 10, ySoft - heightSoft/2 - 10);

        // --- SCRITTA "HUDSON GROUP" (Piccola in alto a sinistra) ---
        Font smallFont = Font.font("Arial", FontWeight.BOLD, 24);
        gc.setFont(smallFont);
        
        String textGroup = "HUDSON GROUP";
        Text tempTextGroup = new Text(textGroup);
        tempTextGroup.setFont(smallFont);
        double widthGroup = tempTextGroup.getLayoutBounds().getWidth();
        
        // Allineato a sinistra con la scritta grande
        double yGroup = ySoft - heightSoft + 15; 
        gc.fillText(textGroup, xStart, yGroup);

        // --- BARRA BLU (A destra di "HUDSON GROUP") ---
        double barStartX = xStart + widthGroup + 15; 
        double barWidth = (xStart + widthSoft) - barStartX;
        double barHeight = 18; 
        
        // La barra è allineata verticalmente col testo "GROUP"
        gc.fillRect(barStartX, yGroup - 18, barWidth, barHeight);

        // --- Dettaglio Pixel Art "Taglietti" (Caratteristica del logo) ---
        // Disegniamo dei piccoli taglietti bianchi verticali per simulare lo stile "blocchi" 
        // tipico del logo originale (le lettere sembrano fatte di mattoncini separati)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);
        
        // Posizioni approssimative dei tagli verticali sulle lettere
        double[] cuts = {0.14, 0.27, 0.40, 0.53, 0.65, 0.78, 0.90}; 
        double textYTop = ySoft - heightSoft + 30;
        double textYBot = ySoft;

        for (double ratio : cuts) {
            double cutX = xStart + (widthSoft * ratio);
            gc.strokeLine(cutX, textYTop, cutX, textYBot); 
        }
    }
}