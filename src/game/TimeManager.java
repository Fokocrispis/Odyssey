package game;

/**
 * Manages time scaling for cinematic effects and special abilities.
 * Allows slowing down or speeding up game time temporarily.
 */
public class TimeManager {
    private static TimeManager instance;
    
    private float timeScale = 1.0f;
    private float targetTimeScale = 1.0f;
    private float transitionDuration = 0f;
    private float transitionTimer = 0f;
    private float returnDelay = 0f;
    private float returnDelayTimer = 0f;
    private boolean returning = false;
    
    /**
     * Gets the singleton instance.
     */
    public static TimeManager getInstance() {
        if (instance == null) {
            instance = new TimeManager();
        }
        return instance;
    }
    
    private TimeManager() {
        // Private constructor for singleton
    }
    
    /**
     * Updates the time manager.
     * Note: deltaTime passed here should be unscaled (real time).
     */
    public void update(long unscaledDeltaTime) {
        float dt = unscaledDeltaTime / 1000.0f;
        
        // Handle returning to normal time after delay
        if (returnDelayTimer > 0) {
            returnDelayTimer -= dt;
            if (returnDelayTimer <= 0) {
                returning = true;
                transitionTimer = transitionDuration;
            }
        }
        
        // Handle time scale transition
        if (transitionTimer > 0) {
            float progress = 1.0f - (transitionTimer / transitionDuration);
            
            // Smooth transition using easing function
            float easedProgress = easeInOutCubic(progress);
            
            if (returning) {
                timeScale = lerp(targetTimeScale, 1.0f, easedProgress);
            } else {
                timeScale = lerp(1.0f, targetTimeScale, easedProgress);
            }
            
            transitionTimer -= dt;
            
            // If done transitioning
            if (transitionTimer <= 0) {
                if (returning) {
                    // Transition complete, reset to normal
                    timeScale = 1.0f;
                    returning = false;
                } else {
                    // Reached target time scale
                    timeScale = targetTimeScale;
                }
            }
        }
    }
    
    /**
     * Sets the time scale with a smooth transition.
     * 
     * @param scale Target time scale (1.0 is normal)
     * @param transitionTime Time to transition in seconds
     * @param returnAfter Time to wait before returning to normal scale
     */
    public void setTimeScale(float scale, float transitionTime, float returnAfter) {
        targetTimeScale = scale;
        transitionDuration = transitionTime;
        transitionTimer = transitionTime;
        returnDelay = returnAfter;
        returnDelayTimer = returnAfter;
        returning = false;
    }
    
    /**
     * Immediately resets the time scale to normal.
     */
    public void resetTimeScale() {
        timeScale = 1.0f;
        targetTimeScale = 1.0f;
        transitionTimer = 0f;
        returnDelayTimer = 0f;
        returning = false;
    }
    
    /**
     * Gets the current time scale.
     */
    public float getTimeScale() {
        return timeScale;
    }
    
    /**
     * Apply time scale to a delta time value.
     */
    public long scaleTime(long deltaTime) {
        return (long)(deltaTime * timeScale);
    }
    
    /**
     * Linear interpolation between two values.
     */
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    
    /**
     * Cubic ease-in-out easing function for smooth transitions.
     */
    private float easeInOutCubic(float t) {
        return t < 0.5 ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
    }
}