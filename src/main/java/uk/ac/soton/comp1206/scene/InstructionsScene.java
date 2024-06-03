package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

/**
 * Instruction scene extends base scene.
 * Displays instruction image.
 * Displays dynamically generated game pieces.
 */
public class InstructionsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create instruction scene.
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);

        logger.info("Creating Instructions Scene");
    }

    /**
     *
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(e -> gameWindow.startMenu());
    }

    /**
     * Build UI elements of instruction scene.
     */
    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        // Setup pane
        StackPane instructionsPane = generateMainPane("menu-background");


        BorderPane mainPane = new BorderPane();
        instructionsPane.getChildren().add(mainPane);

        // Generate layout
        VBox vBox = new VBox();
        BorderPane.setAlignment(vBox, Pos.CENTER);
        vBox.setAlignment(Pos.TOP_CENTER);
        mainPane.setCenter(vBox);

        // Generate title
        Text instructions = new Text("Instructions");
        instructions.getStyleClass().add("heading");
        vBox.getChildren().add(instructions);

        // Generate text
        Text instructionText = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
        TextFlow instructionFlow = new TextFlow(instructionText);
        instructionText.getStyleClass().add("instructions");
        instructionText.setTextAlignment(TextAlignment.CENTER);
        instructionFlow.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(instructionFlow);

        // Generate instructions image
        ImageView instructionImage = new ImageView(Objects.requireNonNull(getClass().getResource("/images/Instructions.png")).toExternalForm());
        instructionImage.setFitWidth(gameWindow.getWidth() / 1.5);
        instructionImage.setPreserveRatio(true);
        vBox.getChildren().add(instructionImage);

        // Generate game piece UI
        Text pieces = new Text("Game Pieces");
        pieces.getStyleClass().add("heading");
        vBox.getChildren().add(pieces);
        GridPane gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        double padding = (double) (gameWindow.getWidth() - gameWindow.getWidth() / 14 * 5 - 50) / 2.0;
        gridPane.setPadding(new Insets(0.0, padding, 0.0, padding));
        gridPane.setVgap(10.0);
        gridPane.setHgap(10.0);

        // Dynamically generate game pieces
        int x = 0;
        int y = 0;
        for (int i = 0; i < 15; i++) {
            GamePiece piece = GamePiece.createPiece(i);
            PieceBoard pieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 14.0, gameWindow.getWidth() / 14.0);
            pieceBoard.setPiece(piece);
            gridPane.add(pieceBoard, x, y);
            if (x++ != 5) continue;
            x = 0;
            y++;
        }
    }
}

