package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Collection;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);
    /**
     * The grid this GameBoard represents.
     */
    protected final Grid grid;
    /**
     * Number of columns in the board.
     */
    private final int cols;
    /**
     * Number of rows in the board.
     */
    private final int rows;
    /**
     * The visual width of the board - has to be specified due to being a Canvas.
     */
    private final double width;
    /**
     * The visual height of the board - has to be specified due to being a Canvas.
     */
    private final double height;
    /**
     * The listener to call when a specific block is right-clicked.
     */
    protected RightClickedListener rightClickListener;
    /**
     * The blocks inside the grid.
     */
    GameBlock[][] blocks;
    /**
     * The listener to call when a specific block is clicked.
     */
    private BlockClickedListener blockClickedListener;
    /**
     * The block being hovered over.
     */
    private GameBlock hoveredBlock;

    /**
     * Variable storing game instance.
     */
    private Game game;

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     *
     * @param grid   linked grid
     * @param width  the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        // Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols   number of columns for internal grid
     * @param rows   number of rows for internal grid
     * @param width  the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols, rows);

        // Build the GameBoard
        build();
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}", cols, rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x, y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     *
     * @param x column
     * @param y row
     */
    protected void createBlock(int x, int y) {
        logger.info("Creating block at {}, {}", x, y);

        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(x, y, blockWidth, blockHeight);

        // Add to the GridPane
        add(block, x, y);

        // Add to our block directory
        blocks[x][y] = block;

        // Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x, y));

        //Add a mouse click listener to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        // Generate hover effects when mouse is moved
        block.setOnMouseEntered((e) -> hovered(block));
        block.setOnMouseExited((e) -> resetHovered());
    }

    /**
     * Set hovered effect for game piece.
     *
     * @param block the block hovered over
     */
    public void hovered(GameBlock block) {
        hoveredBlock = block;

        if (game != null && game.getCurrentPiece() != null) {
            var piece = game.getCurrentPiece();
            var pieceBlocks = piece.getBlocks();

            for (int x = 0; x < pieceBlocks.length; x++) {
                for (int y = 0; y < pieceBlocks[x].length; y++) {
                    // X and Y positions of blocks
                    int blockX = block.getX();
                    int blockY = block.getY();

                    // Game board position of blocks
                    int xVal = blockX + x - 1;
                    int yVal = blockY + y - 1;

                    // If there is a block in the piece and it is within the game board, set hovered effect
                    if (pieceBlocks[x][y] != 0 && checkBounds(xVal, yVal)) {
                        logger.info("{}, {} is valid", block.getX(), block.getY());
                        blocks[xVal][yVal].setHovered(true, grid.canPlayPiece(piece, blockX - 1, blockY - 1));
                    }
                }
            }
        }
    }

    /**
     * Reset all game blocks to not hovered.
     */
    public void resetHovered() {
        // logger.info("Exited block {}", this);
        try {
            for (GameBlock[] gameBlocks : blocks) {
                for (GameBlock gameBlock : gameBlocks) {
                    gameBlock.setHovered(false, false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Reset all game board blocks hovered status then redraw hovered effect for currently hovered block.
     *
     * @param block the block to refresh for
     */
    public void refreshHovered(GameBlock block) {
        resetHovered();
        hovered(block);
    }

    /**
     * Sets fade out animation on game blocks in a Collection
     *
     * @param blocks list of blocks
     */
    public void fadeBlocks(Collection<GameBlockCoordinate> blocks) {
        for (GameBlockCoordinate block : blocks) {
            getBlock(block.getX(), block.getY()).fadeOut();
        }
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     *
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if (event.getButton() == MouseButton.PRIMARY && blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
        if (event.getButton() == MouseButton.SECONDARY && rightClickListener != null) {
            rightClickListener.rightClicked();
        }
    }

    /**
     * Check if piece is within game board bounds.
     *
     * @param x position of the piece
     * @param y position of the piece
     * @return whether piece is within bounds or not
     */
    private boolean checkBounds(int x, int y) {
        return y >= 0 && y <= rows - 1 && x >= 0 && x <= cols - 1;
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     *
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Number of columns getter.
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Number of rows getter.
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get currently hovered game block
     *
     * @return game block hovered
     */
    public GameBlock getHoveredBlock() {
        return hoveredBlock;
    }

    /**
     * Set the game instance to the game board game variable.
     *
     * @param game the game
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Set the listener to handle an event when a block is clicked.
     *
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Setter for right-click listener.
     *
     * @param listener the listener to set
     */
    public void setOnRightClick(RightClickedListener listener) {
        rightClickListener = listener;
    }
}
