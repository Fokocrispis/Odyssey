package game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Handles keyboard input for the game.
 */
public class KeyboardInput implements KeyListener {
    private static final int KEY_COUNT = 256;
    
    // Arrays to track key states
    private final boolean[] currentKeyState = new boolean[KEY_COUNT];
    private final boolean[] previousKeyState = new boolean[KEY_COUNT];
    
    /**
     * Updates key states at the end of a frame.
     * This should be called once per frame after processing all updates.
     */
    public void update() {
        // Copy current state to previous state
        System.arraycopy(currentKeyState, 0, previousKeyState, 0, KEY_COUNT);
    }
    
    /**
     * Checks if a key is currently pressed.
     * 
     * @param keyCode The key code to check.
     * @return True if the key is pressed, false otherwise.
     */
    public boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT && currentKeyState[keyCode];
    }
    
    /**
     * Checks if a key was just pressed this frame.
     * 
     * @param keyCode The key code to check.
     * @return True if the key was just pressed, false otherwise.
     */
    public boolean isKeyJustPressed(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT && 
               currentKeyState[keyCode] && !previousKeyState[keyCode];
    }
    
    /**
     * Checks if a key was just released this frame.
     * 
     * @param keyCode The key code to check.
     * @return True if the key was just released, false otherwise.
     */
    public boolean isKeyJustReleased(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT && 
               !currentKeyState[keyCode] && previousKeyState[keyCode];
    }
    
    /**
     * Checks if no keys are pressed.
     * 
     * @return True if no keys are pressed, false otherwise.
     */
    public boolean isNothingPressed() {
        for (boolean pressed : currentKeyState) {
            if (pressed) {
                return false;
            }
        }
        return true;
    }
    
    // KeyListener implementation
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            currentKeyState[keyCode] = true;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            currentKeyState[keyCode] = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used, but required by the KeyListener interface
    }
}