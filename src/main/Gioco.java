package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.logic.GameController;
import main.logic.MenuController;
import main.logic.SplashController;

import java.io.IOException;

public class Gioco extends Application {

    private Stage primaryStage;
    private Scene splashScene;
    private Scene menuScene;
    private Scene gameScene;
    
    // Riferimenti ai controller per gestirne il ciclo di vita
    private GameController gameController; 
    private MenuController menuController;
    private SplashController splashController; 

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        // Carica tutte le scene all'avvio
        loadSplashScreen();
        loadMenuScene();
        loadGameScene();

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

    private void loadMenuScene() throws IOException {
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/main/view/menu.fxml"));
        Parent menuRoot = menuLoader.load();
        menuController = menuLoader.getController();
        menuController.setMainApp(this);
        menuScene = new Scene(menuRoot);
        
        // Collega input menu
        menuScene.setOnKeyPressed(event -> menuController.handleKeyPressed(event));
    }

    private void loadGameScene() throws IOException {
        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/main/view/game.fxml"));
        Parent gameRoot = gameLoader.load();
        gameController = gameLoader.getController();
        gameController.setMainApp(this); 
        gameScene = new Scene(gameRoot);
        
        // Collega input gioco
        gameScene.setOnKeyPressed(event -> gameController.onKeyPressed(event.getCode()));
        gameScene.setOnKeyReleased(event -> gameController.onKeyReleased(event.getCode()));
    }

    public void showGameScreen() {
        // Ferma i loop grafici delle altre schermate per risparmiare risorse
        if (menuController != null) menuController.stopLoop();
        if (splashController != null) splashController.stopLoop(); 
        
        primaryStage.setScene(gameScene);
        gameController.startGame(); 
    }

    public void showMenuScreen() {
        // Ferma il gioco se è in corso
        if (gameController != null) gameController.stopGame();
        if (splashController != null) splashController.stopLoop();
        
        primaryStage.setScene(menuScene);
        
        // --- MODIFICA FONDAMENTALE ---
        // Riavvia il loop grafico del menu.
        // NON chiamare initialize(), usa il metodo dedicato startLoop()
        if (menuController != null) {
             menuController.startLoop(); 
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}