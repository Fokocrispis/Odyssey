package game.sprites;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Sprite that handles sequences of individual frames.
 * This is the core sprite class that handles all sprite functionality.
 */
public class SequenceSprite implements Sprite {
    protected final String name;
    protected final List<BufferedImage> frames;
    protected final Dimension frameSize;
    protected final Duration duration;
    protected final boolean looping;
    
    // These are no longer final so they can be adjusted
    protected double scaleX;
    protected double scaleY;
    protected int offsetX;
    protected int offsetY;
    
    protected long timeElapsed;
    protected int frameIndex;
    protected double timePerFrame;
    protected boolean completed = false;
    protected int currentLoop = 0;
    protected int maxLoops = -1; // -1 for infinite loops

    /**
     * Creates a new sprite sequence.
     */
    public SequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
        
        this.name = name;
        this.frames = new ArrayList<>(frames);
        this.frameSize = new Dimension(frameSize);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.duration = duration;
        this.looping = looping;
        
        this.timeElapsed = 0;
        this.frameIndex = 0;
        
        // Calculate time per frame
        int frameCount = Math.max(1, frames.size());
        this.timePerFrame = (double) duration.toMillis() / frameCount;
    }

    /**
     * Creates a new sprite sequence with uniform scaling.
     */
    public SequenceSprite(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scale,
            Duration duration,
            boolean looping) {
        
        this(name, frames, frameSize, scale, scale, 0, 0, duration, looping);
    }

    @Override
    public void update(long deltaTime) {
        if (completed) return;
        
        // Store current frame for loop detection
        int previousFrame = frameIndex;
        
        timeElapsed += deltaTime;
        
        // Update frame index
        if (timePerFrame > 0) {
            int newFrameIndex = (int)(timeElapsed / timePerFrame);
            
            if (looping) {
                // Handle looping - calculate the correct frame within loop bounds
                frameIndex = newFrameIndex % frames.size();
                
                // Check if we've completed a loop
                if (previousFrame > frameIndex && maxLoops > 0) {
                    currentLoop++;
                    if (currentLoop >= maxLoops) {
                        completed = true;
                        // Stay on last frame
                        frameIndex = frames.size() - 1;
                    }
                }
            } else {
                // Non-looping - clamp to last frame
                frameIndex = Math.min(newFrameIndex, frames.size() - 1);
                
                // Check if animation is complete
                if (frameIndex == frames.size() - 1) {
                    completed = true;
                }
            }
        }
    }

    @Override
    public BufferedImage getFrame() {
        if (frames.isEmpty()) {
            return createErrorFrame();
        }
        
        // Ensure frame index is valid
        if (frameIndex < 0 || frameIndex >= frames.size()) {
            return frames.get(0);
        }
        
        return frames.get(frameIndex);
    }

    @Override
    public Dimension getSize() {
        return new Dimension(
            (int)(frameSize.width * scaleX),
            (int)(frameSize.height * scaleY)
        );
    }

    @Override
    public Dimension getFrameSize() {
        return new Dimension(frameSize);
    }

    @Override
    public int getFrameIndex() {
        return frameIndex;
    }

    @Override
    public int getTotalFrames() {
        return frames.size();
    }

    @Override
    public void reset() {
        frameIndex = 0;
        timeElapsed = 0;
        completed = false;
        currentLoop = 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean hasCompleted() {
        return completed;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public double getScaleX() {
        return scaleX;
    }

    @Override
    public double getScaleY() {
        return scaleY;
    }

    @Override
    public void setScale(double scaleX, double scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    @Override
    public int getOffsetX() {
        return offsetX;
    }

    @Override
    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    @Override
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    @Override
    public int getRenderX(double entityX) {
        return (int)(entityX - getSize().width / 2.0) + offsetX;
    }

    @Override
    public int getRenderY(double entityY, int collisionHeight) {
        // Align sprite bottom with collision bottom
        int spriteBottom = (int)(entityY + collisionHeight / 2.0);
        return spriteBottom - getSize().height + offsetY;
    }

    /**
     * Sets the maximum number of loops before the animation stops.
     *
     * @param maxLoops Number of loops, or -1 for infinite
     */
    public void setMaxLoops(int maxLoops) {
        this.maxLoops = maxLoops;
    }

    /**
     * Gets the current loop count.
     *
     * @return The current loop count
     */
    public int getCurrentLoop() {
        return currentLoop;
    }

    /**
     * Creates an error frame for when loading fails.
     *
     * @return A placeholder error frame
     */
    protected BufferedImage createErrorFrame() {
        BufferedImage errorFrame = new BufferedImage(
                frameSize.width, frameSize.height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = errorFrame.createGraphics();
        
        // Fill with pink to indicate error
        g.setColor(java.awt.Color.MAGENTA);
        g.fillRect(0, 0, frameSize.width, frameSize.height);
        
        // Add error text
        g.setColor(java.awt.Color.BLACK);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 8));
        String errorText = "ERROR";
        g.drawString(errorText, frameSize.width / 4, frameSize.height / 2);
        
        g.dispose();
        return errorFrame;
    }

    /**
     * Gets debug information about the sprite.
     *
     * @return Debug information string
     */
    public String getDebugInfo() {
        return String.format("Sprite '%s': frame %d/%d, %dx%d (scale %.1f,%.1f), offset (%d,%d), %s",
            name, frameIndex + 1, frames.size(),
            getSize().width, getSize().height,
            scaleX, scaleY,
            offsetX, offsetY,
            looping ? "looping" : "non-looping");
    }
}