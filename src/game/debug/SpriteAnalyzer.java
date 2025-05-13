package game.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Tool to analyze spritesheet files and determine frame counts and sizes.
 */
public class SpriteAnalyzer {
    
    public static void main(String[] args) {
        // Analyze all spritesheet files in the Sprites folder
        analyzeSpriteFolder("resources/Sprites");
    }
    
    /**
     * Analyzes all spritesheets in a folder and its subfolders.
     */
    public static void analyzeSpriteFolder(String folderPath) {
        File folder = new File(folderPath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Folder not found: " + folderPath);
            return;
        }
        
        // Look for subfolders
        for (File subfolder : folder.listFiles()) {
            if (subfolder.isDirectory()) {
                System.out.println("\n=== Analyzing folder: " + subfolder.getName() + " ===");
                analyzeFolder(subfolder);
            }
        }
    }
    
    /**
     * Analyzes sprites in a specific folder.
     */
    private static void analyzeFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                System.out.println("\nAnalyzing: " + file.getName());
                analyzeSpritesheet(file);
            }
        }
    }
    
    /**
     * Analyzes a single spritesheet file.
     */
    private static void analyzeSpritesheet(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            
            System.out.println("  Image size: " + image.getWidth() + "x" + image.getHeight());
            
            // Detect typical sprite sizes by looking for common patterns
            detectSpriteGrid(image);
            
        } catch (Exception e) {
            System.err.println("  Error reading file: " + e.getMessage());
        }
    }
    
    /**
     * Attempts to detect the grid pattern of sprites in the sheet.
     */
    private static void detectSpriteGrid(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Common sprite sizes to check
        int[] commonSizes = {32, 48, 64, 96, 128};
        
        for (int size : commonSizes) {
            if (width % size == 0 && height % size == 0) {
                int cols = width / size;
                int rows = height / size;
                int totalFrames = cols * rows;
                
                System.out.println("  Possible sprite size: " + size + "x" + size);
                System.out.println("  Grid: " + cols + " columns x " + rows + " rows = " + totalFrames + " frames");
            }
        }
        
        // Try to detect actual sprite boundaries by looking for transparent pixels
        // This is a more advanced technique for irregular sprite layouts
        detectSpriteBoundaries(image);
    }
    
    /**
     * Attempts to detect individual sprite boundaries in the sheet.
     */
    private static void detectSpriteBoundaries(BufferedImage image) {
        // This is a simplified version - you can expand it to be more sophisticated
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Check if sprites are separated by transparent or specific color boundaries
        // Count potential sprite rows/columns
        
        // For demo purposes, just print info about transparency
        boolean hasTransparency = image.getColorModel().hasAlpha();
        System.out.println("  Has transparency: " + hasTransparency);
        
        // You could add more sophisticated boundary detection here
    }
    
    /**
     * Generates sprite configuration code based on analysis.
     */
    public static void generateSpriteConfig(String animationName, int frameWidth, int frameHeight, 
                                          int totalFrames, double scale, long durationMs) {
        System.out.println("\n// " + animationName + " configuration");
        System.out.println("Dimension frameSize = new Dimension(" + frameWidth + ", " + frameHeight + ");");
        System.out.println("double scale = " + scale + ";");
        System.out.println("createSprite(\"player_" + animationName.toLowerCase() + "\", \"" + 
                         animationName.toLowerCase() + "\", frameSize, scale,");
        System.out.println("            0, " + totalFrames + ", Duration.ofMillis(" + durationMs + "));");
    }
    
    /**
     * Utility method to print recommended sprite configuration.
     */
    public static void printRecommendedConfig() {
        System.out.println("\n=== Recommended Sprite Configuration ===");
        System.out.println("Based on typical JoannaD'ArcIII animations:");
        
        generateSpriteConfig("Idle", 64, 64, 6, 3.0, 1000);
        generateSpriteConfig("Run", 64, 64, 8, 3.0, 800);
        generateSpriteConfig("Dash", 64, 64, 3, 3.0, 200);
        generateSpriteConfig("Attack", 64, 64, 4, 3.0, 350);
        generateSpriteConfig("Jump", 64, 64, 3, 3.0, 500);
        generateSpriteConfig("Fall", 64, 64, 3, 3.0, 600);
        generateSpriteConfig("Hurt", 64, 64, 3, 3.0, 500);
        generateSpriteConfig("Death", 64, 64, 6, 3.0, 800);
    }
}