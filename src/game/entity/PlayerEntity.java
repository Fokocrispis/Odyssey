package game.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import game.Game;
import game.Vector2D;
import game.entity.component.BuffComponent;
import game.entity.component.PlayerAnimationComponent;
import game.entity.component.PlayerAttackComponent;
import game.entity.component.PlayerInputComponent;
import game.entity.component.PlayerPhysicsComponent;
import game.entity.component.PlayerStateComponent;
import game.entity.component.PlayerStateComponent.MovementContext;
import game.input.KeyboardInput;
import game.physics.AABB;
import game.physics.Collision;
import game.physics.PhysicsObject;
import game.physics.PhysicsSystem;
import game.sprites.Sprite;

public class PlayerEntity extends AbstractEntity {
    // Reference to the game instance
    private final Game game;
    
    // Dimensions
    private static final int HITBOX_WIDTH = 40;
    private static final int HITBOX_HEIGHT = 140;
    
    // Components
    private final PlayerInputComponent inputComponent;
    private final PlayerPhysicsComponent physicsComponent;
    private final PlayerStateComponent stateComponent;
    private final PlayerAnimationComponent animationComponent;
    private final BuffComponent buffComponent;
    
    // State (minimized but accessible to components)
    private PlayerState currentState = PlayerState.IDLE;
    private MovementContext movementContext = MovementContext.NORMAL;
    private boolean isFacingRight = true;
    private boolean isCrouching = false;
    private boolean isDashing = false;
    private boolean isHooking = false;
    private boolean isTurning = false;
    private boolean isSliding = false;
    private boolean isJumping = false;
    private boolean isFalling = false;
    private boolean isAttacking = false;
    private boolean isCasting = false;
    private boolean wasTryingToMove = false;
    
    // Animation locking system
    private boolean animationLocked = false;
    private long animationLockStartTime = 0;
    private long animationLockDuration = 0;
    private Map<PlayerState, Long> stateLockDurations = new HashMap<>();
    private Map<String, Long> actionCooldowns = new HashMap<>();
    
    // Movement data
    private Vector2D targetVelocity = new Vector2D(0, 0);
    private Vector2D dashDirection = new Vector2D(0, 0);
    private Vector2D hookTarget = null;
    
    // Timing data
    private long stateChangeTime = 0;
    private long actionStartTime = 0;
    private long dashStartTime = 0;
    private long turnStartTime = 0;
    private long slideStartTime = 0;
    private long slideEndTime = 0;
    private long lastGroundTime = 0;
    private long lastSpellTime = 0;
    private long crouchStartTime = 0;
    
    // Jump variables
    private int jumpCount = 0;
    private int maxJumps = 2;
    
    // Combat data
    private static final int MAX_HEALTH = 100;
    private static final int MAX_MANA = 50;
    private static final long SPELL_COOLDOWN = 800;
    private int health = MAX_HEALTH;
    private int mana = MAX_MANA;
    
    // State transition priorities
    private static final int PRIORITY_SLIDING = 100;
    private static final int PRIORITY_DASHING = 90;
    private static final int PRIORITY_JUMPING = 80;
    private static final int PRIORITY_ATTACKING = 70;
    private static final int PRIORITY_CROUCHING = 60;
    private static final int PRIORITY_WALKING = 50;
    private static final int PRIORITY_IDLE = 40;
    
    public PlayerEntity(double x, double y, KeyboardInput input, Game game) {
        super(x, y, HITBOX_WIDTH, HITBOX_HEIGHT);
        
        // Store game reference
        this.game = game;
        
        // Initialize components
        this.inputComponent = new PlayerInputComponent(this, input);
        this.physicsComponent = new PlayerPhysicsComponent(this);
        this.stateComponent = new PlayerStateComponent(this);
        this.animationComponent = new PlayerAnimationComponent(this);
        this.buffComponent = new BuffComponent();
        
        // Add all components to entity
        addComponent(inputComponent);
        addComponent(physicsComponent);
        addComponent(stateComponent);
        addComponent(animationComponent);
        addComponent(buffComponent);
        addComponent(new PlayerAttackComponent(this));
        
        // Initialize physics properties
        this.mass = 1.0f;
        this.affectedByGravity = true;
        this.collisionShape = new AABB(position, HITBOX_WIDTH, HITBOX_HEIGHT);
        this.friction = 0.9f;
        this.restitution = 0.0f;
        
        // Set up animation lock durations
        setupAnimationLocks();
    }
    
    // Alternative constructor for backward compatibility
    public PlayerEntity(double x, double y, KeyboardInput input) {
        this(x, y, input, null);
    }
    
    /**
     * Sets up duration for animation locks based on state
     */
    private void setupAnimationLocks() {
        // Set up animation lock durations for each state
        stateLockDurations.put(PlayerState.SLIDING, 400L); // Slide is locked for 400ms
        stateLockDurations.put(PlayerState.DASHING, 250L); // Dash is locked for 250ms
        stateLockDurations.put(PlayerState.ATTACKING, 300L); // Attack is locked for 300ms
        stateLockDurations.put(PlayerState.LANDING, 200L); // Landing is locked for 200ms
        stateLockDurations.put(PlayerState.JUMPING, 100L); // Jump start is locked for 100ms
        
        // Set up cooldowns for various actions
        actionCooldowns.put("slide", 500L); // Can't slide again for 500ms
        actionCooldowns.put("dash", 600L); // Can't dash again for 600ms
        actionCooldowns.put("attack", 300L); // Can't attack again for 300ms
        actionCooldowns.put("combo", 800L); // Combo cooldown
        actionCooldowns.put("teleport", 800L); // Teleport cooldown
        actionCooldowns.put("short_teleport", 400L); // Short teleport cooldown
        actionCooldowns.put("hook", 1000L); // Hook cooldown
    }
    
    @Override
    public void update(long deltaTime) {
        // Update animation lock status
        updateAnimationLock();
        
        // Update all components
        buffComponent.update(deltaTime);
        
        // Input component should always update to capture inputs
        inputComponent.update(deltaTime);
        
        // Only update physics and state if not animation-locked
        if (!animationLocked) {
            physicsComponent.update(deltaTime);
            stateComponent.update(deltaTime);
        } else {
            // Even if locked, do minimal physics updates
            updateMinimalPhysics(deltaTime);
        }
        
        // Animation component should always update
        animationComponent.update(deltaTime);
        
        // Update base entity physics
        super.update(deltaTime);
    }
    
    /**
     * Updates minimal physics even when animation-locked
     */
    private void updateMinimalPhysics(long deltaTime) {
        float dt = deltaTime / 1000.0f;
        
        // Apply gravity even when locked
        if (isAffectedByGravity()) {
            Vector2D velocity = getVelocity();
            velocity.setY(velocity.getY() + PhysicsSystem.gravity.getY() * dt);
            setVelocity(velocity);
        }
        
        // Limit maximum speeds
        Vector2D vel = getVelocity();
        if (vel.getX() > 1000) vel.setX(1000);
        if (vel.getX() < -1000) vel.setX(-1000);
        if (vel.getY() > 1500) vel.setY(1500);
        setVelocity(vel);
    }
    
    /**
     * Updates the animation lock status
     */
    private void updateAnimationLock() {
        long currentTime = System.currentTimeMillis();
        
        // Check if lock has expired
        if (animationLocked && currentTime - animationLockStartTime >= animationLockDuration) {
            animationLocked = false;
        }
    }
    
    /**
     * Locks animation for the given duration
     */
    public void lockAnimation(long duration) {
        animationLocked = true;
        animationLockStartTime = System.currentTimeMillis();
        animationLockDuration = duration;
    }
    
    /**
     * Locks animation based on the current state
     */
    public void lockAnimationForCurrentState() {
        Long duration = stateLockDurations.get(currentState);
        if (duration != null && duration > 0) {
            lockAnimation(duration);
        }
    }
    
    /**
     * Forcefully unlocks the animation
     */
    public void unlockAnimation() {
        animationLocked = false;
    }
    
    /**
     * Checks if an action is on cooldown
     */
    public boolean isActionOnCooldown(String actionName) {
        Long cooldown = actionCooldowns.get(actionName);
        if (cooldown == null) {
            return false;
        }
        
        long lastTime = 0;
        
        // Map action names to their respective last-used times
        if ("slide".equals(actionName)) {
            lastTime = slideEndTime;
        } else if ("dash".equals(actionName)) {
            lastTime = dashStartTime;
        } else if ("attack".equals(actionName) || "combo".equals(actionName)) {
            lastTime = actionStartTime;
        } else if ("teleport".equals(actionName) || "short_teleport".equals(actionName) || "hook".equals(actionName)) {
            // These use the general action start time if nothing more specific is available
            lastTime = actionStartTime;
        }
        
        return System.currentTimeMillis() - lastTime < cooldown;
    }
    
    /**
     * Checks if transitioning to a new state is allowed based on priority
     * 
     * @param currentPriority Priority of current state
     * @param newPriority Priority of new state
     * @return True if transition is allowed
     */
    public boolean canTransitionTo(int currentPriority, int newPriority) {
        // If animation is locked, only allow higher priority transitions
        if (animationLocked) {
            return newPriority > currentPriority + 20; // Require significantly higher priority to break lock
        }
        
        return newPriority >= currentPriority;
    }
    
    /**
     * Gets the priority of the current state
     */
    public int getCurrentStatePriority() {
        switch (currentState) {
            case SLIDING: return PRIORITY_SLIDING;
            case DASHING: return PRIORITY_DASHING;
            case JUMPING:
            case FALLING: return PRIORITY_JUMPING;
            case ATTACKING: return PRIORITY_ATTACKING;
            case RUNNING:
            case WALKING: return PRIORITY_WALKING;
            case IDLE: return PRIORITY_IDLE;
            default: return PRIORITY_IDLE;
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        animationComponent.render(g);
    }
    
    @Override
    public void onCollision(PhysicsObject other, Collision collision) {
        // Basic collision response
        
        // Reset special states on collision
        if (isDashing && other.getMass() > 0) {
            isDashing = false;
            affectedByGravity = true;
        }
        
        // Handle sliding collisions - end slide if hitting a wall
        if (isSliding && Math.abs(collision.getNormal().getX()) > 0.5) {
            exitSlidingState();
        }
        
        // Check for ground collision
        if (collision.getNormal().getY() < -0.5 && other.getMass() <= 0) {
            setOnGround(true);
        }
    }
    
    // Movement methods
    
    public void jump(double force) {
        // Check if we can transition to jumping
        if (!canTransitionTo(getCurrentStatePriority(), PRIORITY_JUMPING)) {
            return;
        }
        
        Vector2D velocity = getVelocity();
        double jumpMultiplier = buffComponent.getBuffMultiplier(BuffType.JUMP_HEIGHT);
        velocity.setY(force * jumpMultiplier);
        setVelocity(velocity);
        
        isJumping = true;
        isCrouching = false; // Cancel crouching when jumping
        jumpCount++;
        currentState = PlayerState.JUMPING;
        stateChangeTime = System.currentTimeMillis();
        
        // Lock animation for jump-start
        lockAnimationForCurrentState();
    }
    
    public void enterSlidingState() {
        // Check if sliding is on cooldown
        if (isActionOnCooldown("slide")) {
            return;
        }
        
        // Check if we can transition to sliding
        if (!canTransitionTo(getCurrentStatePriority(), PRIORITY_SLIDING)) {
            return;
        }
        
        isSliding = true;
        isCrouching = false; // Can't be both sliding and crouching
        slideStartTime = System.currentTimeMillis();
        
        // Apply initial slide momentum
        double slideSpeed = velocity.getX() * 1.2; // SLIDE_SPEED_MULTIPLIER
        if (Math.abs(slideSpeed) < 600) { // RUN_SPEED
            slideSpeed = isFacingRight ? 600 : -600;
        }
        velocity.setX(slideSpeed);
        
        // Change state
        currentState = PlayerState.SLIDING;
        movementContext = MovementContext.SLIDING;
        stateChangeTime = System.currentTimeMillis();
        
        // Lock animation for sliding
        lockAnimationForCurrentState();
        
        // Update sprite
        updateSpriteForState();
    }
    
    public void exitSlidingState() {
        isSliding = false;
        slideEndTime = System.currentTimeMillis();
        
        // Apply slight speed reduction on exit
        velocity.setX(velocity.getX() * 0.8);
        
        // Update state based on current conditions
        if (isOnGround()) {
            // Check if DOWN is still held
            if (isDownKeyPressed()) {
                enterCrouchingState();
            } else {
                // Normal movement state based on velocity
                if (Math.abs(velocity.getX()) > 450) {
                    currentState = PlayerState.RUNNING;
                    movementContext = MovementContext.RUNNING;
                } else if (Math.abs(velocity.getX()) > 5) {
                    currentState = PlayerState.WALKING;
                    movementContext = MovementContext.NORMAL;
                } else {
                    currentState = PlayerState.IDLE;
                    movementContext = MovementContext.NORMAL;
                }
            }
        } else {
            currentState = velocity.getY() < 0 ? PlayerState.JUMPING : PlayerState.FALLING;
            movementContext = MovementContext.NORMAL;
        }
        
        // Update sprite
        updateSpriteForState();
    }
    
    public void enterCrouchingState() {
        // Check if we can transition to crouching
        if (!canTransitionTo(getCurrentStatePriority(), PRIORITY_CROUCHING)) {
            return;
        }
        
        if (!isCrouching) {
            isCrouching = true;
            crouchStartTime = System.currentTimeMillis();
            
            // Set the appropriate state and context
            if (Math.abs(velocity.getX()) < 5) {
                currentState = PlayerState.IDLE;
            } else {
                currentState = PlayerState.WALKING;
            }
            movementContext = MovementContext.CROUCHING;
            
            // Update sprite
            updateSpriteForState();
        }
    }
    
    public void exitCrouchingState() {
        if (isCrouching) {
            isCrouching = false;
            
            // Reset context if it was crouching
            if (movementContext == MovementContext.CROUCHING) {
                movementContext = MovementContext.NORMAL;
            }
            
            // Update sprite
            updateSpriteForState();
        }
    }
    
    public void startDashing() {
        // Check if dashing is on cooldown
        if (isActionOnCooldown("dash")) {
            return;
        }
        
        // Check if we can transition to dashing
        if (!canTransitionTo(getCurrentStatePriority(), PRIORITY_DASHING)) {
            return;
        }
        
        // Clear crouching when dashing
        isCrouching = false;
        
        isDashing = true;
        dashStartTime = System.currentTimeMillis();
        
        // Calculate dash direction
        double dashX = isFacingRight ? 1 : -1;
        double dashY = 0;
        
        // Allow directional dashing
        if (getGame() != null && getGame().getKeyboardInput() != null) {
            if (getGame().getKeyboardInput().isKeyPressed(java.awt.event.KeyEvent.VK_UP)) {
                dashY = -0.7;
            } else if (getGame().getKeyboardInput().isKeyPressed(java.awt.event.KeyEvent.VK_DOWN)) {
                dashY = 0.7;
            }
        }
        
        // Normalize and apply dash
        Vector2D dashDir = new Vector2D(dashX, dashY);
        if (dashDir.length() > 0) {
            dashDir.normalize();
            Vector2D dashVel = dashDir.times(1200.0); // DASH_SPEED
            setVelocity(dashVel);
            
            // Cancel gravity during dash if buffed
            if (hasActiveBuff(BuffType.GRAVITY_DASH)) {
                setAffectedByGravity(false);
            }
        }
        
        currentState = PlayerState.DASHING;
        movementContext = MovementContext.DASHING;
        stateChangeTime = System.currentTimeMillis();
        
        // Lock animation for dashing
        lockAnimationForCurrentState();
        
        // Add buff effect
        addBuffEffect(BuffType.DASH_IMMUNITY, 250);
        
        // Store dash direction
        dashDirection.set(dashDir);
        
        // Update sprite
        updateSpriteForState();
    }
    
    public void startTurnAnimation(boolean turningRight) {
        isTurning = true;
        turnStartTime = System.currentTimeMillis();
        currentState = PlayerState.RUNNING; // Use running state with turn sprite
        updateSpriteForState();
    }
    
    // Combat methods
    
    public void performBasicAttack() {
        // Check if attacking is on cooldown
        if (isActionOnCooldown("attack")) {
            return;
        }
        
        // Check if we can transition to attacking
        if (!canTransitionTo(getCurrentStatePriority(), PRIORITY_ATTACKING)) {
            return;
        }
        
        isAttacking = true;
        currentState = PlayerState.ATTACKING;
        actionStartTime = System.currentTimeMillis();
        
        // Lock animation for attack
        lockAnimationForCurrentState();
        
        // Create attack hitbox (simplified)
        Rectangle attackBox = new Rectangle(
            (int)(position.getX() + (isFacingRight ? 40 : -80)),
            (int)(position.getY() - 40),
            80, 80
        );
        
        // Update sprite
        updateSpriteForState();
    }
    
    public void performHeavyAttack() {
        // Check if attacking is on cooldown
        if (isActionOnCooldown("attack")) {
            return;
        }
        
        // Check if we can transition to attacking
        if (!canTransitionTo(getCurrentStatePriority(), PRIORITY_ATTACKING)) {
            return;
        }
        
        isAttacking = true;
        currentState = PlayerState.ATTACKING;
        actionStartTime = System.currentTimeMillis();
        
        // Lock animation for heavy attack (longer)
        lockAnimation(500);
        
        // Create larger attack hitbox
        Rectangle attackBox = new Rectangle(
            (int)(position.getX() + (isFacingRight ? 30 : -100)),
            (int)(position.getY() - 50),
            100, 100
        );
        
        // Apply knockback to self
        Vector2D knockback = new Vector2D(isFacingRight ? -100 : 100, -50);
        velocity.add(knockback);
        
        // Update sprite
        updateSpriteForState();
    }
    
    public void castFireball() {
        isCasting = true;
        currentState = PlayerState.CASTING;
        lastSpellTime = System.currentTimeMillis();
        
        // Lock animation for spell casting
        lockAnimation(400);
        
        // Consume mana
        mana -= 5;
        if (mana < 0) mana = 0;
        
        System.out.println("Casting Fireball!");
        
        // Update sprite
        updateSpriteForState();
    }
    
    public void castHeal() {
        isCasting = true;
        currentState = PlayerState.CASTING;
        lastSpellTime = System.currentTimeMillis();
        
        // Lock animation for spell casting
        lockAnimation(400);
        
        // Consume mana
        mana -= 10;
        if (mana < 0) mana = 0;
        
        // Heal player
        health += 20;
        if (health > MAX_HEALTH) health = MAX_HEALTH;
        
        System.out.println("Casting Heal! Health: " + health);
        
        // Update sprite
        updateSpriteForState();
    }
    
    // Integration with other components
    
    public void updateSpriteForState() {
        animationComponent.updateSpriteForState();
    }
    
    public void toggleDebugRender() {
        animationComponent.toggleDebugRender();
    }
    
    public void addBuffEffect(BuffType type, long duration) {
        buffComponent.addBuffEffect(type, duration);
    }
    
    public boolean hasActiveBuff(BuffType type) {
        return buffComponent.hasActiveBuff(type);
    }
    
    public double getBuffMultiplier(BuffType type) {
        return buffComponent.getBuffMultiplier(type);
    }
    
    public Map<BuffType, BuffEffect> getActiveBuffs() {
        return buffComponent.getActiveBuffs();
    }
    
    // Get game reference for components
    public Game getGame() {
        return game;
    }
    
    /**
     * Safe way to check the DOWN key state without requiring Game instance
     * This method will work even if the game reference is null
     */
    public boolean isDownKeyPressed() {
        if (game != null && game.getKeyboardInput() != null) {
            return game.getKeyboardInput().isKeyPressed(java.awt.event.KeyEvent.VK_DOWN);
        }
        return false;
    }
    
    // Animation lock getters
    public boolean isAnimationLocked() {
        return animationLocked;
    }
    
    public long getAnimationLockTimeRemaining() {
        if (!animationLocked) return 0;
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - animationLockStartTime;
        return Math.max(0, animationLockDuration - elapsed);
    }
    
    public long getCrouchStartTime() {
        return crouchStartTime;
    }
    
    // Getters and setters
    
    // State getters/setters
    public PlayerState getCurrentState() { return currentState; }
    public void setCurrentState(PlayerState state) { this.currentState = state; }
    
    public MovementContext getMovementContext() { return movementContext; }
    public void setMovementContext(MovementContext context) { this.movementContext = context; }
    
    public boolean isFacingRight() { return isFacingRight; }
    public void setFacingRight(boolean facingRight) { this.isFacingRight = facingRight; }
    
    public boolean isCrouching() { return isCrouching; }
    public void setCrouching(boolean crouching) { this.isCrouching = crouching; }
    
    public boolean isDashing() { return isDashing; }
    public void setDashing(boolean dashing) { this.isDashing = dashing; }
    
    public boolean isHooking() { return isHooking; }
    public void setHooking(boolean hooking) { this.isHooking = hooking; }
    
    public boolean isTurning() { return isTurning; }
    public void setTurning(boolean turning) { this.isTurning = turning; }
    
    public boolean isSliding() { return isSliding; }
    public void setSliding(boolean sliding) { this.isSliding = sliding; }
    
    public boolean isJumping() { return isJumping; }
    public void setJumping(boolean jumping) { this.isJumping = jumping; }
    
    public boolean isFalling() { return isFalling; }
    public void setFalling(boolean falling) { this.isFalling = falling; }
    
    public boolean isAttacking() { return isAttacking; }
    public void setAttacking(boolean attacking) { this.isAttacking = attacking; }
    
    public boolean isCasting() { return isCasting; }
    public void setCasting(boolean casting) { this.isCasting = casting; }
    
    public boolean wasTryingToMove() { return wasTryingToMove; }
    public void setWasTryingToMove(boolean trying) { this.wasTryingToMove = trying; }
    
    // Movement data getters/setters
    public double getTargetVelocityX() { return targetVelocity.getX(); }
    public void setTargetVelocityX(double x) { this.targetVelocity.setX(x); }
    
    public Vector2D getDashDirection() { return dashDirection; }
    public void setDashDirection(Vector2D direction) { this.dashDirection.set(direction); }
    
    public Vector2D getHookTarget() { return hookTarget; }
    public void setHookTarget(Vector2D target) { this.hookTarget = target; }
    
    // Timing getters/setters
    public long getStateChangeTime() { return stateChangeTime; }
    public void setStateChangeTime(long time) { this.stateChangeTime = time; }
    
    public long getActionStartTime() { return actionStartTime; }
    public void setActionStartTime(long time) { this.actionStartTime = time; }
    
    public long getDashStartTime() { return dashStartTime; }
    public void setDashStartTime(long time) { this.dashStartTime = time; }
    
    public long getTurnStartTime() { return turnStartTime; }
    public void setTurnStartTime(long time) { this.turnStartTime = time; }
    
    public long getSlideStartTime() { return slideStartTime; }
    public void setSlideStartTime(long time) { this.slideStartTime = time; }
    
    public long getSlideEndTime() { return slideEndTime; }
    public void setSlideEndTime(long time) { this.slideEndTime = time; }
    
    public long getLastGroundTime() { return lastGroundTime; }
    public void setLastGroundTime(long time) { this.lastGroundTime = time; }
    
    public long getLastSpellTime() { return lastSpellTime; }
    public void setLastSpellTime(long time) { this.lastSpellTime = time; }
    
    // Jump variables getters/setters
    public int getJumpCount() { return jumpCount; }
    public void setJumpCount(int count) { this.jumpCount = count; }
    
    public int getMaxJumps() { return maxJumps; }
    public void setMaxJumps(int max) { this.maxJumps = max; }
    
    // Combat data getters/setters
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    
    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }
    
    public int getMaxHealth() { return MAX_HEALTH; }
    public int getMaxMana() { return MAX_MANA; }
    public long getSpellCooldown() { return SPELL_COOLDOWN; }
    
    // Sprite getter
    public Sprite getCurrentSprite() { return animationComponent.getCurrentSprite(); }
}