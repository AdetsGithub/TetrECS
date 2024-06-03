package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.util.Multimedia;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static javafx.application.Platform.runLater;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public abstract class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows.
     */
    protected final int rows;

    /**
     * Number of columns.
     */
    protected final int cols;
    protected final Grid grid;
    protected final ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
    protected final IntegerProperty score = new SimpleIntegerProperty(0);
    protected final IntegerProperty level = new SimpleIntegerProperty(1);
    protected final IntegerProperty lives = new SimpleIntegerProperty(3);
    protected final IntegerProperty multiplier = new SimpleIntegerProperty(1);
    private final StringProperty multString = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty();
    private final ScheduledExecutorService executor;
    protected GamePiece followingPiece;
    private GamePiece currentPiece;
    private NextPieceListener nextPieceListener;
    private LineClearedListener lineClearedListener;
    private GameLoopListener gameLoopListener;
    private GameOverListener gameOverListener;
    private ScheduledFuture<?> nextLoop;
    private ScheduledFuture<?> lowTimeSound1;
    private ScheduledFuture<?> lowTimeSound2;
    private ScheduledFuture<?> lowTimeSound3;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        // Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);

        // Create single threaded executor for the game loop
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Dummy constructor for when the leaderboard scene is opened.
     */
    public Game() {
        rows = 0;
        cols = 0;
        executor = null;
        grid = new Grid(cols, rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startGameLoop();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        // Initialise game values
        level.set(0);
        lives.set(3);
        multiplier.set(1);
        score.set(0);


        // Initialise game pieces
        followingPiece = spawnPiece();
        nextPiece();

    }

    /**
     * Handle what should happen when a particular block is clicked.
     *
     * @param gameBlock the block that was clicked
     * @return whether block was clicked, or not
     */
    public boolean blockClicked(GameBlock gameBlock) {
        // Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        logger.info("Block clicked: {},{}", x, y);

        // Play piece
        if (currentPiece != null) {
            if (grid.playPiece(currentPiece, x, y)) {
                afterPiece();
                nextPiece();
                return true;
            }
        }
        //logger.error("Cant add piece at {}, {}", x, y);
        return false;
    }

    /**
     * Creates a new game piece and returns it.
     * Method implemented in singleplayer and multiplayer
     *
     * @return a random piece
     */
    public abstract GamePiece spawnPiece();

    /**
     * Make the following piece the current piece.
     * Spawn new piece and set it to the following piece.
     */
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();

        logger.info("Current piece: " + currentPiece);
        logger.info("Next piece: " + followingPiece);

        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece);
        }
    }

    /**
     * Swap current piece and following piece.
     */
    public void swapCurrentPiece() {
        GamePiece tmp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = tmp;
    }

    /**
     * Rotate current piece.
     */
    public void rotatePiece() {
        currentPiece.rotate();
    }

    /**
     * Check if there are full rows or columns in the game space and clear if necessary.
     */
    public void afterPiece() {
        int lines = 0;

        // Sets keeping value and coordinates
        HashSet<IntegerProperty> clearVal = new HashSet<>();
        HashSet<GameBlockCoordinate> clearCoord = new HashSet<>();

        // Iterate through rows to see if there are any complete rows
        int totalRows;

        for (int y = 0; y < rows; y++) {
            totalRows = rows;
            for (int x = 0; x < cols; x++) {
                if (grid.get(x, y) != 0) {
                    totalRows--;
                }
            }

            // If total is equal to 0, the row is full
            if (totalRows == 0) {
                lines++;
                for (int x = 0; x < cols; x++) {
                    clearVal.add(grid.getGridProperty(x, y));
                    clearCoord.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        // Iterate through columns to see if there are any complete columns
        int totalCols;

        for (int x = 0; x < cols; x++) {
            totalCols = cols;
            for (int y = 0; y < rows; y++) {
                if (grid.get(x, y) != 0) {
                    totalCols--;
                }
            }

            // If total is equal to 0, the row is full
            if (totalCols == 0) {
                lines++;
                for (int y = 0; y < rows; y++) {
                    clearVal.add(grid.getGridProperty(x, y));
                    clearCoord.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        // Update score, multiplier, and grid to reflect new game state
        if (lines != 0) {
            score(lines, clearCoord);
            clearGrid(clearVal);
        } else {
            resetMultiplier();
        }
    }

    /**
     * Modify score, multiplier and level to reflect game state.
     * Set relevant grid cells to 0.
     *
     * @param lines      to be cleared
     * @param clearCoord the game blocks to be cleared
     */
    private void score(int lines, HashSet<GameBlockCoordinate> clearCoord) {
        increaseScore(lines * clearCoord.size() * 10 * multiplier.get());
        multiplier.set(multiplier.get() + 1);
        multString.set("X" + multiplier.get());
        level.set(score.get() / 1000);

        if (lineClearedListener != null) {
            lineClearedListener.lineCleared(clearCoord);
        }
    }

    /**
     * Set grid cells to 0.
     *
     * @param clear the cells to clear
     */
    private void clearGrid(HashSet<IntegerProperty> clear) {
        for (IntegerProperty cell : clear) {
            cell.set(0);
        }
    }

    /**
     * Reset game multiplier to 1.
     */
    private void resetMultiplier() {
        if (multiplier.get() > 1) {
            logger.info("Multiplier reset");
            multiplier.set(1);
            multString.set("X1");
        }
    }

    /**
     * Stop game.
     */
    public void stop() {
        logger.info("Stopping game!");
        executor.shutdownNow();
    }

    /**
     * Starts game loop.
     */
    public void startGameLoop() {
        nextLoop = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        setLowTimer();

        if (gameLoopListener != null) {
            gameLoopListener.gameLoop(getTimerDelay());
        }
    }

    /**
     * Restart game loop.
     */
    public void restartGameLoop() {
        nextLoop.cancel(false);
        cancelLowTimer();
        startGameLoop();
    }

    /**
     * Play a sound when time is getting low.
     */
    private void playLowTime() {
        Multimedia.playAudio("lifelose.wav");
    }

    /**
     * Cancel the timers so sound isn't played.
     */
    private void cancelLowTimer() {
        lowTimeSound1.cancel(false);
        lowTimeSound2.cancel(false);
        lowTimeSound3.cancel(false);
    }

    /**
     * Game over event.
     */
    private void gameOver() {
        logger.info("Game over!");
        if (gameOverListener != null) {
            runLater(() -> gameOverListener.gameOver());
        }
    }

    /**
     * Runs game loop.
     */
    public void gameLoop() {
        // Reset multiplier
        resetMultiplier();

        // Decrease number of lives
        decreaseLives();

        // Generate next piece
        nextPiece();

        // Get timer for the next loop
        int nextTimer = getTimerDelay();

        if (gameLoopListener != null) {
            gameLoopListener.gameLoop(nextTimer);
        }

        // Cancel game loop timers
        nextLoop.cancel(false);
        cancelLowTimer();

        // Set new game loop timers
        nextLoop = executor.schedule(this::gameLoop, nextTimer, TimeUnit.MILLISECONDS);
        setLowTimer();
    }

    /**
     * Decrease number of lives.
     * Start game over event if lives run out.
     */
    private void decreaseLives() {
        if (lives.get() > 0) {
            lives.set(lives.get() - 1);
        } else {
            gameOver();
        }
    }

    /**
     * Increases score.
     *
     * @param amount to increase score
     */
    public void increaseScore(int amount) {
        score.set(score.add(amount).get());
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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
     * Gets name and score ArrayList.
     *
     * @return ArrayList with name, score pairs
     */
    public ArrayList<Pair<String, Integer>> getScores() {
        return scores;
    }

    /**
     * Get the timer delay for the next loop.
     *
     * @return time
     */
    public int getTimerDelay() {
        int time = 12000 - 500 * level.get();
        return Math.max(time, 2500);
    }

    /**
     * Get current piece.
     *
     * @return current piece
     */

    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * Get following piece
     *
     * @return following piece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * Gets value of score
     *
     * @return score value
     */
    public int getScore() {
        return score.get();
    }

    /**
     * Gets score as an IntegerProperty.
     *
     * @return score
     */
    public IntegerProperty getScoreProperty() {
        return score;
    }

    /**
     * Get level object as an IntegerProperty.
     *
     * @return level
     */
    public IntegerProperty getLevelProperty() {
        return level;
    }

    /**
     * Get lives as an IntegerProperty.
     *
     * @return lives
     */
    public IntegerProperty getLivesProperty() {
        return lives;
    }

    /**
     * Get multiplier as a StringProperty.
     *
     * @return multiplier
     */
    public StringProperty getMultiplierProperty() {
        return multString;
    }

    /**
     * Get name as a StringProperty.
     *
     * @return name
     */
    public StringProperty getNameProperty() {
        return name;
    }

    /**
     * Set next piece listener.
     *
     * @param listener for next piece
     */
    public void setNextPieceListener(NextPieceListener listener) {
        nextPieceListener = listener;
    }

    /**
     * Set line cleared listener.
     *
     * @param listener for when line is cleared
     */

    public void setOnLineCleared(LineClearedListener listener) {
        lineClearedListener = listener;
    }

    /**
     * Set game over listener.
     *
     * @param listener for game over event
     */
    public void setOnGameOver(GameOverListener listener) {
        gameOverListener = listener;
    }

    /**
     * Set game loop listener.
     *
     * @param listener for game loop
     */
    public void setOnGameLoop(GameLoopListener listener) {
        gameLoopListener = listener;
    }

    /**
     * Setting timers for when loop reaches near the end.
     */
    private void setLowTimer() {
        lowTimeSound1 = executor.schedule(this::playLowTime, (long) (getTimerDelay() * 0.75), TimeUnit.MILLISECONDS);
        lowTimeSound2 = executor.schedule(this::playLowTime, (long) (getTimerDelay() * 0.85), TimeUnit.MILLISECONDS);
        lowTimeSound3 = executor.schedule(this::playLowTime, (long) (getTimerDelay() * 0.95), TimeUnit.MILLISECONDS);
    }
}
