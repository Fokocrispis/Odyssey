package game.camera.effects;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages visual effects for the camera.
 */
public class CameraEffectsManager {
    private final List<CameraVisualEffect> effects = new ArrayList<>();
    private final LetterboxEffect letterboxEffect;
    
    /**
     * Creates a new camera effects manager.
     */
    public CameraEffectsManager(int screenWidth, int screenHeight) {
        letterboxEffect = new LetterboxEffect(screenWidth, screenHeight);
    }
    
    /**
     * Adds a visual effect.
     */
    public void addEffect(CameraVisualEffect effect) {
        effects.add(effect);
        // Sort by priority (lower values are rendered first)
        effects.sort(Comparator.comparingInt(CameraVisualEffect::getPriority));
    }
    
    /**
     * Removes a visual effect.
     */
    public void removeEffect(CameraVisualEffect effect) {
        effects.remove(effect);
    }
    
    /**
     * Updates all effects.
     */
    public void update(long deltaTime) {
        // Update letterbox effect
        letterboxEffect.update(deltaTime);
        
        // Update all other effects
        for (CameraVisualEffect effect : effects) {
            effect.update(deltaTime);
        }
    }
    
    /**
     * Renders all effects.
     */
    public void render(Graphics2D g) {
        // Render all effects in priority order
        for (CameraVisualEffect effect : effects) {
            if (effect.isVisible()) {
                effect.render(g);
            }
        }
        
        // Render letterbox at the end (highest priority)
        letterboxEffect.render(g);
    }
    
    /**
     * Gets the letterbox effect.
     */
    public LetterboxEffect getLetterboxEffect() {
        return letterboxEffect;
    }
    
    /**
     * Creates a cinematic effect for the ultimate attack.
     * 
     * @param duration Duration of the effect in milliseconds
     */
    public void createUltimateAttackEffect(long duration) {
        // Show letterbox bars with quick animation
        letterboxEffect.setLetterboxSize(0.15f); // 15% of screen height
        letterboxEffect.show(500);
        
        // Add any other effects needed for the ultimate attack
        // ...
        
        // Schedule automatic removal after duration
        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    letterboxEffect.hide(500);
                }
            },
            duration
        );
    }
}