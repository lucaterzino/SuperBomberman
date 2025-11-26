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
    private GameController gameController; 

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        loadMenuScene();
        loadGameScene();

        stage.setTitle("Super Bomberman 2D");
        stage.setScene(menuScene);
        stage.setResizable(false);
        stage.show();
    }

    private void loadMenuScene() throws IOException {
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/main/view/menu.fxml"));
        Parent menuRoot = menuLoader.load();
        
        MenuController menuController = menuLoader.getController();
        menuController.setMainApp(this);
        
        menuScene = new Scene(menuRoot);
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
        primaryStage.setScene(gameScene);
        gameController.startGame(); 
    }

    public void showMenuScreen() {
        gameController.stopGame(); 
        primaryStage.setScene(menuScene);
        AudioManager.getInstance().playMusic("/music/menu_theme.mp3"); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}