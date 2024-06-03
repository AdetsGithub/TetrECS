package uk.ac.soton.comp1206.component;

import javafx.scene.Group;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * A visual component which represents items in the menu.
 * Extends group and is not responsible for drawing itself, but is responsible for adding visual effects to component.
 */
public class MenuItem extends Group {

    private static final Logger logger = LogManager.getLogger(MenuItem.class);
    /**
     * Text containing name of menu item.
     */
    private final Text text;
    /**
     * Runnable action used for selecting menu item.
     */
    private Runnable action;

    /**
     * Initialises menu item.
     *
     * @param name of menu item
     */
    public MenuItem(String name) {
        text = new Text(name);
        text.getStyleClass().add("menuItem");
        getChildren().add(text);
    }

    /**
     * Adds selected effect to text.
     */
    public void hover() {
        this.text.getStyleClass().add("selected");
    }

    /**
     * Removes selected effect from text.
     */
    public void stopHover() {
        this.text.getStyleClass().remove("selected");
    }

    /**
     * Runs action.
     */
    public void fire() {
        action.run();
    }

    public Text getName() {
        return text;
    }

    /**
     * Sets runnable action to instance variable.
     * Sets action to run when mouse button is clicked.
     *
     * @param action to do when menu item is clicked
     */
    public void setOnClick(Runnable action) {
        this.action = action;
        this.setOnMouseClicked(e -> {
            Multimedia.playAudio("rotate.wav");
            logger.info("Selected \"{}\"", getName().getText());
            fire();
        });
    }
}