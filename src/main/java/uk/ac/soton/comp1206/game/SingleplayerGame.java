package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Singleplayer game extends Game.
 * Implements spawnPiece() method
 */
public class SingleplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(SingleplayerGame.class);

    /**
     * Generate random number to select piece.
     */
    private final ThreadLocalRandom tlr = ThreadLocalRandom.current();

    /**
     * Create SingleplayerGame
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public SingleplayerGame(int cols, int rows) {
        super(cols, rows);
    }

    /**
     * Spawns random piece
     *
     * @return a random game piece
     */
    public GamePiece spawnPiece() {
        logger.info("Spawning next piece");
        return GamePiece.createPiece(tlr.nextInt(0, 15));
    }
}
