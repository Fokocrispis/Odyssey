package game.debug;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import game.sprites.SpriteSheetManager;
import game.sprites.Sprite;

/**
 * Debug utility to help identify sprite sheet issues
 */
public class SpriteDebugger {
    
    public static void main(String[] args) {
        // Test the sprite loading with debug information
        debugSpriteLoading();
    }
    
    /**
     * Debug sprite loading with detailed output
     */
    public static void debugSpriteLoading() {
        System.out.println("=== SPRITE LOADING DEBUG ===\n");
        
        try {
            // Create sprite manager
            SpriteSheetManager manager = new SpriteSheetManager();
            
            // Test loading the sprite sheet
            String spritePath = "resources/JoannaD'ArcIII_v1.9.2/Sprites/JoannaD'Arc_base-Sheet.png";
            
            // Check if file exists
            File file = new File(spritePath);
            if (!file.exists()) {
                System.err.println("File not found: " + spritePath);
                listAvailableFiles("resources");
                return;
            }
            
            // Load and analyze the sprite sheet
            BufferedImage sheet = ImageIO.read(file);
            System.out.println("Loaded sprite sheet:");
            System.out.println("  File: " + spritePath);
            System.out.println("  Dimensions: " + sheet.getWidth() + "x" + sheet.getHeight());
            
            // Calculate grid information
            int frameWidth = 64;
            int frameHeight = 64;
            int cols = sheet.getWidth() / frameWidth;
            int rows = sheet.getHeight() / frameHeight;
            int totalFrames = cols * rows;
            
            System.out.println("  Frame size: " + frameWidth + "x" + frameHeight);
            System.out.println("  Grid: " + cols + " columns x " + rows + " rows");
            System.out.println("  Total frames: " + totalFrames);
            
            // Create sample sprites with safe parameters
            createAndTestSprite(manager, sheet, "debug_idle", 0, 4);
            createAndTestSprite(manager, sheet, "debug_run", cols, 6);
            
            // Generate a visual grid overlay
            generateGridOverlay(sheet, frameWidth, frameHeight, "debug_grid_overlay.png");
            
        } catch (Exception e) {
            System.err.println("Error during sprite debugging: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates and tests a sprite with safe parameters
     */
    private static void createAndTestSprite(SpriteSheetManager manager, BufferedImage sheet, 
                                          String name, int startFrame, int frameCount) {
        try {
            // Calculate safe frame count
            int cols = sheet.getWidth() / 64;
            int rows = sheet.getHeight() / 64;
            int maxFrames = cols * rows;
            
            // Ensure we don't exceed available frames
            int safeFrameCount = Math.min(frameCount, maxFrames - startFrame);
            if (safeFrameCount < 1) safeFrameCount = 1;
            
            System.out.println("\nTesting sprite '" + name + "':");
            System.out.println("  Start frame: " + startFrame);
            System.out.println("  Requested frames: " + frameCount);
            System.out.println("  Safe frame count: " + safeFrameCount);
            System.out.println("  Available frames: " + maxFrames);
            
            // This is where you would create the sprite
            // For now, just simulate the bounds checking
            for (int i = 0; i < safeFrameCount; i++) {
                int frameIndex = startFrame + i;
                int x = (frameIndex % cols) * 64;
                int y = (frameIndex / cols) * 64;
                
                if (y + 64 > sheet.getHeight()) {
                    System.err.println("  WARNING: Frame " + frameIndex + " would exceed sheet bounds!");
                    System.err.println("    Calculated Y: " + y + ", Height needed: " + (y + 64));
                    System.err.println("    Sheet height: " + sheet.getHeight());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error testing sprite '" + name + "': " + e.getMessage());
        }
    }
    
    /**
     * Generates a visual grid overlay to help identify sprite boundaries
     */
    private static void generateGridOverlay(BufferedImage sprite, int frameWidth, int frameHeight, String outputPath) {
        try {
            BufferedImage overlay = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = overlay.createGraphics();
            
            // Draw original sprite
            g.drawImage(sprite, 0, 0, null);
            
            // Draw grid
            g.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red
            
            // Vertical lines
            for (int x = frameWidth; x < sprite.getWidth(); x += frameWidth) {
                g.drawLine(x, 0, x, sprite.getHeight());
            }
            
            // Horizontal lines
            for (int y = frameHeight; y < sprite.getHeight(); y += frameHeight) {
                g.drawLine(0, y, sprite.getWidth(), y);
            }
            
            // Add frame numbers
            g.setColor(Color.YELLOW);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
            
            int frameIndex = 0;
            for (int y = 0; y < sprite.getHeight(); y += frameHeight) {
                for (int x = 0; x < sprite.getWidth(); x += frameWidth) {
                    g.drawString(String.valueOf(frameIndex), x + 5, y + 15);
                    frameIndex++;
                }
            }
            
            g.dispose();
            
            // Save overlay image
            File output = new File(outputPath);
            ImageIO.write(overlay, "PNG", output);
            System.out.println("\nGenerated grid overlay: " + outputPath);
            
        } catch (Exception e) {
            System.err.println("Error generating grid overlay: " + e.getMessage());
        }
    }
    
    /**
     * Lists available files in a directory
     */
    private static void listAvailableFiles(String directory) {
        System.out.println("\nAvailable files in " + directory + ":");
        listFiles(new File(directory), 0);
    }
    
    /**
     * Recursively lists files
     */
    private static void listFiles(File directory, int depth) {
        if (!directory.exists() || !directory.isDirectory()) return;
        
        String indent = "  ".repeat(depth);
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println(indent + file.getName() + "/");
                    if (depth < 3) { // Limit recursion depth
                        listFiles(file, depth + 1);
                    }
                } else {
                    System.out.println(indent + file.getName());
                }
            }
        }
    }
    
    /**
     * Validates a specific sprite configuration
     */
    public static void validateSpriteConfig(String spritePath, int frameWidth, int frameHeight,
                                          int startFrame, int frameCount) {
        try {
            File file = new File(spritePath);
            if (!file.exists()) {
                System.err.println("File not found: " + spritePath);
                return;
            }
            
            BufferedImage sheet = ImageIO.read(file);
            int cols = sheet.getWidth() / frameWidth;
            int rows = sheet.getHeight() / frameHeight;
            int totalFrames = cols * rows;
            
            System.out.println("\n=== Sprite Configuration Validation ===");
            System.out.println("File: " + spritePath);
            System.out.println("Sheet size: " + sheet.getWidth() + "x" + sheet.getHeight());
            System.out.println("Frame size: " + frameWidth + "x" + frameHeight);
            System.out.println("Grid: " + cols + " cols x " + rows + " rows = " + totalFrames + " frames");
            System.out.println("Requested: frames " + startFrame + " to " + (startFrame + frameCount - 1));
            
            // Validate the configuration
            if (startFrame < 0) {
                System.err.println("ERROR: Start frame cannot be negative!");
            }
            
            if (startFrame + frameCount > totalFrames) {
                System.err.println("ERROR: Requested frames exceed available frames!");
                System.err.println("  Last requested frame: " + (startFrame + frameCount - 1));
                System.err.println("  Last available frame: " + (totalFrames - 1));
                
                // Suggest correction
                int maxFrames = totalFrames - startFrame;
                System.err.println("SUGGESTION: Reduce frame count to " + maxFrames);
            } else {
                System.out.println("VALID: Configuration within available frames");
            }
            
        } catch (Exception e) {
            System.err.println("Error validating sprite config: " + e.getMessage());
        }
    }
}