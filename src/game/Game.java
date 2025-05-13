package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFrame;

import game.camera.Camera;
import game.input.KeyboardInput;
import game.scene.SceneManager;
import game.physics.PhysicsSystem;

/**
 * Main game class that sets up the window and manages the game loop
 * Updated with enhanced physics system and overridable hook methods
 */
public class Game {
    // Window settings
    private static final String WINDOW_TITLE = "2D Platformer";
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int TARGET_FPS = 60;
    private static final boolean SHOULD_PRINT_FPS = true;
    
    // Game window components
    private final JFrame window;
    private final GamePanel gamePanel;
    
    // Game loop
    private final GameLoop gameLoop;
    private boolean isRunning = false;
    
    // Game systems
    private final KeyboardInput keyboardInput;
    private final SceneManager sceneManager;
    private final PhysicsSystem physicsSystem;
    
    // World size (for physics) - 200m x 10m
    private final float worldWidth = 20000;  // 200m * 100 pixels per meter
    private final float worldHeight = 1000;  // 10m * 100 pixels per meter
    
    /**
     * Creates a new game instance
     */
    public Game() {
        // Initialize systems
        this.keyboardInput = new KeyboardInput();
        this.sceneManager = new SceneManager(this);
        this.physicsSystem = new PhysicsSystem(worldWidth, worldHeight);
        
        // Initialize window
        this.window = new JFrame(WINDOW_TITLE);
        this.gamePanel = new GamePanel(this::render);
        
        // Set up the window
        setupWindow();
        
        // Create game loop
        this.gameLoop = new GameLoop(
            TARGET_FPS,
            SHOULD_PRINT_FPS,
            this::update,
            this::renderGame,
            () -> isRunning,
            this::shutdown
        );
    }
    
    /**
     * Sets up the game window
     */
    private void setupWindow() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(gamePanel);
        window.addKeyListener(keyboardInput);
        window.pack();
        window.setLocationRelativeTo(null); // Center window
        window.setVisible(true);
    }
    
    /**
     * Starts the game
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            
            // Initialize scenes here
            initializeScenes();
            
            // Start the game loop in a separate thread
            Thread gameThread = new Thread(() -> gameLoop.start());
            gameThread.start();
            
            System.out.println("Game started!");
        }
    }
    
    /**
     * Initializes game scenes
     * Override this to add your own scenes
     */
    protected void initializeScenes() {
        // This should be overridden by subclasses to register their scenes
    }
    
    /**
     * Updates the game state
     * Called each frame by the game loop
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    private void update(long deltaTime) {
        // Update scene manager (will update the active scene)
        sceneManager.update(deltaTime);
        
        // Update physics system - this is now our main physics update
        physicsSystem.update(deltaTime);
        
        // Update input state (must be called after all updates)
        keyboardInput.update();
        
        // Call the hook for custom update logic in subclasses
        processCustomUpdates(deltaTime);
    }
    
    /**
     * Hook method for subclasses to add custom update logic.
     * This is called after the main update steps are complete.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    protected void processCustomUpdates(long deltaTime) {
        // To be overridden by subclasses
    }
    
    /**
     * Triggers a repaint of the game panel
     */
    private void renderGame() {
        gamePanel.repaint();
    }
    
    /**
     * Renders the game
     * Called by the game panel when it's being painted
     * 
     * @param g The graphics context
     */
    private void render(Graphics2D g) {
        // Clear the screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Render the current scene
        sceneManager.render(g);
        
        // Render debug info if needed
        if (SHOULD_PRINT_FPS) {
            renderDebugInfo(g);
        }
        
        // Call the hook for custom rendering
        processCustomRendering(g);
    }
    
    /**
     * Hook method for subclasses to add custom rendering.
     * This is called after all standard rendering is complete.
     * 
     * @param g The graphics context
     */
    protected void processCustomRendering(Graphics2D g) {
        // To be overridden by subclasses
    }
    
    /**
     * Renders debug information
     * 
     * @param g The graphics context
     */
    private void renderDebugInfo(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + GameLoop.getCurrentFPS(), 10, 20);
        g.drawString("Objects: " + physicsSystem.getPhysicsObjects().size(), 10, 40);
        
        // Show physics debug info when F3 is pressed
        if (keyboardInput.isKeyPressed(KeyEvent.VK_F3)) {
            g.drawString("Physics System: Enhanced (Robust)", 10, 60);
            g.drawString("World Size: " + worldWidth/100 + "m x " + worldHeight/100 + "m", 10, 80);
            g.drawString("Physics Debug: ON", 10, 100);
        }
    }
    
    /**
     * Shuts down the game
     */
    private void shutdown() {
        System.out.println("Game shutting down...");
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
    
    /**
     * Gets the keyboard input handler
     */
    public KeyboardInput getKeyboardInput() {
        return keyboardInput;
    }
    
    /**
     * Gets the scene manager
     */
    public SceneManager getSceneManager() {
        return sceneManager;
    }
    
    /**
     * Gets the physics system
     */
    public PhysicsSystem getPhysicsSystem() {
        return physicsSystem;
    }
    
    /**
     * Gets the window width
     */
    public int getWidth() {
        return WINDOW_WIDTH;
    }
    
    /**
     * Gets the window height
     */
    public int getHeight() {
        return WINDOW_HEIGHT;
    }
    
    /**
     * Gets the world width
     */
    public float getWorldWidth() {
        return worldWidth;
    }
    
    /**
     * Gets the world height
     */
    public float getWorldHeight() {
        return worldHeight;
    }
    
    /**
     * Inner class for the game's rendering panel
     */
    private class GamePanel extends JComponent {
        private final Consumer<Graphics2D> renderFunction;
        
        public GamePanel(Consumer<Graphics2D> renderFunction) {
            this.renderFunction = renderFunction;
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setDoubleBuffered(true);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            renderFunction.accept((Graphics2D) g);
        }
    }
}