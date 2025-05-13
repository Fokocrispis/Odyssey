package game;

import java.awt.Graphics2D;

/**
 * Base interface for all game objects.
 */
public interface GameObject {
    /**
     * Updates the game object's state.
     * 
     * @param deltaTime The time elapsed since the last update in milliseconds.
     */
    void update(long deltaTime);
    
    /**
     * Renders the game object.
     * 
     * @param g The graphics context.
     */
    void render(Graphics2D g);
}