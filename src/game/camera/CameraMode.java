// src/game/camera/CameraMode.java
package game.camera;

/**
 * Enum representing different camera modes.
 */
public enum CameraMode {
    /**
     * Camera follows a target object
     */
    FOLLOW,
    
    /**
     * Camera is fixed at a specific position
     */
    FIXED,
    
    /**
     * Camera is moving cinematically from one position to another
     */
    CINEMATIC
}