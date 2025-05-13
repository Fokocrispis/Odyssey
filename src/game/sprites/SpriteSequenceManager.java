package game.sprites;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.resource.ResourceManager;

/**
 * Manager for sprite sequences loaded from individual image files.
 */
public class SpriteSequenceManager {
    // Cache for loaded sprites and frames
    private final Map<String, Sprite> sprites = new HashMap<>();
    private final Map<String, List<BufferedImage>> frameCache = new HashMap<>();
    private final Map<String, SpriteAdjustment> adjustments = new HashMap<>();
    private final ResourceManager resourceManager;
    
    public SpriteSequenceManager() {
        this.resourceManager = ResourceManager.getInstance();
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
        registerAdjustment("player_run", new Dimension(180, 170), 0, 50);
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
     * Gets all sprite adjustments.
     */
    public List<SpriteAdjustment> getAllAdjustments() {
        return new ArrayList<>(adjustments.values());
    }
    
    /**
     * Updates a sprite adjustment with new values.
     */
    public void updateAdjustment(String spriteId, double scaleX, double scaleY, int offsetX, int offsetY) {
        SpriteAdjustment adjustment = getAdjustment(spriteId);
        adjustment.setScaleX(scaleX);
        adjustment.setScaleY(scaleY);
        adjustment.setOffsetX(offsetX);
        adjustment.setOffsetY(offsetY);
        
        // Apply to sprite if it exists
        Sprite sprite = sprites.get(spriteId);
        if (sprite != null) {
            adjustment.applyTo(sprite, sprite.getFrameSize());
        }
    }
    
    /**
     * Updates a sprite adjustment with a new display size.
     */
    public void updateAdjustment(String spriteId, Dimension displaySize, int offsetX, int offsetY) {
        SpriteAdjustment adjustment = getAdjustment(spriteId);
        adjustment.setDisplaySize(displaySize);
        adjustment.setOffsetX(offsetX);
        adjustment.setOffsetY(offsetY);
        
        // Apply to sprite if it exists
        Sprite sprite = sprites.get(spriteId);
        if (sprite != null) {
            adjustment.applyTo(sprite, sprite.getFrameSize());
        }
    }
    
    /**
     * Loads a sprite sequence from a folder of image files
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
        
        // Get adjustment if one exists, otherwise use default values
        SpriteAdjustment adjustment = adjustments.getOrDefault(name, 
            new SpriteAdjustment(name, scale, scale, 0, 0));
        
        if (adjustment.getDisplaySize() != null) {
            // If display size is set, calculate scale
            double scaleX = (double)adjustment.getDisplaySize().width / frameSize.width;
            double scaleY = (double)adjustment.getDisplaySize().height / frameSize.height;
            return loadSpriteSequence(name, path, framePrefix, frameCount, 
                frameSize, scaleX, scaleY, adjustment.getOffsetX(), adjustment.getOffsetY(), 
                duration, looping);
        } else {
            // Otherwise use the adjustment's scale values
            return loadSpriteSequence(name, path, framePrefix, frameCount, 
                frameSize, adjustment.getScaleX(), adjustment.getScaleY(), 
                adjustment.getOffsetX(), adjustment.getOffsetY(), duration, looping);
        }
    }
    
    /**
     * Loads a sprite sequence with custom scaling and offsets
     */
    public Sprite loadSpriteSequence(
            String name,
            String path,
            String framePrefix,
            int frameCount,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
        
        // Check if already loaded
        if (sprites.containsKey(name)) {
            return sprites.get(name);
        }
        
        // Load all frames
        List<BufferedImage> frames = loadFrames(path, framePrefix, frameCount);
        
        // If no frames were loaded, return null
        if (frames.isEmpty()) {
            System.err.println("No frames loaded for animation: " + name + " at path: " + path);
            return null;
        }
        
        // Store frames in cache
        frameCache.put(name, frames);
        
        // Create sprite using factory
        Sprite sprite = SpriteFactory.fromFrameList(
            name, frames, frameSize, scaleX, scaleY, offsetX, offsetY, duration, looping);
        
        sprites.put(name, sprite);
        return sprite;
    }
    
    /**
     * Loads a sprite sequence with DisplaySize and offsets - the missing method that's causing errors
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
        
        // Calculate scale factors from display size
        double scaleX = (double)displaySize.width / frameSize.width;
        double scaleY = (double)displaySize.height / frameSize.height;
        
        // Use the existing method with calculated scale factors
        return loadSpriteSequence(
            name, path, framePrefix, frameCount, 
            frameSize, scaleX, scaleY, offsetX, offsetY, 
            duration, looping
        );
    }
    
    /**
     * Loads frames from the file system
     */
    private List<BufferedImage> loadFrames(String path, String framePrefix, int frameCount) {
        // Make sure path ends with slash
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        // Load all frames
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 1; i <= frameCount; i++) {
            String fileName = path + framePrefix + i + ".png";
            
            try {
                BufferedImage frame = resourceManager.loadImage(fileName);
                if (frame != null) {
                    frames.add(frame);
                } else {
                    System.err.println("Failed to load frame: " + fileName);
                }
            } catch (IOException e) {
                System.err.println("Error loading frame " + fileName + ": " + e.getMessage());
            }
        }
        
        // If no frames were loaded, try alternative naming patterns
        if (frames.isEmpty()) {
            System.out.println("Trying alternative naming patterns for path: " + path);
            frames = tryAlternativeNamingPatterns(path, framePrefix, frameCount);
        }
        
        return frames;
    }
    
    /**
     * Tries multiple naming patterns to load frames
     */
    private List<BufferedImage> tryAlternativeNamingPatterns(String path, String framePrefix, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        
        String[][] patterns = {
            // Try frame_01.png format
            {"frame_%02d.png", ""},
            // Try prefix_1.png format (no padding)
            {"%s_%d.png", framePrefix},
            // Try prefix01.png format
            {"%s%02d.png", framePrefix},
            // Try just numbers: 1.png
            {"%d.png", ""},
            // Try frame-1.png format
            {"frame-%d.png", ""},
            // Try lowercase prefix
            {"%s%d.png", framePrefix.toLowerCase()}
        };
        
        for (String[] pattern : patterns) {
            frames.clear();
            String format = pattern[0];
            String prefix = pattern[1];
            
            for (int i = 1; i <= frameCount; i++) {
                String fileName = path;
                
                try {
                    if (prefix.isEmpty()) {
                        fileName += String.format(format, i);
                    } else {
                        fileName += String.format(format, prefix, i);
                    }
                    
                    BufferedImage frame = resourceManager.loadImage(fileName);
                    if (frame != null) {
                        frames.add(frame);
                    } else {
                        // Frame failed to load, break out of this pattern
                        frames.clear();
                        break;
                    }
                } catch (Exception e) {
                    // Silently continue to next pattern
                    frames.clear();
                    break;
                }
            }
            
            // If all frames loaded successfully, return them
            if (frames.size() == frameCount) {
                System.out.println("Successfully loaded frames using pattern: " + format);
                return frames;
            }
        }
        
        return frames;
    }
    
    /**
     * Gets a sprite by name
     */
    public Sprite getSprite(String name) {
        return sprites.get(name);
    }
    
    /**
     * Checks if a sprite exists
     */
    public boolean hasSprite(String name) {
        return sprites.containsKey(name);
    }
    
    /**
     * Clears the cache
     */
    public void clearCache() {
        sprites.clear();
        frameCache.clear();
    }
}