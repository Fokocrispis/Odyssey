package game.sprites;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import game.input.KeyboardInput;

/**
 * Interactive tool for adjusting sprite size and position in real-time.
 * Allows designers to fine-tune sprite appearance during gameplay.
 */
public class SpriteAdjustmentTool {
    private static SpriteAdjustmentTool instance;
    
    // Use instance variable instead of final
    private SpriteSequenceManager sequenceManager;
    private String currentSpriteId;
    private int adjustSpeed = 1;
    private boolean enabled = false;
    
    /**
     * Gets the singleton instance.
     */
    public static SpriteAdjustmentTool getInstance() {
        if (instance == null) {
            instance = new SpriteAdjustmentTool();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private SpriteAdjustmentTool() {
        // Initialize with a default sequence manager
        this.sequenceManager = new SpriteSequenceManager();
    }
    
    /**
     * Sets the sprite sequence manager to use.
     */
    public void setSequenceManager(SpriteSequenceManager manager) {
        if (manager != null) {
            this.sequenceManager = manager;
        }
    }
    
    /**
     * Enables or disables the adjustment tool.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("Sprite adjustment tool " + (enabled ? "enabled" : "disabled"));
        
        if (enabled && currentSpriteId != null) {
            printCurrentAdjustment();
        }
    }
    
    /**
     * Toggles the adjustment tool.
     */
    public void toggle() {
        setEnabled(!enabled);
    }
    
    /**
     * Checks if the tool is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets the current sprite to adjust.
     */
    public void setCurrentSprite(String spriteId) {
        if (spriteId == null) return;
        
        this.currentSpriteId = spriteId;
        if (enabled) {
            printCurrentAdjustment();
        }
    }
    
    /**
     * Processes keyboard input for adjustments.
     */
    public void processInput(KeyboardInput input) {
        if (!enabled || currentSpriteId == null) return;
        
        // Get current adjustment
        SpriteAdjustment adjustment = sequenceManager.getAdjustment(currentSpriteId);
        if (adjustment == null) return;
        
        // Determine adjustment speed
        adjustSpeed = input.isKeyPressed(KeyEvent.VK_SHIFT) ? 5 : 1;
        
        // Get current values
        Dimension displaySize = adjustment.getDisplaySize();
        double scaleX = adjustment.getScaleX();
        double scaleY = adjustment.getScaleY();
        int offsetX = adjustment.getOffsetX();
        int offsetY = adjustment.getOffsetY();
        
        boolean changed = false;
        
        // Size/scale adjustments
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD4)) {
            if (displaySize != null) {
                displaySize.width -= adjustSpeed;
                changed = true;
            } else {
                scaleX -= 0.05 * adjustSpeed;
                changed = true;
            }
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD6)) {
            if (displaySize != null) {
                displaySize.width += adjustSpeed;
                changed = true;
            } else {
                scaleX += 0.05 * adjustSpeed;
                changed = true;
            }
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD8)) {
            if (displaySize != null) {
                displaySize.height -= adjustSpeed;
                changed = true;
            } else {
                scaleY -= 0.05 * adjustSpeed;
                changed = true;
            }
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD2)) {
            if (displaySize != null) {
                displaySize.height += adjustSpeed;
                changed = true;
            } else {
                scaleY += 0.05 * adjustSpeed;
                changed = true;
            }
        }
        
        // Offset adjustments
        if (input.isKeyJustPressed(KeyEvent.VK_LEFT)) {
            offsetX -= adjustSpeed;
            changed = true;
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_RIGHT)) {
            offsetX += adjustSpeed;
            changed = true;
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_UP)) {
            offsetY -= adjustSpeed;
            changed = true;
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            offsetY += adjustSpeed;
            changed = true;
        }
        
        // Reset values
        if (input.isKeyJustPressed(KeyEvent.VK_R)) {
            if (displaySize != null) {
                Sprite sprite = sequenceManager.getSprite(currentSpriteId);
                if (sprite != null) {
                    Dimension frameSize = sprite.getFrameSize();
                    displaySize.width = (int)(frameSize.width * 3.0);
                    displaySize.height = (int)(frameSize.height * 3.0);
                }
            } else {
                scaleX = 3.0;
                scaleY = 3.0;
            }
            offsetX = 0;
            offsetY = 0;
            changed = true;
        }
        
        // Toggle between scale and size modes
        if (input.isKeyJustPressed(KeyEvent.VK_S)) {
            Sprite sprite = sequenceManager.getSprite(currentSpriteId);
            if (sprite != null) {
                if (displaySize == null) {
                    // Switch to size mode
                    Dimension frameSize = sprite.getFrameSize();
                    displaySize = new Dimension(
                        (int)(frameSize.width * scaleX),
                        (int)(frameSize.height * scaleY)
                    );
                    adjustment.setDisplaySize(displaySize);
                } else {
                    // Switch to scale mode
                    Dimension frameSize = sprite.getFrameSize();
                    scaleX = (double)displaySize.width / frameSize.width;
                    scaleY = (double)displaySize.height / frameSize.height;
                    adjustment.setDisplaySize(null);
                    adjustment.setScaleX(scaleX);
                    adjustment.setScaleY(scaleY);
                }
                changed = true;
            }
        }
        
        // Print current adjustment
        if (input.isKeyJustPressed(KeyEvent.VK_P) || changed) {
            printCurrentAdjustment();
        }
        
        // Generate code
        if (input.isKeyJustPressed(KeyEvent.VK_G)) {
            printAdjustmentCode();
        }
     // Update adjustment if changed
        if (changed) {
            if (displaySize != null) {
                adjustment.setDisplaySize(displaySize);
            } else {
                adjustment.setScaleX(scaleX);
                adjustment.setScaleY(scaleY);
            }
            adjustment.setOffsetX(offsetX);
            adjustment.setOffsetY(offsetY);
            
            // Apply changes to sprite
            Sprite sprite = sequenceManager.getSprite(currentSpriteId);
            if (sprite != null) {
                adjustment.applyTo(sprite, sprite.getFrameSize());
            }
        }
    }
    
    /**
     * Prints the current adjustment values.
     */
    private void printCurrentAdjustment() {
        if (currentSpriteId == null) return;
        
        SpriteAdjustment adjustment = sequenceManager.getAdjustment(currentSpriteId);
        if (adjustment == null) {
            System.out.println("No adjustment found for: " + currentSpriteId);
            return;
        }
        
        Sprite sprite = sequenceManager.getSprite(currentSpriteId);
        if (sprite == null) {
            System.out.println("No sprite found for: " + currentSpriteId);
            return;
        }
        
        System.out.println("\n=== Sprite Adjustment: " + currentSpriteId + " ===");
        if (adjustment.getDisplaySize() != null) {
            System.out.println("Mode: Display Size");
            System.out.println("Display size: " + adjustment.getDisplaySize().width + "x" + 
                              adjustment.getDisplaySize().height);
        } else {
            System.out.println("Mode: Scale Factors");
            System.out.println("Scale: " + String.format("%.2f", adjustment.getScaleX()) + "," + 
                              String.format("%.2f", adjustment.getScaleY()));
        }
        
        System.out.println("Offset: (" + adjustment.getOffsetX() + ", " + 
                          adjustment.getOffsetY() + ")");
        System.out.println("Frame size: " + sprite.getFrameSize().width + "x" + 
                          sprite.getFrameSize().height);
        System.out.println("Rendered size: " + sprite.getSize().width + "x" + 
                          sprite.getSize().height);
        System.out.println("\nControls:");
        System.out.println("  Numpad 4/6: Adjust width or horizontal scale");
        System.out.println("  Numpad 8/2: Adjust height or vertical scale");
        System.out.println("  Arrow keys: Adjust offset position");
        System.out.println("  Shift: Increase adjustment speed");
        System.out.println("  R: Reset to default values");
        System.out.println("  S: Toggle between size and scale modes");
        System.out.println("  P: Print current values");
        System.out.println("  G: Generate code");
    }
    
    /**
     * Generates code for the current adjustments.
     */
    public void printAdjustmentCode() {
        if (currentSpriteId == null) return;
        
        SpriteAdjustment adjustment = sequenceManager.getAdjustment(currentSpriteId);
        if (adjustment == null) return;
        
        System.out.println("\n=== Adjustment Code ===");
        if (adjustment.getDisplaySize() != null) {
            System.out.println("// " + currentSpriteId + " display size and offset");
            System.out.println("registerAdjustment(\"" + currentSpriteId + "\", " +
                "new Dimension(" + adjustment.getDisplaySize().width + ", " + 
                adjustment.getDisplaySize().height + "), " +
                adjustment.getOffsetX() + ", " + adjustment.getOffsetY() + ");");
        } else {
            System.out.println("// " + currentSpriteId + " scale and offset");
            System.out.println(String.format("registerAdjustment(\"%s\", %.2f, %.2f, %d, %d);",
                currentSpriteId, adjustment.getScaleX(), adjustment.getScaleY(),
                adjustment.getOffsetX(), adjustment.getOffsetY()));
        }
    }
    
    /**
     * Generates code for all registered adjustments.
     */
    public void generateAllAdjustmentCode() {
        System.out.println("\n=== All Sprite Adjustments ===");
        System.out.println("// Add these to the createPlayerSprites() method:");
        
        for (SpriteAdjustment adjustment : sequenceManager.getAllAdjustments()) {
            if (adjustment.getDisplaySize() != null) {
                System.out.println("registerAdjustment(\"" + adjustment.getSpriteId() + "\", " +
                    "new Dimension(" + adjustment.getDisplaySize().width + ", " + 
                    adjustment.getDisplaySize().height + "), " +
                    adjustment.getOffsetX() + ", " + adjustment.getOffsetY() + ");");
            } else {
                System.out.println(String.format("registerAdjustment(\"%s\", %.2f, %.2f, %d, %d);",
                    adjustment.getSpriteId(), adjustment.getScaleX(), adjustment.getScaleY(),
                    adjustment.getOffsetX(), adjustment.getOffsetY()));
            }
        }
    }
    
    /**
     * Adds a sprite adjustment.
     */
    public void addSpriteAdjustment(String name, String path, String prefix, int frameCount,
                                  Dimension sourceSize, Dimension displaySize, 
                                  int offsetX, int offsetY, int durationMs) {
        
        // Create adjustment
        SpriteAdjustment adjustment = new SpriteAdjustment(name, displaySize, offsetX, offsetY);
        
        // Register with sequence manager
        if (sequenceManager != null) {
            sequenceManager.registerAdjustment(name, displaySize, offsetX, offsetY);
        }
    }
}