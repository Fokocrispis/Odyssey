package game.entity.component;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import game.Game;
import game.TimeManager;
import game.Vector2D;
import game.camera.CinematicCamera;
import game.entity.PlayerEntity;
import game.entity.PlayerState;
import game.sprites.Sprite;
import game.sprites.AttackSequenceManager;

/**
 * Handles player attack animations and combat logic.
 * Extended with ultimate attack ability and cinematic effects.
 */
public class PlayerAttackComponent implements Component {
    private final PlayerEntity player;
    private final AttackSequenceManager attackManager;
    private final Game game;
    
    // Attack states
    private boolean isAttacking = false;
    private boolean isComboAttacking = false;
    private int comboCount = 0;
    private long lastAttackTime = 0;
    private long comboWindow = 500; // Time window for combo inputs in ms
    
    // Ultimate attack state
    private boolean isChargingUltimate = false;
    private boolean isExecutingUltimate = false;
    private long ultimateChargeStartTime = 0;
    private long ultimateChargeTime = 1000; // Charge time in ms
    private long ultimateExecutionTime = 0;
    private long ultimateDuration = 1500; // Duration of the ultimate attack
    private long ultimateCooldown = 8000; // Cooldown in ms
    private long lastUltimateTime = 0;
    private float ultimateChargingProgress = 0f;
    
    // Damage multiplier during ultimate
    private float ultimateDamageMultiplier = 3.0f;
    
    // Ultimate attack parameters
    private static final float ULTIMATE_DASH_SPEED = 2000f;
    private static final int ULTIMATE_DAMAGE = 40;
    private static final int ULTIMATE_RANGE = 400;
    private static final float ULTIMATE_TIME_SCALE = 0.3f;
    private static final int ULTIMATE_MANA_COST = 30;
    
    // Attack hitboxes
    private Map<String, Rectangle> attackHitboxes = new HashMap<>();
    
    // Attack damage values
    private Map<String, Integer> attackDamage = new HashMap<>();
    
    // Effects for ultimate attack
    private boolean hasAppliedSlowmo = false;
    private boolean hasAppliedLetterbox = false;
    private boolean hasAppliedCameraEffects = false;
    
    /**
     * Creates a new player attack component
     */
    public PlayerAttackComponent(PlayerEntity player, Game game) {
        this.player = player;
        this.game = game;
        this.attackManager = new AttackSequenceManager();
        
        // Load all attack animations
        attackManager.loadAllAnimations();
        
        // Initialize attack hitboxes and damage values
        initializeAttackProperties();
    }
    
    // Constructor for backward compatibility
    public PlayerAttackComponent(PlayerEntity player) {
        this(player, null);
    }
    
    /**
     * Initializes attack hitboxes and damage values
     */
    private void initializeAttackProperties() {
        // Original attack hitboxes
        attackHitboxes.put("light_attack", new Rectangle(50, -30, 80, 60));
        attackDamage.put("light_attack", 10);
        
        attackHitboxes.put("combo_attack_1", new Rectangle(60, -40, 90, 80));
        attackHitboxes.put("combo_attack_2", new Rectangle(70, -30, 100, 70));
        attackHitboxes.put("combo_attack_3", new Rectangle(80, -50, 120, 100));
        
        attackDamage.put("combo_attack_1", 8);
        attackDamage.put("combo_attack_2", 12);
        attackDamage.put("combo_attack_3", 20);
        
        attackHitboxes.put("dash", new Rectangle(30, -20, 60, 50));
        attackDamage.put("dash", 5);
        
        // Ultimate attack hitbox - longer horizontal range and higher damage
        attackHitboxes.put("ultimate", new Rectangle(0, -40, ULTIMATE_RANGE, 80));
        attackDamage.put("ultimate", ULTIMATE_DAMAGE);
    }
    
    @Override
    public void update(long deltaTime) {
        // Update attack state
        if (isAttacking) {
            updateAttack(deltaTime);
        }
        
        // Update ultimate attack state
        if (isChargingUltimate) {
            updateUltimateCharging(deltaTime);
        }
        
        if (isExecutingUltimate) {
            updateUltimateExecution(deltaTime);
        }
        
        // Check for combo timeout
        if (comboCount > 0 && System.currentTimeMillis() - lastAttackTime > comboWindow) {
            resetCombo();
        }
    }
    
    /**
     * Updates the ultimate attack charging state
     */
    private void updateUltimateCharging(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - ultimateChargeStartTime;
        
        // Update charge progress
        ultimateChargingProgress = Math.min(1.0f, (float)elapsedTime / ultimateChargeTime);
        
        // Apply visual charge effect - particle effects could be added here
        
        // Apply slow-mo effect as charging progresses
        if (game != null && !hasAppliedSlowmo && ultimateChargingProgress > 0.3f) {
            TimeManager.getInstance().setTimeScale(ULTIMATE_TIME_SCALE, 0.3f, 0f);
            hasAppliedSlowmo = true;
        }
        
        // Apply letterbox effect halfway through charging
        if (game != null && !hasAppliedLetterbox && ultimateChargingProgress > 0.5f) {
            if (game.getSceneManager().getCurrentScene().getCamera() instanceof CinematicCamera) {
                CinematicCamera camera = (CinematicCamera) game.getSceneManager().getCurrentScene().getCamera();
                camera.getEffectsManager().getLetterboxEffect().show(300);
                hasAppliedLetterbox = true;
            }
        }
        
        // Check if charging is complete
        if (elapsedTime >= ultimateChargeTime) {
            // Execute the ultimate attack
            executeUltimateAttack();
        }
        
        // Check for cancellation
        if (player.getInputComponent() != null && 
            player.getInputComponent().isUltimateCancelled()) {
            cancelUltimateCharging();
        }
    }
    
    /**
     * Cancels the ultimate attack charging
     */
    private void cancelUltimateCharging() {
        isChargingUltimate = false;
        ultimateChargingProgress = 0f;
        
        // Reset time scale
        TimeManager.getInstance().resetTimeScale();
        
        // Hide letterbox if it was applied
        if (hasAppliedLetterbox && game != null) {
            if (game.getSceneManager().getCurrentScene().getCamera() instanceof CinematicCamera) {
                CinematicCamera camera = (CinematicCamera) game.getSceneManager().getCurrentScene().getCamera();
                camera.getEffectsManager().getLetterboxEffect().hide(300);
            }
        }
        
        // Reset player state
        player.setCasting(false);
        player.setCurrentState(player.isOnGround() ? PlayerState.IDLE : PlayerState.FALLING);
        player.unlockAnimation();
        
        // Reset flags
        hasAppliedSlowmo = false;
        hasAppliedLetterbox = false;
    }
    
    /**
     * Updates the ultimate attack execution state
     */
    private void updateUltimateExecution(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - ultimateExecutionTime;
        
        // Apply additional camera effects during the execution
        if (!hasAppliedCameraEffects && elapsedTime > 300) {
            if (game != null && game.getSceneManager().getCurrentScene().getCamera() instanceof CinematicCamera) {
                CinematicCamera camera = (CinematicCamera) game.getSceneManager().getCurrentScene().getCamera();
                // Flash effect halfway through execution
                camera.flash(2, new java.awt.Color(255, 200, 100), 0.7f);
                hasAppliedCameraEffects = true;
            }
        }
        
        // Check for collision with enemies during execution
        checkUltimateAttackCollisions();
        
        // Ultimate attack lasts for the defined duration
        if (elapsedTime >= ultimateDuration) {
            // End ultimate attack
            completeUltimateAttack();
        }
    }
    
    /**
     * Checks for collisions with enemies during ultimate attack
     */
    private void checkUltimateAttackCollisions() {
        // Get the ultimate attack hitbox
        Rectangle baseHitbox = attackHitboxes.get("ultimate");
        if (baseHitbox == null) return;
        
        // Adjust hitbox based on player facing direction
        int hitboxX;
        if (player.isFacingRight()) {
            hitboxX = (int)player.getPosition().getX() + baseHitbox.x;
        } else {
            hitboxX = (int)player.getPosition().getX() - baseHitbox.x - baseHitbox.width;
        }
        
        int hitboxY = (int)player.getPosition().getY() + baseHitbox.y;
        
        // Create actual hitbox rectangle
        Rectangle hitbox = new Rectangle(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
        
        // You would typically check for collisions with the physics system here
        // This is a simplified version - in a real implementation, you'd check
        // against all potential entities in the scene
        
        // For example:
        /* 
        for (PhysicsObject obj : game.getPhysicsSystem().getPhysicsObjects()) {
            if (obj instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) obj;
                if (hitbox.intersects(new Rectangle(
                        (int)enemy.getPosition().getX() - enemy.getWidth()/2,
                        (int)enemy.getPosition().getY() - enemy.getHeight()/2,
                        enemy.getWidth(),
                        enemy.getHeight()))) {
                    // Apply damage to enemy
                    enemy.takeDamage(attackDamage.get("ultimate"));
                    
                    // Apply knockback
                    Vector2D knockback = new Vector2D(
                        player.isFacingRight() ? 400 : -400, 
                        -200
                    );
                    enemy.applyImpulse(knockback.getX(), knockback.getY());
                }
            }
        }
        */
    }
    
    /**
     * Starts charging the ultimate attack
     */
    public void chargeUltimateAttack() {
        // Check if on cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUltimateTime < ultimateCooldown) {
            return;
        }
        
        // Check if player has enough mana
        if (player.getMana() < ULTIMATE_MANA_COST) {
            return;
        }
        
        // Check if already attacking or in an animation lock
        if (isAttacking || player.isAnimationLocked()) {
            return;
        }
        
        // Start charging
        isChargingUltimate = true;
        ultimateChargeStartTime = currentTime;
        ultimateChargingProgress = 0f;
        
        // Reset effect flags
        hasAppliedSlowmo = false;
        hasAppliedLetterbox = false;
        hasAppliedCameraEffects = false;
        
        // Set player state
        player.setCurrentState(PlayerState.CASTING);
        player.setCasting(true);
        
        // Disable player input during charge
        player.setInputEnabled(false);
        
        // Lock animation during charge
        player.lockAnimation(ultimateChargeTime);
        
        // Play charge sound effect
        if (game != null) {
            game.getAudioManager().playSoundEffect("charge_ultimate.wav", 0.8f);
        }
        
        // Apply initial camera effect - slight zoom out
        if (game != null && game.getSceneManager().getCurrentScene().getCamera() instanceof CinematicCamera) {
            CinematicCamera camera = (CinematicCamera) game.getSceneManager().getCurrentScene().getCamera();
            camera.setZoom(0.9, 0.9, 0.3);
        }
    }
    
    /**
     * Executes the ultimate attack after charging
     */
    private void executeUltimateAttack() {
        // End charging state
        isChargingUltimate = false;
        isExecutingUltimate = true;
        ultimateExecutionTime = System.currentTimeMillis();
        
        // Consume mana
        player.setMana(player.getMana() - ULTIMATE_MANA_COST);
        
        // Set direction based on player facing
        double dashDirection = player.isFacingRight() ? 1 : -1;
        
        // Apply dash velocity
        Vector2D dashVelocity = new Vector2D(dashDirection * ULTIMATE_DASH_SPEED, 0);
        player.setVelocity(dashVelocity);
        
        // Reset normal gravity during dash
        player.setAffectedByGravity(false);
        
        // Set appropriate sprite for ultimate attack
        PlayerAnimationComponent animComponent = null;
        if (player.hasComponent(ComponentType.ANIMATION)) {
            animComponent = player.getComponent(ComponentType.ANIMATION);
            
            // Try to get the LightCut animation
            Sprite ultimateSprite = animComponent.getAnimation("light_cut");
            if (ultimateSprite != null) {
                ultimateSprite.reset();
                player.setCurrentState(PlayerState.ATTACKING);
                // Lock animation during ultimate attack
                player.lockAnimation(ultimateDuration);
            }
        }
        
        // Create attack hitbox
        Rectangle baseHitbox = attackHitboxes.get("ultimate");
        
        int hitboxX;
        if (player.isFacingRight()) {
            hitboxX = (int)player.getPosition().getX() + baseHitbox.x;
        } else {
            hitboxX = (int)player.getPosition().getX() - baseHitbox.x - baseHitbox.width;
        }
        
        int hitboxY = (int)player.getPosition().getY() + baseHitbox.y;
        
        // Create final hitbox rectangle
        Rectangle hitbox = new Rectangle(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
        
        // Play ultimate attack sound
        if (game != null) {
            game.getAudioManager().playSoundEffect("ultimate_attack.wav", 1.0f);
        }
        
        // Apply cinematic camera effect
        if (game != null && game.getSceneManager().getCurrentScene().getCamera() instanceof CinematicCamera) {
            CinematicCamera camera = (CinematicCamera) game.getSceneManager().getCurrentScene().getCamera();
            
            // Calculate focus target - area in front of player
            double focusX = player.getPosition().getX() + (player.isFacingRight() ? ULTIMATE_RANGE/2 : -ULTIMATE_RANGE/2);
            Vector2D focusTarget = new Vector2D(focusX, player.getPosition().getY());
            
            // Set focus and zoom - wide horizontal view, narrow vertical view
            camera.setFocusTarget(focusTarget, false);
            camera.setZoom(1.2, 0.8, 0.1);
            
            // Apply ultimate attack effect with letterbox
            camera.createUltimateAttackEffect(2000); // 2 seconds of cinematic effect
        }
        
        // Slow down time with TimeManager
        TimeManager.getInstance().setTimeScale(ULTIMATE_TIME_SCALE, 0.1f, 1.0f);
    }
    
    /**
     * Completes the ultimate attack
     */
    private void completeUltimateAttack() {
        isExecutingUltimate = false;
        lastUltimateTime = System.currentTimeMillis();
        
        // Reset player state
        player.setAffectedByGravity(true);
        player.setCasting(false);
        player.setCurrentState(player.isOnGround() ? PlayerState.IDLE : PlayerState.FALLING);
        
        // Re-enable player input
        player.setInputEnabled(true);
        
        // Apply camera flash effect
        if (game != null && game.getSceneManager().getCurrentScene().getCamera() instanceof CinematicCamera) {
            CinematicCamera camera = (CinematicCamera) game.getSceneManager().getCurrentScene().getCamera();
            camera.flash(1, 0.9f);
            
            // Reset camera zoom with slight delay
            camera.setZoomWithReset(1.0, 1.0, 0.5, 0);
            camera.clearFocus();
        }
        
        // Reset time scale if needed
        TimeManager.getInstance().resetTimeScale();
        
        // Play completion sound
        if (game != null) {
            game.getAudioManager().playSoundEffect("ultimate_complete.wav", 0.7f);
        }
    }
    
    /**
     * Performs a light attack
     */
    public void performLightAttack() {
        if (player.isAnimationLocked() || isAttacking || isChargingUltimate || isExecutingUltimate) {
            // Check for combo opportunity
            if (isAttacking && comboCount < 3 && 
                System.currentTimeMillis() - lastAttackTime < comboWindow) {
                queueComboAttack();
            }
            return;
        }
        
        isAttacking = true;
        player.setAttacking(true);
        lastAttackTime = System.currentTimeMillis();
        
        // Set appropriate sprite
        Sprite attackSprite = attackManager.getSprite("light_attack");
        if (attackSprite != null) {
            player.setCurrentState(PlayerState.ATTACKING);
            
            // Reset the sprite animation
            attackSprite.reset();
            
            // Lock animation for attack duration
            player.lockAnimation(400);
            
            // Create attack hitbox (position will be updated during rendering)
            createHitbox("light_attack");
        }
    }
    
    /**
     * Performs a dash attack - implementation from error
     */
    public void performDashAttack() {
        if (player.isAnimationLocked() || !player.isDashing()) return;
        
        isAttacking = true;
        player.setAttacking(true);
        
        // Set appropriate sprite
        Sprite dashSprite = attackManager.getSprite("dash");
        if (dashSprite != null) {
            dashSprite.reset();
            
            // Create dash attack hitbox
            createHitbox("dash");
        }
    }
    
    /**
     * Queues the next combo attack
     */
    private void queueComboAttack() {
        comboCount++;
        isComboAttacking = true;
        lastAttackTime = System.currentTimeMillis();
        
        // Set appropriate combo animation
        Sprite comboSprite = attackManager.getSprite("combo_attack");
        if (comboSprite != null) {
            comboSprite.reset();
            
            // Lock animation for combo duration (slightly longer)
            player.lockAnimation(500);
            
            // Create appropriate hitbox for this combo stage
            createHitbox("combo_attack_" + comboCount);
        }
    }
    
    /**
     * Resets the combo state
     */
    private void resetCombo() {
        comboCount = 0;
        isComboAttacking = false;
    }
    
    /**
     * Updates the current attack
     */
    private void updateAttack(long deltaTime) {
        // Check if attack animation is complete
        Sprite currentSprite = player.getCurrentSprite();
        
        if (currentSprite != null) {
            if (!currentSprite.isLooping() && currentSprite.hasCompleted()) {
                // Attack animation complete
                completeAttack();
            }
        } else if (currentSprite != null && 
                  currentSprite.getFrameIndex() == currentSprite.getTotalFrames() - 1) {
            // Attack animation likely complete based on frame index
            completeAttack();
        }
    }
    
    /**
     * Completes the current attack
     */
    private void completeAttack() {
        isAttacking = false;
        
        // Return to appropriate idle state
        if (player.isOnGround()) {
            player.setCurrentState(PlayerState.IDLE);
        } else {
            player.setCurrentState(player.getVelocity().getY() < 0 ? 
                PlayerState.JUMPING : PlayerState.FALLING);
        }
        
        // Unlock animations
        player.unlockAnimation();
        
        // Update sprite for new state
        player.updateSpriteForState();
    }
    
    /**
     * Creates an attack hitbox based on the specified attack type
     */
    private void createHitbox(String attackType) {
        Rectangle baseHitbox = attackHitboxes.get(attackType);
        if (baseHitbox == null) return;
        
        // Adjust hitbox based on player facing direction
        int hitboxX;
        if (player.isFacingRight()) {
            hitboxX = (int)player.getPosition().getX() + baseHitbox.x;
        } else {
            hitboxX = (int)player.getPosition().getX() - baseHitbox.x - baseHitbox.width;
        }
        
        int hitboxY = (int)player.getPosition().getY() + baseHitbox.y;
        
        // Create actual hitbox rectangle
        Rectangle hitbox = new Rectangle(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
        
        // Register hitbox with physics system for actual damage
        System.out.println("Created " + attackType + " hitbox at: " + hitbox);
    }
    
    /**
     * Checks if the ultimate attack is ready
     */
    public boolean isUltimateReady() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUltimateTime >= ultimateCooldown) && 
               (player.getMana() >= ULTIMATE_MANA_COST);
    }
    
    /**
     * Gets the cooldown progress of the ultimate attack
     * @return 0-1 value representing cooldown progress (1 = ready)
     */
    public float getUltimateCooldownProgress() {
        long currentTime = System.currentTimeMillis();
        long elapsedSinceUse = currentTime - lastUltimateTime;
        return Math.min(1.0f, (float)elapsedSinceUse / ultimateCooldown);
    }
    
    /**
     * Gets the charging progress of the ultimate attack
     * @return 0-1 value representing charging progress
     */
    public float getUltimateChargingProgress() {
        return ultimateChargingProgress;
    }
    
    /**
     * Checks if the ultimate is currently charging
     */
    public boolean isChargingUltimate() {
        return isChargingUltimate;
    }
    
    /**
     * Checks if the ultimate is currently executing
     */
    public boolean isExecutingUltimate() {
        return isExecutingUltimate;
    }
    
    /**
     * Checks if combo attacking is active
     */
    public boolean isComboAttacking() {
        return isComboAttacking;
    }
    
    /**
     * Gets the current combo count
     */
    public int getComboCount() {
        return comboCount;
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.COMBAT;
    }
    
    /**
     * Renders debug info for attacks (hitboxes, etc.)
     */
    public void renderDebug(Graphics2D g) {
        if (!isAttacking && !isChargingUltimate && !isExecutingUltimate) return;
        
        // Draw current attack hitbox if in debug mode
        String currentAttack;
        
        if (isExecutingUltimate) {
            currentAttack = "ultimate";
        } else if (isComboAttacking) {
            currentAttack = "combo_attack_" + comboCount;
        } else {
            currentAttack = "light_attack";
        }
            
        Rectangle baseHitbox = attackHitboxes.get(currentAttack);
        if (baseHitbox == null) return;
        
        // Adjust hitbox based on player facing direction
        int hitboxX;
        if (player.isFacingRight()) {
            hitboxX = (int)player.getPosition().getX() + baseHitbox.x;
        } else {
            hitboxX = (int)player.getPosition().getX() - baseHitbox.x - baseHitbox.width;
        }
        
        int hitboxY = (int)player.getPosition().getY() + baseHitbox.y;
        
        // Draw hitbox rectangle
        if (isExecutingUltimate) {
            // Draw ultimate hitbox with different color
            g.setColor(new java.awt.Color(255, 255, 0, 128));
        } else {
            g.setColor(new java.awt.Color(255, 0, 0, 128));
        }
        
        g.fillRect(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
        
        // Draw outline
        if (isExecutingUltimate) {
            g.setColor(java.awt.Color.YELLOW);
        } else {
            g.setColor(java.awt.Color.RED);
        }
        
        g.drawRect(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
        
        // Draw charging progress bar for ultimate
        if (isChargingUltimate) {
            int barWidth = 100;
            int barHeight = 10;
            int barX = (int)player.getPosition().getX() - barWidth/2;
            int barY = (int)player.getPosition().getY() - player.getHeight()/2 - 20;
            
            // Background
            g.setColor(java.awt.Color.DARK_GRAY);
            g.fillRect(barX, barY, barWidth, barHeight);
            
            // Progress
            g.setColor(java.awt.Color.YELLOW);
            g.fillRect(barX, barY, (int)(barWidth * ultimateChargingProgress), barHeight);
            
            // Text
            g.setColor(java.awt.Color.WHITE);
            g.drawString("ULTIMATE", barX, barY - 5);
        }
    }
}