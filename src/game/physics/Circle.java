package game.physics;

import game.Vector2D;

/**
 * Circle collision shape.
 */
public class Circle implements CollisionShape {
    private Vector2D position; // Center position
    private float radius;
    
    /**
     * Creates a new circle.
     * 
     * @param x The center x position.
     * @param y The center y position.
     * @param radius The radius.
     */
    public Circle(float x, float y, float radius) {
        this.position = new Vector2D(x, y);
        this.radius = radius;
    }
    
    /**
     * Creates a new circle.
     * 
     * @param position The center position.
     * @param radius The radius.
     */
    public Circle(Vector2D position, float radius) {
        this.position = new Vector2D(position);
        this.radius = radius;
    }
    
    /**
     * Gets the radius of this circle.
     */
    public float getRadius() {
        return radius;
    }
    
    @Override
    public boolean intersects(CollisionShape other) {
        if (other instanceof Circle) {
            Circle otherCircle = (Circle) other;
            float radiusSum = radius + otherCircle.radius;
            float distanceSquared = (float) position.distanceSquared(otherCircle.position);
            return distanceSquared < radiusSum * radiusSum;
        } else if (other instanceof AABB) {
            AABB aabb = (AABB) other;
            
            // Find the closest point on the AABB to the circle center
            float closestX = Math.max(aabb.getLeft(), Math.min((float)position.getX(), aabb.getRight()));
            float closestY = Math.max(aabb.getTop(), Math.min((float)position.getY(), aabb.getBottom()));
            
            // Calculate the distance between the closest point and circle center
            float distanceX = (float)position.getX() - closestX;
            float distanceY = (float)position.getY() - closestY;
            float distanceSquared = distanceX * distanceX + distanceY * distanceY;
            
            return distanceSquared < radius * radius;
        }
        
        // Default to bounding box check for unknown shapes
        return getBoundingBox().intersects(other.getBoundingBox());
    }
    
    @Override
    public AABB getBoundingBox() {
        return new AABB(position, radius * 2, radius * 2);
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
        return new Circle(position, radius);
    }
}