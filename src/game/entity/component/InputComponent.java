package game.entity.component;

import java.awt.event.KeyEvent;

import game.entity.AbstractEntity;
import game.input.KeyboardInput;

/**
 * Generic input component for entities
 */
public class InputComponent implements Component {
    protected AbstractEntity entity;
    protected KeyboardInput input;
    
    public InputComponent(AbstractEntity entity, KeyboardInput input) {
        this.entity = entity;
        this.input = input;
    }
    
    @Override
    public void update(long deltaTime) {
        // Basic WASD movement
        if (entity.hasComponent(ComponentType.PHYSICS)) {
            PhysicsComponent physics = entity.getComponent(ComponentType.PHYSICS);
            
            double speed = 300.0;
            double velocityX = 0;
            double velocityY = 0;
            
            if (input.isKeyPressed(KeyEvent.VK_A)) {
                velocityX = -speed;
                setFacingLeft();
            }
            
            if (input.isKeyPressed(KeyEvent.VK_D)) {
                velocityX = speed;
                setFacingRight();
            }
            
            if (input.isKeyPressed(KeyEvent.VK_W)) {
                velocityY = -speed;
            }
            
            if (input.isKeyPressed(KeyEvent.VK_S)) {
                velocityY = speed;
            }
            
            // Apply movement
            physics.getVelocity().set(velocityX, velocityY);
        }
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.INPUT;
    }
    
    protected void setFacingLeft() {
        if (entity.hasComponent(ComponentType.ANIMATION)) {
            AnimationComponent anim = entity.getComponent(ComponentType.ANIMATION);
            anim.setFlipHorizontal(true);
        }
    }
    
    protected void setFacingRight() {
        if (entity.hasComponent(ComponentType.ANIMATION)) {
            AnimationComponent anim = entity.getComponent(ComponentType.ANIMATION);
            anim.setFlipHorizontal(false);
        }
    }
}