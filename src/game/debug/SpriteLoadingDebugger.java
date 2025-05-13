package game.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Debug class to identify sprite loading issues
 */
public class SpriteLoadingDebugger {
    
    public static void main(String[] args) {
        debugSpriteLoading();
    }
    
    public static void debugSpriteLoading() {
        System.out.println("=== Sprite Loading Debug ===\n");
        
        // Check if the file exists
        checkFileExists();
        
        // Verify image can be loaded
        testImageLoading();
        
        // Check resource loading path
        verifyResourcePath();
        
        // Test simple sprite creation
        testBasicSpriteCreation();
    }
    
    private static void checkFileExists() {
        System.out.println("1. Checking if sprite file exists...");
        
        String[] possiblePaths = {
            "resources/Sprites/JoannaD'ArcIII-Sheet#1.png",
            "resources/Sprites/JoannaDArcIII-Sheet1.png",
            "resources/Sprites/JoannaD'ArcIII-Sheet#1.png",
            "src/resources/Sprites/JoannaD'ArcIII-Sheet#1.png",
            "./resources/Sprites/JoannaD'ArcIII-Sheet#1.png"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("✓ Found file at: " + path);
                System.out.println("  File size: " + file.length() + " bytes");
            } else {
                System.out.println("✗ Not found: " + path);
            }
        }
        System.out.println();
    }
    
    private static void testImageLoading() {
        System.out.println("2. Testing image loading...");
        
        try {
            // Test direct file loading
            File file = new File("resources/Sprites/JoannaD'ArcIII-Sheet#1.png");
            if (file.exists()) {
                BufferedImage image = ImageIO.read(file);
                System.out.println("✓ Image loaded successfully");
                System.out.println("  Dimensions: " + image.getWidth() + "x" + image.getHeight());
                System.out.println("  Type: " + image.getType());
            } else {
                System.out.println("✗ Image file not found");
            }
        } catch (Exception e) {
            System.out.println("✗ Error loading image: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void verifyResourcePath() {
        System.out.println("3. Verifying resource loading...");
        
        try {
            // Test resource loading (as used in game)
            ClassLoader classLoader = SpriteLoadingDebugger.class.getClassLoader();
            java.net.URL resource = classLoader.getResource("Sprites/JoannaD'ArcIII-Sheet#1.png");
            
            if (resource != null) {
                System.out.println("✓ Resource found at: " + resource);
                BufferedImage image = ImageIO.read(resource);
                System.out.println("✓ Image loaded via resource path");
                System.out.println("  Dimensions: " + image.getWidth() + "x" + image.getHeight());
            } else {
                System.out.println("✗ Resource not found via classpath");
                System.out.println("  Tried: Sprites/JoannaD'ArcIII-Sheet#1.png");
            }
        } catch (Exception e) {
            System.out.println("✗ Error with resource loading: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testBasicSpriteCreation() {
        System.out.println("4. Testing basic sprite creation...");
        
        try {
            // This simulates what happens in your game
            game.resource.ResourceManager resourceManager = game.resource.ResourceManager.getInstance();
            
            // Test loading with different path variations
            String[] pathVariations = {
                "Sprites/JoannaD'ArcIII-Sheet#1.png",
                "JoannaD'ArcIII-Sheet#1.png",
                "Sprites/JoannaDArcIII-Sheet1.png"
            };
            
            for (String path : pathVariations) {
                try {
                    BufferedImage image = resourceManager.loadImage(path);
                    if (image != null) {
                        System.out.println("✓ ResourceManager loaded: " + path);
                        System.out.println("  Dimensions: " + image.getWidth() + "x" + image.getHeight());
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("✗ Failed: " + path + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Error in sprite creation test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}