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
    private GameController gameController; 
    private MenuController menuController;
    private SplashController splashController; // Riferimento al controller splash

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        loadSplashScreen();
        loadMenuScene();
        loadGameScene();

        stage.setTitle("Super Bomberman 2D");
        stage.setScene(splashScene);
        stage.setResizable(false);
        stage.show();
    }

    private void loadSplashScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/splash.fxml"));
        Parent root = loader.load();
        splashController = loader.getController(); // Ottieni il controller
        splashController.setMainApp(this);
        splashScene = new Scene(root);
        
        // --- NUOVO: Collega gli eventi della tastiera al controller splash ---
        splashScene.setOnKeyPressed(event -> splashController.handleKeyPressed(event));
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

    public void showGameScreen() {
        if (menuController != null) menuController.stopLoop();
        if (splashController != null) splashController.stopLoop(); // Ferma anche il loop splash
        primaryStage.setScene(gameScene);
        gameController.startGame(); 
    }

    public void showMenuScreen() {
        if (gameController != null) gameController.stopGame();
        if (splashController != null) splashController.stopLoop(); // Ferma anche il loop splash
        primaryStage.setScene(menuScene);
        if (menuController != null) menuController.initialize(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}