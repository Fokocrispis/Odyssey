package game.physics;

import game.Vector2D;

/**
 * Base interface for collision shapes.
 */
public interface CollisionShape {
    /**
     * Checks if this shape intersects with another.
     * 
     * @param other The other shape.
     * @return True if they intersect, false otherwise.
     */
    boolean intersects(CollisionShape other);
    
    /**
     * Gets the smallest axis-aligned rectangle that contains this shape.
     * 
     * @return The bounding rectangle.
     */
    AABB getBoundingBox();
    
    /**
     * Gets the position of this shape.
     * 
     * @return The position vector.
     */
    Vector2D getPosition();
    
    /**
     * Sets the position of this shape.
     * 
     * @param position The new position.
     */
    void setPosition(Vector2D position);
    
    /**
     * Creates a copy of this shape.
     * 
     * @return A new shape with the same dimensions.
     */
    CollisionShape copy();
}