package uk.ac.soton.comp1206.component;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * The Visual User Interface component which represents the menu.
 * Extends Group and is responsible for drawing itself and menu items.
 * Displays menu and menu items.
 */
public class Menu extends Group {

    private static final Logger logger = LogManager.getLogger(Menu.class);
    private final VBox box;

    /**
     * ArrayList containing menu items
     */
    private final ArrayList<MenuItem> items = new ArrayList<>();

    /**
     * Used to choose element from the ArrayList
     */
    private int selected = 0;

    /**
     * Create menu
     */
    public Menu() {
        box = new VBox(5.0);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("menu");
        getChildren().add(box);
    }

    /**
     * Deselect all menu items.
     * Apply selected visual effect to a single menu item.
     */
    private void paint() {
        for (MenuItem item : items) {
            item.stopHover();
        }
        items.get(selected).hover();
    }

    /**
     * Add menu item to ArrayList of menu items.
     * Add menu item to menu.
     *
     * @param name   of menu item
     * @param action associated with clicking menu item
     */
    public void addItem(String name, Runnable action) {
        MenuItem item = new MenuItem(name);
        items.add(item);
        box.getChildren().add(item);
        item.setOnClick(action);
    }

    /**
     * Action when menu item is selected
     */
    public void select() {
        logger.info("Selected \"{}\"", items.get(selected).getName().getText());
        items.get(selected).fire();
    }

    /**
     * Action when up arrow / W is pressed.
     */
    public void up() {
        logger.info("Up key pressed");

        selected--;
        if (selected < 0) {
            selected = items.size() - 1;
        }
        selected %= items.size();
        paint();
    }

    /**
     * Action when down arrow / S is pressed
     */
    public void down() {
        logger.info("Down key pressed");

        selected++;
        selected %= items.size();
        paint();
    }
}

