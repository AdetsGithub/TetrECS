package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Next Piece Listener is used to listen to when a new piece is generated.
 */
public interface NextPieceListener {

    /**
     * Handle new game piece.
     *
     * @param piece to be added to the game
     */
    void nextPiece(GamePiece piece);
}
