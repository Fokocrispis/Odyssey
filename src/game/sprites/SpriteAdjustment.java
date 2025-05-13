package game.sprites;

import java.awt.Dimension;

/**
 * Stores adjustment settings for sprite animations.
 * Used to consistently apply the same adjustments when reloading sprites.
 */
public class SpriteAdjustment {
    private final String spriteId;
    private double scaleX;
    private double scaleY;
    private int offsetX;
    private int offsetY;
    private Dimension displaySize;
    
    /**
     * Creates a new sprite adjustment.
     */
    public SpriteAdjustment(String spriteId) {
        this.spriteId = spriteId;
        this.scaleX = 1.0;
        this.scaleY = 1.0;
        this.offsetX = 0;
        this.offsetY = 0;
        this.displaySize = null; // Will be calculated from scale if null
    }
    
    /**
     * Creates a new sprite adjustment with initial values.
     */
    public SpriteAdjustment(String spriteId, double scaleX, double scaleY, int offsetX, int offsetY) {
        this.spriteId = spriteId;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.displaySize = null;
    }
    
    /**
     * Creates a new sprite adjustment with display size.
     */
    public SpriteAdjustment(String spriteId, Dimension displaySize, int offsetX, int offsetY) {
        this.spriteId = spriteId;
        this.displaySize = new Dimension(displaySize);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        // Scale will be calculated when applied
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }
    
    public String getSpriteId() {
        return spriteId;
    }
    
    public double getScaleX() {
        return scaleX;
    }
    
    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }
    
    public double getScaleY() {
        return scaleY;
    }
    
    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }
    
    public int getOffsetX() {
        return offsetX;
    }
    
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }
    
    public int getOffsetY() {
        return offsetY;
    }
    
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
    
    public Dimension getDisplaySize() {
        return displaySize != null ? new Dimension(displaySize) : null;
    }
    
    public void setDisplaySize(Dimension displaySize) {
        this.displaySize = displaySize != null ? new Dimension(displaySize) : null;
    }
    
    /**
     * Applies the adjustment to a sprite.
     * 
     * @param sprite The sprite to adjust
     * @param sourceFrameSize The original frame size, used if display size is set
     */
    public void applyTo(Sprite sprite, Dimension sourceFrameSize) {
        if (sprite == null) return;
        
        // If display size is set, calculate scale from it
        if (displaySize != null && sourceFrameSize != null) {
            scaleX = (double)displaySize.width / sourceFrameSize.width;
            scaleY = (double)displaySize.height / sourceFrameSize.height;
        }
        
        sprite.setScale(scaleX, scaleY);
        sprite.setOffset(offsetX, offsetY);
    }
    
    /**
     * Creates a string representation for debug purposes.
     */
    @Override
    public String toString() {
        if (displaySize != null) {
            return String.format("Adjustment[%s]: size=%dx%d, offset=(%d,%d)", 
                spriteId, displaySize.width, displaySize.height, offsetX, offsetY);
        } else {
            return String.format("Adjustment[%s]: scale=(%.2f,%.2f), offset=(%d,%d)", 
                spriteId, scaleX, scaleY, offsetX, offsetY);
        }
    }
}