package futsal;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.Image;

/**
 * Saves and loads named settings profiles.
 * Files stored at: ~/.futsal-scoreboard/<profileName>.properties
 */
public class SettingsManager {

    private static final Path SETTINGS_DIR = Path.of(System.getProperty("user.home"), ".futsal-scoreboard");

    public static void save(GameData data, String profileName) {
        try {
            Files.createDirectories(SETTINGS_DIR);
            Properties p = new Properties();
            p.setProperty("leagueTitle", data.getLeagueTitle());
            p.setProperty("homeTeamName", data.getHomeTeamName());
            p.setProperty("awayTeamName", data.getAwayTeamName());
            p.setProperty("matchDurationMinutes", String.valueOf(data.getMatchDurationMinutes()));
            p.setProperty("timeoutDurationMinutes", String.valueOf(data.getTimeoutDurationMinutes()));
            p.setProperty("countDown", String.valueOf(data.isCountDown()));
            p.setProperty("round", String.valueOf(data.getRound()));
            p.setProperty("bgOpacity", String.valueOf(data.getBgOpacity()));
            p.setProperty("showAggregate", String.valueOf(data.isShowAggregate()));
            // ── Image paths ───────────────────────────────────────────────
            if (data.getHomeLogoPath() != null)
                p.setProperty("homeLogoPath", data.getHomeLogoPath());
            if (data.getAwayLogoPath() != null)
                p.setProperty("awayLogoPath", data.getAwayLogoPath());
            if (data.getLeagueLogoPath() != null)
                p.setProperty("leagueLogoPath", data.getLeagueLogoPath());
            if (data.getBackgroundImagePath() != null)
                p.setProperty("backgroundImagePath", data.getBackgroundImagePath());
            if (data.getBuzzerSoundPath() != null)
                p.setProperty("buzzerSoundPath", data.getBuzzerSoundPath());
            Path file = SETTINGS_DIR.resolve(profileName + ".properties");
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Futsal Scoreboard — " + profileName);
            }
        } catch (IOException e) {
            System.err.println("Could not save: " + e.getMessage());
        }
    }

    public static void load(GameData data, String profileName) {
        Path file = SETTINGS_DIR.resolve(profileName + ".properties");
        if (!Files.exists(file))
            return;
        try (InputStream in = Files.newInputStream(file)) {
            Properties p = new Properties();
            p.load(in);
            if (p.containsKey("leagueTitle"))
                data.setLeagueTitle(p.getProperty("leagueTitle"));
            if (p.containsKey("homeTeamName"))
                data.setHomeTeamName(p.getProperty("homeTeamName"));
            if (p.containsKey("awayTeamName"))
                data.setAwayTeamName(p.getProperty("awayTeamName"));
            if (p.containsKey("matchDurationMinutes"))
                data.setMatchDurationMinutes(Integer.parseInt(p.getProperty("matchDurationMinutes")));
            if (p.containsKey("timeoutDurationMinutes"))
                data.setTimeoutDurationMinutes(Integer.parseInt(p.getProperty("timeoutDurationMinutes")));
            if (p.containsKey("countDown"))
                data.setCountDown(Boolean.parseBoolean(p.getProperty("countDown")));
            if (p.containsKey("round"))
                data.setRound(Integer.parseInt(p.getProperty("round")));
            if (p.containsKey("bgOpacity"))
                data.setBgOpacity(Double.parseDouble(p.getProperty("bgOpacity")));
            if (p.containsKey("showAggregate"))
                data.setShowAggregate(Boolean.parseBoolean(p.getProperty("showAggregate")));
            // ── Image paths — reload images from disk ─────────────────────
            loadImage(p, "homeLogoPath", data::setHomeLogoPath,
                    img -> data.setHomeLogo(img));
            loadImage(p, "awayLogoPath", data::setAwayLogoPath,
                    img -> data.setAwayLogo(img));
            loadImage(p, "leagueLogoPath", data::setLeagueLogoPath,
                    img -> data.setLeagueLogo(img));
            loadImage(p, "backgroundImagePath", data::setBackgroundImagePath,
                    img -> data.setBackgroundImage(img));
            if (p.containsKey("buzzerSoundPath"))
                data.setBuzzerSoundPath(p.getProperty("buzzerSoundPath"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not load: " + e.getMessage());
        }
    }

    // Auto-save/load uses "default" profile
    public static void save(GameData data) {
        save(data, "default");
    }

    public static void load(GameData data) {
        load(data, "default");
    }

    public static void delete(String profileName) {
        try {
            Files.deleteIfExists(SETTINGS_DIR.resolve(profileName + ".properties"));
        } catch (IOException e) {
            System.err.println("Could not delete: " + e.getMessage());
        }
    }

    /**
     * Reads an image path from properties, verifies the file exists, then
     * calls pathSetter (to remember the path) and imageSetter (to apply the Image).
     */
    private static void loadImage(Properties p, String key,
            java.util.function.Consumer<String> pathSetter,
            java.util.function.Consumer<Image> imageSetter) {
        if (!p.containsKey(key))
            return;
        String path = p.getProperty(key);
        if (path == null || path.isBlank())
            return;
        Path file = Path.of(path);
        if (!Files.exists(file)) {
            System.err.println("Image file not found, skipping: " + path);
            return;
        }
        try {
            imageSetter.accept(new Image(file.toUri().toString()));
            pathSetter.accept(path);
        } catch (Exception e) {
            System.err.println("Could not load image " + path + ": " + e.getMessage());
        }
    }

    public static List<String> listProfiles() {
        try {
            if (!Files.exists(SETTINGS_DIR))
                return new ArrayList<>();
            return Files.list(SETTINGS_DIR)
                    .filter(p -> p.toString().endsWith(".properties"))
                    .map(p -> p.getFileName().toString().replace(".properties", ""))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}