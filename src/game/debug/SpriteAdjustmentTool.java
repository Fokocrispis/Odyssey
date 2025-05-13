package game.debug;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import game.sprites.SpriteSequenceManager;
import game.sprites.Sprite;

/**
 * A tool to help adjust sprite dimensions and positions.
 * Allows real-time modification of sprite parameters using keyboard input.
 */
public class SpriteAdjustmentTool {
    // Singleton instance
    private static SpriteAdjustmentTool instance;
    
    // Sprite adjustment data
    private final Map<String, SpriteAdjustment> adjustments = new HashMap<>();
    private final SpriteSequenceManager sequenceManager = new SpriteSequenceManager();
    
    // Current sprite being adjusted
    private String currentAdjustmentName = null;
    private boolean enabled = false;
    
    // Adjustment parameters
    private int adjustSpeed = 1;
    
    /**
     * Gets the singleton instance
     */
    public static SpriteAdjustmentTool getInstance() {
        if (instance == null) {
            instance = new SpriteAdjustmentTool();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern
     */
    private SpriteAdjustmentTool() {
        // Initialize standard adjustments
        initStandardAdjustments();
    }
    
    /**
     * Initialize standard sprite adjustments
     */
    private void initStandardAdjustments() {
        // Add default sprite adjustments
        addSpriteAdjustment("walk", "Walk/Walking", "Walking", 18, 
                           new Dimension(64, 64), new Dimension(190, 160), 0, 0, 850);
        
        // More animations can be added here as needed
    }
    
    /**
     * Register a sprite for adjustment
     */
    public void addSpriteAdjustment(String name, String path, String prefix, int frameCount,
                                   Dimension sourceSize, Dimension displaySize, 
                                   int offsetX, int offsetY, int durationMs) {
        
        SpriteAdjustment adjustment = new SpriteAdjustment(
            name, path, prefix, frameCount, sourceSize, displaySize, offsetX, offsetY, durationMs
        );
        
        adjustments.put(name, adjustment);
        
        // If no current adjustment, set this as current
        if (currentAdjustmentName == null) {
            currentAdjustmentName = name;
        }
    }
    
    /**
     * Enable/disable the adjustment tool
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("Sprite adjustment tool is now " + (enabled ? "ENABLED" : "DISABLED"));
        
        if (enabled && currentAdjustmentName != null) {
            printCurrentAdjustment();
        }
    }
    
    /**
     * Toggle the adjustment tool state
     */
    public void toggle() {
        setEnabled(!enabled);
    }
    
    /**
     * Check if the tool is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set the current sprite being adjusted
     */
    public void setCurrentAdjustment(String name) {
        if (adjustments.containsKey(name)) {
            currentAdjustmentName = name;
            System.out.println("Now adjusting: " + name);
            
            if (enabled) {
                printCurrentAdjustment();
            }
        } else {
            System.err.println("No adjustment found for: " + name);
        }
    }
    
    /**
     * Process input to adjust the current sprite
     */
    public void processInput(game.input.KeyboardInput input) {
        if (!enabled || currentAdjustmentName == null) return;
        
        SpriteAdjustment adjustment = adjustments.get(currentAdjustmentName);
        if (adjustment == null) return;
        
        // Handle fine/coarse adjustment speed
        if (input.isKeyPressed(KeyEvent.VK_SHIFT)) {
            adjustSpeed = 5;
        } else {
            adjustSpeed = 1;
        }
        
        // Width adjustment
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD4)) {
            adjustment.displayWidth -= adjustSpeed;
            reloadCurrentSprite();
        }
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD6)) {
            adjustment.displayWidth += adjustSpeed;
            reloadCurrentSprite();
        }
        
        // Height adjustment
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD8)) {
            adjustment.displayHeight -= adjustSpeed;
            reloadCurrentSprite();
        }
        if (input.isKeyJustPressed(KeyEvent.VK_NUMPAD2)) {
            adjustment.displayHeight += adjustSpeed;
            reloadCurrentSprite();
        }
        
        // X offset adjustment
        if (input.isKeyJustPressed(KeyEvent.VK_LEFT)) {
            adjustment.offsetX -= adjustSpeed;
            reloadCurrentSprite();
        }
        if (input.isKeyJustPressed(KeyEvent.VK_RIGHT)) {
            adjustment.offsetX += adjustSpeed;
            reloadCurrentSprite();
        }
        
        // Y offset adjustment
        if (input.isKeyJustPressed(KeyEvent.VK_UP)) {
            adjustment.offsetY -= adjustSpeed;
            reloadCurrentSprite();
        }
        if (input.isKeyJustPressed(KeyEvent.VK_DOWN)) {
            adjustment.offsetY += adjustSpeed;
            reloadCurrentSprite();
        }
        
        // Toggle between sprite types
        if (input.isKeyJustPressed(KeyEvent.VK_TAB)) {
            // Cycle through available adjustments
            String[] names = adjustments.keySet().toArray(new String[0]);
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(currentAdjustmentName)) {
                    int nextIndex = (i + 1) % names.length;
                    setCurrentAdjustment(names[nextIndex]);
                    break;
                }
            }
        }
        
        // Print current values
        if (input.isKeyJustPressed(KeyEvent.VK_P)) {
            printCurrentAdjustment();
        }
        
        // Reset to default values
        if (input.isKeyJustPressed(KeyEvent.VK_R)) {
            adjustment.resetToDefault();
            reloadCurrentSprite();
        }
    }
    
    /**
     * Print the current adjustment values
     */
    private void printCurrentAdjustment() {
        if (currentAdjustmentName == null) return;
        
        SpriteAdjustment adjustment = adjustments.get(currentAdjustmentName);
        if (adjustment == null) return;
        
        System.out.println("=== Sprite Adjustment: " + adjustment.name + " ===");
        System.out.println("Source size: " + adjustment.sourceSize.width + "x" + adjustment.sourceSize.height);
        System.out.println("Display size: " + adjustment.displayWidth + "x" + adjustment.displayHeight);
        System.out.println("Offset: (" + adjustment.offsetX + ", " + adjustment.offsetY + ")");
        System.out.println("Duration: " + adjustment.durationMs + "ms");
        System.out.println("Path: " + adjustment.path + "/" + adjustment.prefix + "1.png ... " + adjustment.frameCount + " frames");
        System.out.println("Controls: Arrow keys (offset), Numpad 4,6,8,2 (size), P (print), R (reset), Tab (next)");
    }
    
    /**
     * Gets the current sprite adjustment values to use in SpriteSheetManager
     */
    public String getAdjustmentCode() {
        StringBuilder code = new StringBuilder();
        code.append("// Copy these values to your SpriteSheetManager:\n\n");
        
        for (Map.Entry<String, SpriteAdjustment> entry : adjustments.entrySet()) {
            SpriteAdjustment adj = entry.getValue();
            
            code.append("// ").append(adj.name).append(" animation adjustments\n");
            code.append("Dimension ").append(adj.name).append("FrameSize = new Dimension(")
                .append(adj.sourceSize.width).append(", ").append(adj.sourceSize.height).append(");\n");
            
            code.append("Dimension ").append(adj.name).append("DisplaySize = new Dimension(")
                .append(adj.displayWidth).append(", ").append(adj.displayHeight).append(");\n");
            
            code.append("int ").append(adj.name).append("OffsetX = ").append(adj.offsetX).append(";\n");
            code.append("int ").append(adj.name).append("OffsetY = ").append(adj.offsetY).append(";\n\n");
            
            code.append("// Load ").append(adj.name).append(" animation\n");
            code.append("Sprite ").append(adj.name).append("Sprite = sequenceManager.loadSpriteSequence(\n")
                .append("    \"player_").append(adj.name).append("\",\n")
                .append("    \"").append(adj.path).append("\",\n")
                .append("    \"").append(adj.prefix).append("\",\n")
                .append("    ").append(adj.frameCount).append(",\n")
                .append("    ").append(adj.name).append("FrameSize,\n")
                .append("    ").append(adj.name).append("DisplaySize,\n")
                .append("    ").append(adj.name).append("OffsetX,\n")
                .append("    ").append(adj.name).append("OffsetY,\n")
                .append("    Duration.ofMillis(").append(adj.durationMs).append("),\n")
                .append("    true\n")
                .append(");\n\n")
                .append("if (").append(adj.name).append("Sprite != null) {\n")
                .append("    sprites.put(\"player_").append(adj.name).append("\", ").append(adj.name).append("Sprite);\n")
                .append("}\n\n");
        }
        
        return code.toString();
    }
    
    /**
     * Helper class to store sprite adjustment parameters
     */
    private static class SpriteAdjustment {
        final String name;
        final String path;
        final String prefix;
        final int frameCount;
        final Dimension sourceSize;
        final int defaultDisplayWidth;
        final int defaultDisplayHeight;
        final int defaultOffsetX;
        final int defaultOffsetY;
        final int durationMs;
        
        int displayWidth;
        int displayHeight;
        int offsetX;
        int offsetY;
        
        SpriteAdjustment(String name, String path, String prefix, int frameCount,
                        Dimension sourceSize, Dimension displaySize, 
                        int offsetX, int offsetY, int durationMs) {
            this.name = name;
            this.path = path;
            this.prefix = prefix;
            this.frameCount = frameCount;
            this.sourceSize = sourceSize;
            this.defaultDisplayWidth = displaySize.width;
            this.defaultDisplayHeight = displaySize.height;
            this.defaultOffsetX = offsetX;
            this.defaultOffsetY = offsetY;
            this.durationMs = durationMs;
            
            // Set current values to defaults
            this.displayWidth = defaultDisplayWidth;
            this.displayHeight = defaultDisplayHeight;
            this.offsetX = defaultOffsetX;
            this.offsetY = defaultOffsetY;
        }
        
        void resetToDefault() {
            displayWidth = defaultDisplayWidth;
            displayHeight = defaultDisplayHeight;
            offsetX = defaultOffsetX;
            offsetY = defaultOffsetY;
        }
    }
    
    /**
     * Reloads the current sprite with updated parameters
     */
    private void reloadCurrentSprite() {
        if (currentAdjustmentName == null) return;
        
        SpriteAdjustment adjustment = adjustments.get(currentAdjustmentName);
        if (adjustment == null) return;
        
        try {
            // Create display size based on current adjustment
            Dimension displaySize = new Dimension(
                adjustment.displayWidth,
                adjustment.displayHeight
            );
            
            // Load the sprite with current adjustment settings
            Sprite sprite = sequenceManager.loadSpriteSequence(
                "adj_" + adjustment.name,
                adjustment.path,
                adjustment.prefix,
                adjustment.frameCount,
                adjustment.sourceSize,
                displaySize,
                adjustment.offsetX,
                adjustment.offsetY,
                Duration.ofMillis(adjustment.durationMs),
                true
            );
            
            if (sprite != null) {
                // The sprite was loaded successfully
                printCurrentAdjustment();
            }
        } catch (Exception e) {
            System.err.println("Error reloading sprite: " + e.getMessage());
        }
    }
}