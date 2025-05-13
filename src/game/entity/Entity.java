package game.entity;

import game.GameObject;
import game.entity.component.Component;
import game.entity.component.Component.ComponentType;

/**
 * Base interface for all game entities
 */
public interface Entity extends GameObject {
    /**
     * Gets the X position
     */
    int getX();
    
    /**
     * Gets the Y position
     */
    int getY();
    
    /**
     * Sets the X position
     */
    void setX(int x);
    
    /**
     * Sets the Y position
     */
    void setY(int y);
    
    /**
     * Gets the width
     */
    int getWidth();
    
    /**
     * Gets the height
     */
    int getHeight();
    
    /**
     * Checks if the entity is visible
     */
    boolean isVisible();
    
    /**
     * Checks if the entity has a component of the specified type
     */
    boolean hasComponent(ComponentType type);
    
    /**
     * Gets a component by type
     */
    <T extends Component> T getComponent(ComponentType type);
    
    /**
     * Adds a component to this entity
     */
    Entity addComponent(Component component);
    
    /**
     * Removes a component from this entity
     */
    Entity removeComponent(ComponentType type);
}