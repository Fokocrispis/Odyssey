package game.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Updated ResourceManager that supports the new resources structure
 */
public class ResourceManager {
    private static ResourceManager instance;
    private final Map<String, BufferedImage> imageCache;
    
    // Base paths for resources
    private static final String[] RESOURCE_PATHS = {
        "",                  // Direct path
        "resources/",        // Standard resources folder
        "resources/Sprites/", // Sprites folder
        "Sprites/",          // Direct sprites folder
    };
    
    private ResourceManager() {
        this.imageCache = new HashMap<>();
    }
    
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * Loads an image with improved path handling
     */
    public BufferedImage loadImage(String fileName) throws IOException {
        // Check cache first
        if (imageCache.containsKey(fileName)) {
            System.out.println("Using cached image: " + fileName);
            return imageCache.get(fileName);
        }
        
        System.out.println("Attempting to load: " + fileName);
        
        // First try the exact path provided
        BufferedImage image = tryLoadImage(fileName);
        if (image != null) {
            imageCache.put(fileName, image);
            System.out.println("Successfully loaded: " + fileName);
            return image;
        }
        
        // Then try various path combinations
        for (String basePath : RESOURCE_PATHS) {
            image = tryLoadImage(basePath + fileName);
            if (image != null) {
                imageCache.put(fileName, image);
                System.out.println("Successfully loaded from: " + basePath + fileName);
                return image;
            }
        }
        
        // Try special handling for character sprite paths
        if (fileName.contains("/")) {
            // Extract parts for better path combinations
            String[] parts = fileName.split("/");
            if (parts.length > 2) {
                // Try rearranging paths
                String characterId = parts[0];
                String animationFolder = parts[1];
                String frameName = parts[parts.length - 1];
                
                // Try variations
                String[] variations = {
                    "resources/Sprites/" + characterId + "/" + animationFolder + "/" + frameName,
                    "Sprites/" + characterId + "/" + animationFolder + "/" + frameName,
                    "resources/" + fileName,
                };
                
                for (String path : variations) {
                    image = tryLoadImage(path);
                    if (image != null) {
                        imageCache.put(fileName, image);
                        System.out.println("Successfully loaded with rearranged path: " + path);
                        return image;
                    }
                }
            }
        }
        
        // If still not found, print debug info
        printDebugInfo(fileName);
        
        // Create a fallback image
        image = createFallbackImage(fileName);
        imageCache.put(fileName, image);
        return image;
    }
    
    /**
     * Tries to load an image from a specific path
     */
    private BufferedImage tryLoadImage(String path) {
        try {
            // First try as a file
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    return img;
                }
            }
            
            // Then try as a resource
            URL resource = getClass().getResource(path);
            if (resource == null) {
                resource = getClass().getResource("/" + path);
            }
            
            if (resource != null) {
                BufferedImage img = ImageIO.read(resource);
                if (img != null) {
                    return img;
                }
            }
        } catch (Exception e) {
            // Silent fail, will try next path
        }
        
        return null;
    }
    
    /**
     * Prints debug information when an image cannot be loaded
     */
    private void printDebugInfo(String fileName) {
        System.err.println("\n=== RESOURCE LOADING DEBUG INFO ===");
        System.err.println("Failed to load: " + fileName);
        System.err.println("Current working directory: " + System.getProperty("user.dir"));
        
        // List contents of key directories
        listDirectoryContents("resources");
        listDirectoryContents("resources/Sprites");
        listDirectoryContents("Sprites");
        
        // Try to find character directories
        File spritesDir = new File("resources/Sprites");
        if (spritesDir.exists() && spritesDir.isDirectory()) {
            File[] characterDirs = spritesDir.listFiles(File::isDirectory);
            if (characterDirs != null) {
                for (File characterDir : characterDirs) {
                    System.err.println("Found character directory: " + characterDir.getName());
                    listDirectoryContents(characterDir.getPath());
                }
            }
        }
        
        System.err.println("=== END DEBUG INFO ===\n");
    }
    
    /**
     * Lists the contents of a directory for debugging
     */
    private void listDirectoryContents(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            System.err.println("Contents of '" + dirPath + "':");
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    System.err.println("  " + file.getName() + 
                                      (file.isDirectory() ? " (dir)" : ""));
                }
            } else {
                System.err.println("  (empty)");
            }
        } else {
            System.err.println("Directory '" + dirPath + "' does not exist");
        }
    }
    
    /**
     * Creates a fallback image with improved visual indicators
     */
    private BufferedImage createFallbackImage(String fileName) {
        int width = 64, height = 64;
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        java.awt.Graphics2D g = fallback.createGraphics();
        
        // Create a checkerboard pattern for better visual recognition
        g.setColor(java.awt.Color.MAGENTA);
        g.fillRect(0, 0, width, height);
        
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, width/2, height/2);
        g.fillRect(width/2, height/2, width/2, height/2);
        
        // Add filename label
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 8));
        
        // Get the filename without path for display
        String displayName = fileName;
        int lastSlash = displayName.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < displayName.length() - 1) {
            displayName = displayName.substring(lastSlash + 1);
        }
        
        // Ensure the name fits
        if (displayName.length() > 10) {
            displayName = displayName.substring(0, 9) + "...";
        }
        
        g.drawString("MISSING", 5, 20);
        g.drawString(displayName, 5, 40);
        
        g.dispose();
        
        return fallback;
    }
    
    /**
     * Loads a specific character's sprite by ID and animation
     * 
     * @param characterId The character ID (folder name)
     * @param animationFolder The animation folder name
     * @param frameName The frame filename
     * @return The loaded image or fallback
     */
    public BufferedImage loadCharacterSprite(String characterId, String animationFolder, String frameName) 
        throws IOException {
        // Build the path using the character structure
        String path = "Sprites/" + characterId + "/" + animationFolder + "/" + frameName;
        return loadImage(path);
    }
    
    /**
     * Clear the image cache
     */
    public void clearCache() {
        imageCache.clear();
    }
}