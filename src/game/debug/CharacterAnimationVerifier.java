package game.debug;

import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;

import game.sprites.CharacterAnimationManager;

/**
 * Utility class to find and verify character animations
 */
public class CharacterAnimationVerifier {
    
    private static final String[] KNOWN_CHARACTERS = {
        "Joanna"
    };
    
    /**
     * Main method to run the verifier
     */
    public static void main(String[] args) {
        System.out.println("=== Character Animation Verification ===");
        
        // Look for character directories in resources
        findCharacterDirectories();
        
        // Test loading animations for known characters
        for (String characterId : KNOWN_CHARACTERS) {
            testCharacterAnimations(characterId);
        }
    }
    
    /**
     * Finds all character directories in resources
     */
    private static void findCharacterDirectories() {
        System.out.println("\nSearching for character directories...");
        
        String[] paths = {
            "resources/Sprites",
            "Sprites"
        };
        
        for (String path : paths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                System.out.println("Found sprites directory: " + path);
                
                File[] characterDirs = dir.listFiles(File::isDirectory);
                if (characterDirs != null && characterDirs.length > 0) {
                    System.out.println("Character directories found: " + 
                                     Arrays.stream(characterDirs)
                                          .map(File::getName)
                                          .reduce((a, b) -> a + ", " + b)
                                          .orElse("None"));
                    
                    // Check character subdirectories
                    for (File charDir : characterDirs) {
                        checkCharacterAnimations(charDir);
                    }
                } else {
                    System.out.println("No character directories found in " + path);
                }
            }
        }
    }
    
    /**
     * Checks for animation directories in a character directory
     */
    private static void checkCharacterAnimations(File characterDir) {
        System.out.println("\nInspecting character: " + characterDir.getName());
        
        File[] animationDirs = characterDir.listFiles(File::isDirectory);
        if (animationDirs != null && animationDirs.length > 0) {
            System.out.println("Animation directories found: " + 
                             Arrays.stream(animationDirs)
                                  .map(File::getName)
                                  .reduce((a, b) -> a + ", " + b)
                                  .orElse("None"));
            
            // Check animation frames
            for (File animDir : animationDirs) {
                File[] frames = animDir.listFiles(file -> 
                    file.isFile() && 
                    file.getName().toLowerCase().endsWith(".png")
                );
                
                if (frames != null) {
                    System.out.println("  " + animDir.getName() + ": " + frames.length + " frames");
                }
            }
        } else {
            System.out.println("No animation directories found!");
        }
    }
    
    /**
     * Tests loading animations for a specific character
     */
    private static void testCharacterAnimations(String characterId) {
        System.out.println("\nTesting animation loading for: " + characterId);
        
        try {
            CharacterAnimationManager manager = new CharacterAnimationManager(characterId);
            manager.loadAllAnimations();
            
            // Print loaded animations
            manager.printLoadedAnimations();
        } catch (Exception e) {
            System.err.println("Error loading animations for " + characterId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}