package uk.ac.soton.comp1206.event;

/**
 * Game Over Listener is used to listen for the game over event,
 */
public interface GameOverListener {

    /**
     * Handle the cleanup after game over event.
     */
    void gameOver();
}
