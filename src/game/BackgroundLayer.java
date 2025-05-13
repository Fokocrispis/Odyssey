package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import game.resource.ResourceManager;

/**
 * Background layer that has height (scene height - floor height) and ends at floor surface.
 */
public class BackgroundLayer implements GameObject {
    private BufferedImage image;
    private Color color;
    private double scrollFactorX;
    private int tileWidth;
    
    /**
     * Creates a new background layer with an image.
     */
    public BackgroundLayer(String imageName, int tileWidth, double scrollFactorX) {
        try {
            this.image = ResourceManager.getInstance().loadImage(imageName);
            if (this.image != null) {
                System.out.println("Loaded background layer: " + imageName);
            } else {
                System.err.println("Failed to load background image: " + imageName);
            }
        } catch (Exception e) {
            System.err.println("Error loading background image: " + imageName);
            e.printStackTrace();
        }
        
        this.tileWidth = tileWidth;
        this.scrollFactorX = scrollFactorX;
    }
    
    /**
     * Creates a new background layer with a solid color.
     */
    public BackgroundLayer(Color color, int tileWidth, double scrollFactorX) {
        this.color = color;
        this.image = null;
        this.tileWidth = tileWidth;
        this.scrollFactorX = scrollFactorX;
    }
    
    @Override
    public void update(long deltaTime) {
        // No updates needed
    }
    
    @Override
    public void render(Graphics2D g) {
        // This is implemented in renderWithCamera
    }
    
    /**
     * Renders the background with proper height and positioning.
     * 
     * @param g Graphics context
     * @param cameraX Camera X position
     * @param sceneHeight Total scene height  
     * @param floorHeight Height of the floor
     * @param playerStartY Y position where player starts (on top of floor)
     * @param sceneWidth Width of the scene
     */
    public void renderWithCamera(Graphics2D g, double cameraX, int sceneHeight, int floorHeight, int playerStartY, int sceneWidth) {
        // Background height is scene height minus floor height
        int backgroundHeight = sceneHeight - floorHeight;
        
        // Calculate the top surface of the floor
        int floorSurfaceY = playerStartY - (floorHeight / 2);
        
        // Background ends at the floor surface
        int backgroundBottom = floorSurfaceY;
        
        // Background starts above the floor surface
        int backgroundTop = backgroundBottom - backgroundHeight;
        
        // Calculate horizontal parallax offset
        // The camera is already translating by -cameraX, so we need to add back
        // some of that translation based on the parallax factor
        // If scrollFactor is 0.0, the background should stay fixed (undo all camera movement)
        // If scrollFactor is 1.0, the background should move with the camera (no parallax effect)
        double parallaxX = cameraX * (1.0 - scrollFactorX);
        
        if (image != null) {
            // Calculate how many tiles we need
            int tilesNeeded = (int)Math.ceil((double)sceneWidth / tileWidth) + 3;
            
            // Calculate which tiles are visible
            int startTile = (int)Math.floor(parallaxX / tileWidth) - 1;
            int endTile = startTile + tilesNeeded;
            
            // Draw the tiles
            for (int i = startTile; i <= endTile; i++) {
                int tileX = (int)(i * tileWidth - parallaxX);
                
                // Draw the tile
                g.drawImage(image, 
                           tileX, 
                           backgroundTop,
                           tileWidth,
                           backgroundHeight,
                           null);
            }
        } else if (color != null) {
            // Draw solid color background
            g.setColor(color);
            g.fillRect((int)(-parallaxX - tileWidth), backgroundTop, 
                      sceneWidth + tileWidth * 2, backgroundHeight);
        }
    }
    
    /**
     * Gets the scroll factor X.
     */
    public double getScrollFactorX() {
        return scrollFactorX;
    }
    
    /**
     * Gets the scroll factor Y.
     */
    public double getScrollFactorY() {
        return 0.0;  // No vertical parallax
    }
}