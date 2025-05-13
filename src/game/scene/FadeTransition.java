package game.scene;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * A transition that fades between scenes.
 */
public class FadeTransition implements SceneTransition {
    private final double duration;
    private final boolean isPush;
    private final Color fadeColor;
    
    private double elapsed = 0;
    
    /**
     * Creates a new fade transition.
     * 
     * @param duration The duration of the transition in milliseconds.
     * @param fadeColor The color to fade through, typically black.
     * @param isPush Whether this is a push transition or replace transition.
     */
    public FadeTransition(double duration, Color fadeColor, boolean isPush) {
        this.duration = duration;
        this.fadeColor = fadeColor;
        this.isPush = isPush;
    }
    
    /**
     * Creates a new fade transition with default color (black).
     * 
     * @param duration The duration of the transition in milliseconds.
     * @param isPush Whether this is a push transition or replace transition.
     */
    public FadeTransition(double duration, boolean isPush) {
        this(duration, Color.BLACK, isPush);
    }
    
    @Override
    public void update(long deltaTime) {
        elapsed += deltaTime;
    }
    
    @Override
    public void render(Graphics2D g, Scene fromScene, Scene toScene) {
        double progress = Math.min(1.0, elapsed / duration);
        
        if (progress < 0.5) {
            // First half: render from scene and fade out
            fromScene.render(g);
            
            float alpha = (float)(progress * 2.0); // 0 to 1 over first half
            Composite originalComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(fadeColor);
            g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
            g.setComposite(originalComposite);
        } else {
            // Second half: render to scene and fade in
            if (toScene != null) {
                toScene.render(g);
            }
            
            float alpha = (float)((1.0 - progress) * 2.0); // 1 to 0 over second half
            Composite originalComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(fadeColor);
            g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
            g.setComposite(originalComposite);
        }
    }
    
    @Override
    public boolean isComplete() {
        return elapsed >= duration;
    }
    
    @Override
    public boolean isPush() {
        return isPush;
    }
}