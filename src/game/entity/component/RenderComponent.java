package game.entity.component;

import java.awt.Color;
import java.awt.Graphics2D;

import game.entity.AbstractEntity;

/**
 * Basic rendering component
 */
public class RenderComponent implements Component {
    protected final AbstractEntity entity;
    private Color mainColor = Color.WHITE;
    private Color outlineColor = Color.BLACK;
    private boolean drawOutline = true;
    
    public RenderComponent(AbstractEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public void update(long deltaTime) {
        // No update needed for basic rendering
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.RENDER;
    }
    
    /**
     * Renders the entity
     */
    public void render(Graphics2D g) {
        if (!entity.isVisible()) return;
        
        // Basic rectangular rendering
        g.setColor(mainColor);
        g.fillRect(
            (int)(entity.getPosition().getX() - entity.getWidth() / 2),
            (int)(entity.getPosition().getY() - entity.getHeight() / 2),
            entity.getWidth(),
            entity.getHeight()
        );
        
        if (drawOutline) {
            g.setColor(outlineColor);
            g.drawRect(
                (int)(entity.getPosition().getX() - entity.getWidth() / 2),
                (int)(entity.getPosition().getY() - entity.getHeight() / 2),
                entity.getWidth(),
                entity.getHeight()
            );
        }
    }
    
    // Getters and setters
    public Color getMainColor() { return mainColor; }
    public void setMainColor(Color color) { this.mainColor = color; }
    
    public Color getOutlineColor() { return outlineColor; }
    public void setOutlineColor(Color color) { this.outlineColor = color; }
    
    public boolean isDrawOutline() { return drawOutline; }
    public void setDrawOutline(boolean draw) { this.drawOutline = draw; }
}