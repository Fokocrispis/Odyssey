package game.entity;

import java.awt.Color;
import java.awt.Graphics2D;

import game.Vector2D;
import game.entity.component.Component.ComponentType;
import game.entity.component.RenderComponent;
import game.physics.Collision;
import game.physics.PhysicsObject;

public class BoxEntity extends AbstractEntity {
    private Color color;
    
    public BoxEntity(double x, double y, int width, int height, Color color) {
        super(x, y, width, height);
        this.color = color;
        
        // Create and add the render component properly
        RenderComponent renderer = new RenderComponent(this);
        renderer.setMainColor(color);
        addComponent(renderer);
    }
    
    public BoxEntity(double x, double y, int width, int height) {
        this(x, y, width, height, Color.WHITE);
    }
    
    @Override
    public void update(long deltaTime) {
        // Call base class update which handles physics and components
        super.update(deltaTime);
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Keep original rendering code for compatibility
        g.setColor(color);
        g.fillRect(
            (int)(position.getX() - width / 2),
            (int)(position.getY() - height / 2),
            width,
            height
        );
        
        // Optionally draw a border
        g.setColor(Color.BLACK);
        g.drawRect(
            (int)(position.getX() - width / 2),
            (int)(position.getY() - height / 2),
            width,
            height
        );
    }
    
    @Override
    public void onCollision(PhysicsObject other, Collision collision) {
        // Handle collision, e.g., bounce, change color, play sound, etc.
        
        // For example, you could change color briefly on collision
        flashOnCollision();
    }
    
    /**
     * Temporarily changes the color of the box to indicate a collision.
     * In a real game, you might use a timer to restore the original color.
     */
    private void flashOnCollision() {
        // This is just a placeholder. In a real game, you'd likely use a timer
        // to change the color back after a short delay.
        // For now, we'll just lighten the color
        color = color.brighter();
        
        // Update the RenderComponent color if present
        if (hasComponent(ComponentType.RENDER)) {
            RenderComponent renderer = getComponent(ComponentType.RENDER);
            renderer.setMainColor(color);
        }
    }
    
    /**
     * Gets the color of this box.
     * 
     * @return The color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the color of this box.
     * 
     * @param color The new color
     */
    public void setColor(Color color) {
        this.color = color;
        
        // Update the RenderComponent color if present
        if (hasComponent(ComponentType.RENDER)) {
            RenderComponent renderer = getComponent(ComponentType.RENDER);
            renderer.setMainColor(color);
        }
    }
}