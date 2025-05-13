package game.entity.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import game.Vector2D;
import game.entity.PlayerEntity;
import game.entity.PlayerState;
import game.entity.component.Component.ComponentType;
import game.sprites.Sprite;
import game.sprites.CharacterAnimationManager;

/**
 * Component that handles player animation and rendering.
 */
public class PlayerAnimationComponent implements Component {
    private final PlayerEntity player;
    private Sprite currentSprite;
    private final Map<PlayerState, Sprite> stateSprites = new HashMap<>();
    private final Map<String, Sprite> contextualSprites = new HashMap<>();
    
    // Character animation manager
    private final CharacterAnimationManager animationManager;
    
    // Sprite dimensions
    private static final int HITBOX_WIDTH = 40;
    private static final int HITBOX_HEIGHT = 140;
    
    private boolean isSpriteLocked = false;
    private long activeSpriteTimer = 0;
    private boolean debugRender = false;
    
    public PlayerAnimationComponent(PlayerEntity player) {
        this.player = player;
        
        // Initialize animation manager with character ID
        this.animationManager = new CharacterAnimationManager("Joanna");
        
        // Load all character animations
        animationManager.loadAllAnimations();
        
        // Map animations to states
        loadSprites();
    }
    
    @Override
    public void update(long deltaTime) {
        updateSprite(deltaTime);
    }
    
    private void loadSprites() {
        // Map character animations to player states
        stateSprites.put(PlayerState.IDLE, animationManager.getAnimation("idle"));
        stateSprites.put(PlayerState.WALKING, animationManager.getAnimation("run"));
        stateSprites.put(PlayerState.RUNNING, animationManager.getAnimation("run"));
        stateSprites.put(PlayerState.ATTACKING, animationManager.getAnimation("light_attack"));
        stateSprites.put(PlayerState.DASHING, animationManager.getAnimation("dash"));
        stateSprites.put(PlayerState.LANDING, animationManager.getAnimation("land"));
        
        // Setup contextual sprites for different states/transitions
        contextualSprites.put("turn_left", animationManager.getAnimation("break_run"));
        contextualSprites.put("turn_right", animationManager.getAnimation("break_run"));
        contextualSprites.put("run_to_stop", animationManager.getAnimation("break_run"));
        contextualSprites.put("run_start", animationManager.getAnimation("to_run"));
        contextualSprites.put("walk", animationManager.getAnimation("player_walk"));
        
        // Common action sprites
        contextualSprites.put("light_attack", animationManager.getAnimation("light_attack"));
        contextualSprites.put("dash", animationManager.getAnimation("dash"));
        contextualSprites.put("land", animationManager.getAnimation("land"));
        
        // Set initial sprite
        currentSprite = stateSprites.get(PlayerState.IDLE);
        
        // Apply manual offsets directly to sprites
        applyManualOffsets();
        
        // Print loaded animations for debugging
        animationManager.printLoadedAnimations();
        
        // Verify sprite offsets after loading
        verifyOffsets();
    }
    
    /**
     * Applies manual offsets directly to sprites
     */
    private void applyManualOffsets() {
        // Idle sprite adjustments
        Sprite idleSprite = stateSprites.get(PlayerState.IDLE);
        if (idleSprite != null) {
            idleSprite.setScale(3.0, 3.0);
            idleSprite.setOffset(0, 30);
        }
        
        // Run sprite adjustments
        Sprite runSprite = stateSprites.get(PlayerState.RUNNING);
        if (runSprite != null) {
            runSprite.setScale(3.0, 3.0);
            runSprite.setOffset(0, 30);
        }
        
        // Run sprite adjustments
        Sprite walkSprite = stateSprites.get(PlayerState.WALKING);
        if (walkSprite != null) {
            walkSprite.setScale(3.0, 3.0);
            walkSprite.setOffset(0, 30);
        }
        
        // Dash sprite adjustments
        Sprite dashSprite = contextualSprites.get("dash");
        if (dashSprite != null) {
            dashSprite.setScale(3.3, 2.8);
            dashSprite.setOffset(15, 30);
        }
        
        // Attack sprite adjustments
        Sprite attackSprite = contextualSprites.get("light_attack");
        if (attackSprite != null) {
            attackSprite.setScale(4.0, 3.0);
            attackSprite.setOffset(30, 30);
        }
        
        // Landing sprite adjustments
        Sprite landSprite = contextualSprites.get("land");
        if (landSprite != null) {
            landSprite.setScale(2.8, 3.0);
            landSprite.setOffset(0, 15);
        }
        
        // Turn sprite adjustments
        Sprite turnSprite = contextualSprites.get("turn_left");
        if (turnSprite != null) {
            turnSprite.setScale(3.1, 3.0);
            turnSprite.setOffset(10, 0);
        }
        
        System.out.println("Applied manual sprite offsets to animations");
    }
    
    /**
     * Verifies sprite offsets are correctly applied
     */
    private void verifyOffsets() {
        if (currentSprite != null) {
            System.out.println("Current sprite offset verification: " + currentSprite.getName() + 
                              ", offsetX: " + currentSprite.getOffsetX() + 
                              ", offsetY: " + currentSprite.getOffsetY());
        }
        
        // Check a few key sprites
        checkSpriteOffset("idle", stateSprites.get(PlayerState.IDLE));
        checkSpriteOffset("run", stateSprites.get(PlayerState.RUNNING));
        checkSpriteOffset("dash", contextualSprites.get("dash"));
    }
    
    private void checkSpriteOffset(String name, Sprite sprite) {
        if (sprite != null) {
            System.out.println("Sprite '" + name + 
                              "' offset: X=" + sprite.getOffsetX() + 
                              ", Y=" + sprite.getOffsetY() + 
                              ", Size=" + sprite.getSize().width + "x" + sprite.getSize().height);
        } else {
            System.out.println("Sprite '" + name + "' is null");
        }
    }
    
    private void updateSprite(long deltaTime) {
        if (currentSprite != null) {
            currentSprite.update(deltaTime);
            
            // Handle non-looping sprites
            if (!currentSprite.isLooping() && currentSprite.hasCompleted()) {
                handleAnimationComplete();
            }
        }
    }
    
    private void handleAnimationComplete() {
        if (player.getCurrentState() == PlayerState.LANDING) {
            player.setCurrentState(PlayerState.IDLE);
            player.setMovementContext(PlayerStateComponent.MovementContext.NORMAL);
            updateSpriteForState();
        } else if (player.getCurrentState() == PlayerState.ATTACKING) {
            player.setAttacking(false);
            player.setCurrentState(PlayerState.IDLE);
            updateSpriteForState();
        }
    }
    
    /**
     * Gets an animation by name
     */
    public Sprite getAnimation(String name) {
        // First check state sprites
        for (Map.Entry<PlayerState, Sprite> entry : stateSprites.entrySet()) {
            if (entry.getKey().name().toLowerCase().equals(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        
        // Then check contextual sprites
        Sprite sprite = contextualSprites.get(name);
        if (sprite != null) {
            return sprite;
        }
        
        // Then check if animation manager has it
        return animationManager.getAnimation(name);
    }
    
    public void updateSpriteForState() {
        Sprite newSprite = null;
        PlayerState currentState = player.getCurrentState();
        PlayerStateComponent.MovementContext context = player.getMovementContext();
        
        // Check if there's a Combat component for special attack states
        PlayerAttackComponent attackComponent = null;
        if (player.hasComponent(ComponentType.COMBAT)) {
            attackComponent = player.getComponent(ComponentType.COMBAT);
        }
        
        // Choose sprite based on state and context
        switch (currentState) {
            case ATTACKING:
                newSprite = contextualSprites.get("light_attack");
                if (newSprite == null) {
                    newSprite = stateSprites.get(PlayerState.ATTACKING);
                }
                break;
                
            case RUNNING:
                if (player.isTurning()) {
                    newSprite = contextualSprites.get("turn_" + (player.isFacingRight() ? "right" : "left"));
                } else {
                    // Check if we're just starting to run
                    double speed = Math.abs(player.getVelocity().getX());
                    long runTime = System.currentTimeMillis() - player.getStateChangeTime();
                    
                    if (runTime < 200 && speed < 400) {
                        // We're just starting to run
                        newSprite = contextualSprites.get("run_start");
                    } else {
                        newSprite = stateSprites.get(PlayerState.RUNNING);
                    }
                }
                break;
                
            case DASHING:
                newSprite = contextualSprites.get("dash");
                if (newSprite == null) {
                    newSprite = stateSprites.get(PlayerState.DASHING);
                }
                break;
                
            case LANDING:
                newSprite = contextualSprites.get("land");
                if (newSprite == null) {
                    newSprite = stateSprites.get(PlayerState.LANDING);
                }
                break;
                
            case WALKING:
                // Check velocity to decide between walking and running
                double speed = Math.abs(player.getVelocity().getX());
                if (speed > 400) {
                    newSprite = stateSprites.get(PlayerState.RUNNING);
                } else {
                    newSprite = stateSprites.get(PlayerState.WALKING);
                }
                break;
                
            default:
                newSprite = stateSprites.get(currentState);
                break;
        }
        
        // Update sprite if changed
        if (newSprite != null && newSprite != currentSprite) {
            currentSprite = newSprite;
            resetCurrentSprite();
            
            // Debug - log the sprite change
            System.out.println("Sprite changed to: " + newSprite.getName() + 
                              ", offsetX: " + newSprite.getOffsetX() + 
                              ", offsetY: " + newSprite.getOffsetY());
        }
    }
    
    private void resetCurrentSprite() {
        if (currentSprite != null) {
            currentSprite.reset();
        }
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.ANIMATION;
    }
    
    public void render(Graphics2D g) {
        if (!player.isVisible() || currentSprite == null) return;
        
        // Get sprite position using its built-in positioning methods
        Vector2D pos = player.getPosition();
        int hitboxHeight = player.getHeight();
        
        // Calculate sprite position using Sprite's methods
        int spriteX = currentSprite.getRenderX(pos.getX());
        int spriteY = currentSprite.getRenderY(pos.getY(), hitboxHeight);
        
        // Get sprite dimensions
        Dimension spriteSize = currentSprite.getSize();
        
        // Draw sprite (flipped if facing left)
        if (player.isFacingRight()) {
            g.drawImage(currentSprite.getFrame(), 
                       spriteX, spriteY, 
                       spriteSize.width, spriteSize.height, null);
        } else {
            g.drawImage(currentSprite.getFrame(), 
                       spriteX + spriteSize.width, spriteY,
                       -spriteSize.width, spriteSize.height, null);
        }
        
        // Draw health and mana bars
        drawHealthManaBar(g);
        
        // Draw hook line if hooking
        if (player.isHooking() && player.getHookTarget() != null) {
            g.setColor(Color.CYAN);
            g.drawLine((int)pos.getX(), (int)pos.getY(), 
                      (int)player.getHookTarget().getX(), (int)player.getHookTarget().getY());
        }
        
        // Draw attack hitboxes if in combat and debug mode
        if (debugRender && player.hasComponent(ComponentType.COMBAT)) {
            PlayerAttackComponent attackComponent = player.getComponent(ComponentType.COMBAT);
            attackComponent.renderDebug(g);
        }
        
        // Draw debug info when enabled
        if (debugRender) {
            renderDebugInfo(g);
        }
    }
    
    private void drawHealthManaBar(Graphics2D g) {
        // Health bar
        int barWidth = 60;
        int barHeight = 8;
        int barX = (int)player.getPosition().getX() - barWidth / 2;
        int barY = (int)player.getPosition().getY() - HITBOX_HEIGHT / 2 - 15; // Above the hitbox
        
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
        
        // Background bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Health bar
        g.setColor(Color.RED);
        int healthWidth = (int)((double)player.getHealth() / player.getMaxHealth() * barWidth);
        g.fillRect(barX, barY, healthWidth, barHeight);
        
        // Mana bar
        barY += 10;
        barHeight = 6;
        
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);
        
        // Background bar
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Mana bar
        g.setColor(Color.BLUE);
        int manaWidth = (int)((double)player.getMana() / player.getMaxMana() * barWidth);
        g.fillRect(barX, barY, manaWidth, barHeight);
    }
    
    private void renderDebugInfo(Graphics2D g) {
        // Draw hitbox
        g.setColor(new Color(255, 0, 0, 128));
        g.drawRect(
            (int)(player.getPosition().getX() - HITBOX_WIDTH / 2),
            (int)(player.getPosition().getY() - HITBOX_HEIGHT / 2),
            HITBOX_WIDTH, 
            HITBOX_HEIGHT
        );
        
        // Draw center point
        g.setColor(Color.YELLOW);
        g.fillOval((int)player.getPosition().getX() - 2, (int)player.getPosition().getY() - 2, 4, 4);
        
        // Display sprite info
        g.setColor(Color.WHITE);
        String spriteInfo = "Unknown";
        
        if (currentSprite != null) {
            spriteInfo = String.format("Sprite: %s [%dx%d] scale(%.1f,%.1f) offset(%d,%d)",
                currentSprite.getName(),
                currentSprite.getSize().width, currentSprite.getSize().height,
                currentSprite.getScaleX(), currentSprite.getScaleY(),
                currentSprite.getOffsetX(), currentSprite.getOffsetY());
        }
        
        g.drawString(spriteInfo, (int)player.getPosition().getX() - 150, (int)player.getPosition().getY() - 100);
        
        // Display state info
        g.drawString("State: " + player.getCurrentState() + ", Context: " + player.getMovementContext(), 
                   (int)player.getPosition().getX() - 80, (int)player.getPosition().getY() - 80);
                   
        // Display flags
        StringBuilder flagsInfo = new StringBuilder();
        if (player.isOnGround()) flagsInfo.append("GROUND ");
        if (player.isCrouching()) flagsInfo.append("CROUCH ");
        if (player.isSliding()) flagsInfo.append("SLIDE ");
        if (player.isDashing()) flagsInfo.append("DASH ");
        if (player.isJumping()) flagsInfo.append("JUMP ");
        if (player.isAttacking()) flagsInfo.append("ATTACK ");
        
        // Add combo info if in combat state
        PlayerAttackComponent attackComponent = null;
        if (player.hasComponent(ComponentType.COMBAT)) {
            attackComponent = player.getComponent(ComponentType.COMBAT);
            if (attackComponent.isComboAttacking()) {
                flagsInfo.append("COMBO(").append(attackComponent.getComboCount()).append(") ");
            }
        }
        
        // Show animation info
        g.drawString("Animation: " + (currentSprite != null ? currentSprite.getName() : "none"), 
                    (int)player.getPosition().getX() - 80, (int)player.getPosition().getY() - 40);
    }
    
    public void toggleDebugRender() {
        debugRender = !debugRender;
    }
    
    public Sprite getCurrentSprite() {
        return currentSprite;
    }
}