// src/game/scene/Scene.java
package game.scene;

import java.awt.Graphics2D;
import game.Game;
import game.camera.Camera;

/**
 * Base interface for all game scenes.
 * A scene represents a distinct state of the game (menu, gameplay, cutscene, etc.)
 */
public interface Scene {
    /**
     * Called when the scene is first created.
     * Used for one-time initialization tasks.
     */
    void initialize();
    
    /**
     * Called when the scene becomes active.
     */
    void onEnter();
    
    /**
     * Called when the scene is no longer active but may become active again.
     */
    void onPause();
    
    /**
     * Called when the scene becomes active again after being paused.
     */
    void onResume();
    
    /**
     * Called when the scene is being removed.
     * Used for cleanup tasks.
     */
    void onExit();
    
    /**
     * Updates the scene.
     * 
     * @param deltaTime Time elapsed since the last update in milliseconds.
     */
    void update(long deltaTime);
    
    /**
     * Renders the scene.
     * 
     * @param g The graphics context.
     */
    void render(Graphics2D g);
    
    /**
     * Gets the camera for this scene.
     * 
     * @return The scene's camera.
     */
    Camera getCamera();
    
    /**
     * Gets the game instance.
     * 
     * @return The game instance.
     */
    Game getGame();
    
    /**
     * Checks if the scene has been initialized.
     * 
     * @return True if the scene has been initialized, false otherwise.
     */
    boolean isInitialized();
}