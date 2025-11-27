package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.controller.GameController;
import main.controller.MenuController;
import main.controller.SplashController;

import java.io.IOException;

// La classe principale che estende l'applicazione JavaFX.
public class Main extends Application {

    private Stage primaryStage;     // La finestra principale dell'applicazione
    private Scene splashScene;      // La scena per la schermata di caricamento iniziale (Logo Hudson)
    private Scene menuScene;        // La scena per il menu di gioco (Selezione modalità)
    private Scene gameScene;        // La scena per la partita vera e propria
    private GameController gameController; // Controller per gestire la logica di gioco
    private MenuController menuController;  // Controller per gestire la logica del menu

    // Metodo start, chiamato all'avvio dell'applicazione.
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        
        // Caricamento di tutte le scene e dei loro controller associati
        loadSplashScreen();
        loadMenuScene();
        loadGameScene();

        stage.setTitle("Super Bomberman 2D");
        
        // Imposta la scena iniziale visualizzata (la Splash Screen)
        stage.setScene(splashScene);
        
        stage.setResizable(false); // Impedisce il ridimensionamento della finestra
        stage.show();              // Mostra la finestra
    }

    // Carica la Splash Screen (Logo Hudson Soft)
    private void loadSplashScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/view/splash.fxml"));
        Parent root = loader.load();
        SplashController controller = loader.getController();
        controller.setMainApp(this);
        splashScene = new Scene(root);
    }

    // Carica la scena del Menu di gioco
    private void loadMenuScene() throws IOException {
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/main/view/menu.fxml"));
        Parent menuRoot = menuLoader.load();
        
        menuController = menuLoader.getController(); // Ottiene il controller del menu
        menuController.setMainApp(this);
        
        menuScene = new Scene(menuRoot);
        
        // Collega gli eventi della tastiera alla scena del menu per la navigazione
        menuScene.setOnKeyPressed(event -> menuController.handleKeyPressed(event));
    }

    // Carica la scena di Gioco
    private void loadGameScene() throws IOException {
        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/main/view/game.fxml"));
        Parent gameRoot = gameLoader.load();
        
        gameController = gameLoader.getController(); // Ottiene il controller del gioco
        gameController.setMainApp(this); 
        
        gameScene = new Scene(gameRoot);
        
        // Collega gli eventi della tastiera alla scena del gioco per il movimento e le azioni
        gameScene.setOnKeyPressed(event -> gameController.onKeyPressed(event.getCode()));
        gameScene.setOnKeyReleased(event -> gameController.onKeyReleased(event.getCode()));
    }

    // Metodo per cambiare la scena e avviare il Gioco
    public void showGameScreen() {
        // Ferma il loop di animazione del menu
        if (menuController != null) {
            menuController.stopLoop();
        }
        primaryStage.setScene(gameScene);
        gameController.startGame(); // Avvia il ciclo di gioco e la musica
    }

    // Metodo per tornare al Menu principale
    public void showMenuScreen() {
        // Ferma il ciclo di gioco se era attivo
        if (gameController != null) {
            gameController.stopGame();
        }
        primaryStage.setScene(menuScene);
        
        // Riavvia il loop di animazione del menu
        if (menuController != null) {
             menuController.initialize(); 
        }
        // Avvia la musica del menu
        // AudioManager.getInstance().playMusic("/music/menu_theme.mp3"); 
    }

    // Metodo main standard per l'avvio dell'applicazione.
    public static void main(String[] args) {
        launch(args);
    }
}