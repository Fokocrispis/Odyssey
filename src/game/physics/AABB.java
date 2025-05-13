package game.physics;

import game.Vector2D;

/**
 * Axis-Aligned Bounding Box for collision detection.
 */
public class AABB implements CollisionShape {
    private Vector2D position; // Center position
    private float width;
    private float height;
    
    /**
     * Creates a new AABB.
     * 
     * @param x The center x position.
     * @param y The center y position.
     * @param width The width.
     * @param height The height.
     */
    public AABB(float x, float y, float width, float height) {
        this.position = new Vector2D(x, y);
        this.width = width;
        this.height = height;
    }
    
    /**
     * Creates a new AABB.
     * 
     * @param position The center position.
     * @param width The width.
     * @param height The height.
     */
    public AABB(Vector2D position, float width, float height) {
        this.position = new Vector2D(position);
        this.width = width;
        this.height = height;
    }
    
    /**
     * Gets the left edge of this AABB.
     */
    public float getLeft() {
        return (float)(position.getX() - width / 2);
    }
    
    /**
     * Gets the right edge of this AABB.
     */
    public float getRight() {
        return (float)(position.getX() + width / 2);
    }
    
    /**
     * Gets the top edge of this AABB.
     */
    public float getTop() {
        return (float)(position.getY() - height / 2);
    }
    
    /**
     * Gets the bottom edge of this AABB.
     */
    public float getBottom() {
        return (float)(position.getY() + height / 2);
    }
    
    /**
     * Gets the width of this AABB.
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Gets the height of this AABB.
     */
    public float getHeight() {
        return height;
    }
    
    @Override
    public boolean intersects(CollisionShape other) {
        if (other instanceof AABB) {
            AABB otherAABB = (AABB) other;
            return !(getRight() < otherAABB.getLeft() ||
                   getLeft() > otherAABB.getRight() ||
                   getBottom() < otherAABB.getTop() ||
                   getTop() > otherAABB.getBottom());
        }
        
        // Default broad phase check using bounding boxes
        return getBoundingBox().intersects(other.getBoundingBox());
    }
    
    @Override
    public AABB getBoundingBox() {
        return this; // An AABB is its own bounding box
    }
    
    @Override
    public Vector2D getPosition() {
        return position;
    }
    
    @Override
    public void setPosition(Vector2D position) {
        this.position = new Vector2D(position);
    }
    
    @Override
    public CollisionShape copy() {
        return new AABB(position, width, height);
    }
}