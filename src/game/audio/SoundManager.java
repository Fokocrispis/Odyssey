package game.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.sound.sampled.*;

/**
 * Manages all audio in the game including background music and sound effects.
 * Supports loading, caching, and playing audio files from resources.
 */
public class SoundManager {
    private static SoundManager instance;
    private static final Logger logger = Logger.getLogger(SoundManager.class.getName());
    
    // Cache for loaded sound clips
    private final Map<String, Clip> clipCache;
    
    // Track active sound effects
    private final List<Clip> activeSoundEffects;
    
    // Background music
    private Clip backgroundMusic;
    private String currentBgMusicKey;
    private boolean isPaused;
    
    // Volume controls (0.0 to 1.0)
    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;
    
    // Resources folder path - for resources folder next to src
    private static final String AUDIO_RESOURCE_PATH = "audio/";
    
    // Preloaded sounds
    private final Map<String, Boolean> preloadedSounds;
    
    // Error tracking
    private final Map<String, Integer> loadErrors;
    
    /**
     * Gets the singleton instance of the SoundManager.
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private SoundManager() {
        this.clipCache = new ConcurrentHashMap<>();
        this.activeSoundEffects = new ArrayList<>();
        this.isPaused = false;
        this.preloadedSounds = new ConcurrentHashMap<>();
        this.loadErrors = new ConcurrentHashMap<>();
        
        // Initialize logging
        logger.setLevel(Level.INFO);
        
        // Preload common sounds
        preloadCommonSounds();
    }
    
    /**
     * Preloads common sound effects to reduce latency.
     */
    private void preloadCommonSounds() {
        String[] commonSounds = {
            "jump.wav",
            "player_land.wav",
            "attack.wav",
            "menu_navigate.wav",
            "menu_select.wav"
        };
        
        for (String sound : commonSounds) {
            try {
                loadSoundClip(sound);
                preloadedSounds.put(sound, true);
                logger.info("Preloaded sound: " + sound);
            } catch (Exception e) {
                logger.warning("Failed to preload sound: " + sound);
                preloadedSounds.put(sound, false);
            }
        }
    }
    
    /**
     * Loads a sound clip from the resources folder.
     * @param filename The name of the audio file (e.g., "background.wav")
     * @return The loaded clip, or null if loading failed
     */
    private Clip loadSoundClip(String filename) {
        // Check cache first
        if (clipCache.containsKey(filename)) {
            return clipCache.get(filename);
        }
        
        try {
            // Load resource
            String resourcePath = "/" + AUDIO_RESOURCE_PATH + filename;
            URL soundURL = getClass().getResource(resourcePath);
            
            if (soundURL == null) {
                // Try alternative path
                resourcePath = AUDIO_RESOURCE_PATH + filename;
                soundURL = getClass().getResource(resourcePath);
                
                if (soundURL == null) {
                    System.err.println("Sound file not found: " + filename);
                    return null;
                }
            }
            
            // Open audio stream
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                new BufferedInputStream(soundURL.openStream())
            );
            
            // Get audio format
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            // Create clip
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioStream);
            
            // Cache the clip
            clipCache.put(filename, clip);
            
            logger.info("Loaded sound: " + filename);
            loadErrors.put(filename, 0); // Reset error count
            return clip;
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // Track loading errors
            Integer errorCount = loadErrors.getOrDefault(filename, 0);
            loadErrors.put(filename, errorCount + 1);
            
            // Only log first few errors to avoid spam
            if (errorCount < 3) {
                logger.log(Level.WARNING, "Failed to load sound (attempt " + (errorCount + 1) + "): " + filename, e);
            }
            
            return null;
        }
    }
    
    /**
     * Plays background music in a loop.
     * @param filename The music file to play
     */
    public void playBackgroundMusic(String filename) {
        // Stop current background music if playing
        stopBackgroundMusic();
        
        // Load new music
        backgroundMusic = loadSoundClip(filename);
        if (backgroundMusic != null) {
            // Set volume
            setVolume(backgroundMusic, masterVolume * musicVolume);
            
            // Loop continuously
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            currentBgMusicKey = filename;
            isPaused = false;
            
            System.out.println("Playing background music: " + filename);
        }
    }
    
    /**
     * Stops the background music.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.setFramePosition(0);
            isPaused = false;
        }
    }
    
    /**
     * Pauses the background music.
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            isPaused = true;
        }
    }
    
    /**
     * Resumes the background music from where it was paused.
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusic != null && isPaused) {
            backgroundMusic.start();
            isPaused = false;
        }
    }
    
    /**
     * Plays a sound effect.
     * @param filename The sound effect file to play
     */
    public void playSoundEffect(String filename) {
        playSoundEffect(filename, 1.0f);
    }
    
    /**
     * Plays a sound effect with custom volume.
     * @param filename The sound effect file to play
     * @param volume Volume multiplier (0.0 to 1.0)
     */
    public void playSoundEffect(String filename, float volume) {
        Clip clip = loadSoundClip(filename);
        if (clip != null) {
            // Create a new instance for this playback
            try {
                // Reset to beginning
                clip.setFramePosition(0);
                
                // Set volume
                setVolume(clip, masterVolume * sfxVolume * volume);
                
                // Start playing
                clip.start();
                
                // Track active sound effects
                synchronized (activeSoundEffects) {
                    activeSoundEffects.add(clip);
                }
                
                // Remove from tracking when finished
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        synchronized (activeSoundEffects) {
                            activeSoundEffects.remove(clip);
                        }
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Failed to play sound effect: " + filename);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Sets the volume for a specific clip.
     * @param clip The clip to set volume for
     * @param volume Volume level (0.0 to 1.0)
     */
    private void setVolume(Clip clip, float volume) {
        if (clip != null) {
            try {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // Convert linear scale to decibels
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                
                // Clamp to valid range
                dB = Math.max(volumeControl.getMinimum(), Math.min(dB, volumeControl.getMaximum()));
                
                volumeControl.setValue(dB);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("Failed to set volume for clip");
            }
        }
    }
    
    /**
     * Sets the master volume (affects all sounds).
     * @param volume Volume level (0.0 to 1.0)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // Update background music volume
        if (backgroundMusic != null) {
            setVolume(backgroundMusic, masterVolume * musicVolume);
        }
        
        // Update active sound effects volume
        synchronized (activeSoundEffects) {
            for (Clip clip : activeSoundEffects) {
                setVolume(clip, masterVolume * sfxVolume);
            }
        }
    }
    
    /**
     * Sets the music volume.
     * @param volume Volume level (0.0 to 1.0)
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // Update background music volume
        if (backgroundMusic != null) {
            setVolume(backgroundMusic, masterVolume * musicVolume);
        }
    }
    
    /**
     * Sets the sound effects volume.
     * @param volume Volume level (0.0 to 1.0)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // Update active sound effects volume
        synchronized (activeSoundEffects) {
            for (Clip clip : activeSoundEffects) {
                setVolume(clip, masterVolume * sfxVolume);
            }
        }
    }
    
    /**
     * Gets the current master volume.
     */
    public float getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Gets the current music volume.
     */
    public float getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Gets the current sound effects volume.
     */
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Checks if a sound file exists in resources.
     */
    public boolean soundExists(String filename) {
        String resourcePath = "/" + AUDIO_RESOURCE_PATH + filename;
        URL soundURL = getClass().getResource(resourcePath);
        
        if (soundURL == null) {
            // Try alternative path
            resourcePath = AUDIO_RESOURCE_PATH + filename;
            soundURL = getClass().getResource(resourcePath);
        }
        
        return soundURL != null;
    }
    
    /**
     * Gets debug information about the sound system.
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("SoundManager Debug Info:\n");
        info.append("Cached sounds: ").append(clipCache.size()).append("\n");
        info.append("Active sound effects: ").append(activeSoundEffects.size()).append("\n");
        info.append("Background music: ").append(currentBgMusicKey).append("\n");
        info.append("Is paused: ").append(isPaused).append("\n");
        info.append("Master volume: ").append(masterVolume).append("\n");
        info.append("Music volume: ").append(musicVolume).append("\n");
        info.append("SFX volume: ").append(sfxVolume).append("\n");
        
        if (!loadErrors.isEmpty()) {
            info.append("\nLoad errors:\n");
            for (Map.Entry<String, Integer> entry : loadErrors.entrySet()) {
                if (entry.getValue() > 0) {
                    info.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" attempts\n");
                }
            }
        }
        
        return info.toString();
    }
    
    /**
     * Cleans up all audio resources.
     */
    public void cleanup() {
        logger.info("Cleaning up audio resources...");
        
        // Stop and close all clips
        for (Clip clip : clipCache.values()) {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        }
        
        // Clear collections
        clipCache.clear();
        activeSoundEffects.clear();
        
        // Reset state
        backgroundMusic = null;
        currentBgMusicKey = null;
        isPaused = false;
        
        logger.info("Audio cleanup complete");
    }
}