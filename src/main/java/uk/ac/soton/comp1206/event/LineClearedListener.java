package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * Line Cleared Listener used to listen for when a line is completed and clear it.
 */
public interface LineClearedListener {

    /**
     * Handle clearing of the blocks in completed lines.
     *
     * @param coordinates of the blocks to be cleared
     */
    void lineCleared(HashSet<GameBlockCoordinate> coordinates);
}
