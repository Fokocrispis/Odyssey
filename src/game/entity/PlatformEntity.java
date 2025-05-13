package game.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import game.physics.Collision;
import game.physics.PhysicsObject;
import game.resource.ResourceManager;

/**
 * A platform entity that uses sprites for rendering with 3x1 meter dimensions.
 */
public class PlatformEntity extends AbstractEntity {
    // Platform sprite resources
    private BufferedImage textureLeft;
    private BufferedImage textureMiddle;
    private BufferedImage textureRight;
    
    /**
     * Creates a new platform entity.
     */
    public PlatformEntity(double x, double y, int width, int height) {
        super(x, y, width, height);
        
        // Platform-specific physics properties
        this.mass = 0; // Infinite mass (immovable)
        this.affectedByGravity = false;
        
        // Load platform sprites
        loadSprites();
    }
    
    /**
     * Loads the sprite textures for the platform.
     */
    private void loadSprites() {
        try {
            ResourceManager resourceManager = ResourceManager.getInstance();
            textureLeft = resourceManager.loadImage("platform_left.png");
            textureMiddle = resourceManager.loadImage("platform_middle.png");
            textureRight = resourceManager.loadImage("platform_right.png");
            
            if (textureLeft != null) {
                System.out.println("Platform left texture size: " + textureLeft.getWidth() + "x" + textureLeft.getHeight());
            }
            if (textureMiddle != null) {
                System.out.println("Platform middle texture size: " + textureMiddle.getWidth() + "x" + textureMiddle.getHeight());
            }
            if (textureRight != null) {
                System.out.println("Platform right texture size: " + textureRight.getWidth() + "x" + textureRight.getHeight());
            }
        } catch (Exception e) {
            System.err.println("Failed to load platform sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Calculate platform position (center the sprite on the collision box)
        int platformX = (int)(position.getX() - width / 2);
        int platformY = (int)(position.getY() - height / 2);
        
        // We want to maintain the aspect ratio of the sprites while filling the platform area
        // Calculate the section sizes
        int leftWidth = width / 4;     // 25% for left
        int rightWidth = width / 4;    // 25% for right  
        int middleWidth = width / 2;   // 50% for middle
        
        // Draw left section
        if (textureLeft != null) {
            g.drawImage(
                textureLeft,
                platformX,
                platformY,
                leftWidth,
                height,
                null
            );
        }
        
        // Draw middle section
        if (textureMiddle != null) {
            // Simply scale the middle texture to fit
            g.drawImage(
                textureMiddle,
                platformX + leftWidth,
                platformY,
                middleWidth,
                height,
                null
            );
        }
        
        // Draw right section
        if (textureRight != null) {
            g.drawImage(
                textureRight,
                platformX + width - rightWidth,
                platformY,
                rightWidth,
                height,
                null
            );
        }
        
        // Debug: Draw collision box outline (optional - uncomment to see hitbox)
        /*
        g.setColor(new java.awt.Color(255, 0, 255, 80));
        g.drawRect(
            platformX,
            platformY,
            width,
            height
        );
        */
    }
    
    @Override
    public void onCollision(PhysicsObject other, Collision collision) {
        // Platforms don't react to collisions
    }
}