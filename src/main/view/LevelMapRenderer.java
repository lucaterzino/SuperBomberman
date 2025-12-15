package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class LevelMapRenderer {
    private GraphicsContext gc;
    private double w, h;

    public LevelMapRenderer(GraphicsContext gc, double w, double h) {
        this.gc = gc;
        this.w = w;
        this.h = h;
    }

    public void drawMap(int selectedLevelIndex, int maxUnlockedLevel) {
        // 1. DISEGNA SFONDO (Cielo)
        drawSkyBackground();

        // Coordinate dei 3 livelli (disposti a triangolo)
        double x1 = w * 0.5;     double y1 = h * 0.3;   // Livello 1 (Alto)
        double x2 = w * 0.25;    double y2 = h * 0.65;  // Livello 2 (Basso Sx)
        double x3 = w * 0.75;    double y3 = h * 0.65;  // Livello 3 (Basso Dx)
        
        double[][] positions = { {x1, y1}, {x2, y2}, {x3, y3} };

        // 2. DISEGNA I PONTI/SENTIERI (Dietro le isole)
        drawPaths(positions, maxUnlockedLevel);

        // 3. DISEGNA LE ISOLE FLUTTUANTI
        drawFloatingIsland(x1, y1);
        drawFloatingIsland(x2, y2);
        drawFloatingIsland(x3, y3);

        // 4. DISEGNA I NODI DEI LIVELLI (Sopra le isole)
        for (int i = 0; i < 3; i++) {
            boolean isLocked = (i + 1) > maxUnlockedLevel;
            boolean isSelected = (i == selectedLevelIndex);
            
            // Colori specifici per ogni livello
            Color levelColor = Color.LIMEGREEN; 
            if (i == 1) levelColor = Color.ORANGE;
            if (i == 2) levelColor = Color.RED;
            
            drawLevelNode(positions[i][0], positions[i][1] - 20, i + 1, isLocked, isSelected, levelColor);
        }

        // 5. Interfaccia Utente
        drawUI();
    }

    private void drawSkyBackground() {
        // Gradiente Cielo (Azzurro -> Blu scuro)
        LinearGradient skyGrad = new LinearGradient(0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#87CEEB")), // SkyBlue
                new Stop(1, Color.web("#4682B4"))); // SteelBlue
        gc.setFill(skyGrad);
        gc.fillRect(0, 0, w, h);
        
        // Nuvole decorative (Ellissi semi-trasparenti)
        gc.setFill(Color.web("#FFFFFF", 0.6));
        gc.fillOval(100, 100, 150, 60);
        gc.fillOval(800, 150, 180, 70);
        gc.fillOval(400, 500, 200, 80);
        gc.fillOval(150, 600, 120, 50);
        
        // Montagne in lontananza (Triangoli alla base)
        gc.setFill(Color.web("#2F4F4F")); // DarkSlateGray
        gc.fillPolygon(new double[]{0, 200, 400}, new double[]{h, h-200, h}, 3);
        gc.fillPolygon(new double[]{300, 600, 900}, new double[]{h, h-300, h}, 3);
        gc.fillPolygon(new double[]{700, 900, 1200}, new double[]{h, h-250, h}, 3);
    }

    private void drawFloatingIsland(double x, double y) {
        double width = 160;
        double height = 100;
        
        // Parte inferiore (Terra/Roccia) - Più scura per dare profondità
        gc.setFill(Color.web("#8B4513")); // SaddleBrown
        // Disegniamo un cono rovesciato irregolare
        gc.fillPolygon(
            new double[]{x - width/2 + 20, x + width/2 - 20, x}, 
            new double[]{y, y, y + height}, 
            3
        );
        
        // Strato intermedio terra
        gc.fillOval(x - width/2, y - 20, width, 60);

        // Parte superiore (Erba)
        gc.setFill(Color.web("#32CD32")); // LimeGreen
        gc.fillOval(x - width/2 - 5, y - 30, width + 10, 50);
        
        // Bordo erba
        gc.setStroke(Color.web("#228B22")); // ForestGreen
        gc.setLineWidth(3);
        gc.strokeOval(x - width/2 - 5, y - 30, width + 10, 50);
    }

    private void drawPaths(double[][] pos, int maxUnlocked) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(6);
        gc.setLineDashes(15, 10); // Tratteggio

        // Coordinate aggiustate per partire dal centro delle isole
        // Percorso 1 -> 2
        if (maxUnlocked >= 1) gc.strokeLine(pos[0][0], pos[0][1], pos[1][0], pos[1][1]);
        // Percorso 2 -> 3
        if (maxUnlocked >= 2) gc.strokeLine(pos[1][0], pos[1][1], pos[2][0], pos[2][1]);
        
        gc.setLineDashes(null); // Reset
    }

    private void drawLevelNode(double x, double y, int levelNum, boolean isLocked, boolean isSelected, Color color) {
        double radius = 40; 

        // SELETTORE (Cerchio pulsante esterno)
        if (isSelected) {
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(5);
            gc.strokeOval(x - radius - 10, y - radius - 10, (radius + 10) * 2, (radius + 10) * 2);
        }

        // SFONDO NODO
        if (isLocked) {
            gc.setFill(Color.GRAY);
        } else {
            // Gradiente leggero per effetto bottone 3D
            LinearGradient btnGrad = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color.brighter()), new Stop(1, color.darker()));
            gc.setFill(btnGrad);
        }

        // Cerchio Principale
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Bordo Bianco
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // CONTENUTO (Numero o Lucchetto)
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        
        if (isLocked) {
            // Lucchetto stilizzato
            gc.fillRect(x - 12, y - 5, 24, 20); // Corpo
            gc.setStroke(Color.WHITE); gc.setLineWidth(3);
            gc.strokeArc(x - 10, y - 20, 20, 30, 0, 180, javafx.scene.shape.ArcType.OPEN);
        } else {
            // Numero Livello
            gc.setFont(Font.font("Impact", FontWeight.BOLD, 50));
            // Ombra testo
            gc.setFill(Color.rgb(0,0,0,0.3));
            gc.fillText(String.valueOf(levelNum), x + 3, y + 20);
            // Testo vero
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(levelNum), x, y + 18);
        }
        
        // ETICHETTA (Solo se selezionato)
        if (isSelected) {
            gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 18));
            gc.setStroke(Color.BLACK); gc.setLineWidth(3);
            gc.setFill(Color.YELLOW);
            
            String label = "LEVEL " + levelNum;
            gc.strokeText(label, x, y + radius + 40);
            gc.fillText(label, x, y + radius + 40);
        }
    }

    private void drawUI() {
        // Titolo "WORLD MAP"
        gc.setFont(Font.font("Impact", FontWeight.BOLD, 60));
        gc.setFill(Color.GOLD); 
        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(3);
        
        gc.strokeText("WORLD MAP", w/2, 80);
        gc.fillText("WORLD MAP", w/2, 80);

        // Istruzioni in basso
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE);
        gc.fillText("[ ARROWS ] Move    [ ENTER ] Select    [ ESC ] Back", w/2, h - 30);
    }
}