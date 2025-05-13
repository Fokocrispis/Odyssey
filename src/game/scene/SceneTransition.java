package game.scene;

import java.awt.Graphics2D;

/**
 * Interface for scene transition effects.
 */
public interface SceneTransition {
    /**
     * Updates the transition.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds.
     */
    void update(long deltaTime);
    
    /**
     * Renders the transition.
     * 
     * @param g The graphics context.
     * @param fromScene The scene transitioning from.
     * @param toScene The scene transitioning to.
     */
    void render(Graphics2D g, Scene fromScene, Scene toScene);
    
    /**
     * Checks if the transition is complete.
     * 
     * @return True if complete, false otherwise.
     */
    boolean isComplete();
    
    /**
     * Checks if this is a push transition (adding to stack vs replacing).
     * 
     * @return True if push, false if replace.
     */
    boolean isPush();
}