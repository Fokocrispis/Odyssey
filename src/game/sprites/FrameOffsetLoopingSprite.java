package game.sprites;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;

/**
 * Sprite class that supports per-frame offsets and looping
 */
public class FrameOffsetLoopingSprite extends FrameOffsetSequenceSprite {
    private final boolean shouldLoop;
    private boolean hasCompleted = false;
    private int maxLoops = -1; // -1 for infinite loops
    private int currentLoops = 0;
    private int lastFrameIndex = -1;
    
    /**
     * Creates a new looping sprite with uniform scale
     */
    public FrameOffsetLoopingSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scale,
            Point[] frameOffsets,
            Duration duration,
            boolean shouldLoop) {
        super(name, frames, frameSize, scale, frameOffsets, duration, shouldLoop);
        this.shouldLoop = shouldLoop;
    }
    
    /**
     * Creates a new looping sprite with separate X and Y scales and global offset
     */
    public FrameOffsetLoopingSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean shouldLoop) {
        super(name, frames, frameSize, scaleX, scaleY, offsetX, offsetY, duration, shouldLoop);
        this.shouldLoop = shouldLoop;
    }
    
    /**
     * Creates a new looping sprite with per-frame offsets
     */
    public FrameOffsetLoopingSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            Point[] frameOffsets,
            Duration duration,
            boolean shouldLoop) {
        super(name, frames, frameSize, scaleX, scaleY, frameOffsets, duration, shouldLoop);
        this.shouldLoop = shouldLoop;
    }
    
    @Override
    public void update(long deltaTime) {
        if (!shouldLoop && hasCompleted) {
            return; // Don't update if non-looping and completed
        }
        
        // Store current frame index before update
        int currentFrameIndex = frameIndex;
        
        super.update(deltaTime);
        
        // Check if we've completed a loop by detecting when we cycle back to frame 0
        if (shouldLoop && lastFrameIndex != -1 && 
            frameIndex == 0 && lastFrameIndex != 0) {
            
            if (maxLoops > 0) {
                currentLoops++;
                if (currentLoops >= maxLoops) {
                    hasCompleted = true;
                }
            }
        }
        
        // Check if we've completed a non-looping animation
        if (!shouldLoop && isAtLastFrame()) {
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