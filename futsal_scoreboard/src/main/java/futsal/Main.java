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
        SettingsManager.load(data); // auto-load saved settings on startup

        // ── Spectator / LED display window (send to HDMI / second screen) ──
        DisplayWindow display = new DisplayWindow(data);
        display.show();

        // ── Referee control panel (primary stage) ───────────────────────────
        ControlPanel panel = new ControlPanel(data, display.getStage(), display);
        panel.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
