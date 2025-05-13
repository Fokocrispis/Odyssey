package game.sprites;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;

/**
 * Sprite class that supports per-frame offsets and dimensions
 */
public class FrameOffsetAndSizeSequenceSprite extends FrameOffsetSequenceSprite {
    protected final Dimension[] frameSizes;
    
    /**
     * Creates a new sprite with uniform scale but different frame sizes
     */
    public FrameOffsetAndSizeSequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension[] frameSizes,
            double scale,
            Point[] frameOffsets,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSizes[0], scale, frameOffsets, duration, looping);
        this.frameSizes = frameSizes;
    }
    
    /**
     * Creates a new sprite with different X/Y scales and frame sizes
     */
    public FrameOffsetAndSizeSequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension[] frameSizes,
            double scaleX,
            double scaleY,
            Point[] frameOffsets,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSizes[0], scaleX, scaleY, frameOffsets, duration, looping);
        this.frameSizes = frameSizes;
    }
    
    /**
     * Creates a new sprite with global offset and different frame sizes
     */
    public FrameOffsetAndSizeSequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension[] frameSizes,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
        
        super(name, frames, frameSizes[0], scaleX, scaleY, offsetX, offsetY, duration, looping);
        this.frameSizes = frameSizes;
    }
    
    /**
     * Gets the size of the current frame with scaling applied
     */
    @Override
    public Dimension getSize() {
        Dimension currentFrameSize = getCurrentFrameSize();
        return new Dimension(
            (int)(currentFrameSize.width * scaleX),
            (int)(currentFrameSize.height * scaleY)
        );
    }
    
    /**
     * Gets the original size of the current frame without scaling
     */
    protected Dimension getCurrentFrameSize() {
        if (frameIndex >= 0 && frameIndex < frameSizes.length) {
            return frameSizes[frameIndex];
        }
        return frameSize; // Fall back to default frame size
    }
    
    /**
     * Gets the properly centered render position for X coordinate with current frame offset
     * Accounts for varying frame sizes
     */
    @Override
    public int getRenderX(double entityX) {
        Point offset = getCurrentFrameOffset();
        Dimension currentSize = getSize();
        return (int)(entityX - currentSize.width / 2.0) + offset.x;
    }
    
    /**
     * Gets the properly centered render position for Y coordinate with current frame offset
     * Accounts for varying frame sizes
     */
    @Override
    public int getRenderY(double entityY, int collisionHeight) {
        Point offset = getCurrentFrameOffset();
        Dimension currentSize = getSize();
        // Align sprite bottom with collision bottom
        int spriteBottom = (int)(entityY + collisionHeight / 2.0);
        return spriteBottom - currentSize.height + offset.y;
    }
}