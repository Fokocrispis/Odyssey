// src/game/GameLoop.java
package game;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manages the main game loop with fixed time steps.
 */
public class GameLoop {
    private final int targetFps;
    private final boolean shouldPrintFps;
    private final double nanosecondsPerFrame;

    private final Consumer<Long> update;
    private final Runnable render;
    private final Supplier<Boolean> isRunning;
    private final Runnable onExit;

    private double loopTimeProgress;
    private int frameCount;
    private long previousLoopTime;
    private long previousFrameTime;
    private long timer;
    
    // Static field to store current FPS for display
    private static int currentFPS = 0;

    /**
     * Creates a new game loop.
     * 
     * @param targetFps The target frames per second.
     * @param shouldPrintFps Whether to print FPS to the console.
     * @param update The update function, called with delta time in milliseconds.
     * @param render The render function.
     * @param isRunning Supplier that returns whether the game is running.
     * @param onExit Called when the game loop exits.
     */
    public GameLoop(
            int targetFps,
            boolean shouldPrintFps,
            Consumer<Long> update,
            Runnable render,
            Supplier<Boolean> isRunning,
            Runnable onExit) {
        this.targetFps = targetFps;
        this.shouldPrintFps = shouldPrintFps;
        this.nanosecondsPerFrame = Duration.ofSeconds(1).toNanos() / this.targetFps;

        this.update = update;
        this.render = render;
        this.isRunning = isRunning;
        this.onExit = onExit;

        this.loopTimeProgress = 0;
        this.frameCount = 0;
        this.previousLoopTime = 0;
        this.previousFrameTime = 0;
        this.timer = 0;
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        previousLoopTime = System.nanoTime();
        previousFrameTime = System.currentTimeMillis();
        timer = System.currentTimeMillis();

        while (this.isRunning.get()) {
            // This game loop is made so that the FPS is capped at targetFps
            long currentLoopTime = System.nanoTime();
            loopTimeProgress += (currentLoopTime - previousLoopTime) / nanosecondsPerFrame;
            previousLoopTime = currentLoopTime;

            if (loopTimeProgress >= 1) {
                // Calculate delta time for smooth updates
                long currentFrameTime = System.currentTimeMillis();
                long deltaTime = currentFrameTime - previousFrameTime;
                previousFrameTime = currentFrameTime;

                // Update and render the game
                this.update.accept(deltaTime);
                this.render.run();

                frameCount++;
                loopTimeProgress--;
            }

            // Calculate and display FPS
            boolean hasOneSecondPassed = System.currentTimeMillis() - timer > Duration.ofSeconds(1).toMillis();
            if (hasOneSecondPassed) {
                currentFPS = frameCount; // Update the static FPS counter
                
                if (this.shouldPrintFps) {
                    System.out.println(String.format("FPS: %d", frameCount));
                }
                frameCount = 0;
                timer += Duration.ofSeconds(1).toMillis();
            }
            
            // Sleep a bit to reduce CPU usage
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Call exit handler
        this.onExit.run();
    }
    
    /**
     * Gets the current FPS.
     * 
     * @return The current FPS.
     */
    public static int getCurrentFPS() {
        return currentFPS;
    }
}