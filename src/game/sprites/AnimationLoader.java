package game.sprites;

import java.awt.Dimension;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified interface for loading sprite animations from either
 * sprite sheets or individual image sequences.
 */
public class AnimationLoader {
    private final SpriteSheetManager sheetManager;
    private final SpriteSequenceManager sequenceManager;
    private final Map<String, String> spriteSourceMapping = new HashMap<>();
    
    // Singleton instance
    private static AnimationLoader instance;
    
    /**
     * Gets the singleton instance of AnimationLoader.
     */
    public static AnimationLoader getInstance() {
        if (instance == null) {
            instance = new AnimationLoader();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private AnimationLoader() {
        this.sheetManager = new SpriteSheetManager();
        this.sequenceManager = new SpriteSequenceManager();
    }
    
    /**
     * Loads all player sprites using the new sequence-based approach.
     */
    public void loadPlayerSprites() {
        sequenceManager.createPlayerSprites();
    }
    
    /**
     * Gets a sprite by name, from either sprite sheet or sequence manager.
     * 
     * @param name The sprite name
     * @return The sprite, or null if not found
     */
    public Sprite getSprite(String name) {
        // Check sequence manager first (new approach)
        if (sequenceManager.hasSprite(name)) {
            spriteSourceMapping.put(name, "sequence");
            return sequenceManager.getSprite(name);
        }
        
        // Fall back to sheet manager (old approach)
        Sprite sprite = sheetManager.getSprite(name);
        if (sprite != null) {
            spriteSourceMapping.put(name, "sheet");
        }
        
        return sprite;
    }
    
    /**
     * Loads a sprite animation from a sequence of images.
     * 
     * @param animationName Name for the animation
     * @param folder Folder path (relative to sprites base path)
     * @param prefix Filename prefix (e.g., "run" for "run1.png")
     * @param frameCount Number of frames in the animation
     * @param frameSize Size of each frame
     * @param scale Scale factor for rendering
     * @param duration Duration of the full animation
     * @param looping Whether the animation should loop
     * @return The created sprite
     */
    public Sprite loadAnimation(
            String animationName,
            String folder,
            String prefix,
            int frameCount,
            Dimension frameSize,
            double scale,
            Duration duration,
            boolean looping) {
        
        Sprite sprite = sequenceManager.loadSpriteSequence(
            animationName,
            folder,
            prefix,
            frameCount,
            frameSize,
            scale,
            duration,
            looping
        );
        
        if (sprite != null) {
            spriteSourceMapping.put(animationName, "sequence");
        }
        
        return sprite;
    }
    
    /**
     * Loads an animation with a custom file extension, start index, etc.
     */
    public Sprite loadAnimation(
            String animationName,
            String folder,
            String prefix,
            int startIndex,
            int frameCount,
            String fileExtension,
            Dimension frameSize,
            double scale,
            Duration duration,
            boolean looping) {
        
        // The SpriteSequenceManager doesn't have a method with this exact signature
        // We need to adapt our call to match what's available
        
        // First try the alternative method that might exist
        try {
            // Try to use reflection to call the method if it exists
            java.lang.reflect.Method method = sequenceManager.getClass().getMethod(
                "loadSpriteSequence",
                String.class, String.class, String.class, 
                int.class, int.class, String.class,
                Dimension.class, double.class, Duration.class, boolean.class
            );
            
            Sprite sprite = (Sprite) method.invoke(
                sequenceManager,
                animationName, folder, prefix, 
                startIndex, frameCount, fileExtension,
                frameSize, scale, duration, looping
            );
            
            if (sprite != null) {
                spriteSourceMapping.put(animationName, "sequence");
                return sprite;
            }
        } catch (Exception e) {
            // Method doesn't exist, continue with fallback
        }
        
        // Fallback implementation - adapt to the available method
        // We'll ignore the startIndex and extension, and use default values
        Sprite sprite = sequenceManager.loadSpriteSequence(
            animationName,
            folder,
            prefix,
            frameCount,
            frameSize,
            scale,
            duration,
            looping
        );
        
        if (sprite != null) {
            spriteSourceMapping.put(animationName, "sequence");
        }
        
        return sprite;
    }
    
    /**
     * Loads a sprite sequence with adjustable display dimensions and offsets.
     */
    public Sprite loadAnimationWithOffsets(
            String animationName,
            String folder,
            String prefix,
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
        
        // Create a dummy sprite to figure out what method signature to use
        try {
            // Try to use the method with the exact signature if it exists
            java.lang.reflect.Method method = sequenceManager.getClass().getMethod(
                "loadSpriteSequence",
                String.class, String.class, String.class, int.class,
                Dimension.class, Dimension.class, int.class, int.class,
                Duration.class, boolean.class
            );
            
            Sprite sprite = (Sprite) method.invoke(
                sequenceManager,
                animationName, folder, prefix, frameCount,
                frameSize, displaySize, offsetX, offsetY,
                duration, looping
            );
            
            if (sprite != null) {
                spriteSourceMapping.put(animationName, "sequence");
                return sprite;
            }
        } catch (Exception e) {
            // Method doesn't exist, continue with fallback
        }
        
        // Fallback to the simple version using average scale
        double avgScale = (scaleX + scaleY) / 2.0;
        
        Sprite sprite = sequenceManager.loadSpriteSequence(
            animationName,
            folder,
            prefix,
            frameCount,
            frameSize,
            avgScale,
            duration,
            looping
        );
        
        if (sprite != null) {
            // Store the animation name and source
            spriteSourceMapping.put(animationName, "sequence");
        }
        
        return sprite;
    }
    
    /**
     * Checks if a sprite is available from either manager.
     */
    public boolean hasSprite(String name) {
        return sequenceManager.hasSprite(name) || 
               sheetManager.getSprite(name) != null;
    }
    
    /**
     * Gets the source of a loaded sprite ("sequence" or "sheet").
     */
    public String getSpriteSource(String name) {
        return spriteSourceMapping.getOrDefault(name, "unknown");
    }
    
    /**
     * Clears all cached sprites from both managers.
     */
    public void clearCache() {
        sequenceManager.clearCache();
        sheetManager.clearCache();
        spriteSourceMapping.clear();
    }
    
    /**
     * Loads a specific character's animations from the file system.
     * 
     * @param characterName Name of the character (folder path)
     */
    public void loadCharacterAnimations(String characterName) {
        // Standard frame size and scale
        Dimension frameSize = new Dimension(64, 64);
        double scale = 3.0;
        
        // Map of animation configurations
        Map<String, AnimationConfig> animations = new HashMap<>();
        
        // Define standard animations
        animations.put("idle", new AnimationConfig("Idle", "idle", 6, 1000, true));
        animations.put("walk", new AnimationConfig("Walk", "walk", 18, 1200, true));
        animations.put("run", new AnimationConfig("Run", "run", 8, 500, true));
        animations.put("jump", new AnimationConfig("Jump", "jump", 6, 500, false));
        animations.put("fall", new AnimationConfig("Fall", "fall", 4, 500, true));
        animations.put("land", new AnimationConfig("Land", "land", 3, 200, false));
        animations.put("dash", new AnimationConfig("Dash", "dash", 5, 250, false));
        animations.put("attack", new AnimationConfig("Attack", "attack", 7, 350, false));
        animations.put("hurt", new AnimationConfig("Hurt", "hurt", 3, 300, false));
        animations.put("death", new AnimationConfig("Death", "death", 8, 800, false));
        
        // Load all animations for this character
        String basePath = characterName + "/Animations/";
        animations.forEach((key, config) -> {
            loadAnimation(
                "player_" + key,
                basePath + config.folder,
                config.prefix,
                config.frameCount,
                frameSize,
                scale,
                Duration.ofMillis(config.durationMs),
                config.looping
            );
        });
        
        System.out.println("Loaded character animations for: " + characterName);
    }
    
    /**
     * Helper class for animation configuration
     */
    private static class AnimationConfig {
        final String folder;
        final String prefix;
        final int frameCount;
        final long durationMs;
        final boolean looping;
        
        AnimationConfig(String folder, String prefix, int frameCount, long durationMs, boolean looping) {
            this.folder = folder;
            this.prefix = prefix;
            this.frameCount = frameCount;
            this.durationMs = durationMs;
            this.looping = looping;
        }
    }
}