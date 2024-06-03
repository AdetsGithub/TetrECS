package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * A visual component which represents a gamePiece board.
 * Extends GameBoard
 */
public class PieceBoard extends GameBoard {

    /**
     * Creates gamePiece board.
     *
     * @param columns of gamePiece board
     * @param rows    of gamePiece board
     * @param width   of gamePiece board
     * @param height  of gamePiece board
     */
    public PieceBoard(int columns, int rows, double width, double height) {
        super(columns, rows, width, height);
    }

    /**
     * Clear all grid values.
     * Place the gamePiece to be displayed.
     *
     * @param gamePiece to be displayed
     */
    public void setPiece(GamePiece gamePiece) {
        clear();
        grid.playPiece(gamePiece, 1, 1);
    }

    /**
     * Add centre dot to the board
     */
    public void setCentre() {
        double midX = Math.ceil((double) getRows() / 2) - 1;
        double midY = Math.ceil((double) getCols() / 2) - 1;
        blocks[(int) midX][(int) midY].setCentre();
    }

    /**
     * Reset grid values to 0.
     */
    public void clear() {
        for (int y = 0; y < grid.getRows(); ++y) {
            for (int x = 0; x < grid.getCols(); ++x) {
                grid.set(x, y, 0);
            }
        }
    }
}
