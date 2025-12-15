package main.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import main.logic.*; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRenderer {

    private GraphicsContext gc;
    private double canvasWidth;
    private double canvasHeight;

    private static final double TILE_SIZE = 60;
    private static final double HUD_HEIGHT = 80;
    
    private final Map<TileType, Color> tileColors;
    private final Map<PowerUpType, Color> powerUpColors;

    public GameRenderer(GraphicsContext gc, double width, double height) {
        this.gc = gc;
        this.canvasWidth = width;
        this.canvasHeight = height;
        
        // Mappe Colori
        this.tileColors = new HashMap<>();
        tileColors.put(TileType.EMPTY, Color.web("#2E8B57")); 
        tileColors.put(TileType.WALL, Color.web("#555555"));  
        tileColors.put(TileType.BRICK, Color.web("#D2691E")); 
        tileColors.put(TileType.BOMB, Color.BLACK);

        this.powerUpColors = new HashMap<>();
        powerUpColors.put(PowerUpType.BOMB_UP, Color.BLACK);
        powerUpColors.put(PowerUpType.FIRE_UP, Color.YELLOW);
        powerUpColors.put(PowerUpType.SPEED_UP, Color.RED);
        powerUpColors.put(PowerUpType.REMOTE, Color.DARKGRAY);
    }

    public void clear() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
    }


    public void drawGameScene(GameMap map, Player player, List<Enemy> enemies, 
        List<Bomb> bombs, List<Explosion> explosions, 
        List<PowerUp> powerUps, List<Objective> objectives, 
        double mapOffsetX, double mapOffsetY) {

        drawDecorativeBackground(mapOffsetX, mapOffsetY, map.getColumns(), map.getRows());
        gc.save();
        gc.translate(mapOffsetX, mapOffsetY);
        drawMap(map);
        
        for (PowerUp p : powerUps) drawPowerUp(p);
        for (Objective o : objectives) drawObjective(o);     
        for (Bomb b : bombs) drawBomb(b);
        for (Enemy e : enemies) drawEnemy(e);
        for (Explosion e : explosions) drawExplosion(e);
        
        drawPlayer(player);
        gc.restore();
    }

    // --- NUOVO: Disegno Schermata Opzioni (In-Game) ---
    public void drawOptionsScreen(double volume, int selectedIndex) {
        // Disegna sfondo semitrasparente sopra il gioco
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);     
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 60));
        
        // Titolo
        gc.setFill(Color.YELLOW);
        gc.fillText("OPTIONS", canvasWidth/2, 150);
        
        // --- 1. VOLUME BAR ---
        double barWidth = 400;
        double barHeight = 40;
        double barX = (canvasWidth - barWidth) / 2;
        double barY = 350;
        
        // Etichetta Volume
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 40));
        gc.setFill(selectedIndex == 0 ? Color.WHITE : Color.GRAY);
        gc.fillText("VOLUME SFX", canvasWidth/2, barY - 30);
        
        // Sfondo Barra (Nero)
        gc.setFill(Color.BLACK);
        gc.fillRect(barX, barY, barWidth, barHeight);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);
        gc.strokeRect(barX, barY, barWidth, barHeight);
        
        // Riempimento Barra (Verde Lime)
        if (volume > 0) {
            gc.setFill(Color.LIME);
            gc.fillRect(barX + 4, barY + 4, (barWidth - 8) * volume, barHeight - 8);
        }
        
        // Testo Percentuale
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 20));
        gc.fillText((int)(volume * 100) + "%", canvasWidth/2, barY + barHeight + 30);
        
        // --- 2. BACK BUTTON ---
        double backY = 600;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 40));
        gc.setFill(selectedIndex == 1 ? Color.WHITE : Color.GRAY);
        gc.fillText("BACK", canvasWidth/2, backY);
        
        // --- CURSORE ---
        gc.setFill(Color.ORANGE);
        if (selectedIndex == 0) {
            gc.fillText(">", barX - 50, barY + 30); // Accanto alla barra
        } else {
            gc.fillText(">", canvasWidth/2 - 100, backY); // Accanto a Back
        }
        
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    // --- DISEGNO HUD ---
    public void drawHUD(int lives, int score, int enemiesKilled, double timeLeft, List<PowerUpType> activePowerUps, PowerUpType lastCollected, double notificationTimer) {
        gc.setFill(Color.web("#008000")); 
        gc.fillRect(0, 0, canvasWidth, HUD_HEIGHT);
        
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(4);
        gc.strokeRect(2, 2, canvasWidth-4, HUD_HEIGHT-4);
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setTextAlign(TextAlignment.LEFT);

        double centerY = HUD_HEIGHT / 2; double startX = 40;

        // 1. Testa Player
        drawMiniPlayerHead(startX, centerY - 15); 
        startX += 45; 

        // 2. Cuore con Vite
        double heartSize = 1.5; double heartX = startX; double heartY = centerY - 20; 
        gc.save(); 
        gc.translate(heartX, heartY); 
        gc.scale(heartSize, heartSize);
        gc.setFill(Color.RED); 
        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(1);
        gc.beginPath(); 
        gc.appendSVGPath("M 12,4 Q 4,4 4,10 Q 4,18 12,24 Q 20,18 20,10 Q 20,4 12,4 z");
        gc.fill(); gc.stroke(); gc.restore();

        // Vite
        gc.setFill(Color.WHITE); 
        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER); 
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        gc.fillText(String.valueOf(lives), heartX + 18, centerY + 8); 
        gc.strokeText(String.valueOf(lives), heartX + 18, centerY + 8);
        gc.setTextAlign(TextAlignment.LEFT);

        // 3. Nemici Uccisi
        startX += 90; double textBaselineY = centerY + 10; 
        drawSkullIcon(startX, centerY - 12);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28)); gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(enemiesKilled), startX + 35, textBaselineY);

        // 4. Punteggio
        startX += 120;
        drawCoinIcon(startX, centerY - 12);
        gc.setFill(Color.WHITE); 
        gc.setStroke(Color.BLACK); gc.setLineWidth(1);
        gc.fillText(String.format("%02d", score), startX + 35, textBaselineY);
        gc.strokeText(String.format("%02d", score), startX + 35, textBaselineY);

        // 5. Timer
        double timerX = canvasWidth - 220;
        gc.setFill(Color.BLACK); 
        gc.fillRect(timerX, centerY - 20, 160, 40);
        gc.setStroke(Color.CYAN); gc.setLineWidth(3); 
        gc.strokeRect(timerX, centerY - 20, 160, 40);
        drawClockIcon(timerX - 35, centerY - 15);
        int minutes = (int) timeLeft / 60; int seconds = (int) timeLeft % 60;
        gc.setFill(timeLeft < 30 ? Color.RED : Color.WHITE);
        gc.fillText(String.format("%02d:%02d", minutes, seconds), timerX + 40, textBaselineY);
        
        // --- SEZIONE POWER-UP / NOTIFICA ---
        double areaPowerUpX = startX + 120; // Spazio dedicato ai powerup      
        // Se c'è una notifica attiva (timer > 0), mostriamo QUELLA al posto della lista
        if (notificationTimer > 0 && lastCollected != null) {
            
            // Effetto lampeggio finale (opzionale)
            if (notificationTimer > 0.5 || (int)(notificationTimer * 10) % 2 == 0) {
                drawPowerUpNotification(lastCollected, areaPowerUpX, centerY - 15);
            }
            
        } else {
            // Altrimenti mostra la lista dei powerup attivi accumulati
            double iconX = areaPowerUpX; 
            for (PowerUpType p : activePowerUps) { 
                drawMiniPowerUp(p, iconX, centerY - 15); 
                iconX += 40; // Icone più vicine
            }
        }
    }
    
    // --- HELPER METODI PER ICONE HUD ---

    private void drawMiniPlayerHead(double x, double y) {
        double size = 30;
        gc.setFill(Color.WHITE); 
        gc.fillRect(x, y, size, size - 4); 
        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(2); gc.strokeRect(x, y, size, size - 4);
        gc.setFill(Color.BLACK); 
        gc.fillRect(x + 8, y + 8, 4, 8); 
        gc.fillRect(x + 18, y + 8, 4, 8);
        gc.setFill(Color.MAGENTA); 
        gc.fillRect(x + 10, y - 6, 10, 6);
        gc.strokeRect(x + 10, y - 6, 10, 6);
    }
    
    private void drawSkullIcon(double x, double y) {
        gc.setFill(Color.RED); 
        gc.fillRect(x, y, 24, 24);
        gc.setFill(Color.WHITE); 
        gc.fillRect(x+4, y+4, 6, 6); 
        gc.fillRect(x+14, y+4, 6, 6);
        gc.setFill(Color.BLACK); 
        gc.fillRect(x+8, y+16, 2, 4); 
        gc.fillRect(x+14, y+16, 2, 4);
    }
    
    private void drawCoinIcon(double x, double y) {
        gc.setFill(Color.GOLD); 
        gc.fillOval(x, y, 24, 24);
        gc.setStroke(Color.WHITE); 
        gc.setLineWidth(2); 
        gc.strokeOval(x+5, y+5, 14, 14);
    }
    
    private void drawClockIcon(double x, double y) {
        double size = 30;
        gc.setFill(Color.WHITE); gc.fillOval(x, y, size, size);
        gc.setStroke(Color.BLACK); gc.setLineWidth(2); gc.strokeOval(x, y, size, size);
        gc.setStroke(Color.RED); gc.strokeLine(x+15, y+15, x+15, y+5); gc.strokeLine(x+15, y+15, x+25, y+15);
    }
    
    private void drawMiniPowerUp(PowerUpType type, double x, double y) {
        double size = 30;
        gc.setFill(powerUpColors.get(type));
        gc.fillRect(x, y, size, size);
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size);
        
        gc.setFill(Color.WHITE);
        gc.fillRect(x + 8, y + 8, size - 16, size - 16);
    }

    // --- DISEGNO DEL MENU DI PAUSA PIXEL ART ---
    public void drawPauseMenu(String[] options, int selectedIndex) {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        double menuW = 400; double menuH = 300;
        double x = (canvasWidth - menuW) / 2; double y = (canvasHeight - menuH) / 2;
        
        gc.setFill(Color.web("#00008B")); 
        gc.fillRect(x, y, menuW, menuH);
        gc.setStroke(Color.ORANGE); 
        gc.setLineWidth(6); 
        gc.strokeRect(x, y, menuW, menuH);
        gc.setStroke(Color.WHITE); 
        gc.setLineWidth(2); 
        gc.strokeRect(x + 10, y + 10, menuW - 20, menuH - 20);

        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PAUSE", canvasWidth / 2, y + 80);
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        for (int i = 0; i < options.length; i++) {
            double textY = y + 160 + (i * 60);
            if (i == selectedIndex) {
                gc.setFill(Color.WHITE);
                gc.fillText(">", canvasWidth / 2 - 100, textY);
            } else {
                gc.setFill(Color.GRAY);
            }
            gc.fillText(options[i], canvasWidth / 2, textY);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    // --- OVERLAYS ---

    public void drawGameOverOverlay() {
        drawOverlay("GAME OVER", Color.RED);
    }
    
    public void drawPauseOverlay() {
        drawOverlay("PAUSA", Color.LIGHTBLUE);
    }

    public void drawOverlay(String text, Color color) {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        gc.setFill(color);
        gc.setFont(Font.font("Arial", 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, canvasWidth/2, canvasHeight/2);
        gc.setFont(Font.font("Arial", 20));
        gc.setFill(Color.WHITE);
        gc.fillText("Premi ENTER per tornare al menu", canvasWidth/2, canvasHeight/2 + 60);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    public void drawRespawnScreen(int lives) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText("VITE RIMASTE: " + lives, canvasWidth/2, canvasHeight/2);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    // --- NUOVO: Schermata Game Over con Timer ---
    public void drawGameOverScreen(int score, double timer) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Titolo GAME OVER Rosso
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 80));
        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", canvasWidth/2, canvasHeight/2 - 50);
        
        // Punteggio
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        gc.setFill(Color.WHITE);
        gc.fillText("Punteggio Finale: " + score, canvasWidth/2, canvasHeight/2 + 20);
        
        // Countdown
        gc.setFont(Font.font("Monospaced", 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Menu in " + (int)Math.ceil(timer) + "...", canvasWidth/2, canvasHeight/2 + 80);
        
        gc.setTextAlign(TextAlignment.LEFT);
    }

    public void drawVictoryScreen(int score, double timer) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Titolo VICTORY Oro
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 80));
        gc.setFill(Color.GOLD);
        gc.fillText("VICTORY", canvasWidth/2, canvasHeight/2 - 50);
        
        // Punteggio
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        gc.setFill(Color.WHITE);
        gc.fillText("Punteggio Finale: " + score, canvasWidth/2, canvasHeight/2 + 20);
        
        // Countdown
        gc.setFont(Font.font("Monospaced", 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Menu in " + (int)Math.ceil(timer) + "...", canvasWidth/2, canvasHeight/2 + 80);
        
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // --- DISEGNO ENTITÀ (INVARIATO) ---

    private void drawMap(GameMap map) {
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getColumns(); c++) {
                TileType type = map.getTile(c, r);
                double x = c * TILE_SIZE; double y = r * TILE_SIZE;

                gc.setFill(tileColors.get(TileType.EMPTY)); 
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.setFill(Color.web("#3CB371")); 
                gc.fillRect(x + 5, y + 5, 4, 4);
                gc.fillRect(x + 40, y + 15, 4, 4);
                gc.fillRect(x + 20, y + 40, 4, 4);
                gc.fillRect(x + 50, y + 50, 4, 4);

                if (type == TileType.WALL) {
                    drawWallBlock(x, y);
                } else if (type == TileType.BRICK) {
                    drawBrickBlock(x, y);
                }
            }
        }
    }
    
    private void drawWallBlock(double x, double y) {
        gc.setFill(tileColors.get(TileType.WALL));
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setFill(Color.web("#777777")); 
        gc.fillRect(x, y, TILE_SIZE, 4); 
        gc.fillRect(x, y, 4, TILE_SIZE);
        gc.setFill(Color.web("#333333")); 
        gc.fillRect(x, y+TILE_SIZE-4, TILE_SIZE, 4); 
        gc.fillRect(x+TILE_SIZE-4, y, 4, TILE_SIZE);
        gc.setFill(Color.web("#444444")); 
        gc.fillRect(x+10, y+10, TILE_SIZE-20, TILE_SIZE-20);
    }
    
    private void drawBrickBlock(double x, double y) {
        gc.setFill(tileColors.get(TileType.BRICK)); 
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(2);
        gc.strokeLine(x, y+15, x+TILE_SIZE, y+15); 
        gc.strokeLine(x, y+30, x+TILE_SIZE, y+30);
        gc.strokeLine(x, y+45, x+TILE_SIZE, y+45); 
        gc.strokeLine(x+30, y, x+30, y+15);
        gc.strokeLine(x+15, y+15, x+15, y+30); 
        gc.strokeLine(x+45, y+15, x+45, y+30);
        gc.strokeLine(x+30, y+30, x+30, y+45); 
        gc.strokeLine(x+15, y+45, x+15, y+60);
        gc.strokeLine(x+45, y+45, x+45, y+60);
        gc.setFill(Color.rgb(0,0,0,0.2)); 
        gc.fillRect(x+TILE_SIZE-4, y, 4, TILE_SIZE); 
        gc.fillRect(x, y+TILE_SIZE-4, TILE_SIZE, 4);
    }

    private void drawPlayer(Player p) {
        if (p.isImmune() && (p.getImmunityFrames() / 4) % 2 == 0) return;
        
        double x = p.getX();
        double y = p.getY();
        double s = Player.SIZE;
        Player.Direction dir = p.getDirection();
        
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillOval(x + 10, y + s - 8, s - 20, 8);

        switch (dir) {
            case DOWN: drawPlayerFront(x, y, s); break;
            case UP: drawPlayerBack(x, y, s); break;
            case LEFT: drawPlayerSide(x, y, s, true); break;
            case RIGHT: drawPlayerSide(x, y, s, false); break;
        }
    }
    
    private void drawPlayerFront(double x, double y, double s) {
        gc.setFill(Color.BLUE); 
        gc.fillRect(x + 16, y + 26, 16, 14);

        gc.setFill(Color.BLACK); 
        gc.fillRect(x + 16, y + 36, 16, 4);

        gc.setFill(Color.GOLD); 
        gc.fillRect(x + 22, y + 36, 4, 4);

        gc.setFill(Color.WHITE); 
        gc.fillRect(x + 12, y + 4, 24, 24); 

        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(2); 
        gc.strokeRect(x + 12, y + 4, 24, 24);

        gc.setFill(Color.PEACHPUFF); 
        gc.fillRect(x + 16, y + 14, 16, 10);

        gc.setFill(Color.BLACK); 
        gc.fillRect(x + 20, y + 16, 2, 6); 
        gc.fillRect(x + 26, y + 16, 2, 6); 

        gc.setFill(Color.MAGENTA);
        gc.fillOval(x + 6, y + 26, 10, 10); 
        gc.fillOval(x + 32, y + 26, 10, 10); 
        gc.fillOval(x + 12, y + 38, 12, 10); 
        gc.fillOval(x + 24, y + 38, 12, 10); 
        gc.fillRect(x + 22, y - 2, 4, 6);
    }

    private void drawPlayerBack(double x, double y, double s) {
        gc.setFill(Color.BLUE); 
        gc.fillRect(x + 16, y + 26, 16, 14);

        gc.setFill(Color.WHITE); 
        gc.fillRect(x + 12, y + 4, 24, 24); 

        gc.setStroke(Color.BLACK); 
        gc.setLineWidth(2); 
        gc.strokeRect(x + 12, y + 4, 24, 24);

        gc.setFill(Color.MAGENTA);
        gc.fillOval(x + 8, y + 28, 8, 8); 
        gc.fillOval(x + 32, y + 28, 8, 8); 
        gc.fillOval(x + 12, y + 38, 12, 10); 
        gc.fillOval(x + 24, y + 38, 12, 10); 
        gc.fillRect(x + 22, y - 2, 4, 6);
    }

    private void drawPlayerSide(double x, double y, double s, boolean isLeft) {
        gc.setFill(Color.BLUE); gc.fillRect(x + 18, y + 26, 12, 14);
        gc.setFill(Color.WHITE); gc.fillRect(x + 14, y + 4, 20, 24); 
        gc.setStroke(Color.BLACK); gc.setLineWidth(2); gc.strokeRect(x + 14, y + 4, 20, 24);
        gc.setFill(Color.PEACHPUFF);
        if (isLeft) gc.fillRect(x + 14, y + 14, 8, 10); else gc.fillRect(x + 26, y + 14, 8, 10); 
        gc.setFill(Color.BLACK);
        if (isLeft) gc.fillRect(x + 16, y + 16, 2, 6); else gc.fillRect(x + 30, y + 16, 2, 6);
        gc.setFill(Color.MAGENTA);
        if (isLeft) {
            gc.fillOval(x + 8, y + 30, 10, 10); gc.fillOval(x + 16, y + 38, 14, 10); 
        } else {
            gc.fillOval(x + 30, y + 30, 10, 10); gc.fillOval(x + 18, y + 38, 14, 10); 
        }
        gc.fillRect(x + 22, y - 2, 4, 6);
    }

    private void drawEnemy(Enemy e) {
        double x = e.getX(); double y = e.getY(); double size = Enemy.SIZE;
        double cx = x + size/2; double cy = y + size/2;
        gc.setFill(Color.ORANGERED); gc.fillRect(x + 8, y + 8, size - 16, size - 16);
        gc.fillRect(x + 4, y + 12, 4, size - 24); gc.fillRect(x + size - 8, y + 12, 4, size - 24);
        gc.fillRect(x + 12, y + 4, size - 24, 4); gc.fillRect(x + 12, y + size - 8, size - 24, 4);
        gc.setFill(Color.BLACK); gc.fillRect(cx - 10, cy - 6, 4, 8); gc.fillRect(cx + 6, cy - 6, 4, 8);  
        gc.setFill(Color.WHITE); gc.fillRect(cx - 10, cy - 6, 2, 2); gc.fillRect(cx + 6, cy - 6, 2, 2);
        gc.setStroke(Color.DARKRED); gc.setLineWidth(2);
        gc.strokeLine(cx - 8, cy + 8, cx - 4, cy + 12); gc.strokeLine(cx - 4, cy + 12, cx, cy + 8);
        gc.strokeLine(cx, cy + 8, cx + 4, cy + 12); gc.strokeLine(cx + 4, cy + 12, cx + 8, cy + 8);
    }

    private void drawBomb(Bomb b) {
        double x = b.getCol() * TILE_SIZE; double y = b.getRow() * TILE_SIZE;
        double r = b.getCurrentRadius(); double offset = (TILE_SIZE - r) / 2;
        gc.setFill(b.isRemote() ? Color.DARKRED : Color.web("#111111")); gc.fillOval(x + offset, y + offset, r, r);
        gc.setFill(Color.WHITE); gc.fillOval(x + offset + r*0.2, y + offset + r*0.2, r*0.25, r*0.25);
        gc.setFill(Color.GOLD); gc.fillRect(x + TILE_SIZE/2.0 - 6, y + offset - 6, 12, 8);
        gc.setStroke(Color.WHITE); gc.setLineWidth(3);
        double mx = x + TILE_SIZE/2.0; double my = y + offset - 6;
        if (b.getTimerFrames() % 20 < 10) gc.strokeLine(mx, my, mx, my - 8);
        else gc.strokeLine(mx, my, mx + 4, my - 8);
        if (!b.isRemote()) {
            gc.setFill((b.getTimerFrames() / 5) % 2 == 0 ? Color.RED : Color.YELLOW);
            gc.fillOval(mx - 3, my - 12, 6, 6);
        }
    }
    
    private void drawExplosion(Explosion e) {
        double x = e.getCol() * TILE_SIZE; double y = e.getRow() * TILE_SIZE;
        double s = TILE_SIZE;
        double life = (double) e.getTimer() / 30.0;
        gc.setFill(life > 0.7 ? Color.WHITE : (life > 0.4 ? Color.YELLOW : Color.ORANGERED));
        double pulse = (e.getTimer() % 6) < 3 ? 4 : 0;
        gc.fillRect(x + 10 + pulse, y + 10 + pulse, s - 20 - pulse*2, s - 20 - pulse*2);
        gc.fillRect(x + 5, y + 20, 5, s - 40); gc.fillRect(x + s - 10, y + 20, 5, s - 40);
        gc.fillRect(x + 20, y + 5, s - 40, 5); gc.fillRect(x + 20, y + s - 10, s - 40, 5);
        gc.setFill(Color.rgb(255, 255, 255, 0.5)); gc.fillRect(x + 20, y + 20, s - 40, s - 40);
    }

    private void drawPowerUp(PowerUp p) {
        double x = p.getCol() * TILE_SIZE + (TILE_SIZE - PowerUp.SIZE) / 2.0;
        double y = p.getRow() * TILE_SIZE + (TILE_SIZE - PowerUp.SIZE) / 2.0;
        double s = PowerUp.SIZE;
        
        // Colore base in base al tipo
        Color baseColor = Color.RED; // Default (Speed o generico)
        Color darkColor = Color.DARKRED;
        Color lightColor = Color.web("#FF6666"); // Rosso chiaro per highlight
        
        switch(p.getType()) {
            case BOMB_UP: 
                baseColor = Color.BLACK; 
                darkColor = Color.web("#333333"); 
                lightColor = Color.GRAY; 
                break;
            case FIRE_UP: 
                baseColor = Color.ORANGE; 
                darkColor = Color.web("#CC8400"); 
                lightColor = Color.YELLOW; 
                break;
            case SPEED_UP: 
                baseColor = Color.RED; 
                darkColor = Color.DARKRED; 
                lightColor = Color.web("#FF4444"); 
                break;
            case REMOTE: 
                baseColor = Color.BLUE; 
                darkColor = Color.DARKBLUE; 
                lightColor = Color.CYAN; 
                break;
        }

        // Disegno Cubo 3D Stilizzato (Simile alla foto)
        // Faccia frontale
        gc.setFill(baseColor);
        gc.fillRect(x, y, s, s);
        
        // Bordo/Ombra leggero 3D (facoltativo, per dare profondità)
        gc.setFill(darkColor);
        gc.fillRect(x + s, y + 5, 5, s - 5); // Lato destro scuro
        gc.fillRect(x + 5, y + s, s, 5);     // Lato sotto scuro
        
        // Bordo Bianco/Highlight (stile "box lucido")
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, s, s);
        
        // Punti esclamativi "!" o simbolo
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, s * 0.8));
        gc.setTextAlign(TextAlignment.CENTER);
        // Disegna un "!" o un simbolo specifico
        String symbol = "!";
        if (p.getType() == PowerUpType.BOMB_UP) symbol = "B";
        if (p.getType() == PowerUpType.FIRE_UP) symbol = "F";
        
        // Ombra del testo
        gc.setFill(Color.rgb(0,0,0,0.3));
        gc.fillText(symbol, x + s/2 + 2, y + s - 5 + 2);
        
        // Testo vero e proprio
        gc.setFill(Color.WHITE);
        gc.fillText(symbol, x + s/2, y + s - 5);
        
        // "Bulloni" agli angoli (come nella foto)
        gc.setFill(darkColor.darker());
        double boltSize = 4;
        gc.fillOval(x + 2, y + 2, boltSize, boltSize);
        gc.fillOval(x + s - 6, y + 2, boltSize, boltSize);
        gc.fillOval(x + 2, y + s - 6, boltSize, boltSize);
        gc.fillOval(x + s - 6, y + s - 6, boltSize, boltSize);
    }

    // --- NUOVO METODO HELPER per disegnare Icona + Nome a destra ---
    private void drawPowerUpNotification(PowerUpType type, double x, double y) {
        // 1. Disegna l'icona
        drawMiniPowerUp(type, x, y);

        // 2. Prepara il testo
        String text = "";
        Color c = Color.WHITE;
        switch(type) {
            case BOMB_UP: text = "BOMB UP"; c = Color.GRAY; break;
            case FIRE_UP: text = "FIRE UP"; c = Color.ORANGE; break;
            case SPEED_UP: text = "SPEED UP"; c = Color.RED; break;
            case REMOTE: text = "REMOTE"; c = Color.CYAN; break;
        }

        // 3. Disegna il testo A DESTRA dell'icona
        // L'icona è larga 30px (definito in drawMiniPowerUp), lasciamo 10px di spazio
        double textX = x + 40; 
        double textY = y + 24; // Centratura verticale approssimativa per font 28

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setTextAlign(TextAlignment.LEFT);
        
        // Ombra nera
        gc.setFill(Color.BLACK);
        gc.fillText(text, textX + 2, textY + 2);
        
        // Testo colorato
        gc.setFill(c);
        gc.fillText(text, textX, textY);
    }
    
    private void drawObjective(Objective o) {
        if (o.isCollected()) return;
        double cx = o.getCol() * TILE_SIZE + TILE_SIZE / 2.0; double cy = o.getRow() * TILE_SIZE + TILE_SIZE / 2.0;
        double s = Objective.SIZE * o.getScale();
        gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.4)); 
        gc.fillOval(cx - s/2 + 4, cy - s/2 + 4, s, s);
        gc.setFill(Color.web("#00CED1")); 
        gc.fillOval(cx - s/2, cy - s/2, s, s);
        gc.setStroke(Color.GOLD); 
        gc.setLineWidth(3); 
        gc.strokeOval(cx - s/2, cy - s/2, s, s);
        gc.setFill(Color.WHITE); 
        gc.fillOval(cx - s/2 + s * 0.2, cy - s/2 + s * 0.2, s * 0.25, s * 0.25);
    }
    
    private void drawDecorativeBackground(double mapX, double mapY, int cols, int rows) {
        gc.setFill(Color.web("#204020")); 
        gc.fillRect(0, HUD_HEIGHT, canvasWidth, canvasHeight - HUD_HEIGHT);
        gc.setFill(Color.rgb(0,0,0,0.5)); 
        gc.fillRect(mapX + 10, mapY + 10, cols * TILE_SIZE, rows * TILE_SIZE);
    }
}