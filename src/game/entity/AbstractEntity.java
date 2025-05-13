package game.entity;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import game.GameObject;
import game.Vector2D;
import game.entity.component.Component;
import game.entity.component.Component.ComponentType;
import game.physics.AABB;
import game.physics.Collision;
import game.physics.CollisionShape;
import game.physics.PhysicsObject;

/**
 * Base class for game entities with component support
 */
public abstract class AbstractEntity implements GameObject, PhysicsObject, Entity {
    // Position and size
    protected Vector2D position;
    protected Vector2D velocity;
    protected Vector2D acceleration;
    protected int width;
    protected int height;
    
    // Physics properties
    protected float mass;
    protected boolean affectedByGravity;
    protected boolean collidable;
    protected CollisionShape collisionShape;
    protected int collisionLayer = 0;
    protected boolean onGround = false;
    
    // Material properties
    protected float friction = 0.5f;
    protected float restitution = 0.0f;
    protected float drag = 0.01f;
    protected float linearDamping = 0.95f;
    
    // Speed limits
    protected float maxSpeedX = 1000f;
    protected float maxSpeedY = 1500f;
    
    // Entity state
    protected boolean active = true;
    protected boolean visible = true;
    
    // Entity identification
    protected String tag = "";
    protected String name = "";
    
    // Component system
    protected Map<ComponentType, Component> components = new HashMap<>();
    
    /**
     * Creates a new entity with position and size.
     */
    public AbstractEntity(double x, double y, int width, int height) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
        this.width = width;
        this.height = height;
        
        // Default physics properties
        this.mass = 1.0f;
        this.affectedByGravity = true;
        this.collidable = true;
        
        // Create default AABB collision shape
        this.collisionShape = new AABB(position, width, height);
    }
    
    @Override
    public void update(long deltaTime) {
        if (!active) return;
        
        // Update components first
        for (Component component : components.values()) {
            component.update(deltaTime);
        }
        
        // Update acceleration (if any forces are applied)
        velocity.add(acceleration.times(deltaTime / 1000.0));
        
        // Apply velocity limits
        velocity.setX(Math.max(-maxSpeedX, Math.min(maxSpeedX, velocity.getX())));
        velocity.setY(Math.max(-maxSpeedY, Math.min(maxSpeedY, velocity.getY())));
        
        // Apply linear damping
        if (!onGround) {
            velocity.multiply(Math.pow(linearDamping, deltaTime / 1000.0));
        }
        
        // Reset acceleration for next frame
        acceleration.set(0, 0);
        
        // Update collision shape position
        if (collisionShape != null) {
            collisionShape.setPosition(position);
        }
    }
    
    /**
     * Adds a component to this entity
     */
    @Override
    public Entity addComponent(Component component) {
        components.put(component.getType(), component);
        return this;
    }
    
    /**
     * Checks if the entity has a component of the specified type
     */
    @Override
    public boolean hasComponent(ComponentType type) {
        return components.containsKey(type);
    }
    
    /**
     * Gets a component by type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(ComponentType type) {
        return (T) components.get(type);
    }
    
    /**
     * Removes a component from this entity
     */
    @Override
    public Entity removeComponent(ComponentType type) {
        components.remove(type);
        return this;
    }
    
    // PhysicsObject implementation
    
    @Override
    public CollisionShape getCollisionShape() {
        return collisionShape;
    }
    
    @Override
    public Vector2D getPosition() {
        return position;
    }
    
    @Override
    public void setPosition(Vector2D position) {
        this.position.set(position);
        if (collisionShape != null) {
            collisionShape.setPosition(position);
        }
    }
    
    @Override
    public Vector2D getVelocity() {
        return velocity;
    }
    
    @Override
    public void setVelocity(Vector2D velocity) {
        this.velocity.set(velocity);
    }
    
    @Override
    public float getMass() {
        return mass;
    }
    
    @Override
    public void setMass(float mass) {
        this.mass = mass;
    }
    
    @Override
    public boolean isAffectedByGravity() {
        return affectedByGravity;
    }
    
    @Override
    public void setAffectedByGravity(boolean affected) {
        this.affectedByGravity = affected;
    }
    
    @Override
    public boolean isCollidable() {
        return collidable;
    }
    
    @Override
    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }
    
    @Override
    public int getCollisionLayer() {
        return collisionLayer;
    }
    
    @Override
    public void setCollisionLayer(int layer) {
        this.collisionLayer = layer;
    }
    
    @Override
    public boolean isOnGround() {
        return onGround;
    }
    
    @Override
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
    
    @Override
    public float getFriction() {
        return friction;
    }
    
    @Override
    public float getRestitution() {
        return restitution;
    }
    
    @Override
    public float getDrag() {
        return drag;
    }
    
    @Override
    public float getLinearDamping() {
        return linearDamping;
    }
    
    // Entity interface implementation
    @Override
    public int getX() {
        return (int)position.getX();
    }
    
    @Override
    public int getY() {
        return (int)position.getY();
    }
    
    @Override
    public void setX(int x) {
        position.setX(x);
        if (collisionShape != null) {
            collisionShape.setPosition(position);
        }
    }
    
    @Override
    public void setY(int y) {
        position.setY(y);
        if (collisionShape != null) {
            collisionShape.setPosition(position);
        }
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public boolean isVisible() {
        return visible;
    }
    
    // Standard entity methods
    
    /**
     * Sets the position of this entity.
     */
    public void setPosition(double x, double y) {
        position.set(x, y);
        if (collisionShape != null) {
            collisionShape.setPosition(position);
        }
    }
    
    /**
     * Applies a force to this entity.
     */
    public void applyForce(double forceX, double forceY) {
        if (mass <= 0) return;
        
        // F = ma, so a = F/m
        acceleration.add(forceX / mass, forceY / mass);
    }
    
    /**
     * Applies an impulse to this entity.
     */
    public void applyImpulse(double impulseX, double impulseY) {
        if (mass <= 0) return;
        
        // Apply impulse directly to velocity
        velocity.add(impulseX / mass, impulseY / mass);
    }
    
    /**
     * Sets the friction coefficient.
     */
    public void setFriction(float friction) {
        this.friction = Math.max(0.0f, Math.min(1.0f, friction));
    }
    
    /**
     * Sets the restitution (bounciness).
     */
    public void setRestitution(float restitution) {
        this.restitution = Math.max(0.0f, Math.min(1.0f, restitution));
    }
    
    /**
     * Sets the collision shape for this entity.
     */
    public void setCollisionShape(CollisionShape shape) {
        this.collisionShape = shape;
        if (shape != null) {
            shape.setPosition(position);
        }
    }
    
    /**
     * Sets the size of this entity.
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        
        // Update collision shape
        if (collisionShape instanceof AABB) {
            collisionShape = new AABB(position, width, height);
        }
    }
    
    /**
     * Checks if this entity is active.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Sets whether this entity is active.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Sets whether this entity is visible.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Gets the tag of this entity.
     */
    public String getTag() {
        return tag;
    }
    
    /**
     * Sets the tag of this entity.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    /**
     * Gets the name of this entity.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of this entity.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets maximum speeds for this entity.
     */
    public void setMaxSpeed(float maxSpeedX, float maxSpeedY) {
        this.maxSpeedX = maxSpeedX;
        this.maxSpeedY = maxSpeedY;
    }
    
    /**
     * Gets the maximum horizontal speed.
     */
    public float getMaxSpeedX() {
        return maxSpeedX;
    }
    
    /**
     * Gets the maximum vertical speed.
     */
    public float getMaxSpeedY() {
        return maxSpeedY;
    }
}