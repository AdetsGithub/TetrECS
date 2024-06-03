package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        // Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];
        // Add a SimpleIntegerProperty to every block in the grid
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Check if piece can be played.
     *
     * @param piece to be placed
     * @param xPos  x coordinate to be placed
     * @param yPos  y coordinate to be placed
     * @return whether piece can be played or not
     */
    public boolean canPlayPiece(GamePiece piece, int xPos, int yPos) {
        int[][] blocks = piece.getBlocks();

        // Ensure piece is on grid and valid
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                if (blocks[x][y] != 0 && get(x + xPos, y + yPos) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Places piece.
     *
     * @param piece to be placed
     * @param xPos  X coordinate to be placed
     * @param yPos  y coordinate to be placed
     * @return whether piece is played or not
     */
    public boolean playPiece(GamePiece piece, int xPos, int yPos) {
        int[][] blocks = piece.getBlocks();

        xPos--;
        yPos--;

        // Checking if piece can be placed
        boolean playable = canPlayPiece(piece, xPos, yPos);

        // Check if piece can be placed and place it
        if (playable) {
            for (int x = 0; x < blocks.length; ++x) {
                for (int y = 0; y < blocks[x].length; ++y) {
                    if (blocks[x][y] != 0) {
                        set(x + xPos, y + yPos, blocks[x][y]);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the value represented at the given x and y index within the grid
     *
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            // Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            // No such index
            return -1;
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     *
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     *
     * @param x     column
     * @param y     row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }
}
