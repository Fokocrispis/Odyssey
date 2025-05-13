package game.entity;

public class BuffEffect {
    public float multiplier;
    public long timeRemaining;
    
    public BuffEffect(float multiplier, long duration) {
        this.multiplier = multiplier;
        this.timeRemaining = duration;
    }
}