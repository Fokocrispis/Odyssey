package game.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import game.Vector2D;
import game.physics.Collision;
import game.physics.PhysicsObject;
import game.resource.ResourceManager;

/**
 * A fixed FloorEntity
 */
public class FloorEntity extends AbstractEntity {
    private BufferedImage textureDirt;
    
    // Fixed tile width in pixels (2 meters)
    private static final int TILE_WIDTH = 200; // 2 meters * 100 pixels per meter
    
    /**
     * Creates a new floor entity.
     */
    public FloorEntity(double x, double y, int width, int height) {
        super(x, y, width, height);
        
        // Floor-specific physics properties
        this.mass = 0; // Static object (immovable)
        this.affectedByGravity = false;
        this.restitution = 0.0f; // No bounce
        this.friction = 0.9f; // High friction for good control
        
        // Load floor sprites
        loadSprites();
    }
    
    /**
     * Loads the sprite textures for the floor.
     */
    private void loadSprites() {
        try {
            ResourceManager resourceManager = ResourceManager.getInstance();
            textureDirt = resourceManager.loadImage("dirt_orange.png");
            
            if (textureDirt == null) {
                System.err.println("Warning: dirt_orange.png not found");
            } else {
                System.out.println("Floor texture size: " + textureDirt.getWidth() + "x" + textureDirt.getHeight());
            }
        } catch (Exception e) {
            System.err.println("Failed to load floor sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible || textureDirt == null) return;
        
        // Calculate position for rendering
        int startX = (int)(position.getX() - width / 2);
        int startY = (int)(position.getY() - height / 2);
        
        // Calculate how many tiles we need to cover the floor width
        int tilesNeeded = (int)Math.ceil((double)width / TILE_WIDTH);
        
        // Draw tiles with consistent 2m width
        for (int i = 0; i < tilesNeeded; i++) {
            int tileX = startX + i * TILE_WIDTH;
            
            // Draw the dirt texture tile (always 2m wide)
            g.drawImage(
                textureDirt,
                tileX,
                startY,
                TILE_WIDTH, // Always use 2m width
                height,
                null
            );
        }
    }
    
    @Override
    public void onCollision(PhysicsObject other, Collision collision) {
        // Floor doesn't react to collisions
    }
}