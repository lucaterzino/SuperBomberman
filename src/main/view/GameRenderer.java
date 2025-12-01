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
    
    // --- DISEGNO HUD ---
    public void drawHUD(int lives, int score, int enemiesKilled, double timeLeft, List<PowerUpType> activePowerUps) {
        gc.setFill(Color.web("#008000")); 
        gc.fillRect(0, 0, canvasWidth, HUD_HEIGHT);
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(4);
        gc.strokeRect(2, 2, canvasWidth-4, HUD_HEIGHT-4);
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setTextAlign(TextAlignment.LEFT);
        double centerY = HUD_HEIGHT / 2;
        double startX = 40;

        drawMiniPlayerHead(startX, centerY - 15);
        startX += 45; 

        double heartSize = 1.5;
        double heartX = startX;
        double heartY = centerY - 20; 
        
        gc.save();
        gc.translate(heartX, heartY);
        gc.scale(heartSize, heartSize);
        gc.setFill(Color.RED); gc.setStroke(Color.BLACK); gc.setLineWidth(1);
        gc.beginPath();
        gc.appendSVGPath("M 12,4 Q 4,4 4,10 Q 4,18 12,24 Q 20,18 20,10 Q 20,4 12,4 z");
        gc.fill(); gc.stroke();
        gc.restore();

        gc.setFill(Color.WHITE); gc.setStroke(Color.BLACK); gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        gc.fillText(String.valueOf(lives), heartX + 22, heartY + 32); 
        gc.strokeText(String.valueOf(lives), heartX + 22, heartY + 32);
        gc.setTextAlign(TextAlignment.LEFT);

        startX += 90; 
        double textBaselineY = centerY + 10; 
        gc.setFill(Color.RED); gc.fillRect(startX, centerY - 12, 24, 24); 
        gc.setFill(Color.WHITE); gc.fillRect(startX+4, centerY-8, 6, 6); gc.fillRect(startX+14, centerY-8, 6, 6);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(enemiesKilled), startX + 35, textBaselineY);

        startX += 120;
        gc.setFill(Color.GOLD); gc.fillOval(startX, centerY - 12, 24, 24);
        gc.setStroke(Color.WHITE); gc.strokeOval(startX+5, centerY-7, 14, 14);
        gc.setFill(Color.WHITE); gc.setStroke(Color.BLACK); gc.setLineWidth(1);
        gc.fillText(String.format("%02d", score), startX + 35, textBaselineY);
        gc.strokeText(String.format("%02d", score), startX + 35, textBaselineY);

        double timerX = canvasWidth - 220;
        gc.setFill(Color.BLACK); gc.fillRect(timerX, centerY - 20, 160, 40);
        gc.setStroke(Color.CYAN); gc.setLineWidth(3); gc.strokeRect(timerX, centerY - 20, 160, 40);
        int min = (int)timeLeft/60; int sec = (int)timeLeft%60;
        gc.setFill(timeLeft < 30 ? Color.RED : Color.WHITE);
        gc.fillText(String.format("%02d:%02d", min, sec), timerX + 40, textBaselineY);
        
        double iconX = startX + 150; 
        for(PowerUpType p : activePowerUps) {
            drawMiniPowerUp(p, iconX, centerY - 15);
            iconX += 80; 
        }
    }

    public void drawOverlay(String text, Color color) {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        gc.setFill(color);
        gc.setFont(Font.font("Arial", 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, canvasWidth/2, canvasHeight/2);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    public void drawGameOverOverlay() {
        drawOverlay("GAME OVER", Color.RED);
    }
    
    public void drawPauseOverlay() {
        drawOverlay("PAUSA", Color.LIGHTBLUE);
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

    public void drawVictoryScreen(int score, double timer) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 80));
        gc.setFill(Color.GOLD);
        gc.fillText("VICTORY", canvasWidth/2, canvasHeight/2 - 50);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        gc.setFill(Color.WHITE);
        gc.fillText("Punteggio Finale: " + score, canvasWidth/2, canvasHeight/2 + 20);
        gc.setFont(Font.font("Monospaced", 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Menu in " + (int)Math.ceil(timer) + "...", canvasWidth/2, canvasHeight/2 + 80);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // --- METODI DI SUPPORTO PRIVATI ---

    private void drawMap(GameMap map) {
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getColumns(); c++) {
                TileType type = map.getTile(c, r);
                double x = c * TILE_SIZE; double y = r * TILE_SIZE;

                gc.setFill(tileColors.get(TileType.EMPTY)); 
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.setFill(Color.web("#3CB371")); gc.fillRect(x+5, y+5, 4, 4); gc.fillRect(x+40, y+15, 4, 4);

                if (type == TileType.WALL) {
                    gc.setFill(tileColors.get(type)); gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    gc.setFill(Color.web("#AAAAAA")); gc.fillRect(x, y, TILE_SIZE, 4); gc.fillRect(x, y, 4, TILE_SIZE);
                    gc.setFill(Color.web("#333333")); gc.fillRect(x, y+TILE_SIZE-4, TILE_SIZE, 4);
                } else if (type == TileType.BRICK) {
                    gc.setFill(tileColors.get(type)); gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.BLACK); gc.setLineWidth(2);
                    gc.strokeLine(x, y+15, x+TILE_SIZE, y+15); gc.strokeLine(x, y+30, x+TILE_SIZE, y+30);
                }
            }
        }
    }

    private void drawPlayer(Player p) {
        if (p.isImmune() && (p.getImmunityFrames()/4)%2==0) return;
        double x = p.getX(); double y = p.getY(); double size = Player.SIZE;
        gc.setFill(Color.BLUE); gc.fillRect(x+14, y+24, 20, 14);
        gc.setFill(Color.WHITE); gc.fillRect(x+10, y+4, 28, 26);
        gc.setStroke(Color.BLACK); gc.setLineWidth(2); gc.strokeRect(x+10, y+4, 28, 26);
        gc.setFill(Color.BLACK); gc.fillRect(x+size/2-6, y+14, 4, 8); gc.fillRect(x+size/2+2, y+14, 4, 8);
        gc.setFill(Color.MAGENTA); gc.fillRect(x+size/2-4, y-4, 8, 8);
    }

    private void drawEnemy(Enemy e) {
        double x = e.getX(); double y = e.getY(); double size = Enemy.SIZE;
        gc.setFill(Color.ORANGERED); gc.fillRect(x+8, y+8, size-16, size-16);
        gc.setFill(Color.BLACK); gc.fillRect(x+size/2-10, y+size/2-6, 4, 8); gc.fillRect(x+size/2+6, y+size/2-6, 4, 8);
        gc.setFill(Color.WHITE); gc.fillRect(x+size/2-10, y+size/2-6, 2, 2); gc.fillRect(x+size/2+6, y+size/2-6, 2, 2);
    }

    private void drawBomb(Bomb b) {
        double x = b.getCol()*TILE_SIZE; double y = b.getRow()*TILE_SIZE;
        double r = b.getCurrentRadius(); double off = (TILE_SIZE-r)/2;
        gc.setFill(b.isRemote() ? Color.DARKRED : Color.web("#111111"));
        gc.fillOval(x+off, y+off, r, r);
        gc.setFill(Color.GOLD); gc.fillRect(x+TILE_SIZE/2.0-6, y+off-6, 12, 8);
        gc.setStroke(Color.WHITE); gc.setLineWidth(3);
        if(b.getTimerFrames()%20 < 10) gc.strokeLine(x+TILE_SIZE/2.0, y+off-6, x+TILE_SIZE/2.0, y+off-14);
        else gc.strokeLine(x+TILE_SIZE/2.0, y+off-6, x+TILE_SIZE/2.0+4, y+off-14);
    }
    
    private void drawExplosion(Explosion e) {
        double x = e.getCol()*TILE_SIZE; double y = e.getRow()*TILE_SIZE;
        double p = (double)e.getTimer()/30.0;
        gc.setFill(p > 0.7 ? Color.WHITE : (p > 0.4 ? Color.YELLOW : Color.ORANGERED));
        gc.fillRect(x+10, y+10, TILE_SIZE-20, TILE_SIZE-20);
    }

    private void drawPowerUp(PowerUp p) {
        double x = p.getCol()*TILE_SIZE+(TILE_SIZE-PowerUp.SIZE)/2;
        double y = p.getRow()*TILE_SIZE+(TILE_SIZE-PowerUp.SIZE)/2;
        gc.setFill(powerUpColors.get(p.getType()));
        gc.fillRect(x, y, PowerUp.SIZE, PowerUp.SIZE);
        gc.setStroke(Color.WHITE); gc.setLineWidth(2); gc.strokeRect(x, y, PowerUp.SIZE, PowerUp.SIZE);
        
        if(p.getType() == PowerUpType.BOMB_UP) {
             gc.setFill(Color.BLACK); gc.fillOval(x+PowerUp.SIZE/2-8, y+PowerUp.SIZE/2-6, 16, 16);
        } else {
             gc.setFill(Color.WHITE); gc.fillRect(x+PowerUp.SIZE/2-5, y+PowerUp.SIZE/2-5, 10, 10);
        }
    }
    
    private void drawMiniPowerUp(PowerUpType type, double x, double y) {
        double size = 30;
        gc.setFill(powerUpColors.get(type));
        gc.fillRect(x, y, size, size);
        gc.setStroke(Color.WHITE); gc.setLineWidth(2); gc.strokeRect(x, y, size, size);
        gc.setFill(Color.WHITE); gc.fillRect(x+8, y+8, size-16, size-16);
    }
    
    private void drawObjective(Objective o) {
        if(o.isCollected()) return;
        double cx = o.getCol()*TILE_SIZE+TILE_SIZE/2.0; double cy = o.getRow()*TILE_SIZE+TILE_SIZE/2.0;
        double s = Objective.SIZE * o.getScale();
        gc.setFill(Color.BLACK.deriveColor(0,0,0,0.4)); gc.fillOval(cx-s/2+4, cy-s/2+4, s, s);
        gc.setFill(Color.web("#00CED1")); gc.fillOval(cx-s/2, cy-s/2, s, s);
        gc.setStroke(Color.GOLD); gc.setLineWidth(3); gc.strokeOval(cx-s/2, cy-s/2, s, s);
        gc.setFill(Color.WHITE); gc.fillOval(cx-s/2+s*0.2, cy-s/2+s*0.2, s*0.25, s*0.25);
    }

    private void drawDecorativeBackground(double mx, double my, int c, int r) {
        gc.setFill(Color.web("#204020")); gc.fillRect(0, HUD_HEIGHT, canvasWidth, canvasHeight-HUD_HEIGHT);
        gc.setFill(Color.rgb(0,0,0,0.5)); gc.fillRect(mx+10, my+10, c*TILE_SIZE, r*TILE_SIZE);
    }
    
    private void drawMiniPlayerHead(double x, double y) {
        double size = 30;
        gc.setFill(Color.WHITE); gc.fillRect(x, y, size, size-4);
        gc.setStroke(Color.BLACK); gc.setLineWidth(2); gc.strokeRect(x, y, size, size-4);
        gc.setFill(Color.BLACK); gc.fillRect(x+8, y+8, 4, 8); gc.fillRect(x+18, y+8, 4, 8);
        gc.setFill(Color.MAGENTA); gc.fillRect(x+10, y-6, 10, 6);
    }
}