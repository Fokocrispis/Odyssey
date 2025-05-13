package game.entity.component;

import java.awt.event.KeyEvent;
import game.Vector2D;
import game.entity.BuffType;
import game.entity.PlayerEntity;
import game.entity.PlayerState;
import game.entity.component.Component.ComponentType;
import game.input.KeyboardInput;

public class PlayerInputComponent implements Component {
    private final PlayerEntity player;
    private final KeyboardInput input;
    
    // Movement parameters
    private static final double WALK_SPEED = 300.0;
    private static final double RUN_SPEED = 600.0;
    private static final double JUMP_FORCE = -750.0;
    private static final double DOUBLE_JUMP_FORCE = -650.0;
    
    // Timing parameters
    private static final long COYOTE_TIME = 150;
    private static final long JUMP_BUFFER_TIME = 200;
    private static final long DOWN_PRESS_THRESHOLD = 150; // Time to recognize DOWN as intentional
    private static final long COMBO_WINDOW = 500; // Time window for combo inputs
    
    // Cooldown tracking
    private long lastJumpPressTime = 0;
    private long lastAttackTime = 0;
    private boolean wasTryingToMove = false;
    private boolean downWasPressed = false;  // Track when DOWN is pressed
    private long downPressedTime = 0;        // When DOWN was first pressed
    
    // Input state for slide detection
    private boolean wasMovingHorizontally = false;
    private boolean wasPressingDown = false;
    private long downAndMoveTime = 0;
    
    public PlayerInputComponent(PlayerEntity player, KeyboardInput input) {
        this.player = player;
        this.input = input;
    }
    
    @Override
    public void update(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        
        // Always capture input, even when animation-locked
        captureInputState(currentTime);
        
        // Process inputs only if animation is not locked
        if (!player.isAnimationLocked()) {
            handleMovementInput(currentTime);
            handleJumpInput(currentTime);
            handleSpecialMovement(currentTime);
            handleCombatInput(currentTime);
        } else {
            // Even when locked, we need to track input states for next frame
            updateInputTracking(currentTime);
            
            // Allow combo input even during animation lock
            if (player.isAttacking()) {
                handleComboInput(currentTime);
            }
        }
    }
    
    // (Keep the existing captureInputState and updateInputTracking methods)
    
    /**
     * Captures the current input state for tracking purposes
     */
    private void captureInputState(long currentTime) {
        // Track when DOWN is first pressed for slide detection
        boolean downPressed = input.isKeyPressed(KeyEvent.VK_DOWN);
        boolean horizontalMovement = input.isKeyPressed(KeyEvent.VK_LEFT) || 
                                    input.isKeyPressed(KeyEvent.VK_RIGHT);
        
        // Track transition to DOWN pressed
        if (downPressed && !wasPressingDown) {
            downWasPressed = true;
            downPressedTime = currentTime;
        } else if (!downPressed) {
            downWasPressed = false;
        }
        
        // Track when DOWN + horizontal movement is first pressed together
        if (downPressed && horizontalMovement) {
            if (!wasPressingDown || !wasMovingHorizontally) {
                downAndMoveTime = currentTime;
            }
        }
        
        // Update previous states
        wasPressingDown = downPressed;
        wasMovingHorizontally = horizontalMovement;
    }
    
    /**
     * Updates input tracking even when animation is locked
     */
    private void updateInputTracking(long currentTime) {
        wasTryingToMove = input.isKeyPressed(KeyEvent.VK_LEFT) || input.isKeyPressed(KeyEvent.VK_RIGHT);
        player.setWasTryingToMove(wasTryingToMove);
    }
    
    private void handleMovementInput(long currentTime) {
        // (Keep the existing code)
        wasTryingToMove = false;
        
        // Handle actions based on DOWN key
        boolean downPressed = input.isKeyPressed(KeyEvent.VK_DOWN);
        boolean wasRunning = player.getCurrentState() == PlayerState.RUNNING || 
                           (Math.abs(player.getVelocity().getX()) > WALK_SPEED * 1.5);
        
        // Only handle crouching & sliding if not dashing
        if (downPressed && player.isOnGround() && !player.isDashing()) {
            // Check for sliding if we're running or moving fast
            if (wasRunning && 
                !player.isSliding() && 
                !player.isActionOnCooldown("slide") && 
                (input.isKeyPressed(KeyEvent.VK_LEFT) || input.isKeyPressed(KeyEvent.VK_RIGHT))) {
                
                // Enter sliding state - this will handle animation locking internally
                player.enterSlidingState();
            } 
            // If we're not sliding (either not running or slide failed to start),
            // then enter crouching state
            else if (!player.isSliding()) {
                player.enterCrouchingState();
            }
        } 
        // If DOWN is released and player is crouching but not sliding, exit crouch
        else if (!downPressed && player.isCrouching() && !player.isSliding()) {
            player.exitCrouchingState();
        }
        
        // Handle left/right movement
        if (input.isKeyPressed(KeyEvent.VK_LEFT)) {
            if (player.isFacingRight() && player.isOnGround() && player.getCurrentState() == PlayerState.RUNNING) {
                player.startTurnAnimation(false);
            }
            player.setFacingRight(false);
            wasTryingToMove = true;
            
            if (!player.isDashing() && !player.isHooking() && !player.isTurning() && !player.isSliding()) {
                // Apply reduced speed when crouching
                double speedMultiplier = player.isCrouching() ? 0.5 : 1.0;
                
                if (input.isKeyPressed(KeyEvent.VK_SHIFT) && !player.isCrouching()) {
                    // Running - but can't run while crouching
                    player.setTargetVelocityX(-RUN_SPEED * speedMultiplier);
                    if (!player.isCrouching()) {
                        player.setMovementContext(PlayerStateComponent.MovementContext.RUNNING);
                    }
                } else {
                    // Walking
                    player.setTargetVelocityX(-WALK_SPEED * speedMultiplier);
                    if (!player.isCrouching()) {
                        player.setMovementContext(PlayerStateComponent.MovementContext.NORMAL);
                    }
                }
            }
        } else if (input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            if (!player.isFacingRight() && player.isOnGround() && player.getCurrentState() == PlayerState.RUNNING) {
                player.startTurnAnimation(true);
            }
            player.setFacingRight(true);
            wasTryingToMove = true;
            
            if (!player.isDashing() && !player.isHooking() && !player.isTurning() && !player.isSliding()) {
                // Apply reduced speed when crouching
                double speedMultiplier = player.isCrouching() ? 0.5 : 1.0;
                
                if (input.isKeyPressed(KeyEvent.VK_SHIFT) && !player.isCrouching()) {
                    // Running - but can't run while crouching
                    player.setTargetVelocityX(RUN_SPEED * speedMultiplier);
                    if (!player.isCrouching()) {
                        player.setMovementContext(PlayerStateComponent.MovementContext.RUNNING);
                    }
                } else {
                    // Walking
                    player.setTargetVelocityX(WALK_SPEED * speedMultiplier);
                    if (!player.isCrouching()) {
                        player.setMovementContext(PlayerStateComponent.MovementContext.NORMAL);
                    }
                }
            }
        } else {
            // No input - set target to 0
            if (!player.isDashing() && !player.isHooking() && !player.isSliding()) {
                player.setTargetVelocityX(0);
            }
        }
        
        player.setWasTryingToMove(wasTryingToMove);
    }
    
    // (Keep the existing handleJumpInput and handleSpecialMovement methods)
    
    private void handleJumpInput(long currentTime) {
        // Jump input with buffering
        if (input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            lastJumpPressTime = currentTime;
            
            // Cancel crouching when jump is pressed
            if (player.isCrouching() && !player.isSliding()) {
                player.exitCrouchingState();
            }
        }
        
        if (lastJumpPressTime != 0 && currentTime - lastJumpPressTime <= JUMP_BUFFER_TIME) {
            if (player.isOnGround() || (currentTime - player.getLastGroundTime() <= COYOTE_TIME)) {
                // First jump
                if (input.isKeyPressed(KeyEvent.VK_SHIFT) || player.getCurrentState() == PlayerState.RUNNING) {
                    player.setMovementContext(PlayerStateComponent.MovementContext.RUNNING);
                }
                player.jump(JUMP_FORCE);
                lastJumpPressTime = 0;
            } else if (player.getJumpCount() < player.getMaxJumps() && player.hasActiveBuff(BuffType.DOUBLE_JUMP)) {
                // Double jump
                player.jump(DOUBLE_JUMP_FORCE);
                lastJumpPressTime = 0;
            }
        }
    }
    
    private void handleSpecialMovement(long currentTime) {
        // Handle dash
        if (input.isKeyJustPressed(KeyEvent.VK_W)) {
            // Check for slide-first if DOWN has been pressed for a while and we're moving
            if (wasPressingDown && wasMovingHorizontally && 
                currentTime - downAndMoveTime < DOWN_PRESS_THRESHOLD) {
                
                // Try to perform slide instead of dash
                if (!player.isSliding() && !player.isActionOnCooldown("slide") && player.isOnGround()) {
                    player.enterSlidingState();
                    return; // Skip dash
                }
            }
            
            // Try to dash if slide didn't happen
            player.startDashing();
        }
        
        // Handle teleports
        if (input.isKeyJustPressed(KeyEvent.VK_E) && !player.isActionOnCooldown("teleport")) {
            performTeleport(350.0);
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_Q) && !player.isActionOnCooldown("short_teleport")) {
            performTeleport(80.0);
        }
        
        // Handle hook
        if (input.isKeyJustPressed(KeyEvent.VK_R) && !player.isActionOnCooldown("hook")) {
            performHook();
        }
    }
    
    private void handleCombatInput(long currentTime) {
        // Check for the combat component
        PlayerAttackComponent attackComponent = null;
        if (player.hasComponent(ComponentType.COMBAT)) {
            attackComponent = player.getComponent(ComponentType.COMBAT);
        }
        
        // Basic attack (using X)
        if (input.isKeyJustPressed(KeyEvent.VK_X)) {
            if (attackComponent != null) {
                attackComponent.performLightAttack();
            } else {
                // Fallback to old attack method if component not found
                player.performBasicAttack();
            }
            lastAttackTime = currentTime;
        }
        
        // Heavy attack or dash attack
        if (input.isKeyJustPressed(KeyEvent.VK_C)) {
            if (player.isDashing() && attackComponent != null) {
                attackComponent.performDashAttack();
            } else {
                // Fallback to old heavy attack
                player.performHeavyAttack();
            }
            lastAttackTime = currentTime;
        }
        
        // Spells
        if (input.isKeyJustPressed(KeyEvent.VK_1) && 
            currentTime - player.getLastSpellTime() >= player.getSpellCooldown()) {
            player.castFireball();
        }
        
        if (input.isKeyJustPressed(KeyEvent.VK_2) && 
            currentTime - player.getLastSpellTime() >= player.getSpellCooldown()) {
            player.castHeal();
        }
    }
    
    /**
     * Handle combo inputs even during animation lock
     */
    private void handleComboInput(long currentTime) {
        // Check if we have the combat component
        if (!player.hasComponent(ComponentType.COMBAT)) return;
        
        PlayerAttackComponent attackComponent = player.getComponent(ComponentType.COMBAT);
        
        // Check for combo opportunity
        if (input.isKeyJustPressed(KeyEvent.VK_X) && 
            currentTime - lastAttackTime <= COMBO_WINDOW && 
            player.isAttacking()) {
            
            // Queue next combo attack
            attackComponent.performLightAttack(); // This will check internally if combo is possible
            lastAttackTime = currentTime;
        }
    }
    
    // (Keep the existing teleport and hook methods)
    
    private void performTeleport(double distance) {
        // Only teleport if not animation-locked
        if (player.isAnimationLocked()) return;
        
        // Teleporting cancels crouching
        player.setCrouching(false);
        
        double teleportX = player.isFacingRight() ? distance : -distance;
        double teleportY = 0;
        
        // Allow directional teleporting
        if (input.isKeyPressed(KeyEvent.VK_UP)) {
            teleportY = -distance * 0.7;
        } else if (input.isKeyPressed(KeyEvent.VK_DOWN)) {
            teleportY = distance * 0.7;
        }
        
        // Check if teleport destination is valid (simplified check)
        Vector2D targetPos = player.getPosition().plus(teleportX, teleportY);
        
        // Teleport
        player.setPosition(targetPos);
        player.setCurrentState(PlayerState.DASHING); // Use dash state for teleport animation
        player.setStateChangeTime(System.currentTimeMillis());
        player.addBuffEffect(BuffType.TELEPORT_IMMUNITY, 500);
        
        // Lock animation briefly
        player.lockAnimation(250);
    }
    
    private void performHook() {
        // Only hook if not animation-locked
        if (player.isAnimationLocked()) return;
        
        // Hooking cancels crouching
        player.setCrouching(false);
        
        double hookX = player.isFacingRight() ? 300.0 : -300.0;
        double hookY = -300.0 * 0.5;
        
        Vector2D hookDirection = new Vector2D(hookX, hookY);
        Vector2D hookTarget = player.getPosition().plus(hookDirection);
        
        // Check if we have a valid hook target (simplified)
        if (player.getPosition().distance(hookTarget) <= 300.0) {
            player.setHooking(true);
            player.setAffectedByGravity(false);
            player.setCurrentState(PlayerState.HOOKING);
            player.setStateChangeTime(System.currentTimeMillis());
            player.setHookTarget(hookTarget);
            
            // Apply immediate velocity toward hook point
            Vector2D hookVelocity = hookTarget.minus(player.getPosition()).normalized().times(800.0);
            player.setVelocity(hookVelocity);
            
            // Lock animation for hook
            player.lockAnimation(400);
        }
    }

    @Override
    public ComponentType getType() {
        return ComponentType.INPUT;
    }
}