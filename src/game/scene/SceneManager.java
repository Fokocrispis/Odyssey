package game.scene;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import game.Game;

/**
 * Manages scenes and transitions between them.
 */
public class SceneManager {
    private final Game game;
    private final Map<String, Scene> scenes;
    private final Stack<Scene> sceneStack;
    
    private Scene currentScene;
    private Scene nextScene;
    private boolean isTransitioning = false;
    private SceneTransition currentTransition;
    
    /**
     * Creates a new scene manager.
     * 
     * @param game The game instance.
     */
    public SceneManager(Game game) {
        this.game = game;
        this.scenes = new HashMap<>();
        this.sceneStack = new Stack<>();
        this.currentTransition = null;
    }
    
    /**
     * Registers a scene with the manager.
     * 
     * @param name The name of the scene.
     * @param scene The scene to register.
     */
    public void registerScene(String name, Scene scene) {
        scenes.put(name, scene);
    }
    
    /**
     * Sets the initial scene.
     * 
     * @param name The name of the scene to set.
     */
    public void setInitialScene(String name) {
        if (currentScene == null) {
            Scene scene = scenes.get(name);
            if (scene != null) {
                currentScene = scene;
                sceneStack.push(scene);
                scene.initialize();
                scene.onEnter();
            }
        }
    }
    
    /**
     * Changes to a new scene, replacing the current one.
     * 
     * @param name The name of the scene to change to.
     */
    public void changeScene(String name) {
        changeScene(name, null);
    }
    
    /**
     * Changes to a new scene with a transition effect.
     * 
     * @param name The name of the scene to change to.
     * @param transition The transition effect to use, or null for immediate change.
     */
    public void changeScene(String name, SceneTransition transition) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            nextScene = scene;
            currentTransition = transition;
            isTransitioning = true;
            
            // If no transition, change immediately
            if (transition == null) {
                performSceneTransition();
            }
        }
    }
    
    /**
     * Pushes a new scene onto the stack, pausing the current one.
     * 
     * @param name The name of the scene to push.
     */
    public void pushScene(String name) {
        pushScene(name, null);
    }
    
    /**
     * Pushes a new scene onto the stack with a transition effect.
     * 
     * @param name The name of the scene to push.
     * @param transition The transition effect to use, or null for immediate change.
     */
    public void pushScene(String name, SceneTransition transition) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            nextScene = scene;
            currentTransition = transition;
            isTransitioning = true;
            
            if (transition == null) {
                if (currentScene != null) {
                    currentScene.onPause();
                }
                
                sceneStack.push(scene);
                currentScene = scene;
                
                if (!scene.isInitialized()) {
                    scene.initialize();
                }
                scene.onEnter();
                
                isTransitioning = false;
                nextScene = null;
            }
        }
    }
    
    /**
     * Pops the current scene from the stack, resuming the previous one.
     * 
     * @return True if a scene was popped, false if the stack was empty.
     */
    public boolean popScene() {
        return popScene(null);
    }
    
    /**
     * Pops the current scene with a transition effect.
     * 
     * @param transition The transition effect to use, or null for immediate change.
     * @return True if a scene was popped, false if the stack was empty.
     */
    public boolean popScene(SceneTransition transition) {
        if (sceneStack.size() <= 1) {
            return false;
        }
        
        if (transition == null) {
            if (currentScene != null) {
                currentScene.onExit();
                sceneStack.pop();
            }
            
            currentScene = sceneStack.peek();
            currentScene.onResume();
        } else {
            nextScene = sceneStack.get(sceneStack.size() - 2); // Get the scene below the current one
            currentTransition = transition;
            isTransitioning = true;
        }
        
        return true;
    }
    
    /**
     * Updates the current scene and any active transitions.
     * 
     * @param deltaTime Time elapsed since the last update in milliseconds.
     */
    public void update(long deltaTime) {
        if (isTransitioning && currentTransition != null) {
            currentTransition.update(deltaTime);
            
            if (currentTransition.isComplete()) {
                performSceneTransition();
            }
        }
        
        if (currentScene != null && !isTransitioning) {
            currentScene.update(deltaTime);
        }
    }
    
    /**
     * Performs the scene transition.
     */
    private void performSceneTransition() {
        if (nextScene == null) {
            // This is a pop operation
            if (currentScene != null) {
                currentScene.onExit();
                sceneStack.pop();
            }
            
            currentScene = sceneStack.peek();
            currentScene.onResume();
        } else if (sceneStack.contains(nextScene)) {
            // We're returning to a scene already in the stack
            // Pop scenes until we reach the target scene
            while (currentScene != nextScene) {
                currentScene.onExit();
                sceneStack.pop();
                currentScene = sceneStack.peek();
            }
            currentScene.onResume();
        } else {
            // This is a change or push operation
            boolean isPush = currentTransition != null && currentTransition.isPush();
            
            if (isPush && currentScene != null) {
                currentScene.onPause();
                sceneStack.push(nextScene);
            } else {
                // For a change operation, clear the stack
                if (currentScene != null) {
                    currentScene.onExit();
                }
                sceneStack.clear();
                sceneStack.push(nextScene);
            }
            
            currentScene = nextScene;
            
            if (!currentScene.isInitialized()) {
                currentScene.initialize();
            }
            currentScene.onEnter();
        }
        
        nextScene = null;
        currentTransition = null;
        isTransitioning = false;
    }
    
    /**
     * Renders the current scene and any active transitions.
     * 
     * @param g The graphics context.
     */
    public void render(Graphics2D g) {
        if (currentScene != null) {
            if (isTransitioning && currentTransition != null) {
                currentTransition.render(g, currentScene, nextScene);
            } else {
                currentScene.render(g);
            }
        }
    }
    
    /**
     * Gets the currently active scene.
     * 
     * @return The current scene.
     */
    public Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * Checks if a scene with the given name is registered.
     * 
     * @param name The scene name to check.
     * @return True if the scene exists, false otherwise.
     */
    public boolean hasScene(String name) {
        return scenes.containsKey(name);
    }
    
    /**
     * Gets a registered scene by name.
     * 
     * @param name The scene name.
     * @return The scene, or null if not found.
     */
    public Scene getScene(String name) {
        return scenes.get(name);
    }
}