package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.controller.AudioManager;
import main.controller.GameController;
import main.controller.MenuController;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private Scene menuScene;
    private Scene gameScene;
    private GameController gameController; // Teniamo un riferimento per riavviare/fermare

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        // Carica le due scene
        loadMenuScene();
        loadGameScene();

        // Imposta la scena iniziale (il menù)
        stage.setTitle("Super Bomberman 2D");
        stage.setScene(menuScene);
        stage.setResizable(false);
        stage.show();
    }

    // Carica la scena del menù
    private void loadMenuScene() throws IOException {
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/main/view/menu.fxml"));
        Parent menuRoot = menuLoader.load();
        
        // Inietta il riferimento di "Main" nel controller del menù
        MenuController menuController = menuLoader.getController();
        menuController.setMainApp(this);
        
        menuScene = new Scene(menuRoot);
    }

    // Carica la scena di gioco
    private void loadGameScene() throws IOException {
        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/main/view/game.fxml"));
        Parent gameRoot = gameLoader.load();
        
        // Salva il controller del gioco
        gameController = gameLoader.getController();
        gameController.setMainApp(this); // Inietta il riferimento di "Main"
        
        gameScene = new Scene(gameRoot);
        
        // Assicura che l'input da tastiera vada alla scena (e quindi al canvas)
        // Il GameController imposterà il focus sul canvas
        gameScene.setOnKeyPressed(event -> gameController.onKeyPressed(event.getCode()));
        gameScene.setOnKeyReleased(event -> gameController.onKeyReleased(event.getCode()));
    }

    // Metodo chiamato da MenuController per mostrare il gioco
    public void showGameScreen() {
        primaryStage.setScene(gameScene);
        gameController.startGame(); // Avvia la musica e il focus
    }

    // Metodo chiamato da GameController per tornare al menù
    public void showMenuScreen() {
        gameController.stopGame(); // Ferma la musica
        primaryStage.setScene(menuScene);
        AudioManager.getInstance().playMusic("/music/menu_theme.mp3"); // Riavvia musica menù
    }

    public static void main(String[] args) {
        launch(args);
    }
}