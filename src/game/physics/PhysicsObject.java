package game.physics;

import game.GameObject;
import game.Vector2D;

/**
 * Enhanced interface for objects that can interact with the physics system
 */
public interface PhysicsObject extends GameObject {
    /**
     * Gets the collision bounds for this object.
     * 
     * @return The collision shape.
     */
    CollisionShape getCollisionShape();
    
    /**
     * Gets the position of this object.
     * 
     * @return The position vector.
     */
    Vector2D getPosition();
    
    /**
     * Sets the position of this object.
     * 
     * @param position The new position.
     */
    void setPosition(Vector2D position);
    
    /**
     * Gets the velocity of this object.
     * 
     * @return The velocity vector.
     */
    Vector2D getVelocity();
    
    /**
     * Sets the velocity of this object.
     * 
     * @param velocity The new velocity.
     */
    void setVelocity(Vector2D velocity);
    
    /**
     * Gets the mass of this object.
     * 
     * @return The mass.
     */
    float getMass();
    
    /**
     * Sets the mass of this object.
     * 
     * @param mass The new mass.
     */
    void setMass(float mass);
    
    /**
     * Checks if this object is affected by gravity.
     * 
     * @return True if affected by gravity, false otherwise.
     */
    boolean isAffectedByGravity();
    
    /**
     * Sets whether this object is affected by gravity.
     * 
     * @param affected True if should be affected by gravity, false otherwise.
     */
    void setAffectedByGravity(boolean affected);
    
    /**
     * Checks if this object can collide with others.
     * 
     * @return True if can collide, false otherwise.
     */
    boolean isCollidable();
    
    /**
     * Sets whether this object can collide with others.
     * 
     * @param collidable True if can collide, false otherwise.
     */
    void setCollidable(boolean collidable);
    
    /**
     * Gets the collision layer of this object.
     * 
     * @return The collision layer index.
     */
    int getCollisionLayer();
    
    /**
     * Sets the collision layer of this object.
     * 
     * @param layer The collision layer index.
     */
    void setCollisionLayer(int layer);
    
    /**
     * Checks if this object is currently on the ground.
     * 
     * @return True if on ground, false otherwise.
     */
    boolean isOnGround();
    
    /**
     * Sets whether this object is on the ground.
     * 
     * @param onGround True if on ground, false otherwise.
     */
    void setOnGround(boolean onGround);
    
    /**
     * Called when this object collides with another.
     * 
     * @param other The other object.
     * @param collision The collision information.
     */
    void onCollision(PhysicsObject other, Collision collision);
    
    /**
     * Gets the friction coefficient of this object.
     * 
     * @return The friction coefficient (0.0 to 1.0).
     */
    default float getFriction() {
        return 0.5f;
    }
    
    /**
     * Gets the restitution (bounciness) of this object.
     * 
     * @return The restitution coefficient (0.0 to 1.0).
     */
    default float getRestitution() {
        return 0.0f;
    }
    
    /**
     * Checks if this object is a kinematic body (moves but isn't affected by physics).
     * 
     * @return True if kinematic, false otherwise.
     */
    default boolean isKinematic() {
        return getMass() <= 0 && isCollidable();
    }
    
    /**
     * Gets the drag coefficient for this object.
     * 
     * @return The drag coefficient.
     */
    default float getDrag() {
        return 0.01f;
    }
    
    /**
     * Gets the linear damping for this object.
     * 
     * @return The linear damping coefficient.
     */
    default float getLinearDamping() {
        return 0.95f;
    }
}