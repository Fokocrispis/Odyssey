package game.entity;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import game.GameObject;
import game.Vector2D;
import game.entity.component.Component;
import game.entity.component.Component.ComponentType;
import game.entity.component.RenderComponent;

/**
 * Base entity class that supports components
 */
public abstract class ComponentEntity implements GameObject, Entity {
    // Map of components by type
    protected final Map<ComponentType, Component> components = new HashMap<>();
    
    // Basic entity data
    protected Vector2D position;
    protected int width;
    protected int height;
    protected boolean active = true;
    protected boolean visible = true;
    
    public ComponentEntity(int x, int y, int width, int height) {
        this.position = new Vector2D(x, y);
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void update(long deltaTime) {
        if (!active) return;
        
        // Update all components
        for (Component component : components.values()) {
            component.update(deltaTime);
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Use the render component if available
        if (hasComponent(ComponentType.RENDER)) {
            RenderComponent renderer = getComponent(ComponentType.RENDER);
            renderer.render(g);
        }
    }
    
    /**
     * Adds a component to this entity
     */
    public ComponentEntity addComponent(Component component) {
        components.put(component.getType(), component);
        return this;
    }
    
    /**
     * Checks if the entity has a component of the specified type
     */
    public boolean hasComponent(ComponentType type) {
        return components.containsKey(type);
    }
    
    /**
     * Gets a component by type
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(ComponentType type) {
        return (T) components.get(type);
    }
    
    /**
     * Removes a component from this entity
     */
    public ComponentEntity removeComponent(ComponentType type) {
        components.remove(type);
        return this;
    }
    
    // Entity interface implementation
    @Override
    public int getX() {
        return (int)position.getX();
    }
    
    @Override
    public int getY() {
        return (int)position.getY();
    }
    
    @Override
    public void setX(int x) {
        position.setX(x);
    }
    
    @Override
    public void setY(int y) {
        position.setY(y);
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public boolean isVisible() {
        return visible;
    }
    
    // Additional getters/setters
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public Vector2D getPosition() {
        return position;
    }
    
    public void setPosition(Vector2D position) {
        this.position.set(position);
    }
}