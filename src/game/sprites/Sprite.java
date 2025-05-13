package game.sprites;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.time.Duration;

/**
 * Base sprite interface that all sprite implementations must implement.
 * Defines common operations that all sprites should support.
 */
public interface Sprite {
    /**
     * Updates the sprite animation state.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    void update(long deltaTime);
    
    /**
     * Renders the current frame.
     * 
     * @return The current frame image
     */
    BufferedImage getFrame();
    
    /**
     * Gets the size of the sprite with scaling applied.
     * 
     * @return The scaled dimensions
     */
    Dimension getSize();
    
    /**
     * Gets the original frame size without scaling.
     * 
     * @return The unscaled frame dimensions
     */
    Dimension getFrameSize();
    
    /**
     * Gets the current frame index.
     * 
     * @return Current frame index
     */
    int getFrameIndex();
    
    /**
     * Gets the total number of frames.
     * 
     * @return Total number of frames
     */
    int getTotalFrames();
    
    /**
     * Resets the animation to the beginning.
     */
    void reset();
    
    /**
     * Gets the name of the sprite.
     * 
     * @return The sprite name
     */
    String getName();
    
    /**
     * Gets the duration of the complete animation.
     * 
     * @return The animation duration
     */
    Duration getDuration();
    
    /**
     * Checks if the sprite animation has completed.
     * For looping sprites, this is typically false unless max loops is reached.
     * 
     * @return True if animation has completed, false otherwise
     */
    boolean hasCompleted();
    
    /**
     * Checks if the sprite animation loops.
     * 
     * @return True if looping, false otherwise
     */
    boolean isLooping();
    
    /**
     * Gets the horizontal scale factor.
     * 
     * @return The horizontal scale
     */
    double getScaleX();
    
    /**
     * Gets the vertical scale factor.
     * 
     * @return The vertical scale
     */
    double getScaleY();
    
    /**
     * Sets the horizontal and vertical scale factors.
     * 
     * @param scaleX Horizontal scale factor
     * @param scaleY Vertical scale factor
     */
    void setScale(double scaleX, double scaleY);
    
    /**
     * Gets the X offset for positioning.
     * 
     * @return The X offset
     */
    int getOffsetX();
    
    /**
     * Gets the Y offset for positioning.
     * 
     * @return The Y offset
     */
    int getOffsetY();
    
    /**
     * Sets the position offsets.
     * 
     * @param offsetX X offset
     * @param offsetY Y offset
     */
    void setOffset(int offsetX, int offsetY);
    
    /**
     * Sets the X position offset.
     * 
     * @param offsetX X offset
     */
    void setOffsetX(int offsetX);
    
    /**
     * Sets the Y position offset.
     * 
     * @param offsetY Y offset
     */
    void setOffsetY(int offsetY);
    
    /**
     * Gets the properly centered render position for X coordinate.
     * 
     * @param entityX The entity's X position
     * @return The X position for rendering
     */
    int getRenderX(double entityX);
    
    /**
     * Gets the properly centered render position for Y coordinate.
     * 
     * @param entityY The entity's Y position
     * @param collisionHeight The entity's collision height
     * @return The Y position for rendering
     */
    int getRenderY(double entityY, int collisionHeight);
}