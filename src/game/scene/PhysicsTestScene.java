package game.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import game.Game;
import game.GameObject;
import game.Vector2D;
import game.entity.BoxEntity;
import game.entity.FloorEntity;
import game.entity.PlayerEntity;
import game.physics.AABB;
import game.physics.PhysicsObject;

/**
 * Complete test scene with interactive sprite offset adjustment tools
 */
public class PhysicsTestScene extends AbstractScene {
    private PlayerEntity player;
    private FloorEntity floor;
    private boolean showDebug = true;
    private boolean showSpriteAlignment = true;
    private int testBoxCount = 0;
    
    // Sprite offset adjustment
    private int spriteOffsetX = 0;
    private int spriteOffsetY = 0;
    private boolean adjustingOffset = false;
    
    // Test dimensions
    private static final double SCALE = 100.0;
    private static final double FLOOR_HEIGHT = 100;
    private static final int SCENE_WIDTH = 2000; // 20m
    private static final int SCENE_HEIGHT = 1000; // 10m
    
    public PhysicsTestScene(Game game) {
        super(game);
    }
    
    @Override
    protected void setupCamera() {
        super.setupCamera();
        camera.setWorldBounds(SCENE_WIDTH, SCENE_HEIGHT);
        camera.setSmoothFactor(0.1);
        camera.setSmoothEnabled(true);
    }
    
    @Override
    protected void createGameObjects() {
        // Create simple floor
        createFloor();
        
        // Create player
        createPlayer();
        
        // Create test platforms
        createTestPlatforms();
        
        // Create initial test objects
        createInitialTestObjects();
    }
    
    private void createFloor() {
        double floorCenterX = SCENE_WIDTH / 2.0;
        double floorCenterY = SCENE_HEIGHT - (FLOOR_HEIGHT / 2.0);
        
        floor = new FloorEntity(floorCenterX, floorCenterY, SCENE_WIDTH, (int)FLOOR_HEIGHT);
        floor.setAffectedByGravity(false);
        floor.setMass(0);
        floor.setFriction(0.9f);
        floor.setRestitution(0.0f);
        
        addGameObject(floor);
        game.getPhysicsSystem().addObject(floor, "GROUND");
        
        System.out.println("Test floor created at: " + floorCenterX + ", " + floorCenterY);
        System.out.println("Floor surface at Y: " + (SCENE_HEIGHT - FLOOR_HEIGHT));
    }
    
    private void createPlayer() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        double playerX = 5 * SCALE;
        double playerY = floorSurfaceY - 70 - 2; // Adjust for smaller hitbox
        
        player = new PlayerEntity(playerX, playerY, game.getKeyboardInput());
        player.setMass(1.0f);
        player.setAffectedByGravity(true);
        player.setFriction(0.9f);
        player.setRestitution(0.0f);
        
        addGameObject(player);
        game.getPhysicsSystem().addObject(player, "PLAYER");
        camera.setTarget(player.getPosition());
        
        System.out.println("Test player created at: " + playerX + ", " + playerY);
        System.out.println("Player hitbox: " + player.getWidth() + "x" + player.getHeight());
    }
    
    private void createTestPlatforms() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        
        // Create a simple platform
        createPlatform(8 * SCALE, floorSurfaceY - 200, 300, 20, Color.GREEN);
        
        // Create a higher platform
        createPlatform(12 * SCALE, floorSurfaceY - 400, 200, 20, Color.CYAN);
        
        // Create a narrow platform to test precision
        createPlatform(6 * SCALE, floorSurfaceY - 100, 60, 20, Color.ORANGE);
        
        // Create a very narrow platform for edge cases
        createPlatform(2 * SCALE, floorSurfaceY - 50, 30, 20, Color.YELLOW);
    }
    
    private void createPlatform(double x, double y, int width, int height, Color color) {
        BoxEntity platform = new BoxEntity(x, y, width, height, color);
        platform.setMass(0);
        platform.setAffectedByGravity(false);
        platform.setFriction(0.9f);
        platform.setRestitution(0.0f);
        
        addGameObject(platform);
        game.getPhysicsSystem().addObject(platform, "PLATFORM");
    }
    
    private void createInitialTestObjects() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        
        // Create some test boxes
        for (int i = 0; i < 3; i++) {
            double x = (i + 2) * 200;
            double y = floorSurfaceY - 50; // Start just above the ground
            
            addTestBox(x, y, 40, 40, Color.BLUE);
            testBoxCount++;
        }
    }
    
    private void addTestBox(double x, double y, int width, int height, Color color) {
        BoxEntity box = new BoxEntity(x, y, width, height, color);
        box.setMass(1.0f);
        box.setAffectedByGravity(true);
        box.setFriction(0.7f);
        box.setRestitution(0.3f);
        
        addGameObject(box);
        game.getPhysicsSystem().addObject(box, "ENEMY");
    }
    
    @Override
    public void update(long deltaTime) {
        // Toggle debug view
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_F3)) {
            showDebug = !showDebug;
        }
        
        // Toggle sprite alignment view
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_F4)) {
            showSpriteAlignment = !showSpriteAlignment;
        }
        
        // Toggle offset adjustment mode
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_F5)) {
            adjustingOffset = !adjustingOffset;
            if (adjustingOffset) {
                System.out.println("Sprite Offset Adjustment Mode: ON");
                System.out.println("Use NumPad 2,4,6,8 to adjust X/Y offsets");
                System.out.println("Current Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY);
            } else {
                System.out.println("Sprite Offset Adjustment Mode: OFF");
                System.out.println("Final Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY);
            }
        }
        
        // Sprite offset adjustment controls
        if (adjustingOffset) {
            // Adjust X offset
            if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_NUMPAD4)) {
                spriteOffsetX -= 1;
                System.out.println("Sprite Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY);
            }
            if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_NUMPAD6)) {
                spriteOffsetX += 1;
                System.out.println("Sprite Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY);
            }
            
            // Adjust Y offset
            if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_NUMPAD8)) {
                spriteOffsetY -= 1;
                System.out.println("Sprite Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY);
            }
            if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_NUMPAD2)) {
                spriteOffsetY += 1;
                System.out.println("Sprite Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY);
            }
            
            // Fine adjustment with Shift
            if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_SHIFT)) {
                if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_LEFT)) {
                    spriteOffsetX -= 5;
                }
                if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_RIGHT)) {
                    spriteOffsetX += 5;
                }
                if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_UP)) {
                    spriteOffsetY -= 5;
                }
                if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_DOWN)) {
                    spriteOffsetY += 5;
                }
            }
            
            // Reset offset
            if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_NUMPAD5)) {
                spriteOffsetX = 0;
                spriteOffsetY = 0;
                System.out.println("Sprite Offset Reset: X=0, Y=0");
            }
        }
        
        // Back to menu
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            game.getSceneManager().changeScene("menu");
        }
        
        // Reset scene
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_R)) {
            resetScene();
        }
        
        // Add new box
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_B)) {
            addRandomBox();
        }
        
        // Add heavy box
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_H)) {
            addHeavyBox();
        }
        
        // Add bouncy box
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_J)) {
            addBouncyBox();
        }
        
        // Test jump for animations
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_T)) {
            testJumpAnimation();
        }
        
        super.update(deltaTime);
    }
    
    private void testJumpAnimation() {
        // Give player upward velocity to test jump/fall animations
        player.setVelocity(new Vector2D(player.getVelocity().getX(), -600));
        System.out.println("Testing jump animation!");
    }
    
    private void resetScene() {
        // Clear all physics objects except floor and player
        for (GameObject obj : new ArrayList<>(gameObjects)) {
            if (obj instanceof BoxEntity && obj != floor) {
                gameObjects.remove(obj);
                game.getPhysicsSystem().removeObject((PhysicsObject)obj);
            }
        }
        
        // Reset player position
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        player.setPosition(5 * SCALE, floorSurfaceY - 70 - 2);
        player.setVelocity(new Vector2D(0, 0));
        
        // Recreate initial objects
        createInitialTestObjects();
        testBoxCount = 3;
    }
    
    private void addRandomBox() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        double x = player.getPosition().getX() + 100;
        double y = floorSurfaceY - 100;
        
        int size = 20 + (int)(Math.random() * 40);
        Color randomColor = new Color(
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255)
        );
        
        addTestBox(x, y, size, size, randomColor);
        testBoxCount++;
    }
    
    private void addHeavyBox() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        double x = player.getPosition().getX() + 100;
        double y = floorSurfaceY - 100;
        
        BoxEntity box = new BoxEntity(x, y, 60, 60, Color.DARK_GRAY);
        box.setMass(5.0f); // Heavy!
        box.setAffectedByGravity(true);
        box.setFriction(0.9f);
        box.setRestitution(0.1f);
        
        addGameObject(box);
        game.getPhysicsSystem().addObject(box, "ENEMY");
        testBoxCount++;
    }
    
    private void addBouncyBox() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        double x = player.getPosition().getX() + 100;
        double y = floorSurfaceY - 200; // Start higher for bouncing effect
        
        BoxEntity box = new BoxEntity(x, y, 40, 40, Color.PINK);
        box.setMass(0.5f); // Light
        box.setAffectedByGravity(true);
        box.setFriction(0.4f);
        box.setRestitution(0.8f); // Very bouncy!
        
        addGameObject(box);
        game.getPhysicsSystem().addObject(box, "ENEMY");
        testBoxCount++;
    }
    
    @Override
    public void render(Graphics2D g) {
        // Apply camera transformations
        camera.apply(g);
        
        // If adjusting offset, override player rendering
        if (adjustingOffset && player != null) {
            renderPlayerWithOffset(g);
            
            // Render other objects normally
            for (GameObject gameObject : gameObjects) {
                if (gameObject != player) {
                    gameObject.render(g);
                }
            }
        } else {
            // Render all game objects normally
            for (GameObject gameObject : gameObjects) {
                gameObject.render(g);
            }
        }
        
        // Reset camera transformation for UI
        camera.reset(g);
        
        // Render UI
        renderUI(g);
    }
    
    private void renderPlayerWithOffset(Graphics2D g) {
        if (!player.isVisible() || player.getCurrentSprite() == null) return;
        
        // Calculate sprite rendering position with custom offset
        Vector2D pos = player.getPosition();
        int hitboxWidth = player.getWidth();
        int hitboxHeight = player.getHeight();
        
        // Sprite width/height includes scaling
        int renderedWidth = player.getCurrentSprite().getSize().width;
        int renderedHeight = player.getCurrentSprite().getSize().height;
        
        // Calculate sprite position with adjustable offset
        int spriteX = (int)(pos.getX() - renderedWidth / 2.0) + spriteOffsetX;
        int spriteY = (int)(pos.getY() - hitboxHeight / 2.0 - (renderedHeight - hitboxHeight)) + spriteOffsetY;
        
        // Draw sprite (flipped if facing left)
        if (player.isFacingRight()) {
            g.drawImage(player.getCurrentSprite().getFrame(), 
                       spriteX, spriteY, 
                       renderedWidth, renderedHeight, null);
        } else {
            g.drawImage(player.getCurrentSprite().getFrame(), 
                       spriteX + renderedWidth, spriteY,
                       -renderedWidth, renderedHeight, null);
        }
        
        // Draw health and mana bars with offset
        drawHealthManaBarWithOffset(g);
    }
    
    private void drawHealthManaBarWithOffset(Graphics2D g) {
        Vector2D pos = player.getPosition();
        int hitboxHeight = player.getHeight();
        
        // Health bar with offset
        int barWidth = 60;
        int barHeight = 8;
        int barX = (int)pos.getX() - barWidth / 2;
        int barY = (int)pos.getY() - hitboxHeight / 2 - 15 + spriteOffsetY;
        
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
        
        // Background bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Health bar
        g.setColor(Color.RED);
        int healthWidth = (int)((double)player.getHealth() / 100 * barWidth);
        g.fillRect(barX, barY, healthWidth, barHeight);
        
        // Mana bar
        barY += 10;
        barHeight = 6;
        
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
        
        // Background bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Mana bar
        g.setColor(Color.BLUE);
        int manaWidth = (int)((double)player.getMana() / 50 * barWidth);
        g.fillRect(barX, barY, manaWidth, barHeight);
    }
    
    @Override
    protected void renderUI(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("Physics Test Scene (Sprite Alignment)", 20, 30);
        g.drawString("Arrow Keys - Move", 20, 50);
        g.drawString("Space - Jump", 20, 70);
        g.drawString("Shift - Run", 20, 90);
        g.drawString("W - Dash", 20, 110);
        
        g.drawString("B - Add Random Box", 20, 140);
        g.drawString("H - Add Heavy Box", 20, 160);
        g.drawString("J - Add Bouncy Box", 20, 180);
        g.drawString("T - Test Jump", 20, 200);
        g.drawString("R - Reset Scene", 20, 220);
        
        g.drawString("F3 - Toggle Debug", 20, 250);
        g.drawString("F4 - Toggle Sprite Alignment", 20, 270);
        g.drawString("F5 - Toggle Offset Adjustment", 20, 290);
        g.drawString("ESC - Back to Menu", 20, 310);
        
        // Offset adjustment controls
        if (adjustingOffset) {
            g.setColor(Color.CYAN);
            g.drawString("=== OFFSET ADJUSTMENT MODE ===", 20, 340);
            g.drawString("NumPad 2,4,6,8 - Fine adjust", 20, 360);
            g.drawString("Shift + Arrows - Coarse adjust", 20, 380);
            g.drawString("NumPad 5 - Reset offset", 20, 400);
            g.drawString("Current Offset: X=" + spriteOffsetX + ", Y=" + spriteOffsetY, 20, 420);
            g.setColor(Color.WHITE);
        }
        
        // Show status
        g.drawString("Objects: " + game.getPhysicsSystem().getPhysicsObjects().size(), 20, 450);
        g.drawString("Test Boxes: " + testBoxCount, 20, 470);
        g.drawString("Player On Ground: " + (player.isOnGround() ? "Yes" : "No"), 20, 490);
        g.drawString("Player Position: " + formatPosition(player.getPosition()), 20, 510);
        g.drawString("Player Velocity: " + formatVelocity(player.getVelocity()), 20, 530);
        g.drawString("Player State: " + player.getCurrentState(), 20, 550);
        
        // Physics system info
        g.drawString("Physics: Simplified & Robust", 20, 580);
        
        // Show additional info when debug is on
        if (showDebug) {
            drawDebugInfo(g);
        }
        
        // Show sprite alignment info
        if (showSpriteAlignment) {
            drawSpriteAlignmentInfo(g);
        }
    }
    
    private void drawDebugInfo(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.drawString("DEBUG MODE - Collision Boxes Visible", 20, 610);
        
        // Draw collision boxes for all physics objects
        for (PhysicsObject obj : game.getPhysicsSystem().getPhysicsObjects()) {
            if (obj.getCollisionShape() != null) {
                AABB bounds = obj.getCollisionShape().getBoundingBox();
                
                // Choose color based on object type
                if (obj == player) {
                    g.setColor(new Color(255, 255, 0, 128)); // Yellow for player
                } else if (obj == floor) {
                    g.setColor(new Color(0, 255, 0, 128)); // Green for floor
                } else if (obj.getMass() <= 0) {
                    g.setColor(new Color(0, 255, 255, 128)); // Cyan for static objects
                } else {
                    g.setColor(new Color(255, 0, 0, 128)); // Red for dynamic objects
                }
                
                g.drawRect(
                    (int)bounds.getLeft(),
                    (int)bounds.getTop(),
                    (int)bounds.getWidth(),
                    (int)bounds.getHeight()
                );
                
                // Draw velocity vector for dynamic objects
                if (obj.getMass() > 0) {
                    Vector2D pos = obj.getPosition();
                    Vector2D vel = obj.getVelocity();
                    g.setColor(Color.WHITE);
                    g.drawLine(
                        (int)pos.getX(), (int)pos.getY(),
                        (int)(pos.getX() + vel.getX() * 0.1), (int)(pos.getY() + vel.getY() * 0.1)
                    );
                }
            }
        }
        
        // Draw floor surface line
        g.setColor(Color.CYAN);
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        g.drawLine(0, (int)floorSurfaceY, SCENE_WIDTH, (int)floorSurfaceY);
    }
    
    private void drawSpriteAlignmentInfo(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.drawString("SPRITE ALIGNMENT MODE", 20, 640);
        
        // Draw player hitbox outline
        if (player != null) {
            Vector2D pos = player.getPosition();
            int hitboxWidth = player.getWidth();
            int hitboxHeight = player.getHeight();
            
            // Draw hitbox
            g.setColor(new Color(255, 0, 255, 128));
            g.fillRect(
                (int)(pos.getX() - hitboxWidth / 2),
                (int)(pos.getY() - hitboxHeight / 2),
                hitboxWidth,
                hitboxHeight
            );
            
            // Draw hitbox outline
            g.setColor(Color.MAGENTA);
            g.drawRect(
                (int)(pos.getX() - hitboxWidth / 2),
                (int)(pos.getY() - hitboxHeight / 2),
                hitboxWidth,
                hitboxHeight
            );
            
            // Draw center point
            g.fillOval((int)pos.getX() - 2, (int)pos.getY() - 2, 4, 4);
            
            // Draw alignment guides
            g.setColor(Color.CYAN);
            // Vertical center line
            g.drawLine((int)pos.getX(), (int)(pos.getY() - hitboxHeight/2 - 50), 
                      (int)pos.getX(), (int)(pos.getY() + hitboxHeight/2 + 50));
            // Horizontal center line
            g.drawLine((int)(pos.getX() - hitboxWidth/2 - 50), (int)pos.getY(), 
                      (int)(pos.getX() + hitboxWidth/2 + 50), (int)pos.getY());
            
            // Draw feet line
            g.setColor(Color.GREEN);
            int feetY = (int)(pos.getY() + hitboxHeight/2);
            g.drawLine((int)(pos.getX() - 30), feetY, (int)(pos.getX() + 30), feetY);
            
            // Draw sprite bounds estimate with offset
            if (player.getCurrentSprite() != null) {
                int spriteWidth = player.getCurrentSprite().getSize().width;
                int spriteHeight = player.getCurrentSprite().getSize().height;
                
                int spriteX = (int)(pos.getX() - spriteWidth / 2) + spriteOffsetX;
                int spriteY = (int)(pos.getY() - hitboxHeight / 2 - (spriteHeight - hitboxHeight)) + spriteOffsetY;
                
                g.setColor(new Color(0, 255, 255, 64));
                g.fillRect(spriteX, spriteY, spriteWidth, spriteHeight);
                
                g.setColor(Color.CYAN);
                g.drawRect(spriteX, spriteY, spriteWidth, spriteHeight);
                
                // Draw offset indicator
                if (adjustingOffset) {
                    g.setColor(Color.RED);
                    g.drawLine((int)pos.getX(), (int)pos.getY(), 
                              (int)pos.getX() + spriteOffsetX, (int)pos.getY() + spriteOffsetY);
                    
                    // Show offset values near the player
                    g.drawString("Offset: " + spriteOffsetX + "," + spriteOffsetY, 
                                (int)pos.getX() + 40, (int)pos.getY() - 40);
                }
            }
        }
    }
    
    private String formatPosition(Vector2D pos) {
        return String.format("(%.0f, %.0f)", pos.getX(), pos.getY());
    }
    
    private String formatVelocity(Vector2D vel) {
        return String.format("(%.0f, %.0f)", vel.getX(), vel.getY());
    }
}