// src/game/scene/SettingsScene.java
package game.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import game.Game;
import game.audio.SoundManager;

/**
 * Settings scene for adjusting game options.
 */
public class SettingsScene extends AbstractScene {
    private final SoundManager soundManager = SoundManager.getInstance();
    
    // Settings options
    private final String[] settingsOptions = {
        "Master Volume",
        "Music Volume",
        "SFX Volume",
        "Back to Menu"
    };
    
    private int selectedOption = 0;
    
    /**
     * Creates a new settings scene.
     * 
     * @param game The game instance.
     */
    public SettingsScene(Game game) {
        super(game);
    }
    
    @Override
    protected void createGameObjects() {
        // Settings scene doesn't need game objects
    }
    
    @Override
    public void initialize() {
        if (!initialized) {
            // Settings scene doesn't change music, inherits from previous scene
            initialized = true;
        }
    }
    
    @Override
    public void update(long deltaTime) {
        // Handle navigation
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_UP)) {
            selectedOption = (selectedOption - 1 + settingsOptions.length) % settingsOptions.length;
            soundManager.playSoundEffect("menu_navigate.wav", 0.5f);
        }
        
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_DOWN)) {
            selectedOption = (selectedOption + 1) % settingsOptions.length;
            soundManager.playSoundEffect("menu_navigate.wav", 0.5f);
        }
        
        // Handle volume adjustments
        if (selectedOption < 3) { // Volume options
            if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_LEFT)) {
                adjustVolume(-0.1f);
            }
            if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_RIGHT)) {
                adjustVolume(0.1f);
            }
        }
        
        // Handle back to menu
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ENTER) && selectedOption == 3) {
            soundManager.playSoundEffect("menu_back.wav", 0.6f);
            game.getSceneManager().popScene();
        }
        
        // ESC also goes back
        if (game.getKeyboardInput().isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            soundManager.playSoundEffect("menu_back.wav", 0.6f);
            game.getSceneManager().popScene();
        }
    }
    
    /**
     * Adjusts the volume for the selected option.
     */
    private void adjustVolume(float change) {
        switch (selectedOption) {
            case 0: // Master Volume
                float newMaster = Math.max(0.0f, Math.min(1.0f, soundManager.getMasterVolume() + change));
                soundManager.setMasterVolume(newMaster);
                soundManager.playSoundEffect("volume_adjust.wav", 0.3f);
                break;
                
            case 1: // Music Volume
                float newMusic = Math.max(0.0f, Math.min(1.0f, soundManager.getMusicVolume() + change));
                soundManager.setMusicVolume(newMusic);
                soundManager.playSoundEffect("volume_adjust.wav", 0.3f);
                break;
                
            case 2: // SFX Volume
                float newSfx = Math.max(0.0f, Math.min(1.0f, soundManager.getSfxVolume() + change));
                soundManager.setSfxVolume(newSfx);
                soundManager.playSoundEffect("volume_adjust.wav", 0.3f);
                break;
        }
    }
    
    @Override
    protected void renderUI(Graphics2D g) {
        // Semi-transparent dark overlay
        g.setColor(new Color(0, 0, 0, 240));
        g.fillRect(0, 0, game.getWidth(), game.getHeight());
        
        // Draw title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "Settings";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (game.getWidth() - titleWidth) / 2, 100);
        
        // Draw settings options
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        int menuStartY = 200;
        int menuSpacing = 70;
        
        for (int i = 0; i < settingsOptions.length; i++) {
            // Highlight selected option
            if (i == selectedOption) {
                g.setColor(Color.YELLOW);
                // Draw selection indicator
                g.drawString(">", 100, menuStartY + i * menuSpacing);
            } else {
                g.setColor(Color.WHITE);
            }
            
            // Draw option name
            g.drawString(settingsOptions[i], 140, menuStartY + i * menuSpacing);
            
            // Draw volume values for volume options
            if (i < 3) {
                float volume = getVolumeForOption(i);
                int percentage = (int)(volume * 100);
                
                // Draw volume bar background
                g.setColor(Color.DARK_GRAY);
                g.fillRect(400, menuStartY + i * menuSpacing - 20, 200, 20);
                
                // Draw volume bar fill
                g.setColor(i == selectedOption ? Color.YELLOW : Color.GREEN);
                g.fillRect(400, menuStartY + i * menuSpacing - 20, (int)(200 * volume), 20);
                
                // Draw percentage
                g.setColor(Color.WHITE);
                g.drawString(percentage + "%", 620, menuStartY + i * menuSpacing);
            }
        }
        
        // Draw instructions
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.GRAY);
        g.drawString("Use UP/DOWN arrows to navigate", 50, game.getHeight() - 100);
        g.drawString("Use LEFT/RIGHT arrows to adjust volume", 50, game.getHeight() - 70);
        g.drawString("Press ENTER or ESC to go back", 50, game.getHeight() - 40);
    }
    
    /**
     * Gets the volume value for a specific option.
     */
    private float getVolumeForOption(int option) {
        switch (option) {
            case 0: return soundManager.getMasterVolume();
            case 1: return soundManager.getMusicVolume();
            case 2: return soundManager.getSfxVolume();
            default: return 0;
        }
    }
}

// -----------------------------------------------------
// Update PlatformerGame.java to use the new scenes
// -----------------------------------------------------

/*
// Complete PlatformerGame.java with both scenes:

package game;

import game.scene.GameplayScene;
import game.scene.MainMenuScene;
import game.scene.SettingsScene;

public class PlatformerGame extends Game {
    
    public PlatformerGame() {
        super();
    }
    
    @Override
    protected void initializeScenes() {
        // Create main menu scene
        MainMenuScene mainMenuScene = new MainMenuScene(this);
        getSceneManager().registerScene("menu", mainMenuScene);
        
        // Create the gameplay scene
        GameplayScene gameplayScene = new GameplayScene(this);
        getSceneManager().registerScene("gameplay", gameplayScene);
        
        // Create settings scene
        SettingsScene settingsScene = new SettingsScene(this);
        getSceneManager().registerScene("settings", settingsScene);
        
        // Set main menu as the initial scene
        getSceneManager().setInitialScene("menu");
    }
}
*/