package game.entity.component;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import game.entity.AbstractEntity;
import game.sprites.Sprite;

/**
 * Generic animation component for entities
 */
public class AnimationComponent implements Component {
    protected AbstractEntity entity;
    protected Sprite currentSprite;
    protected Map<String, Sprite> sprites = new HashMap<>();
    protected boolean flipHorizontal = false;
    
    public AnimationComponent(AbstractEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public void update(long deltaTime) {
        if (currentSprite != null) {
            currentSprite.update(deltaTime);
        }
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.ANIMATION;
    }
    
    /**
     * Renders the current sprite
     */
    public void render(Graphics2D g) {
        if (currentSprite == null || !entity.isVisible()) return;
        
        int x = entity.getX() - currentSprite.getSize().width / 2;
        int y = entity.getY() - currentSprite.getSize().height / 2;
        int width = currentSprite.getSize().width;
        int height = currentSprite.getSize().height;
        
        if (flipHorizontal) {
            g.drawImage(currentSprite.getFrame(), x + width, y, -width, height, null);
        } else {
            g.drawImage(currentSprite.getFrame(), x, y, width, height, null);
        }
    }
    
    /**
     * Adds a sprite with the given name
     */
    public void addSprite(String name, Sprite sprite) {
        sprites.put(name, sprite);
        
        // Set as current if we don't have one yet
        if (currentSprite == null) {
            currentSprite = sprite;
        }
    }
    
    /**
     * Sets the current sprite by name
     * @return True if the sprite was found and set
     */
    public boolean setCurrentSprite(String name) {
        Sprite sprite = sprites.get(name);
        if (sprite != null) {
            currentSprite = sprite;
            return true;
        }
        return false;
    }
    
    /**
     * Gets the current sprite
     */
    public Sprite getCurrentSprite() {
        return currentSprite;
    }
    
    /**
     * Sets whether sprites should be flipped horizontally
     */
    public void setFlipHorizontal(boolean flip) {
        this.flipHorizontal = flip;
    }
    
    /**
     * Checks if sprites are being flipped horizontally
     */
    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }
}