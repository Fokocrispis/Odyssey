package game.entity.component;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import game.Vector2D;
import game.entity.PlayerEntity;
import game.entity.PlayerState;
import game.sprites.Sprite;
import game.sprites.AttackSequenceManager;

/**
 * Handles player attack animations and combat logic.
 * Manages different types of attacks and their hitboxes.
 */
public class PlayerAttackComponent implements Component {
    private final PlayerEntity player;
    private final AttackSequenceManager attackManager;
    
    // Attack states
    private boolean isAttacking = false;
    private boolean isComboAttacking = false;
    private int comboCount = 0;
    private long lastAttackTime = 0;
    private long comboWindow = 500; // Time window for combo inputs in ms
    
    // Attack hitboxes
    private Map<String, Rectangle> attackHitboxes = new HashMap<>();
    
    // Attack damage values
    private Map<String, Integer> attackDamage = new HashMap<>();
    
    /**
     * Creates a new player attack component
     */
    public PlayerAttackComponent(PlayerEntity player) {
        this.player = player;
        this.attackManager = new AttackSequenceManager();
        
        // Load all attack animations
        attackManager.loadAllAnimations();
        
        // Initialize attack hitboxes and damage values
        initializeAttackProperties();
    }
    
    /**
     * Initializes attack hitboxes and damage values
     */
    private void initializeAttackProperties() {
        // Light attack hitbox (relative to player position and facing)
        attackHitboxes.put("light_attack", new Rectangle(50, -30, 80, 60));
        attackDamage.put("light_attack", 10);
        
        // Combo attack hitboxes for each stage
        attackHitboxes.put("combo_attack_1", new Rectangle(60, -40, 90, 80));
        attackHitboxes.put("combo_attack_2", new Rectangle(70, -30, 100, 70));
        attackHitboxes.put("combo_attack_3", new Rectangle(80, -50, 120, 100));
        
        attackDamage.put("combo_attack_1", 8);
        attackDamage.put("combo_attack_2", 12);
        attackDamage.put("combo_attack_3", 20);
        
        // Dash attack (if dash can damage enemies)
        attackHitboxes.put("dash", new Rectangle(30, -20, 60, 50));
        attackDamage.put("dash", 5);
    }
    
    @Override
    public void update(long deltaTime) {
        // Update attack state
        if (isAttacking) {
            updateAttack(deltaTime);
        }
        
        // Check for combo timeout
        if (comboCount > 0 && System.currentTimeMillis() - lastAttackTime > comboWindow) {
            resetCombo();
        }
    }
    
    /**
     * Updates the current attack state
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
     * Handles the completion of an attack animation
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
    
    @Override
    public ComponentType getType() {
        return ComponentType.COMBAT;
    }
    
    /**
     * Starts a light attack
     */
    public void performLightAttack() {
        if (player.isAnimationLocked() || isAttacking) {
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
     * Queues the next attack in a combo
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
     * Starts a dash attack
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
     * Resets the combo state
     */
    private void resetCombo() {
        comboCount = 0;
        isComboAttacking = false;
    }
    
    /**
     * Gets the current combo count
     */
    public int getComboCount() {
        return comboCount;
    }
    
    /**
     * Checks if player is currently in a combo attack
     */
    public boolean isComboAttacking() {
        return isComboAttacking;
    }
    
    /**
     * Renders debug info for attacks (hitboxes, etc.)
     */
    public void renderDebug(Graphics2D g) {
        if (!isAttacking) return;
        
        // Draw current attack hitbox if in debug mode
        String currentAttack = isComboAttacking ? 
            "combo_attack_" + comboCount : "light_attack";
            
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
        g.setColor(new java.awt.Color(255, 0, 0, 128));
        g.fillRect(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
        
        g.setColor(java.awt.Color.RED);
        g.drawRect(
            hitboxX,
            hitboxY,
            baseHitbox.width,
            baseHitbox.height
        );
    }
}