package main.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
//import main.AudioManager;
import main.Main;

public class MenuController {

    private Main mainApp;

    @FXML
    public void initialize() {
        AudioManager.getInstance().playMusic("/music/menu_theme.mp3");
        AudioManager.getInstance().loadSoundEffect("confirm", "/sfx/confirm.wav");
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    void handlePlayButton(ActionEvent event) {
        AudioManager.getInstance().playSoundEffect("confirm");
        AudioManager.getInstance().stopMusic();
        if (mainApp != null) {
            mainApp.showGameScreen();
        }
    }

    @FXML
    void handleQuitButton(ActionEvent event) {
        System.exit(0);
    }
}