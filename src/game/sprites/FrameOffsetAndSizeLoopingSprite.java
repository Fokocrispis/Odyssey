package game.sprites;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;

/**
 * Sprite class that supports per-frame offsets, dimensions, and looping
 */
public class FrameOffsetAndSizeLoopingSprite extends FrameOffsetAndSizeSequenceSprite {
    // Remove duplicate shouldLoop field since it's inherited from parent
    private boolean hasCompleted = false;
    private int maxLoops = -1; // -1 for infinite loops
    private int currentLoops = 0;
    private int lastFrameIndex = -1;
    
    /**
     * Creates a new looping sprite with uniform scale but different frame sizes
     */
    public FrameOffsetAndSizeLoopingSprite(
            String name,
            List<BufferedImage> frames,
            Dimension[] frameSizes,
            double scale,
            Point[] frameOffsets,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSizes, scale, frameOffsets, duration, looping);
    }
    
    /**
     * Creates a new looping sprite with different X/Y scales and frame sizes
     */
    public FrameOffsetAndSizeLoopingSprite(
            String name,
            List<BufferedImage> frames,
            Dimension[] frameSizes,
            double scaleX,
            double scaleY,
            Point[] frameOffsets,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSizes, scaleX, scaleY, frameOffsets, duration, looping);
    }
    
    /**
     * Creates a new looping sprite with global offset and different frame sizes
     */
    public FrameOffsetAndSizeLoopingSprite(
            String name,
            List<BufferedImage> frames,
            Dimension[] frameSizes,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSizes, scaleX, scaleY, offsetX, offsetY, duration, looping);
    }
    
    @Override
    public void update(long deltaTime) {
        if (!isLooping() && hasCompleted) {
            return; // Don't update if non-looping and completed
        }
        
        // Store current frame index before update
        int currentFrameIndex = frameIndex;
        
        super.update(deltaTime);
        
        // Check if we've completed a loop by detecting when we cycle back to frame 0
        if (isLooping() && lastFrameIndex != -1 && 
            frameIndex == 0 && lastFrameIndex != 0) {
            
            if (maxLoops > 0) {
                currentLoops++;
                if (currentLoops >= maxLoops) {
                    hasCompleted = true;
                }
            }
        }
        
        // Check if we've completed a non-looping animation
        if (!isLooping() && isAtLastFrame()) {
            hasCompleted = true;
        }
        
        lastFrameIndex = currentFrameIndex;
    }
    
    @Override
    public void reset() {
        super.reset();
        hasCompleted = false;
        currentLoops = 0;
        lastFrameIndex = -1;
    }
    
    /**
     * Checks if the sprite is at the last frame
     */
    private boolean isAtLastFrame() {
        return frameIndex == (frames.size() - 1);
    }
    
    /**
     * Returns whether this sprite has completed its animation
     */
    @Override
    public boolean hasCompleted() {
        return hasCompleted;
    }
    
    /**
     * Sets the maximum number of loops
     * @param maxLoops Number of loops, or -1 for infinite
     */
    public void setMaxLoops(int maxLoops) {
        this.maxLoops = maxLoops;
    }
    
    /**
     * Gets the current loop count
     */
    public int getCurrentLoops() {
        return currentLoops;
    }
}