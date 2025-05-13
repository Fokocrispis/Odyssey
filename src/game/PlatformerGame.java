package game;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import game.debug.SpriteAdjustmentTool;
import game.scene.GameplayScene;
import game.scene.MainMenuScene;
import game.scene.PhysicsTestScene;
import game.scene.SettingsScene;
import game.sprites.SpriteSheetManager;
import game.entity.PlayerEntity;

/**
 * Custom game implementation for our platformer with enhanced physics
 * and sprite sequence loading
 */
public class PlatformerGame extends Game {
    
    private SpriteAdjustmentTool adjustmentTool;
    private SpriteSheetManager spriteManager;
    private boolean debugRenderEnabled = false;
    
    /**
     * Creates a new platformer game
     */
    public PlatformerGame() {
        super();
        
        // Initialize the sprite manager early
        spriteManager = new SpriteSheetManager();
        
        // Initialize the sprite adjustment tool
        initializeSpriteAdjustmentTool();
    }
    
    /**
     * Initializes the sprite adjustment tool
     */
    private void initializeSpriteAdjustmentTool() {
        // Get the tool instance
        adjustmentTool = SpriteAdjustmentTool.getInstance();
        
        // Register the walking animation for adjustment
        adjustmentTool.addSpriteAdjustment(
            "walk",                          // Animation name 
            "Walk/Walking",                  // Path to frames
            "Walking",                       // Filename prefix
            18,                              // Number of frames
            new Dimension(64, 64),           // Source frame size
            new Dimension(190, 160),         // Initial display size
            0,                               // Initial X offset
            0,                               // Initial Y offset
            850                              // Animation duration (ms)
        );
        
        System.out.println("Sprite adjustment tool initialized. Press F10 to toggle.");
    }
    
    @Override
    protected void initializeScenes() {
        // Load sprites before creating scenes
        spriteManager.createPlayerSprites();
        
        // Create main menu scene
        MainMenuScene mainMenuScene = new MainMenuScene(this);
        getSceneManager().registerScene("menu", mainMenuScene);
        
        // Create the main gameplay scene
        GameplayScene gameplayScene = new GameplayScene(this);
        getSceneManager().registerScene("gameplay", gameplayScene);
        
        // Create settings scene
        SettingsScene settingsScene = new SettingsScene(this);
        getSceneManager().registerScene("settings", settingsScene);
        
        // Create physics test scene for debugging
        PhysicsTestScene testScene = new PhysicsTestScene(this);
        getSceneManager().registerScene("test", testScene);
        
        // Start with gameplay scene to test sprites
        getSceneManager().setInitialScene("gameplay");
        
        System.out.println("Initialized scenes - Starting with gameplay scene");
        System.out.println("Press F10 to toggle sprite adjustment tool");
    }
    
    /**
     * Overrides the custom update hook to handle sprite adjustment and other features
     */
    @Override
    protected void processCustomUpdates(long deltaTime) {
        // Handle sprite adjustment tool toggling with F10
        if (getKeyboardInput().isKeyJustPressed(KeyEvent.VK_F10)) {
            adjustmentTool.toggle();
            debugRenderEnabled = !debugRenderEnabled;
            
            // Also toggle debug rendering in player entity if present
            togglePlayerDebugRendering();
            
            System.out.println("Debug rendering " + (debugRenderEnabled ? "enabled" : "disabled"));
        }
        
        // Process adjustment input when tool is enabled
        if (adjustmentTool.isEnabled()) {
            adjustmentTool.processInput(getKeyboardInput());
            
            // Generate and print code when F11 is pressed
            if (getKeyboardInput().isKeyJustPressed(KeyEvent.VK_F11)) {
                System.out.println("\n=== SPRITE ADJUSTMENT CODE ===");
                System.out.println(adjustmentTool.getAdjustmentCode());
                System.out.println("==============================\n");
            }
        }
        
        // Check for key F12 to switch scenes for testing
        if (getKeyboardInput().isKeyJustPressed(KeyEvent.VK_F12)) {
            cycleScenes();
        }
    }
    
    /**
     * Override custom rendering hook for debugging visuals
     */
    @Override
    protected void processCustomRendering(Graphics2D g) {
        // Add any custom debug rendering here
        if (debugRenderEnabled && adjustmentTool.isEnabled()) {
            // You could add debug overlays here
            g.setColor(java.awt.Color.GREEN);
            g.drawString("Sprite Adjustment Active - Use Arrow Keys & Numpad", 10, getHeight() - 40);
            g.drawString("F11: Generate Code | F12: Cycle Scenes", 10, getHeight() - 20);
        }
    }
    
    /**
     * Toggle debug rendering for the player entity
     */
    private void togglePlayerDebugRendering() {
        // Find the player entity in the current scene
        GameplayScene gameplayScene = null;
        
        if (getSceneManager().getCurrentScene() instanceof GameplayScene) {
            gameplayScene = (GameplayScene) getSceneManager().getCurrentScene();
        } else if (getSceneManager().hasScene("gameplay")) {
            gameplayScene = (GameplayScene) getSceneManager().getScene("gameplay");
        }
        
        if (gameplayScene != null) {
            // Find a player entity in the game objects
            for (GameObject obj : gameplayScene.getGameObjects()) {
                if (obj instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) obj;
                    player.toggleDebugRender();
                    break;
                }
            }
        }
    }
    
    /**
     * Cycles between scenes for quick testing
     */
    private void cycleScenes() {
        String currentSceneName = "";
        if (getSceneManager().getCurrentScene() instanceof MainMenuScene) {
            currentSceneName = "menu";
        } else if (getSceneManager().getCurrentScene() instanceof GameplayScene) {
            currentSceneName = "gameplay";
        } else if (getSceneManager().getCurrentScene() instanceof PhysicsTestScene) {
            currentSceneName = "test";
        } else if (getSceneManager().getCurrentScene() instanceof SettingsScene) {
            currentSceneName = "settings";
        }
        
        // Cycle to next scene
        String nextScene = "menu";
        switch (currentSceneName) {
            case "menu":
                nextScene = "gameplay";
                break;
            case "gameplay":
                nextScene = "test";
                break;
            case "test":
                nextScene = "settings";
                break;
            case "settings":
                nextScene = "menu";
                break;
        }
        
        System.out.println("Switching from " + currentSceneName + " to " + nextScene);
        getSceneManager().changeScene(nextScene);
    }
    
    /**
     * Gets the sprite manager
     */
    public SpriteSheetManager getSpriteManager() {
        return spriteManager;
    }
    
    /**
     * Gets whether debug rendering is enabled
     */
    public boolean isDebugRenderEnabled() {
        return debugRenderEnabled;
    }
}