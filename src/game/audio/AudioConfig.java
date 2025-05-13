// src/game/audio/AudioConfig.java
package game.audio;

/**
 * Audio configuration constants for the game.
 * Contains file names and volume presets for different audio elements.
 */
public class AudioConfig {
    
    // Background Music Tracks
    public static final String GAMEPLAY_MUSIC = "background_music.wav";
    public static final String MENU_MUSIC = "menu_music.wav";
    public static final String BOSS_MUSIC = "boss_music.wav";
    public static final String VICTORY_MUSIC = "victory_music.wav";
    public static final String GAME_OVER_MUSIC = "game_over_music.wav";
    
    // Player Sound Effects
    public static final String PLAYER_JUMP = "jump.wav";
    public static final String PLAYER_LAND = "player_land.wav";
    public static final String PLAYER_FOOTSTEP = "footstep.wav";
    public static final String PLAYER_ATTACK = "attack.wav";
    public static final String PLAYER_AIR_ATTACK = "air_attack.wav";
    public static final String PLAYER_HURT = "hurt.wav";
    public static final String PLAYER_DASH = "dash.wav";
    public static final String PLAYER_DOUBLE_JUMP = "double_jump.wav";
    
    // UI Sound Effects
    public static final String UI_NAVIGATE = "menu_navigate.wav";
    public static final String UI_SELECT = "menu_select.wav";
    public static final String UI_BACK = "menu_back.wav";
    public static final String UI_PAUSE = "menu_pause.wav";
    public static final String UI_UNPAUSE = "menu_unpause.wav";
    public static final String UI_VOLUME_ADJUST = "volume_adjust.wav";
    public static final String UI_CONFIRM = "menu_confirm.wav";
    public static final String UI_ERROR = "menu_error.wav";
    
    // Game Sound Effects
    public static final String GAME_START = "game_start.wav";
    public static final String GAME_END = "game_end.wav";
    public static final String CHECKPOINT = "checkpoint.wav";
    public static final String ITEM_PICKUP = "item_pickup.wav";
    public static final String ENEMY_HIT = "enemy_hit.wav";
    public static final String PLATFORM_LAND = "platform_land.wav";
    public static final String DOOR_OPEN = "door_open.wav";
    public static final String DOOR_CLOSE = "door_close.wav";
    public static final String SWITCH_ACTIVATE = "switch_activate.wav";
    
    // Volume Presets
    public static final float DEFAULT_MASTER_VOLUME = 1.0f;
    public static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    public static final float DEFAULT_SFX_VOLUME = 0.9f;
    
    // Volume levels for specific sounds
    public static final float FOOTSTEP_VOLUME = 0.4f;
    public static final float ATTACK_VOLUME = 0.6f;
    public static final float JUMP_VOLUME = 0.5f;
    public static final float LAND_VOLUME = 0.7f;
    public static final float UI_VOLUME = 0.8f;
    
    // Audio fade durations (milliseconds)
    public static final int MUSIC_FADE_OUT_DURATION = 1000;
    public static final int MUSIC_FADE_IN_DURATION = 500;
    public static final int SFX_FADE_DURATION = 250;
    
    /**
     * Gets the recommended volume for a specific sound type.
     */
    public static float getRecommendedVolume(String soundName) {
        if (soundName.contains("footstep")) return FOOTSTEP_VOLUME;
        if (soundName.contains("attack")) return ATTACK_VOLUME;
        if (soundName.contains("jump")) return JUMP_VOLUME;
        if (soundName.contains("land")) return LAND_VOLUME;
        if (soundName.contains("menu") || soundName.contains("ui")) return UI_VOLUME;
        return 1.0f; // Default volume
    }
    
    /**
     * Applies default volume settings to the sound manager.
     */
    public static void applyDefaultSettings(SoundManager soundManager) {
        soundManager.setMasterVolume(DEFAULT_MASTER_VOLUME);
        soundManager.setMusicVolume(DEFAULT_MUSIC_VOLUME);
        soundManager.setSfxVolume(DEFAULT_SFX_VOLUME);
    }
}

// ----------------------------------------------------------
// Updated GameplayScene using AudioConfig
// ----------------------------------------------------------

/*
// Modify your GameplayScene to use AudioConfig:

private void startBackgroundMusic() {
    // Use AudioConfig constants
    soundManager.playBackgroundMusic(AudioConfig.GAMEPLAY_MUSIC);
    
    // Set music volume to default
    soundManager.setMusicVolume(AudioConfig.DEFAULT_MUSIC_VOLUME);
    
    // Play start sound
    soundManager.playSoundEffect(AudioConfig.GAME_START, AudioConfig.getRecommendedVolume(AudioConfig.GAME_START));
}

private void handlePauseToggle() {
    isPaused = !isPaused;
    
    if (isPaused) {
        soundManager.pauseBackgroundMusic();
        soundManager.playSoundEffect(AudioConfig.UI_PAUSE, AudioConfig.UI_VOLUME);
    } else {
        soundManager.resumeBackgroundMusic();
        soundManager.playSoundEffect(AudioConfig.UI_UNPAUSE, AudioConfig.UI_VOLUME);
    }
}

@Override
public void onExit() {
    super.onExit();
    
    // Stop background music and play exit sound
    soundManager.stopBackgroundMusic();
    soundManager.playSoundEffect(AudioConfig.GAME_END, AudioConfig.getRecommendedVolume(AudioConfig.GAME_END));
}
*/