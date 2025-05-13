package game.entity.component;

import game.Vector2D;
import game.entity.AbstractEntity;

/**
 * Generic physics component for entities
 */
public class PhysicsComponent implements Component {
    protected AbstractEntity entity;
    
    // Physics properties
    protected Vector2D velocity = new Vector2D();
    protected Vector2D acceleration = new Vector2D();
    protected float mass = 1.0f;
    protected boolean affectedByGravity = true;
    protected boolean isCollidable = true;
    protected boolean isOnGround = false;
    protected float friction = 0.5f;
    protected float restitution = 0.0f;
    protected float maxSpeedX = 1000f;
    protected float maxSpeedY = 1500f;
    
    public PhysicsComponent(AbstractEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public void update(long deltaTime) {
        float dt = deltaTime / 1000.0f;
        
        // Apply acceleration to velocity
        velocity.add(acceleration.times(dt));
        
        // Apply velocity limits
        limitVelocity();
        
        // Apply position change
        entity.setX((int) (entity.getX() + velocity.getX() * dt));
        entity.setY((int) (entity.getY() + velocity.getY() * dt));
        
        // Reset acceleration for next frame
        acceleration.set(0, 0);
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.PHYSICS;
    }
    
    /**
     * Applies a force to this entity
     */
    public void applyForce(double forceX, double forceY) {
        if (mass <= 0) return;
        
        // F = ma, so a = F/m
        acceleration.add(forceX / mass, forceY / mass);
    }
    
    /**
     * Applies an impulse (immediate velocity change)
     */
    public void applyImpulse(double impulseX, double impulseY) {
        if (mass <= 0) return;
        
        velocity.add(impulseX / mass, impulseY / mass);
    }
    
    /**
     * Limits velocity to maximum speeds
     */
    protected void limitVelocity() {
        velocity.setX(Math.max(-maxSpeedX, Math.min(maxSpeedX, velocity.getX())));
        velocity.setY(Math.max(-maxSpeedY, Math.min(maxSpeedY, velocity.getY())));
    }
    
    // Getters and setters
    public Vector2D getVelocity() { return velocity; }
    public void setVelocity(Vector2D velocity) { this.velocity.set(velocity); }
    
    public float getMass() { return mass; }
    public void setMass(float mass) { this.mass = mass; }
    
    public boolean isAffectedByGravity() { return affectedByGravity; }
    public void setAffectedByGravity(boolean affected) { this.affectedByGravity = affected; }
    
    public boolean isCollidable() { return isCollidable; }
    public void setCollidable(boolean collidable) { this.isCollidable = collidable; }
    
    public boolean isOnGround() { return isOnGround; }
    public void setOnGround(boolean onGround) { this.isOnGround = onGround; }
    
    public float getFriction() { return friction; }
    public void setFriction(float friction) { this.friction = friction; }
    
    public float getRestitution() { return restitution; }
    public void setRestitution(float restitution) { this.restitution = restitution; }
}