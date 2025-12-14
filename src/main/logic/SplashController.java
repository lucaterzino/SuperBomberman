package main.logic;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import main.Gioco;
import main.view.MenuRenderer; // Import needed to draw the Options screen
import main.view.SplashRenderer;

public class SplashController {

    private Gioco mainApp;

    @FXML private StackPane rootPane;
    @FXML private VBox loadingContainer;
    @FXML private Button playNowButton;
    @FXML private ProgressBar loadingBar;
    @FXML private Canvas splashCanvas; 
    
    private SplashRenderer renderer;
    private MenuRenderer optionsRenderer; // Helper renderer for the options screen
    private AnimationTimer splashLoop;

    // Stati della Splash Screen
    private enum SplashState {
        WAITING,    // Aspetta il click su "PLAY NOW"
        LOADING,    // Barra di caricamento
        LOGO,       // Logo Hudson Soft
        MENU,       // Menu Principale del gioco
        OPTIONS     // Menu Opzioni
    }
    
    private SplashState currentState = SplashState.WAITING;
    private double stateTimer = 0;
    
    // Variabili Menu
    private String[] options = {"NORMAL GAME", "OPTION", "EXIT"};
    private int selectedIndex = 0;
    
    // Variabili Schermata Options (0 = Volume, 1 = Back)
    private int optionIndex = 0; 

    // Variabili Animazione
    private double time = 0;
    private double cloudX = 0;

    public void setMainApp(Gioco mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        splashCanvas.toBack(); 

        // Initialize Renderers
        renderer = new SplashRenderer(splashCanvas.getGraphicsContext2D(), 1024, 768);
        optionsRenderer = new MenuRenderer(splashCanvas.getGraphicsContext2D(), 1024, 768);
        
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

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 1))
        );
        
        timeline.setOnFinished(e -> {
            loadingContainer.setVisible(false);
            splashCanvas.toFront(); 
            splashCanvas.requestFocus(); 
            currentState = SplashState.LOGO;
            stateTimer = 3.0; 
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
            renderer.drawBackground();
        }
        else if (currentState == SplashState.LOGO) {
            renderer.clear();
            renderer.drawHudsonLogo();
            AudioManager.getInstance().playMusic("/audio/song_menu.mp3");
        }
        else if (currentState == SplashState.MENU) {
            renderer.clear();
            renderer.drawTitleScreen(time, cloudX, options, selectedIndex);
        }
        else if (currentState == SplashState.OPTIONS) {
            // Use MenuRenderer to draw options screen (since logic is identical)
            optionsRenderer.drawOptionsScreen(AudioManager.getInstance().getVolume(), optionIndex);
        }
    }

    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (currentState == SplashState.MENU) {
            handleMainInput(code);
        } else if (currentState == SplashState.OPTIONS) {
            handleOptionsInput(code);
        }
    }
    
    // --- Input Logic Same as MenuController ---

    private void handleMainInput(KeyCode code) {
        if (code == KeyCode.UP) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = options.length - 1;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.DOWN) {
            selectedIndex++;
            if (selectedIndex >= options.length) selectedIndex = 0;
            AudioManager.getInstance().playSound("cursor");
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE || code == KeyCode.Z) {
            selectOption();
        }
    }

    private void handleOptionsInput(KeyCode code) {
        // Navigazione Verticale (Tra Volume e Back)
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            optionIndex = (optionIndex == 0) ? 1 : 0; 
            AudioManager.getInstance().playSound("cursor");
        } 
        // Regolazione Volume
        else if (code == KeyCode.LEFT) {
            if (optionIndex == 0) { 
                double vol = AudioManager.getInstance().getVolume();
                AudioManager.getInstance().setVolume(vol - 0.1);
                AudioManager.getInstance().playSound("cursor");
            }
        } else if (code == KeyCode.RIGHT) {
            if (optionIndex == 0) { 
                double vol = AudioManager.getInstance().getVolume();
                AudioManager.getInstance().setVolume(vol + 0.1);
                AudioManager.getInstance().playSound("cursor");
            }
        } 
        // Torna Indietro
        else if (code == KeyCode.ENTER || code == KeyCode.Z || code == KeyCode.ESCAPE) {
            if (optionIndex == 1 || code == KeyCode.ESCAPE) { 
                currentState = SplashState.MENU; // Back to Splash Menu
                AudioManager.getInstance().playSound("confirm");
            }
        }
    }

    private void selectOption() {
        if (selectedIndex == 0) { // NORMAL GAME
            AudioManager.getInstance().playSound("confirm");
            AudioManager.getInstance().stopMusic(); // Stop menu/splash music
            if (mainApp != null) {
                mainApp.showGameScreen();
            }
        } else if (selectedIndex == 1) { // OPTION
            currentState = SplashState.OPTIONS;
            optionIndex = 0; // Reset selection to Volume
            AudioManager.getInstance().playSound("confirm");
        } else if (selectedIndex == 2) { // EXIT
            System.exit(0);
        }
    }
    
    public void stopLoop() {
        if (splashLoop != null) splashLoop.stop();
    }
}