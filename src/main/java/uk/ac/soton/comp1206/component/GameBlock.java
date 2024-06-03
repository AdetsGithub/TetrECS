package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * Extends Canvas and is responsible for drawing itself.
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    /**
     * Width of canvas
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };
    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    /**
     * Animation timer for when row is cleared.
     */
    private static AnimationTimer timer = null;
    private final double width;
    /**
     * Height of canvas
     */
    private final double height;
    /**
     * The column this block exists as in the grid
     */
    private final int x;
    /**
     * The row this block exists as in the grid
     */
    private final int y;
    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    /**
     * Set if the block has a centre dot.
     */
    private boolean centre = false;
    /**
     * Hover status.
     */
    private boolean hovering;

    /**
     * Hover color.
     */
    private Color hoverColour;

    /**
     * Create a new single Game Block
     *
     * @param x      the column the block exists in
     * @param y      the row the block exists in
     * @param width  the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(int x, int y, double width, double height) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        // A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        // Do an initial paint
        paint();

        // When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated repaint it.
     *
     * @param observable what was updated
     * @param oldValue   the old value
     * @param newValue   the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a
     * corresponding block in the Grid.
     *
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Handle painting of the block canvas.
     */
    private void paint() {

        // If the block is empty, paint as empty
        if (value.get() == 0) {
            paintEmpty();
        } else {
            // If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        // Check if the block has centre dot and, if it does, paint it
        if (centre) {
            paintCentre();
        }

        // Check if the block has a hover effect and, if it does, paint it
        if (hovering) {
            paintHovered();
        }
    }

    /**
     * Paint hovered effect.
     */
    private void paintHovered() {
        var gc = getGraphicsContext2D();
        gc.setFill(hoverColour);
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Paint this canvas empty.
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Setting up empty block gradient
        var start = new Stop(0, Color.color(0, 0, 0, 0.3));
        var end = new Stop(1, Color.color(0, 0, 0, 0.5));

        // Fill
        gc.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.REFLECT, start, end));
        gc.fillRect(0, 0, width, height);

        // Border
        gc.setStroke(Color.color(1, 1, 1, 0.5));
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paint this canvas with the given colour.
     *
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        // Making lighter side
        gc.setFill(Color.color(1, 1, 1, 0.3));
        gc.fillPolygon(new double[]{0, width, 0}, new double[]{0, 0, height}, 3);

        // Adding dark accent
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.fillRect(0, 0, width, 3);
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.fillRect(0, 0, 3, height);

        // Adding light accent
        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillRect(width - 3, 0, width, height);
        gc.setFill(Color.color(0, 0, 0, 0.4));
        gc.fillRect(0, height - 3, width, height);

        // Border
        gc.setStroke(Color.color(0, 0, 0, 0.6));
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paint centre dot.
     */
    protected void paintCentre() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1, 0.7));
        gc.fillOval(width / 4, height / 4, width / 2, height / 2);
    }

    /**
     * Set fade out effect on single game block.
     */
    protected void fadeOut() {
        timer =
                new AnimationTimer() {
                    double opacity = 1;

                    @Override
                    public void handle(long l) {
                        paintEmpty();
                        opacity -= 0.02;
                        if (opacity <= 0) {
                            stop();
                            timer = null;
                        } else {
                            var gc = getGraphicsContext2D();
                            gc.setFill(Color.color(0, 1, 0, opacity));
                            gc.fillRect(0, 0, width, height);
                        }
                    }
                };
        timer.start();
    }

    /**
     * Get the column of this block.
     *
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block.
     *
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Determine the colour of the hover visual, depending on whether the block can be placed
     *
     * @param hovered  hover status of game block
     * @param canPlace check if the game piece can be placed
     */
    protected void setHovered(boolean hovered, boolean canPlace) {
        hovering = hovered;
        // Grey if block can be placed, red if cannot
        if (canPlace) {
            hoverColour = Color.color(1, 1, 1, 0.5);
        } else {
            hoverColour = Color.color(1, 0.2, 0.2, 0.5);
        }

        // Repaint block
        paint();
    }

    /**
     * Set centre dot to true and repaint block.
     */
    protected void setCentre() {
        centre = true;
        paint();
    }
}
