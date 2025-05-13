package game.sprites;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import game.resource.ResourceManager;

/**
 * Manager for sprite sequences loaded from individual image files.
 * Updated to work with the new sprite system.
 */
public class SpriteSheetManager {
    // Cache for loaded sprites and frames
    private final Map<String, Sprite> sprites = new HashMap<>();
    private final Map<String, BufferedImage> spriteSheets = new HashMap<>();
    private final Map<String, SpriteAdjustment> adjustments = new HashMap<>();
    private final SpriteSequenceManager sequenceManager;
    
    private static final String SPRITE_PATH = "JoannaD'ArcIII_v1.9.2/Sprites/";
    
    public SpriteSheetManager() {
        this.sequenceManager = new SpriteSequenceManager();
    }
    
    /**
     * Creates player sprites from animation sequences
     */
    public void createPlayerSprites() {
        // Define standard frame size and scale
        Dimension frameSize = new Dimension(64, 64);
        double scale = 3.0;
        
        // Configure adjustments for animations that need them
        registerAdjustment("player_walk", new Dimension(190, 160), 0, 0);
        registerAdjustment("player_run", new Dimension(180, 170), 0, 10);
        registerAdjustment("player_dash", new Dimension(200, 150), 10, 5);
        
        // Load basic player animations
        loadSpriteSequence(
            "player_idle",
            "Sprites/Joanna/Idle",
            "Idle",
            6,
            frameSize,
            scale,
            Duration.ofMillis(1000),
            true
        );
        
        loadSpriteSequence(
            "player_run",
            "Sprites/Joanna/Running",
            "Running",
            8,
            frameSize,
            scale,
            Duration.ofMillis(800),
            true
        );
        
        loadSpriteSequence(
            "player_to_run",
            "Sprites/Joanna/ToRun",
            "ToRun",
            3,
            frameSize,
            scale,
            Duration.ofMillis(300),
            false
        );
        
        loadSpriteSequence(
            "player_light_attack",
            "Sprites/Joanna/LightAtk",
            "LightAtk",
            12,
            frameSize,
            scale,
            Duration.ofMillis(500),
            false
        );
        
        loadSpriteSequence(
            "player_dash",
            "Sprites/Joanna/Dashing",
            "Dashing",
            3,
            frameSize,
            scale,
            Duration.ofMillis(200),
            false
        );
        
        loadSpriteSequence(
            "player_break_run",
            "Sprites/Joanna/BreakRun",
            "BreakRun",
            7,
            frameSize,
            scale,
            Duration.ofMillis(400),
            false
        );
        
        loadSpriteSequence(
            "player_land",
            "Sprites/Joanna/Land",
            "Land",
            5,
            frameSize,
            scale,
            Duration.ofMillis(300),
            false
        );
    }
    
    /**
     * Registers a sprite adjustment by name.
     */
    public void registerAdjustment(String spriteId, Dimension displaySize, int offsetX, int offsetY) {
        adjustments.put(spriteId, new SpriteAdjustment(spriteId, displaySize, offsetX, offsetY));
    }
    
    /**
     * Registers a sprite adjustment with scale factors.
     */
    public void registerAdjustment(String spriteId, double scaleX, double scaleY, int offsetX, int offsetY) {
        adjustments.put(spriteId, new SpriteAdjustment(spriteId, scaleX, scaleY, offsetX, offsetY));
    }
    
    /**
     * Gets a sprite adjustment by ID, creating a default one if not found.
     */
    public SpriteAdjustment getAdjustment(String spriteId) {
        if (!adjustments.containsKey(spriteId)) {
            adjustments.put(spriteId, new SpriteAdjustment(spriteId));
        }
        return adjustments.get(spriteId);
    }
    
    /**
     * Load a sprite sheet from resources
     */
    public boolean loadSpriteSheet(String name, String fileName) {
        try {
            BufferedImage spriteSheet = ResourceManager.getInstance().loadImage(SPRITE_PATH + fileName);
            if (spriteSheet != null) {
                spriteSheets.put(name, spriteSheet);
                System.out.println("Loaded sprite sheet: " + name);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to load sprite sheet: " + fileName);
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Create a sprite from a sprite sheet
     */
    public Sprite createSprite(String name, String sheetName, Dimension frameSize, 
                               double scale, int firstFrame, int frameCount, 
                               Duration duration) {
        
        BufferedImage sheet = spriteSheets.get(sheetName);
        if (sheet == null) {
            System.err.println("Sprite sheet not found: " + sheetName);
            return null;
        }
        
        // Use SpriteFactory to create the sprite
        Sprite sprite = SpriteFactory.fromSpriteSheet(
            name, sheet, frameSize, scale, firstFrame, frameCount, duration, true);
        
        if (sprite != null) {
            sprites.put(name, sprite);
        }
        
        return sprite;
    }
    
    /**
     * Loads a sprite sequence from the sequence manager
     */
    public Sprite loadSpriteSequence(
            String name,
            String path,
            String framePrefix,
            int frameCount,
            Dimension frameSize,
            double scale,
            Duration duration,
            boolean looping) {
        
        Sprite sprite = sequenceManager.loadSpriteSequence(
            name, path, framePrefix, frameCount, 
            frameSize, scale, duration, looping);
        
        if (sprite != null) {
            sprites.put(name, sprite);
        }
        
        return sprite;
    }
    
    /**
     * Loads a sprite sequence with custom display dimensions
     */
    public Sprite loadSpriteSequence(
            String name,
            String path,
            String framePrefix,
            int frameCount,
            Dimension frameSize,
            Dimension displaySize,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
        
        Sprite sprite = sequenceManager.loadSpriteSequence(
            name, path, framePrefix, frameCount, 
            frameSize, displaySize, offsetX, offsetY, 
            duration, looping);
        
        if (sprite != null) {
            sprites.put(name, sprite);
        }
        
        return sprite;
    }
    
    /**
     * Gets a sprite by name
     */
    public Sprite getSprite(String name) {
        return sprites.get(name);
    }
    
    /**
     * Clears the cache
     */
    public void clearCache() {
        sprites.clear();
        spriteSheets.clear();
    }
    
    /**
     * Gets the sequence manager
     */
    public SpriteSequenceManager getSequenceManager() {
        return sequenceManager;
    }
}