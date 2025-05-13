package game.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Map;

import game.BackgroundLayer;
import game.Game;
import game.GameObject;
import game.Vector2D;
import game.audio.SoundManager;
import game.entity.BoxEntity;
import game.entity.BuffEffect;
import game.entity.BuffType;
import game.entity.PlayerEntity;
import game.entity.PlatformEntity;
import game.entity.FloorEntity;
import game.physics.PhysicsObject;
import game.sprites.Sprite;
/**
 * Gameplay scene with simplified and reliable physics integration
 */
public class GameplayScene extends AbstractScene {
    private PlayerEntity player;
    private FloorEntity floor;
    private boolean isPaused = false;
    
    // Background layers for parallax effect
    private BackgroundLayer[] backgroundLayers;
    
    // Audio system
    private final SoundManager soundManager = SoundManager.getInstance();
    
    // Scale factors for game units (1 unit = 100 pixels)
    private static final double SCALE = 100.0;
    private static final double FLOOR_HEIGHT = 100;
    
    // Scene dimensions (200m x 30m)
    private static final int SCENE_WIDTH_METERS = 200;
    private static final int SCENE_HEIGHT_METERS = 10;
    private static final int SCENE_WIDTH = (int)(SCENE_WIDTH_METERS * SCALE);
    private static final int SCENE_HEIGHT = (int)(SCENE_HEIGHT_METERS * SCALE);
    
    // Background pattern dimensions
    private static final int BACKGROUND_TILE_WIDTH_METERS = 20;
    private static final int BACKGROUND_TILE_WIDTH = (int)(BACKGROUND_TILE_WIDTH_METERS * SCALE);
    
    /**
     * Creates a new gameplay scene
     */
    public GameplayScene(Game game) {
        super(game);
    }
    
    @Override
    protected void setupCamera() {
        super.setupCamera();
        
        // Set camera world bounds to match scene size
        camera.setWorldBounds(SCENE_WIDTH, SCENE_HEIGHT);
        
        // Set camera to follow player smoothly
        camera.setSmoothFactor(0.15);
        camera.setSmoothEnabled(true);
    }
    
    @Override
    protected void createGameObjects() {
        // Create background layers first
        createBackground();
        
        // Create the floor with proper positioning
        createFloor();
        
        // Create the player with proper positioning
        createPlayer();
        
        // Create some platforms
        createPlatforms();
        
        // Create some physics objects
        createPhysicsObjects();
    }
    
    /**
     * Creates the floor with proper positioning
     */
    private void createFloor() {
        // Floor position - bottom of the scene
        double floorCenterX = SCENE_WIDTH / 2.0;
        double floorCenterY = SCENE_HEIGHT - (FLOOR_HEIGHT / 2.0);
        
        floor = new FloorEntity(
            floorCenterX,
            floorCenterY,
            SCENE_WIDTH,
            (int)FLOOR_HEIGHT
        );
        
        // Ensure floor is static and immovable
        floor.setAffectedByGravity(false);
        floor.setMass(0); // Static object
        floor.setVelocity(new Vector2D(0, 0));
        floor.setFriction(0.8f);
        floor.setRestitution(0.0f);
        
        addGameObject(floor);
        game.getPhysicsSystem().addObject(floor, "GROUND");
        
        System.out.println("Floor created at: " + floorCenterX + ", " + floorCenterY);
        System.out.println("Floor dimensions: " + SCENE_WIDTH + "x" + (int)FLOOR_HEIGHT);
    }
    
    /**
     * Creates the player with proper 1.6m dimensions and positioning
     */
    private void createPlayer() {
        // Calculate floor surface position
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        
        // Player dimensions (1.6m = 160 pixels)
        int playerHeight = 160;
        int playerWidth = 80;
        
        // Position player ABOVE the floor surface with a small gap
        double playerStartX = 5 * SCALE; // 5 meters from left
        double playerStartY = floorSurfaceY - (playerHeight / 2.0) - 200; // 2 pixel gap above floor
        
        player = new PlayerEntity(
            playerStartX,
            playerStartY,
            game.getKeyboardInput()
        );
        
        // Set player physics properties
        player.setMass(1.0f);
        player.setAffectedByGravity(true);
        player.setFriction(0.9f); // High friction for good control
        player.setRestitution(0.0f); // No bounce
        
        // Add player to physics system with proper layer
        addGameObject(player);
        game.getPhysicsSystem().addObject(player, "PLAYER");
        
        // Set player as camera target
        camera.setTarget(player.getPosition());
        
        System.out.println("Player created at: " + playerStartX + ", " + playerStartY);
        System.out.println("Player dimensions: " + playerWidth + "x" + playerHeight);
        System.out.println("Floor surface at Y: " + floorSurfaceY);
    }
    
    /**
     * Creates the parallax background layers
     */
    private void createBackground() {
        backgroundLayers = new BackgroundLayer[4];
        
        // Layer 0: Sky color (completely fixed)
        backgroundLayers[0] = new BackgroundLayer(
            new Color(135, 206, 235), // Sky blue
            BACKGROUND_TILE_WIDTH,
            0.0  // Completely fixed
        );
        
        // Layer 1: Far background
        backgroundLayers[1] = new BackgroundLayer(
            "background_layer_1.png",
            BACKGROUND_TILE_WIDTH,
            1.8  // Very slow parallax
        );
        
        // Layer 2: Middle background
        backgroundLayers[2] = new BackgroundLayer(
            "background_layer_2.png",
            BACKGROUND_TILE_WIDTH,
            1.4   // Medium parallax
        );
        
        // Layer 3: Near background
        backgroundLayers[3] = new BackgroundLayer(
            "background_layer_3.png",
            BACKGROUND_TILE_WIDTH,
            1   // Faster parallax
        );
    }
    
    /**
     * Creates platforms in the level
     */
    private void createPlatforms() {
        double floorY = SCENE_HEIGHT - FLOOR_HEIGHT;
        
        // Create platforms at various positions
        createPlatform(20 * SCALE, floorY - 3 * SCALE, 3 * SCALE, 1 * SCALE);
        createPlatform(50 * SCALE, floorY - 5 * SCALE, 3 * SCALE, 1 * SCALE);
        createPlatform(80 * SCALE, floorY - 4 * SCALE, 3 * SCALE, 1 * SCALE);
        createPlatform(120 * SCALE, floorY - 8 * SCALE, 3 * SCALE, 1 * SCALE);
        createPlatform(160 * SCALE, floorY - 6 * SCALE, 3 * SCALE, 1 * SCALE);
        createPlatform(190 * SCALE, floorY - 2 * SCALE, 3 * SCALE, 1 * SCALE);
    }
    
    /**
     * Helper method to create a single platform
     */
    private void createPlatform(double x, double y, double width, double height) {
        PlatformEntity platform = new PlatformEntity(x, y, (int)width, (int)height);
        
        platform.setAffectedByGravity(false);
        platform.setMass(0); // Static object
        platform.setVelocity(new Vector2D(0, 0));
        platform.setFriction(0.7f);  // Good friction for platforms
        platform.setRestitution(0.0f);
        
        addGameObject(platform);
        game.getPhysicsSystem().addObject(platform, "PLATFORM");
    }
    
    /**
     * Creates physics objects in the level
     */
    private void createPhysicsObjects() {
        Random random = new Random();
        int numBoxes = 20;
        
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        
        for (int i = 0; i < numBoxes; i++) {
            double x = random.nextDouble() * SCENE_WIDTH;
            double y = random.nextDouble() * (floorSurfaceY - 400) + 100; // Above floor
            int size = (int)(20 + random.nextInt(30));
            
            Color color = new Color(
                random.nextInt(200) + 55,
                random.nextInt(200) + 55,
                random.nextInt(200) + 55
            );
            
            BoxEntity box = new BoxEntity(x, y, size, size, color);
            box.setFriction(0.7f);
            box.setRestitution(0.2f);  // Slightly bouncy
            box.setMass(1.0f);
            box.setAffectedByGravity(true);
            
            addGameObject(box);
            game.getPhysicsSystem().addObject(box, "ENEMY");  // Using enemy layer for test boxes
        }
    }
    
    @Override
    public void initialize() {
        if (!initialized) {
            super.initialize();
            startBackgroundMusic();
            initialized = true;
        }
    }
    
    /**
     * Starts the background music
     */
    private void startBackgroundMusic() {
        soundManager.playBackgroundMusic("background_music.wav");
        soundManager.setMusicVolume(0.7f);
        System.out.println("Started background music");
    }
    
    @Override
    public void onEnter() {
        super.onEnter();
        
        if (!initialized) {
            initialize();
        } else {
            soundManager.resumeBackgroundMusic();
            System.out.println("Resumed background music");
        }
    }
    
    @Override
    public void onExit() {
        super.onExit();
        soundManager.stopBackgroundMusic();
        System.out.println("Stopped background music");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        soundManager.pauseBackgroundMusic();
        System.out.println("Paused background music");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        soundManager.resumeBackgroundMusic();
        System.out.println("Resumed background music");
    }
    
    @Override
    public void update(long deltaTime) {
        // Check for pause toggle
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_P)) {
            isPaused = !isPaused;
            
            if (isPaused) {
                soundManager.pauseBackgroundMusic();
            } else {
                soundManager.resumeBackgroundMusic();
            }
        }
        
        // Quick volume controls
        handleVolumeControls();
        
        // Don't update if paused
        if (isPaused) {
            return;
        }
        
        // Update all game objects
        super.update(deltaTime);
        
        // Ensure camera follows player
        camera.setTarget(player.getPosition());
        
        // Floor should stay perfectly still (redundant but safe)
        if (floor != null) {
            double floorCenterY = SCENE_HEIGHT - (FLOOR_HEIGHT / 2.0);
            floor.setPosition(SCENE_WIDTH / 2, floorCenterY);
            floor.setVelocity(new Vector2D(0, 0));
        }
        
        // Handle special controls for testing movement abilities
        handleTestControls();
        
        // Check for scene change
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            // Transition to menu scene
            game.getSceneManager().changeScene("menu");
        }
        
        // Check if player has fallen off the world
        if (player.getPosition().getY() > SCENE_HEIGHT + 100) {
            resetPlayer();
        }
    }
    
    /**
     * Handles test controls for movement abilities
     */
    private void handleTestControls() {
        // Add speed buff with 1 key
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_1)) {
            player.addBuffEffect(BuffType.SPEED, 5000);
        }
        
        // Add jump height buff with 2 key
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_2)) {
            player.addBuffEffect(BuffType.JUMP_HEIGHT, 5000);
        }
        
        // Add double jump buff with 3 key
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_3)) {
            player.addBuffEffect(BuffType.DOUBLE_JUMP, 10000);
        }
        
        // Add gravity dash buff with 4 key
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_4)) {
            player.addBuffEffect(BuffType.GRAVITY_DASH, 5000);
        }
    }
    
    /**
     * Handles quick volume control inputs
     */
    private void handleVolumeControls() {
        // Master volume controls
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_SUBTRACT)) {
            float newVolume = Math.max(0.0f, soundManager.getMasterVolume() - 0.1f);
            soundManager.setMasterVolume(newVolume);
        }
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ADD)) {
            float newVolume = Math.min(1.0f, soundManager.getMasterVolume() + 0.1f);
            soundManager.setMasterVolume(newVolume);
        }
        
        // Music volume controls (with CTRL modifier)
        if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_CONTROL)) {
            if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_SUBTRACT)) {
                float newVolume = Math.max(0.0f, soundManager.getMusicVolume() - 0.1f);
                soundManager.setMusicVolume(newVolume);
            }
            if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ADD)) {
                float newVolume = Math.min(1.0f, soundManager.getMusicVolume() + 0.1f);
                soundManager.setMusicVolume(newVolume);
            }
        }
    }
    
    /**
     * Resets the player position
     */
    private void resetPlayer() {
        double floorSurfaceY = SCENE_HEIGHT - FLOOR_HEIGHT;
        double playerY = floorSurfaceY - player.getHeight() / 2.0 - 2; // 2 pixel gap
        
        player.setPosition(5 * SCALE, playerY);
        player.setVelocity(new Vector2D(0, 0));
    }
    
    @Override
    public void render(Graphics2D g) {
        // Apply camera transformations
        camera.apply(g);
        
        // Render the parallax background layers
        renderBackground(g);
        
        // Render all game objects (floor, player, platforms, etc.)
        for (GameObject gameObject : gameObjects) {
            if (!(gameObject instanceof BackgroundLayer)) {
                gameObject.render(g);
            }
        }
        
        // Reset camera transformation for UI
        camera.reset(g);
        
        // Render UI
        renderUI(g);
    }
    
    /**
     * Renders the scene background with parallax layers
     */
    private void renderBackground(Graphics2D g) {
        double cameraX = camera.getPosition().getX();
        float floorCenterY = (float)(SCENE_HEIGHT - FLOOR_HEIGHT / 2);
        
        for (int i = 0; i < backgroundLayers.length; i++) {
            if (backgroundLayers[i] != null) {
                backgroundLayers[i].renderWithCamera(g, cameraX, SCENE_HEIGHT, (int)FLOOR_HEIGHT, 
                                                    (int)floorCenterY, SCENE_WIDTH);
            }
        }
    }
    
    @Override
    protected void renderUI(Graphics2D g) {
        // Draw game UI elements
        g.setColor(Color.WHITE);
        
        // Display controls
        g.drawString("Controls:", 20, 30);
        g.drawString("Arrow Keys - Move", 20, 50);
        g.drawString("Shift - Run", 20, 70);
        g.drawString("Space - Jump", 20, 90);
        g.drawString("W - Dash", 20, 110);
        g.drawString("E - Teleport", 20, 130);
        g.drawString("Q - Close Teleport", 20, 150);
        g.drawString("R - Hook", 20, 170);
        g.drawString("P - Pause", 20, 190);
        g.drawString("ESC - Menu", 20, 210);
        
        // Test controls
        g.drawString("Test Buffs:", 20, 240);
        g.drawString("1 - Speed Buff", 20, 260);
        g.drawString("2 - Jump Height Buff", 20, 280);
        g.drawString("3 - Double Jump", 20, 300);
        g.drawString("4 - Gravity Dash", 20, 320);
        
        // Audio controls
        g.drawString("NumPad +/- - Volume", 20, 350);
        g.drawString("Ctrl + NumPad +/- - Music Volume", 20, 370);
        
        // Scene info
        g.drawString("Scene dimensions: " + SCENE_WIDTH_METERS + "m x " + SCENE_HEIGHT_METERS + "m", 20, 400);
        
        // Volume indicators
        g.drawString(String.format("Master Volume: %d%%", (int)(soundManager.getMasterVolume() * 100)), 20, 430);
        g.drawString(String.format("Music Volume: %d%%", (int)(soundManager.getMusicVolume() * 100)), 20, 450);
        g.drawString(String.format("SFX Volume: %d%%", (int)(soundManager.getSfxVolume() * 100)), 20, 470);
        
        // Player info
        if (player != null) {
            int yOffset = 500;
            g.drawString("Player Height: 1.6m (" + player.getHeight() + " pixels)", 20, yOffset);
            g.drawString("Player Position: " + formatVector(player.getPosition()), 20, yOffset + 20);
            g.drawString("Player Velocity: " + formatVector(player.getVelocity()), 20, yOffset + 40);
            g.drawString("Player State: " + player.getCurrentState(), 20, yOffset + 60);
            g.drawString("On Ground: " + (player.isOnGround() ? "Yes" : "No"), 20, yOffset + 80);
            
            // Active buffs
            Map<BuffType, BuffEffect> activeBuffs = player.getActiveBuffs();
            if (!activeBuffs.isEmpty()) {
                g.drawString("Active Buffs:", 20, yOffset + 100);
                int buffOffset = 0;
                for (Map.Entry<BuffType, BuffEffect> entry : activeBuffs.entrySet()) {
                    BuffType buffType = entry.getKey();
                    BuffEffect buff = entry.getValue();
                    g.drawString("  " + buffType + " (" + buff.timeRemaining/1000 + "s)", 20, yOffset + 120 + buffOffset * 20);
                    buffOffset++;
                }
            }
            
            if (player.getCurrentSprite() != null) {
                g.drawString("Current Sprite: " + player.getCurrentSprite().getName(), 20, yOffset + 200);
            }
        }
        
        // Draw pause screen if paused
        if (isPaused) {
            drawPauseScreen(g);
        }
        
        // Draw debug info
        if (game.getKeyboardInput().isKeyPressed(KeyEvent.VK_F3)) {
            drawPlayerDebugInfo(g);
        }
        
        g.drawString("Press F3 to show collision bounds", 20, game.getHeight() - 20);
    }
    
    /**
     * Draws debug information for the player
     */
    private void drawPlayerDebugInfo(Graphics2D g) {
        if (player != null) {
            // Draw collision box
            g.setColor(new Color(255, 0, 0, 128));
            g.drawRect(
                (int)(player.getPosition().getX() - player.getWidth() / 2),
                (int)(player.getPosition().getY() - player.getHeight() / 2),
                player.getWidth(),
                player.getHeight()
            );
            
            // Draw floor collision box
            if (floor != null) {
                g.setColor(new Color(0, 255, 0, 128));
                g.drawRect(
                    (int)(floor.getPosition().getX() - floor.getWidth() / 2),
                    (int)(floor.getPosition().getY() - floor.getHeight() / 2),
                    floor.getWidth(),
                    floor.getHeight()
                );
            }
            
            // Draw movement abilities range indicators
            g.setColor(new Color(0, 255, 255, 80));
            
            // Teleport range
            g.drawArc(
                (int)(player.getPosition().getX() - 150),
                (int)(player.getPosition().getY() - 150),
                300, 300, 0, 360
            );
            
            // Hook range
            g.setColor(new Color(255, 255, 0, 80));
            g.drawArc(
                (int)(player.getPosition().getX() - 300),
                (int)(player.getPosition().getY() - 300),
                600, 600, 0, 360
            );
            
            // Facing direction indicator
            g.setColor(Color.WHITE);
            int dirX = player.isFacingRight() ? 40 : -40;
            g.drawLine(
                (int)player.getPosition().getX(),
                (int)player.getPosition().getY(),
                (int)player.getPosition().getX() + dirX,
                (int)player.getPosition().getY()
            );
        }
    }
    
    /**
     * Formats a vector for display
     */
    private String formatVector(Vector2D vector) {
        return String.format("(%.1f, %.1f)", vector.getX(), vector.getY());
    }
    
    /**
     * Draws the pause screen overlay
     */
    private void drawPauseScreen(Graphics2D g) {
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, game.getWidth(), game.getHeight());
        
        // Pause text
        g.setColor(Color.WHITE);
        String pauseText = "PAUSED";
        
        // Center the text
        int textX = game.getWidth() / 2 - 50;
        int textY = game.getHeight() / 2;
        
        g.setFont(g.getFont().deriveFont(48f));
        g.drawString(pauseText, textX, textY);
        
        // Additional instructions
        g.setFont(g.getFont().deriveFont(24f));
        g.drawString("Press P to resume", textX - 40, textY + 50);
        g.drawString("Press ESC for menu", textX - 50, textY + 80);
        
        // Audio status
        g.setFont(g.getFont().deriveFont(16f));
        g.setColor(Color.GRAY);
        g.drawString("Music: " + (soundManager.getMusicVolume() > 0 ? "ON" : "OFF"), textX - 30, textY + 120);
        g.drawString("Volume: " + (int)(soundManager.getMasterVolume() * 100) + "%", textX - 30, textY + 140);
    }
}