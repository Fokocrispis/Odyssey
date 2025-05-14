package game.camera.effects;

import java.awt.Graphics2D;
import game.Vector2D;

/**
 * Base class for visual effects applied to the camera.
 * Used for cinematic effects like letterboxing, vignettes, etc.
 */
public abstract class CameraVisualEffect {
    // Effect position (in screen coordinates)
    protected Vector2D position;
    
    // Effect dimensions
    protected int width;
    protected int height;
    
    // Effect properties
    protected boolean visible = true;
    protected int priority = 0;
    protected float opacity = 1.0f;
    
    // Animation properties
    protected boolean animating = false;
    protected Vector2D targetPosition;
    protected long animationDuration = 0;
    protected long animationStartTime = 0;
    protected EasingFunction easingFunction = EasingFunction.LINEAR;

    /**
     * Creates a new camera visual effect.
     * 
     * @param x Initial X position
     * @param y Initial Y position
     * @param width Effect width
     * @param height Effect height
     * @param priority Rendering priority (higher = rendered later/on top)
     */
    public CameraVisualEffect(int x, int y, int width, int height, int priority) {
        this.position = new Vector2D(x, y);
        this.width = width;
        this.height = height;
        this.priority = priority;
    }
    
    /**
     * Updates the effect.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void update(long deltaTime) {
        if (animating) {
            updateAnimation(deltaTime);
        }
    }
    
    /**
     * Updates the animation.
     */
    protected void updateAnimation(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;
        
        if (elapsedTime >= animationDuration) {
            // Animation complete
            position.set(targetPosition);
            animating = false;
            return;
        }
        
        // Calculate progress
        float progress = (float)elapsedTime / animationDuration;
        progress = Math.max(0, Math.min(1, progress));
        
        // Apply easing
        float easedProgress = applyEasing(progress);
        
        // Interpolate position
        double startX = position.getX();
        double startY = position.getY();
        double endX = targetPosition.getX();
        double endY = targetPosition.getY();
        
        position.setX(startX + (endX - startX) * easedProgress);
        position.setY(startY + (endY - startY) * easedProgress);
    }
    
    /**
     * Applies easing to the animation progress.
     */
    protected float applyEasing(float progress) {
        switch (easingFunction) {
            case LINEAR:
                return progress;
            case EASE_IN:
                return progress * progress;
            case EASE_OUT:
                return 1 - (1 - progress) * (1 - progress);
            case EASE_IN_OUT:
                return (float) (progress < 0.5f ? 
                    2 * progress * progress : 
                    1 - Math.pow(-2 * progress + 2, 2) / 2);
            default:
                return progress;
        }
    }
    
    /**
     * Renders the effect.
     * 
     * @param g The graphics context
     */
    public abstract void render(Graphics2D g);
    
    /**
     * Moves the effect to a new position instantly.
     */
    public void moveTo(int x, int y) {
        position.set(x, y);
        animating = false;
    }
    
    /**
     * Animates the effect to a new position.
     * 
     * @param x Target X position
     * @param y Target Y position
     * @param duration Animation duration in milliseconds
     */
    public void animateTo(int x, int y, long duration) {
        animateTo(x, y, duration, EasingFunction.LINEAR);
    }
    
    /**
     * Animates the effect to a new position with easing.
     * 
     * @param x Target X position
     * @param y Target Y position
     * @param duration Animation duration in milliseconds
     * @param easing Easing function to use
     */
    public void animateTo(int x, int y, long duration, EasingFunction easing) {
        targetPosition = new Vector2D(x, y);
        animationDuration = duration;
        animationStartTime = System.currentTimeMillis();
        easingFunction = easing;
        animating = true;
    }
    
    // Getters and setters
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = Math.max(0, Math.min(1, opacity)); }
    
    public boolean isAnimating() { return animating; }
    
    /**
     * Easing functions for animations.
     */
    public enum EasingFunction {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT
    }
}