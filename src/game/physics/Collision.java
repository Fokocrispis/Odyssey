package game.physics;

import game.Vector2D;

/**
 * Contains information about a collision between two physics objects.
 */
public class Collision {
    private final PhysicsObject objectA;
    private final PhysicsObject objectB;
    private final Vector2D normal;
    private final float penetration;
    private final Vector2D contactPoint;
    
    /**
     * Creates a new collision.
     * 
     * @param objectA The first object.
     * @param objectB The second object.
     * @param normal The collision normal (from A to B).
     * @param penetration The penetration depth.
     * @param contactPoint The point of contact.
     */
    public Collision(PhysicsObject objectA, PhysicsObject objectB, 
                      Vector2D normal, float penetration, Vector2D contactPoint) {
        this.objectA = objectA;
        this.objectB = objectB;
        this.normal = normal;
        this.penetration = penetration;
        this.contactPoint = contactPoint;
    }
    
    /**
     * Gets the first object involved in the collision.
     */
    public PhysicsObject getObjectA() {
        return objectA;
    }
    
    /**
     * Gets the second object involved in the collision.
     */
    public PhysicsObject getObjectB() {
        return objectB;
    }
    
    /**
     * Gets the collision normal vector.
     * Points from objectA to objectB.
     */
    public Vector2D getNormal() {
        return normal;
    }
    
    /**
     * Gets the penetration depth.
     */
    public float getPenetration() {
        return penetration;
    }
    
    /**
     * Gets the point of contact.
     */
    public Vector2D getContactPoint() {
        return contactPoint;
    }
    
    /**
     * Gets the other object involved in the collision.
     * 
     * @param object One of the objects.
     * @return The other object, or null if the provided object is not part of this collision.
     */
    public PhysicsObject getOther(PhysicsObject object) {
        if (object == objectA) return objectB;
        if (object == objectB) return objectA;
        return null;
    }
}