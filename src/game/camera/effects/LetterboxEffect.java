package game.camera.effects;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Creates a letterbox effect with two bars at the top and bottom of the screen.
 * Used for cinematic sequences.
 */
public class LetterboxEffect {
    // Top and bottom bars
    private final CameraBar topBar;
    private final CameraBar bottomBar;
    
    // Screen dimensions
    private final int screenWidth;
    private final int screenHeight;
    
    // Effect state
    private boolean active = false;
    private float letterboxSize = 0.1f; // Size as percentage of screen height
    
    /**
     * Creates a new letterbox effect for the given screen dimensions.
     */
    public LetterboxEffect(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Calculate bar heights
        int barHeight = (int)(screenHeight * letterboxSize);
        
        // Create bars with highest priority (1000)
        topBar = new CameraBar(0, -barHeight, screenWidth, barHeight, 1000);
        bottomBar = new CameraBar(0, screenHeight, screenWidth, barHeight, 1000);
    }
    
    /**
     * Shows the letterbox effect with animation.
     * 
     * @param duration Animation duration in milliseconds
     */
    public void show(long duration) {
        if (active) return;
        
        int barHeight = (int)(screenHeight * letterboxSize);
        topBar.animateTo(0, 0, duration, CameraVisualEffect.EasingFunction.EASE_OUT);
        bottomBar.animateTo(0, screenHeight - barHeight, duration, CameraVisualEffect.EasingFunction.EASE_OUT);
        active = true;
    }
    
    /**
     * Hides the letterbox effect with animation.
     * 
     * @param duration Animation duration in milliseconds
     */
    public void hide(long duration) {
        if (!active) return;
        
        int barHeight = (int)(screenHeight * letterboxSize);
        topBar.animateTo(0, -barHeight, duration, CameraVisualEffect.EasingFunction.EASE_IN);
        bottomBar.animateTo(0, screenHeight, duration, CameraVisualEffect.EasingFunction.EASE_IN);
        active = false;
    }
    
    /**
     * Updates the letterbox effect.
     */
    public void update(long deltaTime) {
        topBar.update(deltaTime);
        bottomBar.update(deltaTime);
    }
    
    /**
     * Renders the letterbox effect.
     */
    public void render(Graphics2D g) {
        topBar.render(g);
        bottomBar.render(g);
    }
    
    /**
     * Sets the letterbox size as percentage of screen height.
     * 
     * @param size Size between 0.0 and 0.5 (0 to 50% of screen height)
     */
    public void setLetterboxSize(float size) {
        letterboxSize = Math.max(0, Math.min(0.5f, size));
        int barHeight = (int)(screenHeight * letterboxSize);
        
        // Update bar heights
        topBar.setHeight(barHeight);
        bottomBar.setHeight(barHeight);
        
        // Reset positions if active
        if (active) {
            topBar.moveTo(0, 0);
            bottomBar.moveTo(0, screenHeight - barHeight);
        } else {
            topBar.moveTo(0, -barHeight);
            bottomBar.moveTo(0, screenHeight);
        }
    }
    
    /**
     * Checks if the letterbox effect is active.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Checks if the letterbox effect is currently animating.
     */
    public boolean isAnimating() {
        return topBar.isAnimating() || bottomBar.isAnimating();
    }
    
    /**
     * Gets the top bar.
     */
    public CameraBar getTopBar() {
        return topBar;
    }
    
    /**
     * Gets the bottom bar.
     */
    public CameraBar getBottomBar() {
        return bottomBar;
    }
    
    /**
     * A solid colored bar used for the letterbox effect.
     */
    public static class CameraBar extends CameraVisualEffect {
        private Color color = Color.BLACK;
        
        public CameraBar(int x, int y, int width, int height, int priority) {
            super(x, y, width, height, priority);
        }
        
        @Override
        public void render(Graphics2D g) {
            if (!visible) return;
            
            // Store original composite
            java.awt.Composite originalComposite = g.getComposite();
            
            // Apply opacity
            if (opacity < 1.0f) {
                g.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, opacity));
            }
            
            // Draw the bar
            g.setColor(color);
            g.fillRect((int)position.getX(), (int)position.getY(), width, height);
            
            // Restore original composite
            g.setComposite(originalComposite);
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
    }
}