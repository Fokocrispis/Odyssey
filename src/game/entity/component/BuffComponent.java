package game.entity.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import game.entity.BuffEffect;
import game.entity.BuffType;
import game.entity.component.Component.ComponentType;

public class BuffComponent implements Component {
    private final Map<BuffType, BuffEffect> activeBuffs = new HashMap<>();
    
    @Override
    public void update(long deltaTime) {
        updateBuffs(deltaTime);
    }
    
    private void updateBuffs(long deltaTime) {
        Iterator<Map.Entry<BuffType, BuffEffect>> iterator = activeBuffs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BuffType, BuffEffect> entry = iterator.next();
            BuffEffect buff = entry.getValue();
            buff.timeRemaining -= deltaTime;
            
            if (buff.timeRemaining <= 0) {
                iterator.remove();
            }
        }
    }
    
    public void addBuffEffect(BuffType type, long duration) {
        BuffEffect effect = new BuffEffect(1.0f, duration);
        
        // Set specific multipliers for different buff types
        switch (type) {
            case SPEED:
                effect.multiplier = 1.5f;
                break;
            case JUMP_HEIGHT:
                effect.multiplier = 1.3f;
                break;
            case DOUBLE_JUMP:
                effect.multiplier = 1.0f;
                break;
            case GRAVITY_DASH:
            case DASH_IMMUNITY:
            case TELEPORT_IMMUNITY:
                effect.multiplier = 1.0f;
                break;
            default:
                effect.multiplier = 1.0f;
                break;
        }
        
        activeBuffs.put(type, effect);
    }
    
    public double getBuffMultiplier(BuffType type) {
        BuffEffect buff = activeBuffs.get(type);
        return buff != null ? buff.multiplier : 1.0;
    }
    
    public boolean hasActiveBuff(BuffType type) {
        return activeBuffs.containsKey(type);
    }
    
    public Map<BuffType, BuffEffect> getActiveBuffs() {
        return new HashMap<>(activeBuffs);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.BUFF;
    }
}