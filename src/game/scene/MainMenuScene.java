// src/game/scene/MainMenuScene.java
package game.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import game.Game;
import game.audio.SoundManager;

/**
 * Main menu scene that appears when the game starts.
 */
public class MainMenuScene extends AbstractScene {
    private final SoundManager soundManager = SoundManager.getInstance();
    
    // Menu options
    private final String[] menuOptions = {
        "Start Game",
        "Settings",
        "Exit"
    };
    
    private int selectedOption = 0;
    private boolean showTitle = true;
    private double titlePulse = 0;
    
    /**
     * Creates a new main menu scene.
     * 
     * @param game The game instance.
     */
    public MainMenuScene(Game game) {
        super(game);
    }
    
    @Override
    protected void createGameObjects() {
        // Main menu doesn't need game objects
    }
    
    @Override
    public void initialize() {
        if (!initialized) {
            // Start menu music
            soundManager.playBackgroundMusic("menu_music.wav");
            soundManager.setMusicVolume(0.7f);
            
            initialized = true;
        }
    }
    
    @Override
    public void onEnter() {
        super.onEnter();
        
        // Play menu music when entering
        if (!soundManager.getDebugInfo().contains("menu_music.wav")) {
            soundManager.playBackgroundMusic("menu_music.wav");
        }
    }
    
    @Override
    public void onExit() {
        super.onExit();
        
        // Don't stop music here, let the next scene handle it
    }
    
    @Override
    public void update(long deltaTime) {
        // Update title pulse animation
        titlePulse += deltaTime * 0.003;
        
        // Handle menu navigation
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_UP)) {
            selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
            soundManager.playSoundEffect("menu_navigate.wav", 0.5f);
        }
        
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_DOWN)) {
            selectedOption = (selectedOption + 1) % menuOptions.length;
            soundManager.playSoundEffect("menu_navigate.wav", 0.5f);
        }
        
        // Handle menu selection
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ENTER) || 
            game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_SPACE)) {
            
            soundManager.playSoundEffect("menu_select.wav", 0.7f);
            
            switch (selectedOption) {
                case 0: // Start Game
                    // Transition to gameplay scene
                    game.getSceneManager().changeScene("gameplay");
                    break;
                    
                case 1: // Settings
                    // Create settings scene if it doesn't exist
                    if (!game.getSceneManager().hasScene("settings")) {
                        SettingsScene settingsScene = new SettingsScene(game);
                        game.getSceneManager().registerScene("settings", settingsScene);
                    }
                    // Push settings scene (so we return to menu when back)
                    game.getSceneManager().pushScene("settings");
                    break;
                    
                case 2: // Exit
                    soundManager.cleanup();
                    System.exit(0);
                    break;
            }
        }
        
        // Quick volume controls (optional)
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_SUBTRACT)) {
            float newVolume = Math.max(0.0f, soundManager.getMasterVolume() - 0.1f);
            soundManager.setMasterVolume(newVolume);
            soundManager.playSoundEffect("volume_adjust.wav", 0.3f);
        }
        
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ADD)) {
            float newVolume = Math.min(1.0f, soundManager.getMasterVolume() + 0.1f);
            soundManager.setMasterVolume(newVolume);
            soundManager.playSoundEffect("volume_adjust.wav", 0.3f);
        }
    }
    
    @Override
    protected void renderUI(Graphics2D g) {
        // Clear screen with dark background
        g.setColor(new Color(20, 20, 30));
        g.fillRect(0, 0, game.getWidth(), game.getHeight());
        
        // Draw title with pulse animation
        g.setColor(Color.WHITE);
        float titleScale = 1.0f + (float)(Math.sin(titlePulse) * 0.05);
        g.setFont(new Font("Arial", Font.BOLD, (int)(72 * titleScale)));
        
        String title = "Proto Knight";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        int titleX = (game.getWidth() - titleWidth) / 2;
        int titleY = 150;
        
        // Draw title with glow effect
        g.setColor(new Color(255, 255, 255, 100));
        g.drawString(title, titleX + 2, titleY + 2);
        g.setColor(Color.WHITE);
        g.drawString(title, titleX, titleY);
        
        // Draw version (optional)
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        g.drawString("v1.0", game.getWidth() - 60, game.getHeight() - 30);
        
        // Draw menu options
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        int menuStartY = 300;
        int menuSpacing = 60;
        
        for (int i = 0; i < menuOptions.length; i++) {
            // Highlight selected option
            if (i == selectedOption) {
                g.setColor(Color.YELLOW);
                // Draw selection bracket
                g.drawString(">", 100, menuStartY + i * menuSpacing);
            } else {
                g.setColor(Color.WHITE);
            }
            
            g.drawString(menuOptions[i], 140, menuStartY + i * menuSpacing);
        }
        
        // Draw controls
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.GRAY);
        g.drawString("Use UP/DOWN arrows to navigate", 50, game.getHeight() - 80);
        g.drawString("Press ENTER or SPACE to select", 50, game.getHeight() - 60);
        g.drawString("Volume: +/- keys", 50, game.getHeight() - 40);
        
        // Draw current volume
        g.drawString("Volume: " + (int)(soundManager.getMasterVolume() * 100) + "%", 
                    game.getWidth() - 200, game.getHeight() - 40);
    }
}

// -----------------------------------------------------
// Update PlatformerGame.java to start with main menu
// -----------------------------------------------------

/*
// In PlatformerGame.java, modify initializeScenes():

@Override
protected void initializeScenes() {
    // Create main menu scene
    MainMenuScene mainMenuScene = new MainMenuScene(this);
    getSceneManager().registerScene("menu", mainMenuScene);
    
    // Create the gameplay scene
    GameplayScene gameplayScene = new GameplayScene(this);
    getSceneManager().registerScene("gameplay", gameplayScene);
    
    // Create settings scene (optional - can be created on demand)
    SettingsScene settingsScene = new SettingsScene(this);
    getSceneManager().registerScene("settings", settingsScene);
    
    // Set main menu as the initial scene
    getSceneManager().setInitialScene("menu");
}
*/

// -----------------------------------------------------
// Audio files needed for main menu:
// -----------------------------------------------------

/*
Audio files to add to resources/audio/:
1. menu_music.wav - Background music for main menu
2. menu_navigate.wav - Sound when navigating menu options
3. menu_select.wav - Sound when selecting an option
4. volume_adjust.wav - Sound when adjusting volume

The menu will automatically transition between:
- Main menu music while on the menu
- Gameplay music when starting the game
- Settings music (if you add it) when entering settings
*/