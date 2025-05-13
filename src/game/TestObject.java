// src/game/TestObject.java
package game;

import java.awt.Color;
import java.awt.Graphics2D;

import game.input.KeyboardInput;

/**
 * A simple test game object that moves with keyboard input.
 * Used to test the camera system.
 */
public class TestObject implements GameObject {
    private static final double SPEED = 0.3; // pixels per millisecond
    private static final double DIAGONAL_FACTOR = 0.7071; // 1/sqrt(2) for diagonal movement normalization
    
    private Vector2D position;
    private int width;
    private int height;
    private Color color;
    
    private KeyboardInput input;
    
    /**
     * Creates a new test object.
     */
    public TestObject(KeyboardInput input, double x, double y) {
        this.position = new Vector2D(x, y);
        this.width = 50;
        this.height = 50;
        this.color = Color.RED;
        this.input = input;
    }
    
    @Override
    public void update(long deltaTime) {
        // Track if we're moving diagonally to normalize speed
        boolean horizontal = false;
        boolean vertical = false;
        
        // Calculate movement based on keyboard input
        if (input.isKeyPressed(java.awt.event.KeyEvent.VK_UP)) {
            position.setY(position.getY() - SPEED * deltaTime);
            vertical = true;
        }
        if (input.isKeyPressed(java.awt.event.KeyEvent.VK_DOWN)) {
            position.setY(position.getY() + SPEED * deltaTime);
            vertical = true;
        }
        if (input.isKeyPressed(java.awt.event.KeyEvent.VK_LEFT)) {
            position.setX(position.getX() - SPEED * deltaTime);
            horizontal = true;
        }
        if (input.isKeyPressed(java.awt.event.KeyEvent.VK_RIGHT)) {
            position.setX(position.getX() + SPEED * deltaTime);
            horizontal = true;
        }
        
        // Normalize diagonal movement
        if (horizontal && vertical) {
            // If we moved this frame, and did so diagonally, adjust position to normalize speed
            double adjustX = position.getX();
            double adjustY = position.getY();
            
            if (input.isKeyPressed(java.awt.event.KeyEvent.VK_UP)) {
                adjustY = position.getY() + (SPEED * deltaTime * (1 - DIAGONAL_FACTOR));
            }
            if (input.isKeyPressed(java.awt.event.KeyEvent.VK_DOWN)) {
                adjustY = position.getY() - (SPEED * deltaTime * (1 - DIAGONAL_FACTOR));
            }
            if (input.isKeyPressed(java.awt.event.KeyEvent.VK_LEFT)) {
                adjustX = position.getX() + (SPEED * deltaTime * (1 - DIAGONAL_FACTOR));
            }
            if (input.isKeyPressed(java.awt.event.KeyEvent.VK_RIGHT)) {
                adjustX = position.getX() - (SPEED * deltaTime * (1 - DIAGONAL_FACTOR));
            }
            
            position.set(adjustX, adjustY);
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillRect(
            (int)(position.getX() - width/2), 
            (int)(position.getY() - height/2), 
            width, 
            height
        );
        
        // Draw position indicator line
        g.setColor(Color.WHITE);
        g.drawLine(
            (int)position.getX(), 
            (int)position.getY(), 
            (int)position.getX(), 
            (int)position.getY() - 20
        );
    }
    
    /**
     * Gets the position of this object.
     */
    public Vector2D getPosition() {
        return position;
    }
    
    /**
     * Sets the position of this object.
     */
    public void setPosition(double x, double y) {
        position.set(x, y);
    }
    
    /**
     * Gets the width of this object.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of this object.
     */
    public int getHeight() {
        return height;
    }
}