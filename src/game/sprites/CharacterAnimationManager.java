package game.sprites;

import java.awt.Dimension;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Character animation manager that loads sprites based on character ID and animation folders.
 * Manages sprites from the resources/Sprites/{characterId} folder structure.
 */
public class CharacterAnimationManager {
    // Base path to sprites folder in resources
    private static final String BASE_SPRITE_PATH = "Sprites/";
    
    // Animation cache
    private final Map<String, Sprite> animations = new HashMap<>();
    
    // Character information
    private final String characterId;
    private final SpriteSequenceManager sequenceManager;
    
    /**
     * Creates a new animation manager for a specific character
     * 
     * @param characterId The unique identifier for the character (folder name)
     */
    public CharacterAnimationManager(String characterId) {
        this.characterId = characterId;
        this.sequenceManager = new SpriteSequenceManager();
    }
    
    /**
     * Loads all standard animations for this character
     */
    public void loadAllAnimations() {
        // Define standard frame size and scale
        Dimension frameSize = new Dimension(64, 64);
        double scale = 3.0;
        
        // Register required animations with their configurations
        registerAnimation("idle", "Idle", 6, frameSize, scale, 1000, true);
        registerAnimation("run", "Running", 8, frameSize, scale, 800, true);
        registerAnimation("to_run", "ToRun", 3, frameSize, scale, 300, false);
        registerAnimation("light_attack", "LightAtk", 12, frameSize, scale, 500, false);
        registerAnimation("dash", "Dashing", 3, frameSize, scale, 200, false);
        registerAnimation("break_run", "BreakRun", 7, frameSize, scale, 400, false);
        registerAnimation("land", "Land", 5, frameSize, scale, 300, false);
        
        // Load any custom animations specific to this character
        loadCustomAnimations();
    }
    
    /**
     * Registers and loads a character animation
     * 
     * @param animationKey The key used to retrieve the animation
     * @param folderName The folder name within the character's directory
     * @param frameCount Number of frames in the animation
     * @param frameSize Size of each frame
     * @param scale Scale factor for rendering
     * @param durationMs Duration of the animation in milliseconds
     * @param loops Whether the animation should loop
     */
    public void registerAnimation(String animationKey, String folderName, int frameCount,
                                 Dimension frameSize, double scale, long durationMs, boolean loops) {
        try {
            // Build path from character ID and animation folder
            String animationPath = buildAnimationPath(folderName);
            
            // Use the folder name as the default frame prefix
            String framePrefix = folderName;
            
            // Load animation using the sequence manager
            Sprite animation = sequenceManager.loadSpriteSequence(
                animationKey,
                animationPath,
                framePrefix,
                frameCount,
                frameSize,
                scale,
                Duration.ofMillis(durationMs),
                loops
            );
            
            if (animation != null) {
                animations.put(animationKey, animation);
                System.out.println("Loaded animation: " + characterId + "/" + folderName + 
                                  " (" + frameCount + " frames)");
            } else {
                System.err.println("Failed to load animation: " + characterId + "/" + folderName);
            }
        } catch (Exception e) {
            System.err.println("Error loading animation: " + characterId + "/" + folderName);
            e.printStackTrace();
        }
    }
    
    /**
     * Builds the path to an animation folder based on character ID
     * 
     * @param animationFolder The animation folder name
     * @return The full path to the animation folder
     */
    private String buildAnimationPath(String animationFolder) {
        return BASE_SPRITE_PATH + characterId + "/" + animationFolder;
    }
    
    /**
     * Loads any custom animations specific to this character
     */
    protected void loadCustomAnimations() {
        // Override in subclasses to add character-specific animations
        
        // For example, Joanna might have unique animations that other characters don't have
        if ("Joanna".equals(characterId)) {
            Dimension frameSize = new Dimension(64, 64);
            double scale = 3.0;
            
            // Add any Joanna-specific animations here
            // registerAnimation("special_attack", "SpecialAtk", 15, frameSize, scale, 600, false);
        }
    }
    
    /**
     * Gets an animation by its key
     * 
     * @param key The animation key
     * @return The sprite animation, or null if not found
     */
    public Sprite getAnimation(String key) {
        return animations.get(key);
    }
    
    /**
     * Checks if an animation exists
     * 
     * @param key The animation key
     * @return True if the animation exists
     */
    public boolean hasAnimation(String key) {
        return animations.containsKey(key);
    }
    
    /**
     * Gets the character ID
     */
    public String getCharacterId() {
        return characterId;
    }
    
    /**
     * Prints information about all loaded animations
     */
    public void printLoadedAnimations() {
        System.out.println("====== Animations for " + characterId + " ======");
        animations.forEach((key, sprite) -> {
            int frameCount = sprite.getTotalFrames();
            boolean isLooping = sprite.isLooping();
            
            System.out.println(key + ": " + frameCount + " frames" + 
                               (isLooping ? " (looping)" : ""));
        });
        System.out.println("==============================");
    }
}