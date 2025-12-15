package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.logic.*;

import java.io.IOException;

public class Gioco extends Application {

    private Stage primaryStage;
    private Scene splashScene;
    private Scene menuScene;
    private Scene gameScene;
    private Scene levelMapScene; 
    private Scene endGameScene; 
    
    // Riferimenti ai controller per gestirne il ciclo di vita
    private GameController gameController; 
    private MenuController menuController;
    private SplashController splashController; 
    private LevelMapController levelMapController; 
    private EndGameController endGameController;

    // Variabile per salvare i progressi
    private int maxUnlockedLevel = 1;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        // Carica tutte le scene all'avvio
        loadSplashScreen();
        loadMenuScene();
        loadLevelMapScene(); // Carica la Mappa
        loadGameScene();
        loadEndGameScene();

        stage.setTitle("Super Bomberman 2D");
        
        // Inizia con la Splash Screen
        stage.setScene(splashScene);      
        stage.setResizable(false);
        stage.show();
    }

    private void loadSplashScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/splash.fxml"));
        Parent root = loader.load();
        splashController = loader.getController();
        splashController.setMainApp(this);
        splashScene = new Scene(root);
        
        // Collega input splash
        splashScene.setOnKeyPressed(event -> splashController.handleKeyPressed(event));
    }


    private void loadLevelMapScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/level_map.fxml"));
        Parent root = loader.load();
        levelMapController = loader.getController();
        levelMapController.setMainApp(this);
        levelMapScene = new Scene(root);
    }


    // --- NUOVO: Mostra la Mappa Livelli ---
    public void showLevelMapScreen() {
        // Ferma altri loop
        if (menuController != null) menuController.stopLoop();
        if (gameController != null) gameController.stopGame();
        
        // Passa i progressi al controller della mappa
        levelMapController.setMaxUnlockedLevel(maxUnlockedLevel);
        
        primaryStage.setScene(levelMapScene);
        levelMapController.startLoop();
    }

    // --- MODIFICATO: Avvia un livello specifico ---
    public void startGameLevel(int level) {
        if (levelMapController != null) levelMapController.stopLoop();
        
        primaryStage.setScene(gameScene);
        // Chiama il nuovo metodo creato nello Step 1
        gameController.startLevel(level); 
    }

    // Vecchio metodo showGameScreen rimosso o reindirizzato (opzionale)
    public void showGameScreen() { showLevelMapScreen(); }

    public void showLevelSelectionScreen() {
        // Usato in caso di Game Over per tornare alla selezione
        showLevelMapScreen();
    }

    public void showMenuScreen() {
        if (gameController != null) gameController.stopGame();
        if (levelMapController != null) levelMapController.stopLoop(); // Ferma mappa
        
        primaryStage.setScene(menuScene);
        AudioManager.getInstance().playMusic("/audio/song_menu.mp3");
        if (menuController != null) menuController.startLoop();
    }

    private void loadMenuScene() throws IOException {
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/main/view/menu.fxml"));
        Parent menuRoot = menuLoader.load();
        menuController = menuLoader.getController();
        menuController.setMainApp(this);
        menuScene = new Scene(menuRoot);
        menuScene.setOnKeyPressed(event -> menuController.handleKeyPressed(event));
    }

    private void loadGameScene() throws IOException {
        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/main/view/game.fxml"));
        Parent gameRoot = gameLoader.load();
        gameController = gameLoader.getController();
        gameController.setMainApp(this); 
        gameScene = new Scene(gameRoot);
        gameScene.setOnKeyPressed(event -> gameController.onKeyPressed(event.getCode()));
        gameScene.setOnKeyReleased(event -> gameController.onKeyReleased(event.getCode()));
    }

    private void loadEndGameScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/end_game.fxml"));
        Parent root = loader.load();
        endGameController = loader.getController();
        endGameController.setMainApp(this);
        endGameScene = new Scene(root);
    }

    // --- NUOVO: Mostra Schermata Finale ---
    public void showEndGameScreen() {
        stopAllLoops();
        primaryStage.setScene(endGameScene);
        endGameController.startLoop();
    }

    // --- NUOVO: Reset Completo (chiamato dal controller EndGame) ---
    public void resetGameAndShowMenu() {
        // Resetta i progressi per una nuova partita
        maxUnlockedLevel = 1; 
        showMenuScreen();
    }

    public void levelCompleted(int levelJustFinished) {
        // Logica di sblocco
        if (levelJustFinished >= maxUnlockedLevel && maxUnlockedLevel < 3) {
            maxUnlockedLevel++;
        }
        
        // SE HO FINITO IL LIVELLO 3 -> FINE GIOCO
        if (levelJustFinished == 3) {
            showEndGameScreen();
        } else {
            // ALTRIMENTI -> TORNA ALLA MAPPA
            showLevelMapScreen();
        }
    }

    // Helper per pulire il codice
    private void stopAllLoops() {
        if (menuController != null) menuController.stopLoop();
        if (gameController != null) gameController.stopGame();
        if (splashController != null) splashController.stopLoop();
        if (levelMapController != null) levelMapController.stopLoop();
        if (endGameController != null) endGameController.stopLoop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}