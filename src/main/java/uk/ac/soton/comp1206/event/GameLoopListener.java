package uk.ac.soton.comp1206.event;

/**
 * Game Loop Listener is used to listen to when the game loop is reset.
 */
public interface GameLoopListener {

    /**
     * Handle when the game loop ends.
     *
     * @param delay for the game loop cycle
     */
    void gameLoop(int delay);
}
