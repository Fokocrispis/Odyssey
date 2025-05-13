package game.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import game.Vector2D;

/**
 * Simplified and robust physics system that properly handles ground collision
 * and prevents objects from constantly trying to go through the floor
 */
public class PhysicsSystem {
    private static final Vector2D DEFAULT_GRAVITY = new Vector2D(0, 1400.0);
    private static final double POSITION_CORRECTION = 0.8;
    private static final double PENETRATION_ALLOWANCE = 0.1;
    private static final double MIN_VELOCITY = 0.1;
    
    private final List<PhysicsObject> physicsObjects;
    public static Vector2D gravity;
    private final float worldWidth;
    private final float worldHeight;
    
    // Collision layers with simple bitmask
    private final Map<String, Integer> layerMap = new HashMap<>();
    private final Map<Integer, Integer> collisionMatrix = new HashMap<>();
    
    public PhysicsSystem(float worldWidth, float worldHeight) {
        this(worldWidth, worldHeight, DEFAULT_GRAVITY);
    }
    
    public PhysicsSystem(float worldWidth, float worldHeight, Vector2D gravity) {
        this.physicsObjects = new ArrayList<>();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.gravity = new Vector2D(gravity);
        
        initializeCollisionLayers();
    }
    
    /**
     * Initialize collision layers with simple bitmask system
     */
    private void initializeCollisionLayers() {
        // Define layers
        layerMap.put("PLAYER", 0);
        layerMap.put("ENEMY", 1);
        layerMap.put("GROUND", 2);
        layerMap.put("PLATFORM", 3);
        layerMap.put("PROJECTILE", 4);
        layerMap.put("TRIGGER", 5);
        
        // Define collision rules (what each layer collides with)
        collisionMatrix.put(0, 0b111110); // Player collides with all except triggers properly
        collisionMatrix.put(1, 0b101110); // Enemy collides with player, ground, platform, projectile
        collisionMatrix.put(2, 0b011111); // Ground collides with all physical objects
        collisionMatrix.put(3, 0b011111); // Platform collides with all physical objects
        collisionMatrix.put(4, 0b001111); // Projectile collides with all except triggers
        collisionMatrix.put(5, 0b000001); // Trigger only collides with player
    }
    
    /**
     * Main physics update loop
     */
    public void update(long deltaTimeMs) {
        float dt = deltaTimeMs / 1000.0f;
        
        // 1. Clear ground state
        for (PhysicsObject obj : physicsObjects) {
            obj.setOnGround(false);
        }
        
        // 2. Apply gravity to all affected objects
        applyGravity(dt);
        
        // 3. Store previous positions
        Map<PhysicsObject, Vector2D> previousPositions = new HashMap<>();
        for (PhysicsObject obj : physicsObjects) {
            previousPositions.put(obj, new Vector2D(obj.getPosition()));
        }
        
        // 4. Update positions based on velocity
        updatePositions(dt);
        
        // 5. Detect and resolve collisions
        for (int iteration = 0; iteration < 4; iteration++) {
            resolveCollisions(previousPositions);
        }
        
        // 6. Apply friction to objects on ground
        applyFriction(dt);
        
        // 7. Constrain to world bounds
        constrainToWorldBounds();
        
        // 8. Clean up tiny velocities
        cleanupVelocities();
    }
    
    /**
     * Apply gravity to all objects affected by it
     */
    private void applyGravity(float dt) {
        for (PhysicsObject obj : physicsObjects) {
            if (obj.isAffectedByGravity() && obj.getMass() > 0) {
                Vector2D velocity = obj.getVelocity();
                velocity.add(gravity.times(dt));
                obj.setVelocity(velocity);
            }
        }
    }
    
    /**
     * Update positions based on current velocities
     */
    private void updatePositions(float dt) {
        for (PhysicsObject obj : physicsObjects) {
            if (obj.getMass() <= 0) continue; // Skip static objects
            
            Vector2D position = obj.getPosition();
            Vector2D velocity = obj.getVelocity();
            
            position.add(velocity.times(dt));
            obj.setPosition(position);
            
            // Update collision shape
            if (obj.getCollisionShape() != null) {
                obj.getCollisionShape().setPosition(position);
            }
        }
    }
    
    /**
     * Detect and resolve all collisions
     */
    private void resolveCollisions(Map<PhysicsObject, Vector2D> previousPositions) {
        for (int i = 0; i < physicsObjects.size(); i++) {
            PhysicsObject objA = physicsObjects.get(i);
            if (!objA.isCollidable()) continue;
            
            for (int j = i + 1; j < physicsObjects.size(); j++) {
                PhysicsObject objB = physicsObjects.get(j);
                if (!objB.isCollidable()) continue;
                
                // Check if they can collide
                if (!canCollide(objA.getCollisionLayer(), objB.getCollisionLayer())) {
                    continue;
                }
                
                // Check for collision
                CollisionInfo collision = checkCollision(objA, objB);
                if (collision != null) {
                    // Resolve the collision
                    resolveCollision(objA, objB, collision);
                    
                    // Trigger callbacks
                    Collision collisionData = new Collision(objA, objB, collision.normal, 
                        collision.penetration, collision.contactPoint);
                    objA.onCollision(objB, collisionData);
                    objB.onCollision(objA, collisionData);
                }
            }
        }
    }
    
    /**
     * Check if two objects are colliding
     */
    private CollisionInfo checkCollision(PhysicsObject objA, PhysicsObject objB) {
        CollisionShape shapeA = objA.getCollisionShape();
        CollisionShape shapeB = objB.getCollisionShape();
        
        if (shapeA == null || shapeB == null) return null;
        
        // For now, we'll only handle AABB collisions
        if (!(shapeA instanceof AABB) || !(shapeB instanceof AABB)) {
            return null;
        }
        
        AABB aabbA = (AABB) shapeA;
        AABB aabbB = (AABB) shapeB;
        
        // Check for overlap
        float overlapX = Math.min(aabbA.getRight(), aabbB.getRight()) - 
                        Math.max(aabbA.getLeft(), aabbB.getLeft());
        float overlapY = Math.min(aabbA.getBottom(), aabbB.getBottom()) - 
                        Math.max(aabbA.getTop(), aabbB.getTop());
        
        if (overlapX <= 0 || overlapY <= 0) return null;
        
        // Determine collision normal and penetration
        Vector2D normal;
        float penetration;
        
        if (overlapX < overlapY) {
            // Horizontal collision
            if (objA.getPosition().getX() < objB.getPosition().getX()) {
                normal = new Vector2D(1, 0);
            } else {
                normal = new Vector2D(-1, 0);
            }
            penetration = overlapX;
        } else {
            // Vertical collision
            if (objA.getPosition().getY() < objB.getPosition().getY()) {
                normal = new Vector2D(0, 1);
            } else {
                normal = new Vector2D(0, -1);
            }
            penetration = overlapY;
        }
        
        // Calculate contact point
        Vector2D contactPoint = new Vector2D(
            (Math.max(aabbA.getLeft(), aabbB.getLeft()) + Math.min(aabbA.getRight(), aabbB.getRight())) / 2,
            (Math.max(aabbA.getTop(), aabbB.getTop()) + Math.min(aabbA.getBottom(), aabbB.getBottom())) / 2
        );
        
        return new CollisionInfo(normal, penetration, contactPoint);
    }
    
    /**
     * Resolve collision between two objects
     */
    private void resolveCollision(PhysicsObject objA, PhysicsObject objB, CollisionInfo collision) {
        float massA = objA.getMass();
        float massB = objB.getMass();
        
        // Skip if both are static
        if (massA <= 0 && massB <= 0) return;
        
        // Position correction
        float correction = (float) (collision.penetration - PENETRATION_ALLOWANCE);
        if (correction > 0) {
            Vector2D correctionVector = collision.normal.times(correction * POSITION_CORRECTION);
            
            if (massA <= 0) {
                // A is static, move B only
                objB.getPosition().add(correctionVector);
            } else if (massB <= 0) {
                // B is static, move A only  
                objA.getPosition().subtract(correctionVector);
            } else {
                // Both dynamic, split correction by mass
                float totalMass = massA + massB;
                float ratioA = massB / totalMass;
                float ratioB = massA / totalMass;
                
                objA.getPosition().subtract(correctionVector.times(ratioA));
                objB.getPosition().add(correctionVector.times(ratioB));
            }
            
            // Update collision shapes
            if (objA.getCollisionShape() != null) objA.getCollisionShape().setPosition(objA.getPosition());
            if (objB.getCollisionShape() != null) objB.getCollisionShape().setPosition(objB.getPosition());
        }
        
        // Set ground state for vertical collisions
        if (Math.abs(collision.normal.getY()) > 0.5) {
            if (collision.normal.getY() > 0 && massA > 0) {
                objA.setOnGround(true);
            }
            if (collision.normal.getY() < 0 && massB > 0) {
                objB.setOnGround(true);
            }
        }
        
        // Velocity resolution
        resolveVelocity(objA, objB, collision);
    }
    
    /**
     * Resolve velocity after collision
     */
    private void resolveVelocity(PhysicsObject objA, PhysicsObject objB, CollisionInfo collision) {
        Vector2D relativeVelocity = objB.getVelocity().minus(objA.getVelocity());
        float velocityAlongNormal = (float)relativeVelocity.dot(collision.normal);
        
        // Don't resolve if velocities are separating
        if (velocityAlongNormal > 0) return;
        
        // Calculate restitution
        float restitution = Math.min(objA.getRestitution(), objB.getRestitution());
        
        // Calculate impulse scalar
        float impulseScalar = -(1 + restitution) * velocityAlongNormal;
        
        float massA = objA.getMass();
        float massB = objB.getMass();
        
        if (massA <= 0 && massB <= 0) return;
        
        if (massA <= 0) {
            impulseScalar /= massB;
        } else if (massB <= 0) {
            impulseScalar /= massA;
        } else {
            impulseScalar /= (1/massA + 1/massB);
        }
        
        // Apply impulse
        Vector2D impulse = collision.normal.times(impulseScalar);
        
        if (massA > 0) objA.getVelocity().subtract(impulse.times(1/massA));
        if (massB > 0) objB.getVelocity().add(impulse.times(1/massB));
        
        // Apply friction
        applyFrictionToCollision(objA, objB, collision, impulseScalar);
    }
    
    /**
     * Apply friction during collision
     */
    private void applyFrictionToCollision(PhysicsObject objA, PhysicsObject objB, CollisionInfo collision, float impulseScalar) {
        Vector2D relativeVelocity = objB.getVelocity().minus(objA.getVelocity());
        
        // Calculate tangent
        Vector2D tangent = relativeVelocity.minus(collision.normal.times(relativeVelocity.dot(collision.normal)));
        if (tangent.length() < 0.001) return;
        tangent.normalize();
        
        // Calculate friction impulse
        float frictionCoefficient = (objA.getFriction() + objB.getFriction()) / 2;
        float frictionImpulse = (float) -relativeVelocity.dot(tangent);
        
        float massA = objA.getMass();
        float massB = objB.getMass();
        
        if (massA <= 0 && massB <= 0) return;
        
        if (massA <= 0) {
            frictionImpulse /= massB;
        } else if (massB <= 0) {
            frictionImpulse /= massA;
        } else {
            frictionImpulse /= (1/massA + 1/massB);
        }
        
        // Clamp friction
        if (Math.abs(frictionImpulse) < Math.abs(impulseScalar * frictionCoefficient)) {
            // Static friction
            Vector2D frictionVector = tangent.times(frictionImpulse);
            if (massA > 0) objA.getVelocity().subtract(frictionVector.times(1/massA));
            if (massB > 0) objB.getVelocity().add(frictionVector.times(1/massB));
        } else {
            // Kinetic friction
            Vector2D frictionVector = tangent.times(impulseScalar * frictionCoefficient);
            if (massA > 0) objA.getVelocity().subtract(frictionVector.times(1/massA));
            if (massB > 0) objB.getVelocity().add(frictionVector.times(1/massB));
        }
    }
    
    /**
     * Apply friction to objects on ground
     */
    private void applyFriction(float dt) {
        for (PhysicsObject obj : physicsObjects) {
            if (obj.isOnGround() && obj.getMass() > 0) {
                Vector2D velocity = obj.getVelocity();
                
                // Apply ground friction to horizontal movement
                double friction = obj.getFriction() * 1500.0 * dt;
                
                if (Math.abs(velocity.getX()) < friction) {
                    velocity.setX(0);
                } else {
                    double sign = velocity.getX() > 0 ? 1 : -1;
                    velocity.setX(velocity.getX() - sign * friction);
                }
                
                obj.setVelocity(velocity);
            }
        }
    }
    
    /**
     * Constrain objects to world boundaries
     */
    private void constrainToWorldBounds() {
        for (PhysicsObject object : physicsObjects) {
            if (object.getMass() <= 0) continue;
            
            AABB bounds = object.getCollisionShape().getBoundingBox();
            Vector2D position = object.getPosition();
            Vector2D velocity = object.getVelocity();
            boolean changed = false;
            
            // Left boundary
            if (bounds.getLeft() < 0) {
                position.setX(bounds.getWidth() / 2);
                if (velocity.getX() < 0) velocity.setX(-velocity.getX() * object.getRestitution());
                changed = true;
            }
            
            // Right boundary
            if (bounds.getRight() > worldWidth) {
                position.setX(worldWidth - bounds.getWidth() / 2);
                if (velocity.getX() > 0) velocity.setX(-velocity.getX() * object.getRestitution());
                changed = true;
            }
            
            // Top boundary
            if (bounds.getTop() < 0) {
                position.setY(bounds.getHeight() / 2);
                if (velocity.getY() < 0) velocity.setY(-velocity.getY() * object.getRestitution());
                changed = true;
            }
            
            // Bottom boundary
            if (bounds.getBottom() > worldHeight) {
                position.setY(worldHeight - bounds.getHeight() / 2);
                if (velocity.getY() > 0) {
                    velocity.setY(-velocity.getY() * object.getRestitution());
                    object.setOnGround(true);
                }
                changed = true;
            }
            
            if (changed) {
                object.setVelocity(velocity);
                object.setPosition(position);
                if (object.getCollisionShape() != null) {
                    object.getCollisionShape().setPosition(position);
                }
            }
        }
    }
    
    /**
     * Clean up tiny velocities to prevent jitter
     */
    private void cleanupVelocities() {
        for (PhysicsObject object : physicsObjects) {
            if (object.getMass() <= 0) continue;
            
            Vector2D velocity = object.getVelocity();
            
            if (Math.abs(velocity.getX()) < MIN_VELOCITY) velocity.setX(0);
            if (Math.abs(velocity.getY()) < MIN_VELOCITY && object.isOnGround()) velocity.setY(0);
            
            object.setVelocity(velocity);
        }
    }
    
    /**
     * Check if two layers can collide
     */
    private boolean canCollide(int layerA, int layerB) {
        Integer maskA = collisionMatrix.get(layerA);
        if (maskA == null) return false;
        
        return (maskA & (1 << layerB)) != 0;
    }
    
    // Utility methods
    public void addObject(PhysicsObject object, String layerName) {
        if (!physicsObjects.contains(object)) {
            physicsObjects.add(object);
            Integer layerIndex = layerMap.get(layerName);
            if (layerIndex != null) {
                object.setCollisionLayer(layerIndex);
            } else {
                object.setCollisionLayer(0); // Default layer
            }
        }
    }
    
    public void addObject(PhysicsObject object) {
        addObject(object, "PLAYER");
    }
    
    public void removeObject(PhysicsObject object) {
        physicsObjects.remove(object);
    }
    
    public List<PhysicsObject> getPhysicsObjects() {
        return new ArrayList<>(physicsObjects);
    }
    
    public void setGravity(Vector2D gravity) {
        this.gravity.set(gravity);
    }
    
    public Vector2D getGravity() {
        return new Vector2D(gravity);
    }
}

/**
 * Simple collision info structure
 */
class CollisionInfo {
    public final Vector2D normal;
    public final float penetration;
    public final Vector2D contactPoint;
    
    public CollisionInfo(Vector2D normal, float penetration, Vector2D contactPoint) {
        this.normal = normal;
        this.penetration = penetration;
        this.contactPoint = contactPoint;
    }
}