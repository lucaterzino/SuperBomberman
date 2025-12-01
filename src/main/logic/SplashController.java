package main.logic;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import main.Gioco;
import main.view.SplashRenderer;

public class SplashController {

    private Gioco mainApp;

    @FXML private StackPane rootPane;
    @FXML private VBox loadingContainer;
    @FXML private Button playNowButton;
    @FXML private ProgressBar loadingBar;

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    void handlePlayNow(ActionEvent event) {
        playNowButton.setVisible(false);
        loadingBar.setVisible(true);
        loadingBar.setProgress(0);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 1))
        );
        
        timeline.setOnFinished(e -> showHudsonLogo());
        timeline.play();
    }

    private void showHudsonLogo() {
        loadingContainer.setVisible(false);
        
        Canvas logoCanvas = new Canvas(1024, 768);
        rootPane.getChildren().add(logoCanvas); 
        
        // Usa il renderer
        SplashRenderer renderer = new SplashRenderer(logoCanvas.getGraphicsContext2D(), 1024, 768);
        renderer.drawHudsonLogo();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            if (mainApp != null) {
                mainApp.showMenuScreen();
            }
        });
        pause.play();
    }
}