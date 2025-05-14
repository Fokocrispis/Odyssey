// src/game/scene/MainMenuScene.java
package game.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import game.Game;
import game.audio.SoundManager;
import game.resource.ResourceManager;

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
    
    // Background wallpaper
    private BufferedImage wallpaper;
    
    // Custom font
    private Font bitPotionFont;
    private boolean customFontLoaded = false;
    
    /**
     * Creates a new main menu scene.
     * 
     * @param game The game instance.
     */
    public MainMenuScene(Game game) {
        super(game);
        loadWallpaper();
        loadCustomFont();
    }
    
    /**
     * Loads the wallpaper background image
     */
    private void loadWallpaper() {
        try {
            // Load wallpaper using ResourceManager
            wallpaper = ResourceManager.getInstance().loadImage("wallpaper.png");
            if (wallpaper != null) {
                System.out.println("Main menu wallpaper loaded successfully");
            } else {
                System.err.println("Failed to load wallpaper.png - will use fallback color");
            }
        } catch (IOException e) {
            System.err.println("Error loading wallpaper: " + e.getMessage());
        }
    }
    
    /**
     * Loads the custom BitPotion font
     */
    private void loadCustomFont() {
        // Try different possible paths to find the font file
        String[] possiblePaths = {
            "resources/BitPotion.ttf",
            "src/resources/BitPotion.ttf",
            "BitPotion.ttf",
            "./BitPotion.ttf",
            "../resources/BitPotion.ttf"
        };
        
        for (String path : possiblePaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    // Load font from file
                    InputStream is = new FileInputStream(fontFile);
                    bitPotionFont = Font.createFont(Font.TRUETYPE_FONT, is);
                    customFontLoaded = true;
                    System.out.println("BitPotion font loaded successfully from: " + path);
                    break;
                }
            } catch (FontFormatException | IOException e) {
                System.err.println("Error loading font from " + path + ": " + e.getMessage());
            }
        }
        
        if (!customFontLoaded) {
            System.err.println("Failed to load BitPotion font - will use fallback font");
            bitPotionFont = new Font("Arial", Font.BOLD, 72);
        }
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
        // Draw the wallpaper background
        drawBackground(g);
        
        // Draw semi-transparent overlay for better text visibility
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(0, 0, game.getWidth(), game.getHeight());
        
        // Draw title with pulse animation
        drawTitle(g);
        
        // Draw version (optional)
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.GRAY);
        g.drawString("v1.0", game.getWidth() - 60, game.getHeight() - 30);
        
        // Draw menu options
        drawMenuOptions(g);
        
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
    
    /**
     * Draws the title with the custom BitPotion font
     */
    private void drawTitle(Graphics2D g) {
        // Apply pulsing effect to font size
        float titleScale = 1.0f + (float)(Math.sin(titlePulse) * 0.05);
        float fontSize = customFontLoaded ? 136.0f : 72.0f; // BitPotion is pixel font so we need larger size
        
        // Create derived font with the right size
        Font titleFont = bitPotionFont.deriveFont(fontSize * titleScale);
        g.setFont(titleFont);
        
        String title = "Odyssey";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        int titleX = (game.getWidth() - titleWidth) / 2;
        int titleY = 180; // Slightly lower position for the new title
        
        // Draw title with glow effect
        g.setColor(new Color(255, 255, 255, 100));
        g.drawString(title, titleX + 3, titleY + 3);
        
        // Draw main title with slight gradient effect
        g.setColor(new Color(255, 235, 180)); // Slight golden tint
        g.drawString(title, titleX, titleY);
        
        // Draw subtitle with smaller font if needed
        if (customFontLoaded) {
            Font subtitleFont = bitPotionFont.deriveFont(28.0f);
            g.setFont(subtitleFont);
            String subtitle = "Adventure Awaits";
            int subtitleWidth = g.getFontMetrics().stringWidth(subtitle);
            g.setColor(new Color(180, 180, 255)); // Light blue tint
            g.drawString(subtitle, (game.getWidth() - subtitleWidth) / 2, titleY + 50);
        }
    }
    
    /**
     * Draws the menu options
     */
    private void drawMenuOptions(Graphics2D g) {
        // Use custom font for menu options if available
        Font menuFont;
        int menuStartY = 350; // Adjust based on the new title position
        int menuSpacing = 60;
        
        if (customFontLoaded) {
            menuFont = bitPotionFont.deriveFont(48.0f);
        } else {
            menuFont = new Font("Arial", Font.PLAIN, 32);
        }
        
        g.setFont(menuFont);
        
        for (int i = 0; i < menuOptions.length; i++) {
            // Highlight selected option
            if (i == selectedOption) {
                g.setColor(Color.YELLOW);
                // Draw selection indicator
                g.drawString(">", 100, menuStartY + i * menuSpacing);
            } else {
                g.setColor(Color.WHITE);
            }
            
            g.drawString(menuOptions[i], 140, menuStartY + i * menuSpacing);
        }
    }
    
    /**
     * Draws the background wallpaper
     */
    private void drawBackground(Graphics2D g) {
        if (wallpaper != null) {
            // Scale the wallpaper to fit the screen
            g.drawImage(wallpaper, 0, 0, game.getWidth(), game.getHeight(), null);
        } else {
            // Fallback to gradient background if wallpaper couldn't be loaded
            Color topColor = new Color(20, 20, 60); // Dark blue
            Color bottomColor = new Color(40, 40, 100); // Lighter blue
            
            // Create a gradient background
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                0, 0, topColor,
                0, game.getHeight(), bottomColor
            );
            
            g.setPaint(gradient);
            g.fillRect(0, 0, game.getWidth(), game.getHeight());
        }
    }
}