package game.entity.component;

/**
 * Base interface for all entity components
 */
public interface Component {
    /**
     * Updates the component
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    void update(long deltaTime);
    
    /**
     * Gets the type of component
     * @return Component type identifier
     */
    ComponentType getType();
    
    /**
     * Enum for common component types
     */
    enum ComponentType {
        PHYSICS,
        RENDER,
        INPUT,
        STATE,
        ANIMATION,
        BUFF,
        COMBAT,
        AI,
        AUDIO
    }
}