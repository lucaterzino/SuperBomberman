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

    private void drawMap(GameMap map) {
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getColumns(); c++) {
                TileType type = map.getTile(c, r);
                double x = c * TILE_SIZE;
                double y = r * TILE_SIZE;

                gc.setFill(tileColors.get(TileType.EMPTY)); 
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.setFill(Color.web("#3CB371")); 
                gc.fillRect(x + 5, y + 5, 4, 4);
                gc.fillRect(x + 40, y + 15, 4, 4);
                gc.fillRect(x + 20, y + 40, 4, 4);
                gc.fillRect(x + 50, y + 50, 4, 4);

                if (type == TileType.WALL) {
                    gc.setFill(tileColors.get(type));
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    gc.setFill(Color.web("#AAAAAA"));
                    gc.fillRect(x, y, TILE_SIZE, 4);
                    gc.fillRect(x, y, 4, TILE_SIZE);
                    gc.setFill(Color.web("#333333"));
                    gc.fillRect(x, y + TILE_SIZE - 4, TILE_SIZE, 4);
                    gc.fillRect(x + TILE_SIZE - 4, y, 4, TILE_SIZE);
                    gc.setFill(Color.web("#222222"));
                    gc.fillRect(x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                    gc.setFill(Color.web("#666666"));
                    gc.fillRect(x + 14, y + 14, TILE_SIZE - 28, TILE_SIZE - 28);
                } else if (type == TileType.BRICK) {
                    gc.setFill(tileColors.get(type)); 
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(2);
                    gc.strokeLine(x, y + 15, x + TILE_SIZE, y + 15);
                    gc.strokeLine(x, y + 30, x + TILE_SIZE, y + 30);
                    gc.strokeLine(x, y + 45, x + TILE_SIZE, y + 45);
                    gc.strokeLine(x + 30, y, x + 30, y + 15);
                    gc.strokeLine(x + 15, y + 15, x + 15, y + 30);
                    gc.strokeLine(x + 45, y + 15, x + 45, y + 30);
                    gc.strokeLine(x + 30, y + 30, x + 30, y + 45);
                    gc.strokeLine(x + 15, y + 45, x + 15, y + 60);
                    gc.strokeLine(x + 45, y + 45, x + 45, y + 60);
                    gc.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.2));
                    gc.fillRect(x + TILE_SIZE - 4, y, 4, TILE_SIZE);
                    gc.fillRect(x, y + TILE_SIZE - 4, TILE_SIZE, 4);
                }
            }
        }
    }

    private void drawPlayer(Player p) {
        if (p.isImmune() && (p.getImmunityFrames() / 4) % 2 == 0) return;

        double x = p.getX();
        double y = p.getY();
        double size = Player.SIZE;
        double cx = x + size / 2;

        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillOval(x + 8, y + size - 8, size - 16, 8);

        gc.setFill(Color.BLUE);
        gc.fillRect(x + 14, y + 24, 20, 14);
        
        gc.setFill(Color.BLACK);
        gc.fillRect(x + 14, y + 34, 20, 4);
        gc.setFill(Color.GOLD);
        gc.fillRect(x + 22, y + 34, 4, 4);

        gc.setFill(Color.WHITE);
        gc.fillRect(x + 10, y + 4, 28, 26); 
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x + 10, y + 4, 28, 26);

        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 6, y + 14, 4, 8); 
        gc.fillRect(cx + 2, y + 14, 4, 8); 
        
        gc.strokeLine(cx - 8, y + 12, cx - 2, y + 16);
        gc.strokeLine(cx + 8, y + 12, cx + 2, y + 16);

        gc.setFill(Color.MAGENTA);
        gc.fillOval(x + 4, y + 24, 10, 10); 
        gc.fillOval(x + 34, y + 24, 10, 10); 

        gc.setFill(Color.MAGENTA);
        gc.fillOval(x + 10, y + 38, 12, 10); 
        gc.fillOval(x + 26, y + 38, 12, 10); 
        
        gc.setFill(Color.MAGENTA);
        gc.fillRect(cx - 4, y - 4, 8, 8);
    }

    private void drawEnemy(Enemy e) {
        double x = e.getX();
        double y = e.getY();
        double size = Enemy.SIZE;
        double cx = x + size/2;
        double cy = y + size/2;

        gc.setFill(Color.ORANGERED);
        gc.fillRect(x + 8, y + 8, size - 16, size - 16);
        gc.fillRect(x + 4, y + 12, 4, size - 24);
        gc.fillRect(x + size - 8, y + 12, 4, size - 24);
        gc.fillRect(x + 12, y + 4, size - 24, 4);
        gc.fillRect(x + 12, y + size - 8, size - 24, 4);
        
        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 10, cy - 6, 4, 8); 
        gc.fillRect(cx + 6, cy - 6, 4, 8);  
        
        gc.setFill(Color.WHITE);
        gc.fillRect(cx - 10, cy - 6, 2, 2);
        gc.fillRect(cx + 6, cy - 6, 2, 2);
        
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeLine(cx - 8, cy + 8, cx - 4, cy + 12);
        gc.strokeLine(cx - 4, cy + 12, cx, cy + 8);
        gc.strokeLine(cx, cy + 8, cx + 4, cy + 12);
        gc.strokeLine(cx + 4, cy + 12, cx + 8, cy + 8);
    }

    private void drawBomb(Bomb b) {
        double x = b.getCol() * TILE_SIZE;
        double y = b.getRow() * TILE_SIZE;
        double radius = b.getCurrentRadius();
        double offset = (TILE_SIZE - radius) / 2;

        gc.setFill(b.isRemote() ? Color.DARKRED : Color.web("#111111"));
        gc.fillOval(x + offset, y + offset, radius, radius);
        
        gc.setFill(Color.WHITE);
        gc.fillOval(x + offset + radius*0.2, y + offset + radius*0.2, radius*0.25, radius*0.25);

        gc.setFill(Color.GOLD);
        gc.fillRect(x + TILE_SIZE/2.0 - 6, y + offset - 6, 12, 8);
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        double micciaX = x + TILE_SIZE/2.0;
        double micciaY = y + offset - 6;
        
        if (b.getTimerFrames() % 20 < 10) {
            gc.strokeLine(micciaX, micciaY, micciaX, micciaY - 8);
        } else {
            gc.strokeLine(micciaX, micciaY, micciaX + 4, micciaY - 8);
        }
        
        if (!b.isRemote()) {
            gc.setFill((b.getTimerFrames() / 5) % 2 == 0 ? Color.RED : Color.YELLOW);
            gc.fillOval(micciaX - 3, micciaY - 12, 6, 6);
        }
    }
    
    private void drawExplosion(Explosion e) {
        double x = e.getCol() * TILE_SIZE;
        double y = e.getRow() * TILE_SIZE;
        double s = TILE_SIZE;
        
        double life = (double) e.getTimer() / 30.0;
        Color fireColor;
        
        if (life > 0.7) fireColor = Color.WHITE;
        else if (life > 0.4) fireColor = Color.YELLOW;
        else fireColor = Color.ORANGERED;

        gc.setFill(fireColor);
        double pulse = (e.getTimer() % 6) < 3 ? 4 : 0;
        
        gc.fillRect(x + 10 + pulse, y + 10 + pulse, s - 20 - pulse*2, s - 20 - pulse*2);
        
        gc.fillRect(x + 5, y + 20, 5, s - 40);
        gc.fillRect(x + s - 10, y + 20, 5, s - 40);
        gc.fillRect(x + 20, y + 5, s - 40, 5);
        gc.fillRect(x + 20, y + s - 10, s - 40, 5);
        
        gc.setFill(Color.rgb(255, 255, 255, 0.5));
        gc.fillRect(x + 20, y + 20, s - 40, s - 40);
    }

    private void drawPowerUp(PowerUp p) {
        double x = p.getCol() * TILE_SIZE + (TILE_SIZE - PowerUp.SIZE) / 2.0;
        double y = p.getRow() * TILE_SIZE + (TILE_SIZE - PowerUp.SIZE) / 2.0;
        double s = PowerUp.SIZE;
        double cx = x + s/2;
        double cy = y + s/2;

        gc.setFill(powerUpColors.get(p.getType()));
        gc.fillRect(x, y, s, s);
        
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, s, s);
        
        switch(p.getType()) {
            case BOMB_UP: 
                gc.setFill(Color.BLACK);
                gc.fillOval(cx - 8, cy - 6, 16, 16);
                gc.setFill(Color.WHITE); 
                gc.fillOval(cx - 4, cy - 4, 4, 4);
                break;
            case FIRE_UP: 
                gc.setFill(Color.YELLOW);
                gc.fillOval(cx - 6, cy - 4, 12, 12);
                gc.setFill(Color.RED);
                gc.fillOval(cx - 3, cy - 1, 6, 6);
                break;
            case SPEED_UP: 
                gc.setFill(Color.RED);
                gc.fillOval(cx - 10, cy, 20, 8); 
                gc.fillRect(cx - 6, cy - 8, 12, 8); 
                gc.setFill(Color.WHITE); 
                gc.fillOval(cx - 8, cy + 6, 4, 4);
                gc.fillOval(cx + 4, cy + 6, 4, 4);
                break;
            case REMOTE: 
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(cx - 6, cy - 8, 12, 16);
                gc.setFill(Color.RED); 
                gc.fillOval(cx - 2, cy - 4, 4, 4);
                break;
        }
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
    
    private void drawObjective(Objective o) {
        if (o.isCollected()) return;
        double cx = o.getCol() * TILE_SIZE + TILE_SIZE / 2.0;
        double cy = o.getRow() * TILE_SIZE + TILE_SIZE / 2.0;
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
        
        gc.setFill(Color.RED);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        String heartPath = "M 12,4 Q 4,4 4,10 Q 4,18 12,24 Q 20,18 20,10 Q 20,4 12,4 z";
        gc.beginPath();
        gc.appendSVGPath(heartPath);
        gc.fill();
        gc.stroke();
        
        gc.restore();

        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        
        double textX = heartX + (24 * heartSize) / 2; 
        double textY = centerY + 8; 
        
        gc.fillText(String.valueOf(lives), textX, textY); 
        gc.strokeText(String.valueOf(lives), textX, textY);
        gc.setTextAlign(TextAlignment.LEFT);

        startX += 90; 
        double textBaselineY = centerY + 10; 

        gc.setFill(Color.RED);
        gc.fillRect(startX, centerY - 12, 24, 24); 
        gc.setFill(Color.WHITE); 
        gc.fillRect(startX + 4, centerY - 8, 6, 6); 
        gc.fillRect(startX + 14, centerY - 8, 6, 6); 
        
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(enemiesKilled), startX + 35, textBaselineY);

        startX += 120;

        gc.setFill(Color.GOLD);
        gc.fillOval(startX, centerY - 12, 24, 24);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(startX + 5, centerY - 7, 14, 14);
        
        String scoreStr = String.format("%02d", score);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.fillText(scoreStr, startX + 35, textBaselineY);
        gc.strokeText(scoreStr, startX + 35, textBaselineY);

        double timerWidth = 160;
        double timerHeight = 40;
        double timerX = canvasWidth - 220;
        double timerY = centerY - timerHeight/2; 
        
        gc.setFill(Color.BLACK);
        gc.fillRect(timerX, timerY, timerWidth, timerHeight);
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(timerX, timerY, timerWidth, timerHeight);

        int minutes = (int) timeLeft / 60;
        int seconds = (int) timeLeft % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        
        gc.setFill(timeLeft < 30 ? Color.RED : Color.WHITE);
        gc.fillText(timeString, timerX + 40, textBaselineY);
        
        double iconX = startX + 150; 
        for (PowerUpType p : activePowerUps) {
            drawMiniPowerUp(p, iconX, centerY - 15);
            iconX += 80; 
        }
    }
    
    private void drawMiniPlayerHead(double x, double y) {
        double size = 30;
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, size, size - 4); 
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, size, size - 4);
        gc.setFill(Color.BLACK);
        gc.fillRect(x + 8, y + 8, 4, 8);
        gc.fillRect(x + 18, y + 8, 4, 8);
        gc.setFill(Color.MAGENTA);
        gc.fillRect(x + 10, y - 6, 10, 6);
        gc.strokeRect(x + 10, y - 6, 10, 6);
    }

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
        gc.fillText("Premi ENTER", canvasWidth/2, canvasHeight/2 + 60);
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
}