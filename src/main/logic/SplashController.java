package main.logic;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML private Canvas splashCanvas; // Iniettato da FXML
    
    private SplashRenderer renderer;
    private AnimationTimer splashLoop;

    // Stati della Splash Screen
    private enum SplashState {
        WAITING,    // Aspetta il click su "PLAY NOW"
        LOADING,    // Barra di caricamento
        LOGO,       // Logo Hudson Soft
        MENU        // Menu Principale del gioco
    }
    
    private SplashState currentState = SplashState.WAITING;
    private double stateTimer = 0;
    
    // Variabili Menu
    private String[] options = {"NORMAL GAME", "OPTION", "EXIT"};
    private int selectedIndex = 0;
    
    // Variabili Animazione
    private double time = 0;
    private double cloudX = 0;

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Usa il canvas iniettato dall'FXML
        splashCanvas.toBack(); // Assicura che il canvas stia dietro ai bottoni inizialmente

        renderer = new SplashRenderer(splashCanvas.getGraphicsContext2D(), 1024, 768);
        
        // Disegna lo sfondo iniziale
        renderer.drawBackground();
        
        splashLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) lastTime = now;
                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                
                update(delta);
                draw();
            }
        };
        splashLoop.start();
        
        rootPane.setFocusTraversable(true);
    }

    @FXML
    void handlePlayNow(ActionEvent event) {
        playNowButton.setVisible(false);
        loadingBar.setVisible(true);
        loadingBar.setProgress(0);
        currentState = SplashState.LOADING;

        // Animazione caricamento (3 secondi)
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 1))
        );
        
        timeline.setOnFinished(e -> {
            // Fine caricamento: nascondi UI e passa al logo
            loadingContainer.setVisible(false);
            splashCanvas.toFront(); // Porta il canvas davanti per disegnare logo e menu
            splashCanvas.requestFocus(); 
            currentState = SplashState.LOGO;
            stateTimer = 3.0; // Mostra logo per 3 secondi
        });
        timeline.play();
    }
    
    private void update(double delta) {
        time += delta * 60; 

        if (currentState == SplashState.LOGO) {
            stateTimer -= delta;
            if (stateTimer <= 0) {
                currentState = SplashState.MENU; 
            }
        }
        else if (currentState == SplashState.MENU) {
            cloudX -= 0.5;
            if (cloudX < -250) cloudX = 0;
        }
    }

    private void draw() {
        if (currentState == SplashState.WAITING || currentState == SplashState.LOADING) {
            // Durante l'attesa o il caricamento, ridisegna solo lo sfondo pulito dietro i controlli
            renderer.drawBackground();
        }
        else if (currentState == SplashState.LOGO) {
            renderer.clear();
            renderer.drawHudsonLogo();
        }
        else if (currentState == SplashState.MENU) {
            renderer.clear();
            renderer.drawTitleScreen(time, cloudX, options, selectedIndex);
        }
    }

    public void handleKeyPressed(KeyEvent event) {
        if (currentState != SplashState.MENU) return;

        KeyCode code = event.getCode();
        
        if (code == KeyCode.UP) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = options.length - 1;
        } else if (code == KeyCode.DOWN) {
            selectedIndex++;
            if (selectedIndex >= options.length) selectedIndex = 0;
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.Z) {
            selectOption();
        }
    }

    private void selectOption() {
        if (selectedIndex == 0) { // NORMAL GAME
            stopLoop();
            if (mainApp != null) {
                mainApp.showGameScreen();
            }
        } else if (selectedIndex == 2) { // EXIT
            System.exit(0);
        } else {
            System.out.println("Opzione non disponibile: " + options[selectedIndex]);
        }
    }
    
    public void stopLoop() {
        if (splashLoop != null) splashLoop.stop();
    }
}