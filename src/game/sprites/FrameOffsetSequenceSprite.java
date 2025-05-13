package game.sprites;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprite class that supports per-frame offsets
 */
public class FrameOffsetSequenceSprite extends SequenceSprite {
    protected final Point[] frameOffsets;
    
    /**
     * Creates a new sprite with uniform scale for X and Y
     */
    public FrameOffsetSequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scale,
            Point[] frameOffsets,
            Duration duration,
            boolean looping) {
        
        this(name, frames, frameSize, scale, scale, 0, 0, duration, looping);
    }
    
    /**
     * Creates a new sprite with separate X and Y scales and global offset
     */
    public FrameOffsetSequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSize, scaleX, scaleY, offsetX, offsetY, duration, looping);
        
        // Create array of offsets with the global offset applied
        this.frameOffsets = new Point[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            this.frameOffsets[i] = new Point(offsetX, offsetY);
        }
    }
    
    /**
     * Creates a new sprite with per-frame offsets
     */
    public FrameOffsetSequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            Point[] frameOffsets,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSize, scaleX, scaleY, 0, 0, duration, looping);
        this.frameOffsets = frameOffsets;
    }
    
    /**
     * Gets the offset for the current frame
     */
    public Point getCurrentFrameOffset() {
        if (frameIndex >= 0 && frameIndex < frameOffsets.length) {
            return frameOffsets[frameIndex];
        }
        return new Point(offsetX, offsetY); // Fall back to global offset
    }
    
    /**
     * Gets the properly centered render position for X coordinate with current frame offset
     */
    @Override
    public int getRenderX(double entityX) {
        Point offset = getCurrentFrameOffset();
        return (int)(entityX - getSize().width / 2.0) + offset.x;
    }
    
    /**
     * Gets the properly centered render position for Y coordinate with current frame offset
     */
    @Override
    public int getRenderY(double entityY, int collisionHeight) {
        Point offset = getCurrentFrameOffset();
        // Align sprite bottom with collision bottom
        int spriteBottom = (int)(entityY + collisionHeight / 2.0);
        return spriteBottom - getSize().height + offset.y;
    }
}