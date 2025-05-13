package game.sprites;

import java.awt.Dimension;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages attack and movement animation sequences for the player character.
 * Loads and organizes sequences from the file system for easy access.
 */
public class AttackSequenceManager {
    private static final String BASE_PATH = "JoannaD'ArcIII_v1.9.2/";
    private static final String JOANNA_FOLDER = "Joanna folder/";
    
    private final SpriteSequenceManager sequenceManager;
    private final Map<String, Sprite> attackSprites = new HashMap<>();
    private final Map<String, AnimationInfo> animationInfo = new HashMap<>();
    
    // Default dimensions and scaling factors
    private static final Dimension DEFAULT_FRAME_SIZE = new Dimension(64, 64);
    private static final double DEFAULT_SCALE = 3.0;
    
    /**
     * Creates a new attack sequence manager
     */
    public AttackSequenceManager() {
        this.sequenceManager = new SpriteSequenceManager();
        initializeAnimationInfo();
    }
    
    /**
     * Initializes information about each animation sequence
     */
    private void initializeAnimationInfo() {
        // Standard attacks
        registerAnimation("light_attack", "Sprites/Attacks/LightAtk", "LightAtk", 12, 400, false);
        registerAnimation("combo_attack", "Sprites/Attacks/ComboAtk", "ComboAtk", 30, 900, false);
        
        // Movement animations
        registerAnimation("dash", "Sprites/Dash/DashFrame", "DashFrame", 8, 250, false);
        registerAnimation("land", "Sprites/JumpAndFall/Land/Land", "Land", 5, 300, false);
    }
    
    /**
     * Registers animation details
     */
    private void registerAnimation(String name, String path, String prefix, int frameCount, 
                                  long durationMs, boolean loops) {
        animationInfo.put(name, new AnimationInfo(
            JOANNA_FOLDER + path,
            prefix,
            frameCount,
            Duration.ofMillis(durationMs),
            loops
        ));
    }
    
    /**
     * Loads all attack and movement animations
     */
    public void loadAllAnimations() {
        for (Map.Entry<String, AnimationInfo> entry : animationInfo.entrySet()) {
            String name = entry.getKey();
            AnimationInfo info = entry.getValue();
            
            loadAnimation(name, info);
        }
    }
    
    /**
     * Loads a single animation based on its registered info
     */
    private void loadAnimation(String name, AnimationInfo info) {
        Sprite sprite = sequenceManager.loadSpriteSequence(
            name,
            info.path,
            info.prefix,
            info.frameCount,
            DEFAULT_FRAME_SIZE,
            DEFAULT_SCALE,
            info.duration,
            info.loops
        );
        
        if (sprite != null) {
            attackSprites.put(name, sprite);
            System.out.println("Loaded animation sequence: " + name + 
                              " (" + info.frameCount + " frames)");
        } else {
            System.err.println("Failed to load animation: " + name);
        }
    }
    
    /**
     * Gets an attack sprite by name
     */
    public Sprite getSprite(String name) {
        return attackSprites.get(name);
    }
    
    /**
     * Checks if a sprite exists
     */
    public boolean hasSprite(String name) {
        return attackSprites.containsKey(name);
    }
    
    /**
     * Gets the animation info for a sprite
     */
    public AnimationInfo getAnimationInfo(String name) {
        return animationInfo.get(name);
    }
    
    /**
     * Animation information class
     */
    public static class AnimationInfo {
        public final String path;
        public final String prefix;
        public final int frameCount;
        public final Duration duration;
        public final boolean loops;
        
        public AnimationInfo(String path, String prefix, int frameCount, 
                            Duration duration, boolean loops) {
            this.path = path;
            this.prefix = prefix;
            this.frameCount = frameCount;
            this.duration = duration;
            this.loops = loops;
        }
    }
}