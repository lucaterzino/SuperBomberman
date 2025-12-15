package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class LevelMapRenderer {
    private GraphicsContext gc;
    private double w, h;

    public LevelMapRenderer(GraphicsContext gc, double w, double h) {
        this.gc = gc; this.w = w; this.h = h;
    }

    public void drawMap(int selectedLevelIndex, int maxUnlockedLevel) {
        gc.clearRect(0, 0, w, h);
        
        // Sfondo Mappa (Verde scuro / Terreno)
        gc.setFill(Color.web("#228B22")); 
        gc.fillRect(0, 0, w, h);
        
        // Titolo
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SELECT LEVEL", w/2, 100);

        // Coordinate dei 3 livelli (disposti a triangolo o linea)
        double[][] positions = {
            {w/2, 300},      // Livello 1 (Alto)
            {w/2 - 200, 500},// Livello 2 (Sinistra)
            {w/2 + 200, 500} // Livello 3 (Destra)
        };

        // Disegna percorsi (Linee tratteggiate)
        gc.setStroke(Color.web("#DEB887")); // Color sabbia
        gc.setLineWidth(8);
        gc.setLineDashes(15);
        if (maxUnlockedLevel >= 2) gc.strokeLine(positions[0][0], positions[0][1], positions[1][0], positions[1][1]);
        if (maxUnlockedLevel >= 3) gc.strokeLine(positions[1][0], positions[1][1], positions[2][0], positions[2][1]);
        gc.setLineDashes(null); // Reset

        // Disegna i nodi dei livelli
        for (int i = 0; i < 3; i++) {
            boolean isLocked = (i + 1) > maxUnlockedLevel;
            boolean isSelected = (i == selectedLevelIndex);
            
            drawLevelNode(positions[i][0], positions[i][1], i + 1, isLocked, isSelected);
        }
        
        // Istruzioni
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("Use ARROW KEYS to select, ENTER to start", w/2, h - 50);
        gc.fillText("Press ESC to return to Title Screen", w/2, h - 20);
    }

    private void drawLevelNode(double x, double y, int levelNum, boolean isLocked, boolean isSelected) {
        double radius = 60;
        
        // Cerchio esterno (Bordo)
        if (isSelected) {
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(6);
            // Effetto pulsante se selezionato
            gc.strokeOval(x - radius - 5, y - radius - 5, radius * 2 + 10, radius * 2 + 10);
        }

        // Riempimento
        if (isLocked) {
            gc.setFill(Color.GRAY);
        } else {
            // Colori diversi per livello
            if (levelNum == 1) gc.setFill(Color.LIMEGREEN);
            else if (levelNum == 2) gc.setFill(Color.ORANGE);
            else gc.setFill(Color.RED);
        }
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Bordo interno
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // Contenuto (Numero o Lucchetto)
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        
        if (isLocked) {
            // Disegno Lucchetto stilizzato
            gc.fillRect(x - 15, y, 30, 25); // Corpo
            gc.setStroke(Color.WHITE); gc.setLineWidth(4);
            gc.strokeArc(x - 10, y - 20, 20, 30, 0, 180, javafx.scene.shape.ArcType.OPEN);
        } else {
            // Numero Livello
            gc.setFont(Font.font("Impact", 60));
            gc.fillText(String.valueOf(levelNum), x, y + 20);
        }
        
        // Etichetta sotto
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        gc.fillText("LEVEL " + levelNum, x, y + radius + 30);
    }
}