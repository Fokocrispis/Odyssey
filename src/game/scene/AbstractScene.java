package game.scene;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.GameObject;
import game.camera.Camera;

/**
 * Abstract base class for scenes that provides common functionality.
 * Updated to expose game objects for debugging.
 */
public abstract class AbstractScene implements Scene {
    protected final Game game;
    protected final List<GameObject> gameObjects;
    protected Camera camera;
    protected boolean initialized = false;
    
    /**
     * Creates a new scene.
     * 
     * @param game The game instance.
     */
    public AbstractScene(Game game) {
        this.game = game;
        this.gameObjects = new ArrayList<>();
        
        // Create a camera for this scene
        this.camera = new Camera(game.getWidth(), game.getHeight());
    }
    
    @Override
    public void initialize() {
        if (!initialized) {
            setupCamera();
            createGameObjects();
            initialized = true;
        }
    }
    
    /**
     * Sets up the camera for this scene.
     * Subclasses can override to configure the camera.
     */
    protected void setupCamera() {
        // Default implementation sets world bounds to match game dimensions
        camera.setWorldBounds(game.getWidth() * 3, game.getHeight() * 3);
    }
    
    /**
     * Creates the game objects for this scene.
     * Subclasses should override this to create their specific objects.
     */
    protected abstract void createGameObjects();
    
    @Override
    public void onEnter() {
        if (!initialized) {
            initialize();
        }
    }
    
    @Override
    public void onPause() {
        // Default implementation does nothing
    }
    
    @Override
    public void onResume() {
        // Default implementation does nothing
    }
    
    @Override
    public void onExit() {
        // Default implementation does nothing
    }
    
    @Override
    public void update(long deltaTime) {
        // Update all game objects
        for (GameObject gameObject : gameObjects) {
            gameObject.update(deltaTime);
        }
        
        // Update camera
        camera.update(deltaTime);
    }
    
    @Override
    public void render(Graphics2D g) {
        // Apply camera transformations
        camera.apply(g);
        
        // Render all game objects
        for (GameObject gameObject : gameObjects) {
            gameObject.render(g);
        }
        
        // Reset camera transformations for UI
        camera.reset(g);
        
        // Render UI (subclasses should override for scene-specific UI)
        renderUI(g);
    }
    
    /**
     * Renders UI elements for this scene.
     * Subclasses should override to provide scene-specific UI.
     * 
     * @param g The graphics context.
     */
    protected void renderUI(Graphics2D g) {
        // Default implementation does nothing
    }
    
    /**
     * Adds a game object to the scene.
     * 
     * @param gameObject The game object to add.
     */
    protected void addGameObject(GameObject gameObject) {
        this.gameObjects.add(gameObject);
    }
    
    /**
     * Removes a game object from the scene.
     * 
     * @param gameObject The game object to remove.
     */
    protected void removeGameObject(GameObject gameObject) {
        this.gameObjects.remove(gameObject);
    }
    
    @Override
    public Camera getCamera() {
        return camera;
    }
    
    @Override
    public Game getGame() {
        return game;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets all game objects in this scene.
     * Added for debugging and accessing objects.
     * 
     * @return List of game objects
     */
    public List<GameObject> getGameObjects() {
        return new ArrayList<>(gameObjects);
    }
}