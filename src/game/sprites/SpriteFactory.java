package game.sprites;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * SpriteFactory creates sprites from various sources (sheets or sequences).
 * This replaces the more specialized classes with a factory pattern.
 */
public class SpriteFactory {
    
    /**
     * Creates a sprite from a sprite sheet.
     * 
     * @param name Sprite name
     * @param sheet Sprite sheet image
     * @param frameSize Size of each frame
     * @param scaleX Horizontal scale factor
     * @param scaleY Vertical scale factor
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param firstFrame Index of first frame
     * @param frameCount Number of frames to extract
     * @param duration Animation duration
     * @param looping Whether the animation should loop
     * @return The created sprite
     */
    public static Sprite fromSpriteSheet(
            String name,
            BufferedImage sheet,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            int firstFrame,
            int frameCount,
            Duration duration,
            boolean looping) {
            
        if (sheet == null) {
            return createErrorSprite(name, frameSize);
        }
        
        // Calculate the sheet layout
        int cols = Math.max(1, sheet.getWidth() / frameSize.width);
        int rows = Math.max(1, sheet.getHeight() / frameSize.height);
        int totalFramesInSheet = cols * rows;
        
        // Validate frames
        int lastFrameIndex = firstFrame + frameCount - 1;
        if (lastFrameIndex >= totalFramesInSheet) {
            System.err.println("Warning: Sprite '" + name + "' requests frames beyond sheet bounds!");
            frameCount = Math.max(1, totalFramesInSheet - firstFrame);
        }
        
        // Extract frames from sheet
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            int frameIndex = firstFrame + i;
            int col = frameIndex % cols;
            int row = frameIndex / cols;
            
            int x = col * frameSize.width;
            int y = row * frameSize.height;
            
            // Make sure we don't exceed sheet bounds
            if (x + frameSize.width <= sheet.getWidth() && 
                y + frameSize.height <= sheet.getHeight()) {
                BufferedImage frame = sheet.getSubimage(x, y, frameSize.width, frameSize.height);
                frames.add(frame);
            } else {
                // Add error frame if out of bounds
                frames.add(createErrorFrame(frameSize));
            }
        }
        
        // Create sequence sprite with extracted frames
        return new SequenceSprite(name, frames, frameSize, scaleX, scaleY, offsetX, offsetY, duration, looping);
    }
    
    /**
     * Creates a sprite from a sprite sheet with uniform scaling.
     */
    public static Sprite fromSpriteSheet(
            String name,
            BufferedImage sheet,
            Dimension frameSize,
            double scale,
            int firstFrame,
            int frameCount,
            Duration duration,
            boolean looping) {
            
        return fromSpriteSheet(name, sheet, frameSize, scale, scale, 0, 0, 
                              firstFrame, frameCount, duration, looping);
    }
    
    /**
     * Creates a sprite from a list of frame images.
     * 
     * @param name Sprite name
     * @param frames List of frame images
     * @param frameSize Size of each frame
     * @param scaleX Horizontal scale factor
     * @param scaleY Vertical scale factor
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param duration Animation duration
     * @param looping Whether the animation should loop
     * @return The created sprite
     */
    public static Sprite fromFrameList(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scaleX,
            double scaleY,
            int offsetX,
            int offsetY,
            Duration duration,
            boolean looping) {
            
        if (frames == null || frames.isEmpty()) {
            return createErrorSprite(name, frameSize);
        }
        
        return new SequenceSprite(name, frames, frameSize, scaleX, scaleY, offsetX, offsetY, duration, looping);
    }
    
    /**
     * Creates a sprite from a list of frame images with uniform scaling.
     */
    public static Sprite fromFrameList(
            String name,
            List<BufferedImage> frames,
            Dimension frameSize,
            double scale,
            Duration duration,
            boolean looping) {
            
        return fromFrameList(name, frames, frameSize, scale, scale, 0, 0, duration, looping);
    }
    
    /**
     * Creates an error sprite when loading fails.
     */
    private static Sprite createErrorSprite(String name, Dimension frameSize) {
        List<BufferedImage> errorFrames = new ArrayList<>();
        errorFrames.add(createErrorFrame(frameSize));
        return new SequenceSprite(name, errorFrames, frameSize, 1.0, 1.0, 0, 0, 
                                 Duration.ofMillis(1000), false);
    }
    
    /**
     * Creates an error frame with the given dimensions.
     */
    private static BufferedImage createErrorFrame(Dimension frameSize) {
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
        
        // Add checkerboard pattern for better visibility
        g.setColor(new java.awt.Color(255, 0, 255, 128));
        for (int x = 0; x < frameSize.width; x += 8) {
            for (int y = 0; y < frameSize.height; y += 8) {
                if ((x / 8 + y / 8) % 2 == 0) {
                    g.fillRect(x, y, 8, 8);
                }
            }
        }
        
        g.dispose();
        return errorFrame;
    }
}