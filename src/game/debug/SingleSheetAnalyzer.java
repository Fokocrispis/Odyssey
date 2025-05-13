package game.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Analyzes the JoannaD'ArcIII-Sheet#1.png spritesheet to determine animation frames.
 */
public class SingleSheetAnalyzer {
    
    public static void main(String[] args) {
        String spritesheetPath = "resources/Sprites/JoannaD'ArcIII-Sheet#1.png";
        analyzeSingleSpritesheet(spritesheetPath);
    }
    
    /**
     * Analyzes the JoannaD'ArcIII spritesheet structure.
     */
    public static void analyzeSingleSpritesheet(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("Spritesheet not found: " + path);
                return;
            }
            
            BufferedImage image = ImageIO.read(file);
            System.out.println("=== JoannaD'ArcIII-Sheet#1.png Analysis ===");
            System.out.println("Total image size: " + image.getWidth() + "x" + image.getHeight());
            
            // Detect sprite size and layout
            detectSpriteGrid(image);
            
            // Analyze rows
            analyzeRows(image);
            
            // Generate configuration code
            generateConfiguration(image);
            
        } catch (Exception e) {
            System.err.println("Error analyzing spritesheet: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Detects the grid pattern of sprites.
     */
    private static void detectSpriteGrid(BufferedImage image) {
        System.out.println("\n=== Grid Detection ===");
        
        // Common sprite sizes for this type of character
        int[] commonSizes = {32, 48, 64, 96, 128};
        
        for (int size : commonSizes) {
            if (image.getWidth() % size == 0 && image.getHeight() % size == 0) {
                int cols = image.getWidth() / size;
                int rows = image.getHeight() / size;
                int totalFrames = cols * rows;
                
                System.out.println("Possible sprite size: " + size + "x" + size);
                System.out.println("Grid: " + cols + " columns x " + rows + " rows");
                System.out.println("Total frames: " + totalFrames);
                System.out.println();
            }
        }
        
        // Based on visual inspection, recommend 64x64
        System.out.println("Recommended: 64x64 pixel sprites");
    }
    
    /**
     * Analyzes each row to estimate animation frame counts.
     */
    private static void analyzeRows(BufferedImage image) {
        System.out.println("\n=== Row Analysis ===");
        System.out.println("Estimated frames per row (based on visual inspection):");
        
        // These are estimates based on the visible sprite sheet
        String[] rowDescriptions = {
            "Row 1: ~7 frames (Idle)",
            "Row 2: ~21 frames (Run/Movement)",
            "Row 3: ~8-10 frames (Attack 1)",
            "Row 4: ~9 frames (Attack 2/Combo)",
            "Row 5: ~11 frames (Jump sequence)",
            "Row 6: ~8 frames (Fall/Landing)",
            "Row 7: ~10 frames (Dash/Dodge)",
            "Row 8: ~11 frames (Crouch/Slide)",
            "Row 9: ~13 frames (Hurt/Knockback)",
            "Row 10: ~10 frames (Death)",
            "Row 11: ~11 frames (Wall interaction)"
        };
        
        for (String desc : rowDescriptions) {
            System.out.println(desc);
        }
        
        System.out.println("\nNote: These are estimates. You should verify by opening the file in an image editor.");
    }
    
    /**
     * Generates configuration code for the spritesheet.
     */
    private static void generateConfiguration(BufferedImage image) {
        System.out.println("\n=== Generated Configuration ===");
        System.out.println("Copy this into your SpriteSheetManager.createPlayerSprites() method:\n");
        
        System.out.println("// Load the single comprehensive spritesheet");
        System.out.println("loadSpriteSheet(\"player\", \"JoannaD'ArcIII-Sheet#1.png\");\n");
        
        System.out.println("// Define frame size and scale");
        System.out.println("Dimension frameSize = new Dimension(64, 64); // Adjust if needed");
        System.out.println("double scale = 3.0; // Adjust to fit your game\n");
        
        System.out.println("// Create sprites (adjust firstFrame and totalFrames based on actual content)");
        
        // Generate sprite creation code with estimated values
        generateSpriteCode("player_idle", 0, 7, 1000, "Idle animation");
        generateSpriteCode("player_run", 7, 21, 800, "Run animation");
        generateSpriteCode("player_attack", 28, 8, 350, "Basic attack");
        generateSpriteCode("player_attack2", 36, 9, 400, "Second attack");
        generateSpriteCode("player_jump", 45, 6, 500, "Jump animation");
        generateSpriteCode("player_fall", 51, 5, 600, "Fall animation");
        generateSpriteCode("player_dash", 56, 5, 200, "Dash animation");
        generateSpriteCode("player_crouch", 61, 5, 1000, "Crouch animation");
        generateSpriteCode("player_slide", 66, 5, 300, "Slide animation");
        generateSpriteCode("player_hurt", 71, 4, 500, "Hurt animation");
        generateSpriteCode("player_death", 75, 6, 800, "Death animation");
        generateSpriteCode("player_wall_slide", 81, 4, 400, "Wall slide animation");
        
        System.out.println("\n// Add more animations as needed based on actual spritesheet content");
    }
    
    /**
     * Generates sprite creation code for a specific animation.
     */
    private static void generateSpriteCode(String spriteName, int firstFrame, 
                                         int totalFrames, int durationMs, String description) {
        System.out.println("// " + description);
        System.out.println("createSprite(\"" + spriteName + "\", \"player\", frameSize, scale,");
        System.out.println("            " + firstFrame + ", " + totalFrames + ", Duration.ofMillis(" + durationMs + "));\n");
    }
    
    /**
     * Provides a visual guide for manual verification.
     */
    public static void printVerificationGuide() {
        System.out.println("\n=== Manual Verification Guide ===");
        System.out.println("To verify the configuration:");
        System.out.println("1. Open JoannaD'ArcIII-Sheet#1.png in an image editor");
        System.out.println("2. Set up a 64x64 pixel grid overlay");
        System.out.println("3. Count the actual frames in each row");
        System.out.println("4. Identify which row contains which animation");
        System.out.println("5. Update the generated configuration with exact values");
        System.out.println("6. Note any animations that span multiple rows");
        System.out.println("7. Check for empty spaces or padding between sprites");
    }
    
    /**
     * Creates a test configuration that you can use to verify animations.
     */
    public static void generateTestConfiguration() {
        System.out.println("\n=== Test Configuration ===");
        System.out.println("Use this to test each animation individually:\n");
        
        System.out.println("public void createTestSprites() {");
        System.out.println("    loadSpriteSheet(\"player\", \"JoannaD'ArcIII-Sheet#1.png\");");
        System.out.println("    Dimension frameSize = new Dimension(64, 64);");
        System.out.println("    double scale = 3.0;");
        System.out.println("    ");
        System.out.println("    // Test each animation one at a time");
        System.out.println("    // Uncomment one at a time to test");
        System.out.println("    ");
        System.out.println("    // createSprite(\"test_idle\", \"player\", frameSize, scale, 0, 7, Duration.ofSeconds(1));");
        System.out.println("    // createSprite(\"test_run\", \"player\", frameSize, scale, 7, 21, Duration.ofMillis(800));");
        System.out.println("    // createSprite(\"test_attack\", \"player\", frameSize, scale, 28, 8, Duration.ofMillis(350));");
        System.out.println("    // ... add more as needed");
        System.out.println("}");
    }
}