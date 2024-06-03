package uk.ac.soton.comp1206.util;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that manages the local storage.
 */
public class Storage {

    private static final Logger logger = LogManager.getLogger(Storage.class);

    /**
     * Create storage.
     * Dummy constructor, no usage.
     */
    public Storage() {
    }

    /**
     * Load local scores from scores.txt file.
     *
     * @return ArrayList of name and score pairs
     */
    public static ArrayList<Pair<String, Integer>> loadScores() {
        logger.info("Loading local scores");

        ArrayList<Pair<String, Integer>> result = new ArrayList<>();

        try {
            var path = Paths.get("scores.txt");

            if (Files.notExists(path)) {
                initialiseDummyScores();
            }

            List<String> scores = Files.readAllLines(path);
            for (String score : scores) {
                String[] components = score.split(":");
                result.add(new Pair<>(components[0], Integer.parseInt(components[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Initialize a dummy score.txt file if file is not found in file path.
     */
    public static void initialiseDummyScores() {
        logger.info("Initialising scores file");

        ArrayList<Pair<String, Integer>> result = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            result.add(new Pair<>("Dummy", 0));
        }

        writeScores(result);
    }

    /**
     * Write scores to scores.txt file.
     *
     * @param scores to write
     */
    public static void writeScores(List<Pair<String, Integer>> scores) {
        logger.info("Writing {} scores to scores.txt", scores.size());

        //Sort score list before writing
        scores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        try {
            Path path = Paths.get("scores.txt");
            StringBuilder result = new StringBuilder();
            int counter = 0;

            for (Pair<String, Integer> score : scores) {
                counter++;
                String scoreString = score.getKey();
                result.append(scoreString).append(":").append(score.getValue()).append("\n");
                if (counter >= 10) {
                    break;
                }
            }
            Files.writeString(path, result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
