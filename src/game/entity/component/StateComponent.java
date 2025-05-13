package game.entity.component;

import java.util.HashMap;
import java.util.Map;

import game.entity.AbstractEntity;

/**
 * Generic state component for entities
 */
public class StateComponent implements Component {
    protected AbstractEntity entity;
    protected String currentState = "idle";
    protected String previousState = "idle";
    protected long stateChangeTime = 0;
    protected Map<String, Long> stateDurations = new HashMap<>();
    
    public StateComponent(AbstractEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public void update(long deltaTime) {
        // Check for state duration timeouts
        long currentTime = System.currentTimeMillis();
        Long duration = stateDurations.get(currentState);
        
        if (duration != null && duration > 0) {
            if (currentTime - stateChangeTime >= duration) {
                // Time to transition to default state
                changeState("idle");
            }
        }
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.STATE;
    }
    
    /**
     * Changes the current state
     * @param newState The state to change to
     */
    public void changeState(String newState) {
        if (!newState.equals(currentState)) {
            previousState = currentState;
            currentState = newState;
            stateChangeTime = System.currentTimeMillis();
            
            // If entity has an animation component, try to update sprite
            if (entity.hasComponent(ComponentType.ANIMATION)) {
                AnimationComponent anim = entity.getComponent(ComponentType.ANIMATION);
                anim.setCurrentSprite(newState);
            }
        }
    }
    
    /**
     * Sets the duration for a state before it automatically reverts
     * @param state The state to set duration for
     * @param duration Duration in milliseconds, or 0 for no timeout
     */
    public void setStateDuration(String state, long duration) {
        stateDurations.put(state, duration);
    }
    
    /**
     * Gets the current state
     */
    public String getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the previous state
     */
    public String getPreviousState() {
        return previousState;
    }
    
    /**
     * Gets the time when the current state began
     */
    public long getStateChangeTime() {
        return stateChangeTime;
    }
    
    /**
     * Checks if the current state has been active for the specified duration
     */
    public boolean hasBeenInState(long duration) {
        return System.currentTimeMillis() - stateChangeTime >= duration;
    }
}