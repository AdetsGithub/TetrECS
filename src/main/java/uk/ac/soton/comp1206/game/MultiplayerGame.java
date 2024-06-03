package uk.ac.soton.comp1206.game;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayDeque;

import static javafx.application.Platform.runLater;

/**
 * Multiplayer game extends Game.
 * Implements spawnPiece() method.
 * Contains methods unique to multiplayer game.
 */
public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * The network communicator for the multiplayer game.
     */
    private final Communicator communicator;
    /**
     * ArrayDequeue containing game pieces from the server
     */
    private final ArrayDeque<GamePiece> gamePieces = new ArrayDeque<>();
    /**
     * Game status
     */
    private boolean gameStarted;

    /**
     * Creates MultiplayerGame.
     *
     * @param communicator for the multiplayer game
     * @param cols         number of columns
     * @param rows         number of rows
     */
    public MultiplayerGame(Communicator communicator, int cols, int rows) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener((message) -> runLater(() -> receiveMessage(message.trim())));
    }

    /**
     * Override game initialisation for multiplayer.
     */
    @Override
    public void initialiseGame() {
        logger.info("Initialising game");

        // Initialise game values
        level.set(0);
        lives.set(3);
        multiplier.set(1);
        score.set(0);


        // Request server for pieces
        initialPieces();
    }

    /**
     * Handle what should happen when a particular block is clicked.
     * Overrides blockClicked() from Game.
     * Sends board status to server.
     *
     * @param gameBlock the block that was clicked
     * @return whether block was clicked or not
     */
    public boolean blockClicked(GameBlock gameBlock) {
        boolean blockClicked = super.blockClicked(gameBlock);
        communicator.send("BOARD " + encodeBoardStatus());
        return blockClicked;
    }

    /**
     * Implement spawnPiece() to request piece from server.
     *
     * @return the piece generated
     */
    public GamePiece spawnPiece() {
        communicator.send("PIECE");
        return gamePieces.removeFirst();
    }

    /**
     * Request server for game pieces.
     */
    private void initialPieces() {
        for (int i = 0; i < 10; i++) {
            communicator.send("PIECE");
        }
    }

    /**
     * Receive message from server, clean and parse.
     *
     * @param message from server
     */
    private void receiveMessage(String message) {
        logger.info("Received message: {}", message);

        String[] components = message.split(" ", 2);
        String command = components[0];
        if (command.equals("PIECE") && components.length > 1) {
            String data = components[1];
            receivePiece(Integer.parseInt(data));
        }
        if (command.equals("SCORES") && components.length > 1) {
            String data = components[1];
            receiveScores(data);
        }
    }

    /**
     * Parse received scores.
     * Add player, score pair to scores
     * Sort players by score.
     *
     * @param data Raw text data
     */
    private void receiveScores(String data) {
        scores.clear();
        String[] scoresAndNames = data.split("\\R");

        // Parsing score lines from server
        for (String scoreAndName : scoresAndNames) {
            String[] components = scoreAndName.split(":");
            String player = components[0];
            int score = Integer.parseInt(components[1]);
            scores.add(new Pair<>(player, score));

            logger.info("Score received: {}, {}", player, score);
        }

        scores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    }

    /**
     * Parse received game piece.
     * Add piece to gamePieces.
     * Start game when there are more than 2 pieces.
     *
     * @param block the number of the block
     */
    private void receivePiece(int block) {
        GamePiece piece = GamePiece.createPiece(block);

        logger.info("Received next piece: {}", piece);

        gamePieces.add(piece);

        logger.info("Game piece queue: {}", gamePieces);

        if (!gameStarted && gamePieces.size() > 2) {
            logger.info("3 pieces received. Game starting");

            followingPiece = spawnPiece();
            nextPiece();
            gameStarted = true;
        }
    }

    /**
     * Encode status of the board so that it can be sent to the server.
     *
     * @return encoded string
     */
    private String encodeBoardStatus() {
        StringBuilder board = new StringBuilder();

        for (int x = 0; x < cols; ++x) {
            for (int y = 0; y < rows; ++y) {
                int tmp = grid.get(x, y);
                board.append(tmp).append(" ");
            }
        }
        return board.toString().trim();
    }
}
