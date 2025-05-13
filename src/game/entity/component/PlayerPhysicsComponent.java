package game.entity.component;

import game.Vector2D;
import game.entity.BuffType;
import game.entity.PlayerEntity;
import game.entity.PlayerState;
import game.entity.component.Component.ComponentType;
import game.sprites.Sprite;

public class PlayerPhysicsComponent implements Component {
    private final PlayerEntity player;
    
    // Movement parameters
    private static final double WALK_ACCELERATION = 2000.0;
    private static final double RUN_ACCELERATION = 3000.0;
    private static final double CROUCH_ACCELERATION = 1000.0; // Slower acceleration when crouching
    private static final double AIR_CONTROL = 0.6;
    private static final double GROUND_FRICTION = 0.95;
    private static final double SLIDE_FRICTION = 0.992;
    private static final double SLIDE_SPEED_MULTIPLIER = 1.2;
    
    // Timing parameters
    private static final long DASH_DURATION = 250;
    private static final long TURN_ANIMATION_DURATION = 150;
    private static final long SLIDE_DURATION = 600;
    
    public PlayerPhysicsComponent(PlayerEntity player) {
        this.player = player;
    }
    
    @Override
    public void update(long deltaTime) {
        float dt = deltaTime / 1000.0f;
        handleRunningSlowdown(dt);
        long currentTime = System.currentTimeMillis();
        
        // Update states based on timing
        if (player.isDashing() && currentTime - player.getDashStartTime() >= DASH_DURATION) {
            endDash();
        }
        
        if (player.isTurning() && currentTime - player.getTurnStartTime() >= TURN_ANIMATION_DURATION) {
            player.setTurning(false);
        }
        
        // Apply movement
        if (!player.isDashing() && !player.isHooking()) {
            if (player.isSliding()) {
                applySlideMovement(dt, currentTime);
            } else {
                applyMovement(dt);
            }
        }
        
        // Limit maximum speeds
        Vector2D vel = player.getVelocity();
        if (vel.getX() > 1000) vel.setX(1000);
        if (vel.getX() < -1000) vel.setX(-1000);
        if (vel.getY() > 1500) vel.setY(1500);
        player.setVelocity(vel);
    }
    
    private void applyMovement(float dt) {
        Vector2D velocity = player.getVelocity();
        double currentSpeed = velocity.getX();
        double targetSpeed = player.getTargetVelocityX();
        
        // Get movement modifiers
        double speedMultiplier = player.getBuffMultiplier(BuffType.SPEED);
        targetSpeed *= speedMultiplier;
        
        // FIX: Better crouching movement logic
        boolean isCrouching = player.isCrouching();
        
        // Calculate acceleration based on state
        double acceleration;
        
        if (player.isOnGround()) {
            if (isCrouching) {
                // FIX: When crouching, use reduced acceleration and max speed
                acceleration = CROUCH_ACCELERATION;
                targetSpeed *= 0.5; // Half speed while crouching
            } else if (Math.abs(targetSpeed) > 300) {
                // Running
                acceleration = RUN_ACCELERATION;
            } else {
                // Walking
                acceleration = WALK_ACCELERATION;
            }
        } else {
            // In air
            acceleration = WALK_ACCELERATION * AIR_CONTROL;
        }
        
        // Apply acceleration towards target speed
        if (Math.abs(targetSpeed - currentSpeed) < acceleration * dt) {
            velocity.setX(targetSpeed);
        } else if (targetSpeed > currentSpeed) {
            velocity.setX(currentSpeed + acceleration * dt);
        } else if (targetSpeed < currentSpeed) {
            velocity.setX(currentSpeed - acceleration * dt);
        }
        
        // Apply friction when on ground and not trying to move
        if (player.isOnGround() && !player.wasTryingToMove()) {
            // FIX: Higher friction when crouching
            double frictionFactor = isCrouching ? GROUND_FRICTION * 0.9 : GROUND_FRICTION;
            velocity.setX(velocity.getX() * frictionFactor);
            
            // Stop completely if very slow
            if (Math.abs(velocity.getX()) < 5) {
                velocity.setX(0);
            }
        }
        
        player.setVelocity(velocity);
    }
    
    private void applySlideMovement(float dt, long currentTime) {
        if (!player.isSliding()) return;
        
        Vector2D velocity = player.getVelocity();
        
        // Apply sliding friction (higher than normal friction)
        velocity.setX(velocity.getX() * SLIDE_FRICTION);
        
        // FIX: Better slide termination logic - more consistent
        boolean shouldEndSlide = false;
        
        // Check if sliding is done based on duration
        if (currentTime - player.getSlideStartTime() >= SLIDE_DURATION) {
            shouldEndSlide = true;
        }
        // Check if sliding speed is too low
        else if (Math.abs(velocity.getX()) < 150) {  // WALK_SPEED * 0.5
            shouldEndSlide = true;
        }
        
        // End the slide if needed
        if (shouldEndSlide) {
            player.exitSlidingState();
            
            // FIX: Use the safe method to check DOWN key
            boolean downPressed = player.isDownKeyPressed();
            if (downPressed && player.isOnGround()) {
                player.setCrouching(true);
                player.setMovementContext(PlayerStateComponent.MovementContext.CROUCHING);
            }
        }
        
        player.setVelocity(velocity);
    }
    
    private void endDash() {
        player.setDashing(false);
        player.setAffectedByGravity(true);
        
        // Preserve dashing context for landing animations
        if (player.getMovementContext() == PlayerStateComponent.MovementContext.DASHING) {
            if (player.isOnGround()) {
                // FIX: Use the safe method to check DOWN key
                boolean downPressed = player.isDownKeyPressed();
                
                if (downPressed) {
                    // If DOWN is still held, go to crouching
                    player.setCrouching(true);
                    player.setCurrentState(PlayerState.IDLE);
                    player.setMovementContext(PlayerStateComponent.MovementContext.CROUCHING);
                } 
                // If not pressing DOWN, determine next state based on velocity
                else if (Math.abs(player.getVelocity().getX()) > 450) {  // WALK_SPEED * 1.5
                    player.setCurrentState(PlayerState.RUNNING);
                    player.setMovementContext(PlayerStateComponent.MovementContext.RUNNING);
                } else if (Math.abs(player.getVelocity().getX()) > 5) {
                    player.setCurrentState(PlayerState.WALKING);
                    player.setMovementContext(PlayerStateComponent.MovementContext.NORMAL);
                } else {
                    player.setCurrentState(PlayerState.IDLE);
                    player.setMovementContext(PlayerStateComponent.MovementContext.NORMAL);
                }
            } else {
                // If in air, keep dash context but update state
                player.setCurrentState(player.getVelocity().getY() < 0 ? 
                    PlayerState.JUMPING : PlayerState.FALLING);
                // Keep DASHING context
            }
        }
    }
    /**
     * Handles transition from running to walking or idle
     */
    private void handleRunningSlowdown(float dt) {
        // Only handle when on ground and not animation-locked
        if (!player.isOnGround() || player.isAnimationLocked()) return;
        
        // Check if we're running but slowing down
        if (player.getCurrentState() == PlayerState.RUNNING) {
            double speed = Math.abs(player.getVelocity().getX());
            double targetSpeed = Math.abs(player.getTargetVelocityX());
            
            // If we're slowing down significantly
            if (speed > 400 && targetSpeed < 100) {
                // Special transition animation from running to stop
                player.setCurrentState(PlayerState.IDLE);
                player.setMovementContext(PlayerStateComponent.MovementContext.NORMAL);
                
                // Use the run_to_stop animation
                if (player.hasComponent(ComponentType.ANIMATION)) {
                    PlayerAnimationComponent animComponent = player.getComponent(ComponentType.ANIMATION);
                    Sprite runToStopSprite = animComponent.getAnimation("run_to_stop");
                        
                    if (runToStopSprite != null) {
                        // Lock animation for the transition
                        player.lockAnimation(400);
                        player.updateSpriteForState();
                    }
                }
            }
        }
    }

    @Override
    public ComponentType getType() {
        return ComponentType.PHYSICS;
    }
}