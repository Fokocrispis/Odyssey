import game.PlatformerGame;

/**
 * Main application entry point.
 */
public class App {
    public static void main(String[] args) {
        try {
            // Create and start the platformer game
            PlatformerGame game = new PlatformerGame();
            game.start();
        } catch (Exception e) {
            System.err.println("Error starting game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}