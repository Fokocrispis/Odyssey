package game.camera;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import game.Vector2D;

/**
 * Camera class that controls the view of the game world.
 * Supports following targets, smooth movement, and cinematic sequences.
 */
public class Camera {
    // Camera position in the world (top-left corner)
    private Vector2D position;
    
    // Camera dimensions (viewport size)
    private int width;
    private int height;
    
    // Target position to follow
    private Vector2D target;
    
    // Camera movement parameters
    private double smoothFactor = 0.1; // Lower value = smoother camera (between 0 and 1)
    private boolean isSmooth = true;
    
    // Camera boundaries
    private int worldWidth;
    private int worldHeight;
    private boolean bounded = false;
    
    // Camera mode
    private CameraMode mode = CameraMode.FOLLOW;
    
    // For cinematic movement
    private Vector2D startPosition;
    private Vector2D endPosition;
    private double moveProgress = 0.0;
    private double moveDuration = 1000.0; // milliseconds
    
    // Original transform for restoring after camera transformations
    private AffineTransform originalTransform;
    
    /**
     * Creates a new camera with the specified viewport size.
     */
    public Camera(int width, int height) {
        this.width = width;
        this.height = height;
        this.position = new Vector2D(0, 0);
        this.target = null;
    }
    
    /**
     * Sets the camera's position.
     */
    public void setPosition(double x, double y) {
        this.position.set(x, y);
    }
    
    /**
     * Sets the camera's position.
     */
    public void setPosition(Vector2D position) {
        this.position.set(position);
    }
    
    /**
     * Sets the target for the camera to follow.
     */
    public void setTarget(Vector2D target) {
        this.target = target;
        this.mode = CameraMode.FOLLOW;
    }
    
    /**
     * Gets the target that the camera is following.
     * 
     * @return The target vector, or null if no target is set.
     */
    public Vector2D getTarget() {
        return target;
    }
    
    /**
     * Clears the camera's target.
     */
    public void clearTarget() {
        this.target = null;
        this.mode = CameraMode.FIXED;
    }
    
    /**
     * Sets the world bounds for the camera.
     */
    public void setWorldBounds(int width, int height) {
        this.worldWidth = width;
        this.worldHeight = height;
        this.bounded = true;
    }
    
    /**
     * Removes world bounds.
     */
    public void clearWorldBounds() {
        this.bounded = false;
    }
    
    /**
     * Sets the smoothness factor for camera movement.
     * Lower values make the camera movement smoother.
     * 
     * @param factor Value between 0.0 and 1.0.
     */
    public void setSmoothFactor(double factor) {
        this.smoothFactor = Math.max(0.0, Math.min(1.0, factor));
    }
    
    /**
     * Enables or disables smooth camera movement.
     */
    public void setSmoothEnabled(boolean enabled) {
        this.isSmooth = enabled;
    }
    
    /**
     * Starts a cinematic camera movement from the current position to the target position.
     * 
     * @param targetX Target X position
     * @param targetY Target Y position
     * @param duration Movement duration in milliseconds
     */
    public void moveTo(double targetX, double targetY, double duration) {
        this.startPosition = new Vector2D(position);
        this.endPosition = new Vector2D(targetX, targetY);
        this.moveDuration = duration;
        this.moveProgress = 0.0;
        this.mode = CameraMode.CINEMATIC;
    }
    
    /**
     * Updates the camera position.
     * 
     * @param deltaTime Time elapsed since the last update in milliseconds.
     */
    public void update(long deltaTime) {
        switch (mode) {
            case FOLLOW:
                updateFollowCamera(deltaTime);
                break;
            case CINEMATIC:
                updateCinematicCamera(deltaTime);
                break;
            case FIXED:
                // Fixed camera doesn't need updates
                break;
        }
        
        // Apply bounds if enabled
        if (bounded) {
            constrainToBounds();
        }
    }
    
    /**
     * Updates the camera when in follow mode.
     */
    private void updateFollowCamera(long deltaTime) {
        if (target != null) {
            // Calculate the target position (center of the screen on the target)
            double targetX = target.getX() - width / 2;
            double targetY = target.getY() - height / 2;
            
            if (isSmooth) {
                // Apply smoothing
                double deltaX = (targetX - position.getX()) * smoothFactor;
                double deltaY = (targetY - position.getY()) * smoothFactor;
                
                position.add(deltaX, deltaY);
            } else {
                // Immediate position update
                position.set(targetX, targetY);
            }
        }
    }
    
    /**
     * Updates the camera when in cinematic mode.
     */
    private void updateCinematicCamera(long deltaTime) {
        moveProgress += deltaTime / moveDuration;
        
        if (moveProgress >= 1.0) {
            // Movement complete
            position.set(endPosition);
            mode = CameraMode.FIXED;
        } else {
            // Interpolate position
            double t = moveProgress;
            // Apply easing function (ease in/out cubic)
            t = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
            
            double x = startPosition.getX() + (endPosition.getX() - startPosition.getX()) * t;
            double y = startPosition.getY() + (endPosition.getY() - startPosition.getY()) * t;
            
            position.set(x, y);
        }
    }
    
    /**
     * Constrains the camera position to the world bounds.
     */
    private void constrainToBounds() {
        if (position.getX() < 0) {
            position.setX(0);
        } else if (position.getX() + width > worldWidth) {
            position.setX(worldWidth - width);
        }
        
        if (position.getY() < 0) {
            position.setY(0);
        } else if (position.getY() + height > worldHeight) {
            position.setY(worldHeight - height);
        }
    }
    
    /**
     * Applies camera transformations to the graphics context.
     * Call this before rendering game objects.
     * 
     * @param g The graphics context
     */
    public void apply(Graphics2D g) {
        // Store the original transform
        originalTransform = g.getTransform();
        
        // Apply camera translation
        g.translate(-position.getX(), -position.getY());
    }
    
    /**
     * Restores the original graphics transform.
     * Call this after rendering game objects and before rendering UI.
     * 
     * @param g The graphics context
     */
    public void reset(Graphics2D g) {
        if (originalTransform != null) {
            g.setTransform(originalTransform);
        }
    }
    
    /**
     * Converts screen coordinates to world coordinates.
     * 
     * @param screenX X coordinate on screen
     * @param screenY Y coordinate on screen
     * @return World coordinates vector
     */
    public Vector2D screenToWorld(int screenX, int screenY) {
        return new Vector2D(
            screenX + position.getX(),
            screenY + position.getY()
        );
    }
    
    /**
     * Converts world coordinates to screen coordinates.
     * 
     * @param worldX X coordinate in world
     * @param worldY Y coordinate in world
     * @return Screen coordinates vector
     */
    public Vector2D worldToScreen(double worldX, double worldY) {
        return new Vector2D(
            worldX - position.getX(),
            worldY - position.getY()
        );
    }
    
    /**
     * Checks if a position in the world is visible on screen.
     * 
     * @param worldX X coordinate in world
     * @param worldY Y coordinate in world
     * @return True if the position is on screen
     */
    public boolean isOnScreen(double worldX, double worldY) {
        return worldX >= position.getX() && worldX <= position.getX() + width &&
               worldY >= position.getY() && worldY <= position.getY() + height;
    }
    
    /**
     * Gets the camera position.
     */
    public Vector2D getPosition() {
        return position;
    }
    
    /**
     * Gets the camera width.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the camera height.
     */
    public int getHeight() {
        return height;
    }
}

