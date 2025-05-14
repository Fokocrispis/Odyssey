package game.camera;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;

import game.Vector2D;
import game.camera.effects.CameraEffectsManager;
import game.camera.effects.LetterboxEffect;

/**
 * Camera extension that supports cinematic effects like
 * zooming, focusing, and screen flash.
 */
public class CinematicCamera extends Camera {
    // Zoom factors
    private double zoomX = 1.0;
    private double zoomY = 1.0;
    private double targetZoomX = 1.0;
    private double targetZoomY = 1.0;
    
    // Zoom transition
    private double zoomTransitionDuration = 0;
    private double zoomTransitionTimer = 0;
    private boolean isZooming = false;
    private boolean autoResetZoom = false;
    private double zoomResetDelay = 0;
    private double zoomResetTimer = 0;
    
    // Flash effect
    private boolean shouldFlash = false;
    private int flashFramesRemaining = 0;
    private Color flashColor = Color.WHITE;
    private float flashAlpha = 1.0f;
    
    // Focus target
    private Vector2D focusTarget = null;
    private boolean isFocusing = false;
    
    // Store original transform separately
    private AffineTransform tempOriginalTransform = null;
    
    // Camera effects manager
    private CameraEffectsManager effectsManager;
    
    /**
     * Creates a new cinematic camera with the specified viewport size.
     */
    public CinematicCamera(int width, int height) {
        super(width, height);
        this.effectsManager = new CameraEffectsManager(width, height);
    }
    
    /**
     * Updates the camera position and effects.
     * 
     * @param deltaTime Time elapsed since the last update in milliseconds.
     */
    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        
        // Convert delta time to seconds for smoother transitions
        float dt = deltaTime / 1000.0f;
        
        // Update zoom transition
        if (isZooming) {
            zoomTransitionTimer -= dt;
            
            // Calculate zoom progress
            float progress = 1.0f - (float)(zoomTransitionTimer / zoomTransitionDuration);
            progress = Math.max(0.0f, Math.min(1.0f, progress));
            
            // Use easing for smoother transition
            float easedProgress = easeInOutQuad(progress);
            
            // Interpolate zoom
            zoomX = lerp(zoomX, targetZoomX, easedProgress);
            zoomY = lerp(zoomY, targetZoomY, easedProgress);
            
            // Check if zoom transition is complete
            if (zoomTransitionTimer <= 0) {
                zoomX = targetZoomX;
                zoomY = targetZoomY;
                isZooming = false;
                
                // Start reset timer if auto-reset is enabled
                if (autoResetZoom) {
                    zoomResetTimer = zoomResetDelay;
                }
            }
        }
        
        // Handle zoom reset after delay
        if (zoomResetTimer > 0) {
            zoomResetTimer -= dt;
            if (zoomResetTimer <= 0) {
                setZoom(1.0, 1.0, 0.5);
            }
        }
        
        // Handle focus transitions
        if (isFocusing && focusTarget != null) {
            // Set position directly if no target, otherwise let target following handle it
            if (getTarget() == null) {
                // Smoothly move towards focus target
                double focusSpeed = 5.0 * dt;
                Vector2D currentPos = getPosition();
                Vector2D newPos = new Vector2D(
                    lerp(currentPos.getX(), focusTarget.getX() - getWidth() / 2, focusSpeed),
                    lerp(currentPos.getY(), focusTarget.getY() - getHeight() / 2, focusSpeed)
                );
                setPosition(newPos);
            }
        }
        
        // Update flash effect
        if (shouldFlash) {
            flashFramesRemaining--;
            if (flashFramesRemaining <= 0) {
                shouldFlash = false;
            }
        }
        
        // Update effects manager
        if (effectsManager != null) {
            effectsManager.update(deltaTime);
        }
    }
    
    /**
     * Sets the camera's zoom with a smooth transition.
     * 
     * @param newZoomX Horizontal zoom factor (1.0 is normal)
     * @param newZoomY Vertical zoom factor (1.0 is normal)
     * @param transitionDuration Transition time in seconds
     */
    public void setZoom(double newZoomX, double newZoomY, double transitionDuration) {
        this.targetZoomX = newZoomX;
        this.targetZoomY = newZoomY;
        this.zoomTransitionDuration = transitionDuration;
        this.zoomTransitionTimer = transitionDuration;
        this.isZooming = true;
        this.autoResetZoom = false;
    }
    
    /**
     * Sets the camera's zoom with automatic reset after delay.
     * 
     * @param newZoomX Horizontal zoom factor
     * @param newZoomY Vertical zoom factor
     * @param transitionDuration Transition time in seconds
     * @param resetDelay Delay before resetting zoom
     */
    public void setZoomWithReset(double newZoomX, double newZoomY, 
                                 double transitionDuration, double resetDelay) {
        setZoom(newZoomX, newZoomY, transitionDuration);
        this.autoResetZoom = true;
        this.zoomResetDelay = resetDelay;
    }
    
    /**
     * Sets a focus target for the camera.
     * 
     * @param focusTarget Target position to focus on
     * @param immediate Whether to focus immediately or smoothly
     */
    public void setFocusTarget(Vector2D focusTarget, boolean immediate) {
        this.focusTarget = new Vector2D(focusTarget);
        this.isFocusing = true;
        
        if (immediate) {
            // Immediately move to focus target
            setPosition(new Vector2D(
                focusTarget.getX() - getWidth() / 2,
                focusTarget.getY() - getHeight() / 2
            ));
        }
    }
    
    /**
     * Clears the focus target and returns to normal.
     */
    public void clearFocus() {
        this.isFocusing = false;
        this.focusTarget = null;
    }
    
    /**
     * Triggers a white screen flash effect.
     * 
     * @param frames Number of frames to show the flash
     * @param alpha Flash transparency (0.0-1.0)
     */
    public void flash(int frames, float alpha) {
        flash(frames, Color.WHITE, alpha);
    }
    
    /**
     * Triggers a screen flash effect with custom color.
     * 
     * @param frames Number of frames to show the flash
     * @param color Flash color
     * @param alpha Flash transparency (0.0-1.0)
     */
    public void flash(int frames, Color color, float alpha) {
        this.shouldFlash = true;
        this.flashFramesRemaining = frames;
        this.flashColor = color;
        this.flashAlpha = alpha;
    }
    
    /**
     * Creates a cinematic effect for the ultimate attack.
     * 
     * @param duration Duration of the effect in milliseconds
     */
    public void createUltimateAttackEffect(long duration) {
        // Apply letterbox effect for ultimate attack
        effectsManager.createUltimateAttackEffect(duration);
        
        // Apply flash effect
        flash(3, Color.WHITE, 0.5f);
        
        // Apply zoom effect with reset
        setZoomWithReset(1.2, 0.8, 0.1, duration / 1000.0 - 0.5);
    }
    
    /**
     * Applies camera transformations to the graphics context.
     * Call this before rendering game objects.
     * 
     * @param g The graphics context
     */
    @Override
    public void apply(Graphics2D g) {
        // Store the original transform
        tempOriginalTransform = g.getTransform();
        
        // Calculate the center point of the view
        Vector2D position = getPosition();
        double centerX = position.getX() + getWidth() / 2.0;
        double centerY = position.getY() + getHeight() / 2.0;
        
        // Apply zoomed transform
        AffineTransform transform = new AffineTransform();
        
        // 1. Translate to view center
        transform.translate(getWidth() / 2.0, getHeight() / 2.0);
        
        // 2. Apply zoom
        transform.scale(zoomX, zoomY);
        
        // 3. Translate back to position
        transform.translate(-centerX, -centerY);
        
        // Apply transform
        g.setTransform(transform);
    }
    
    /**
     * Resets the graphics transform and restores the original.
     * Override to handle our stored transform.
     */
    @Override
    public void reset(Graphics2D g) {
        if (tempOriginalTransform != null) {
            g.setTransform(tempOriginalTransform);
        } else {
            super.reset(g);
        }
    }
    
    /**
     * Renders the camera effects (like screen flash).
     * Call this after rendering all game objects and calling reset().
     * 
     * @param g The graphics context
     */
    public void renderEffects(Graphics2D g) {
        // Apply built-in flash effect
        if (shouldFlash) {
            // Create a composite for transparency
            java.awt.AlphaComposite composite = java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER, flashAlpha);
            
            // Store original composite
            java.awt.Composite originalComposite = g.getComposite();
            
            // Apply flash effect
            g.setComposite(composite);
            g.setColor(flashColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Restore original composite
            g.setComposite(originalComposite);
        }
        
        // Render additional effects from manager
        if (effectsManager != null) {
            effectsManager.render(g);
        }
    }
    
    /**
     * Linear interpolation between two values.
     */
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    /**
     * Quadratic ease-in-out for smooth transitions.
     */
    private float easeInOutQuad(float t) {
        return t < 0.5f ? 2.0f * t * t : 1.0f - (float)Math.pow(-2.0 * t + 2.0, 2) / 2.0f;
    }
}