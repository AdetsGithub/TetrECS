package uk.ac.soton.comp1206.component;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Visual component which displays a timer
 */
public class Timer extends Rectangle {

    /**
     * Create a timer
     */
    public Timer() {
        setHeight(20.0);
        setFill(Color.RED);
    }
}

