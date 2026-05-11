package futsal;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point – launches both the Referee Control Panel (primary stage)
 * and the LED Scoreboard Display window simultaneously.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GameData data = new GameData();
        // ── Spectator / LED display window (send to HDMI / second screen) ──
        DisplayWindow display = new DisplayWindow(data);
        display.show();

        // ── Referee control panel (primary stage) ───────────────────────────
        ControlPanel panel = new ControlPanel(data, display.getStage(), display);
        panel.show(primaryStage);

        // ── App icon for both windows ────────────────────────────────────────
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                    Main.class.getResourceAsStream("/icon.png"));
            primaryStage.getIcons().add(icon);
            display.getStage().getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Icon not found: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}