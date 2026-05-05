package futsal;

import javafx.beans.property.*;
import javafx.scene.image.Image;

/**
 * Shared observable game state. Both the DisplayWindow and ControlPanel
 * bind to these properties so changes are reflected instantly everywhere.
 */
public class GameData {

    // ── Match Info ────────────────────────────────────────────────────────
    private final StringProperty leagueTitle = new SimpleStringProperty("FUTSAL LEAGUE 2025");
    private final IntegerProperty round = new SimpleIntegerProperty(1);
    private final IntegerProperty matchDurationMinutes = new SimpleIntegerProperty(20);

    // ── Teams ─────────────────────────────────────────────────────────────
    private final StringProperty homeTeamName = new SimpleStringProperty("HOME TEAM");
    private final StringProperty awayTeamName = new SimpleStringProperty("AWAY TEAM");
    private final IntegerProperty homeScore = new SimpleIntegerProperty(0);
    private final IntegerProperty homeAgg = new SimpleIntegerProperty(0);
    private final IntegerProperty awayAgg = new SimpleIntegerProperty(0);
    private final IntegerProperty homeYellow = new SimpleIntegerProperty(0);
    private final IntegerProperty awayYellow = new SimpleIntegerProperty(0);
    private final IntegerProperty homeRed = new SimpleIntegerProperty(0);
    private final IntegerProperty awayRed = new SimpleIntegerProperty(0);
    private final IntegerProperty awayScore = new SimpleIntegerProperty(0);
    private final IntegerProperty homeFouls = new SimpleIntegerProperty(0);
    private final IntegerProperty awayFouls = new SimpleIntegerProperty(0);

    // ── Images ────────────────────────────────────────────────────────────
    private final ObjectProperty<Image> homeLogo = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> awayLogo = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> backgroundImage = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Image> leagueLogo = new SimpleObjectProperty<>(null);

    // ── Image file paths (persisted with profiles) ────────────────────────
    private final StringProperty homeLogoPath = new SimpleStringProperty(null);
    private final StringProperty awayLogoPath = new SimpleStringProperty(null);
    private final StringProperty backgroundImagePath = new SimpleStringProperty(null);
    private final StringProperty leagueLogoPath = new SimpleStringProperty(null);

    // ── Timer ─────────────────────────────────────────────────────────────
    private final LongProperty elapsedSeconds = new SimpleLongProperty(0);
    private final BooleanProperty timerRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty countDown = new SimpleBooleanProperty(true);

    // ── Display ───────────────────────────────────────────────────────────
    private final DoubleProperty bgOpacity = new SimpleDoubleProperty(0.28);
    private final DoubleProperty displayScale = new SimpleDoubleProperty(1.0);
    private final BooleanProperty showAggregate = new SimpleBooleanProperty(false);
    private final StringProperty resolution = new SimpleStringProperty("1280x720");

    // ═════════════════════════════════════════════════════════════════════
    // Property accessors (for bindings)
    // ═════════════════════════════════════════════════════════════════════
    public StringProperty leagueTitleProperty() {
        return leagueTitle;
    }

    public IntegerProperty roundProperty() {
        return round;
    }

    public IntegerProperty matchDurationMinutesProperty() {
        return matchDurationMinutes;
    }

    public StringProperty homeTeamNameProperty() {
        return homeTeamName;
    }

    public StringProperty awayTeamNameProperty() {
        return awayTeamName;
    }

    public IntegerProperty homeScoreProperty() {
        return homeScore;
    }

    public IntegerProperty homeAggProperty() {
        return homeAgg;
    }

    public IntegerProperty awayAggProperty() {
        return awayAgg;
    }

    public IntegerProperty homeYellowProperty() {
        return homeYellow;
    }

    public IntegerProperty awayYellowProperty() {
        return awayYellow;
    }

    public IntegerProperty homeRedProperty() {
        return homeRed;
    }

    public IntegerProperty awayRedProperty() {
        return awayRed;
    }

    public IntegerProperty awayScoreProperty() {
        return awayScore;
    }

    public IntegerProperty homeFoulsProperty() {
        return homeFouls;
    }

    public IntegerProperty awayFoulsProperty() {
        return awayFouls;
    }

    public ObjectProperty<Image> homeLogoProperty() {
        return homeLogo;
    }

    public ObjectProperty<Image> leagueLogoProperty() {
        return leagueLogo;
    }

    public ObjectProperty<Image> awayLogoProperty() {
        return awayLogo;
    }

    public ObjectProperty<Image> backgroundImageProperty() {
        return backgroundImage;
    }

    public LongProperty elapsedSecondsProperty() {
        return elapsedSeconds;
    }

    public BooleanProperty timerRunningProperty() {
        return timerRunning;
    }

    public BooleanProperty countDownProperty() {
        return countDown;
    }

    public DoubleProperty bgOpacityProperty() {
        return bgOpacity;
    }

    public DoubleProperty displayScaleProperty() {
        return displayScale;
    }

    public BooleanProperty showAggregateProperty() {
        return showAggregate;
    }

    public StringProperty resolutionProperty() {
        return resolution;
    }

    // ═════════════════════════════════════════════════════════════════════
    // Plain getters / setters
    // ═════════════════════════════════════════════════════════════════════
    public String getLeagueTitle() {
        return leagueTitle.get();
    }

    public void setLeagueTitle(String v) {
        leagueTitle.set(v == null ? "" : v);
    }

    public int getRound() {
        return round.get();
    }

    public void setRound(int v) {
        round.set(Math.max(1, v));
    }

    public int getMatchDurationMinutes() {
        return matchDurationMinutes.get();
    }

    public void setMatchDurationMinutes(int v) {
        matchDurationMinutes.set(Math.max(1, v));
    }

    public String getHomeTeamName() {
        return homeTeamName.get();
    }

    public void setHomeTeamName(String v) {
        homeTeamName.set(v == null ? "" : v);
    }

    public String getAwayTeamName() {
        return awayTeamName.get();
    }

    public void setAwayTeamName(String v) {
        awayTeamName.set(v == null ? "" : v);
    }

    public int getHomeYellow() {
        return homeYellow.get();
    }

    public void setHomeYellow(int v) {
        homeYellow.set(Math.max(0, v));
    }

    public int getAwayYellow() {
        return awayYellow.get();
    }

    public void setAwayYellow(int v) {
        awayYellow.set(Math.max(0, v));
    }

    public int getHomeRed() {
        return homeRed.get();
    }

    public void setHomeRed(int v) {
        homeRed.set(Math.max(0, v));
    }

    public int getAwayRed() {
        return awayRed.get();
    }

    public void setAwayRed(int v) {
        awayRed.set(Math.max(0, v));
    }

    public int getHomeAgg() {
        return homeAgg.get();
    }

    public void setHomeAgg(int v) {
        homeAgg.set(Math.max(0, v));
    }

    public int getAwayAgg() {
        return awayAgg.get();
    }

    public void setAwayAgg(int v) {
        awayAgg.set(Math.max(0, v));
    }

    public int getHomeScore() {
        return homeScore.get();
    }

    public void setHomeScore(int v) {
        homeScore.set(Math.max(0, v));
    }

    public int getAwayScore() {
        return awayScore.get();
    }

    public void setAwayScore(int v) {
        awayScore.set(Math.max(0, v));
    }

    public int getHomeFouls() {
        return homeFouls.get();
    }

    public void setHomeFouls(int v) {
        homeFouls.set(Math.max(0, v));
    }

    public int getAwayFouls() {
        return awayFouls.get();
    }

    public void setAwayFouls(int v) {
        awayFouls.set(Math.max(0, v));
    }

    public Image getLeagueLogo() {
        return leagueLogo.get();
    }

    public void setLeagueLogo(Image v) {
        leagueLogo.set(v);
    }

    public Image getHomeLogo() {
        return homeLogo.get();
    }

    public void setHomeLogo(Image v) {
        homeLogo.set(v);
    }

    public Image getAwayLogo() {
        return awayLogo.get();
    }

    public void setAwayLogo(Image v) {
        awayLogo.set(v);
    }

    public Image getBackgroundImage() {
        return backgroundImage.get();
    }

    public void setBackgroundImage(Image v) {
        backgroundImage.set(v);
    }

    // ── Image path getters / setters ──────────────────────────────────────
    public String getHomeLogoPath() {
        return homeLogoPath.get();
    }

    public void setHomeLogoPath(String v) {
        homeLogoPath.set(v);
    }

    public String getAwayLogoPath() {
        return awayLogoPath.get();
    }

    public void setAwayLogoPath(String v) {
        awayLogoPath.set(v);
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath.get();
    }

    public void setBackgroundImagePath(String v) {
        backgroundImagePath.set(v);
    }

    public String getLeagueLogoPath() {
        return leagueLogoPath.get();
    }

    public void setLeagueLogoPath(String v) {
        leagueLogoPath.set(v);
    }

    public long getElapsedSeconds() {
        return elapsedSeconds.get();
    }

    public void setElapsedSeconds(long v) {
        elapsedSeconds.set(Math.max(0, v));
    }

    public boolean isTimerRunning() {
        return timerRunning.get();
    }

    public void setTimerRunning(boolean v) {
        timerRunning.set(v);
    }

    public boolean isCountDown() {
        return countDown.get();
    }

    public void setCountDown(boolean v) {
        countDown.set(v);
    }

    public double getBgOpacity() {
        return bgOpacity.get();
    }

    public void setBgOpacity(double v) {
        bgOpacity.set(Math.min(1.0, Math.max(0.0, v)));
    }

    public boolean isShowAggregate() {
        return showAggregate.get();
    }

    public void setShowAggregate(boolean v) {
        showAggregate.set(v);
    }

    public double getDisplayScale() {
        return displayScale.get();
    }

    public void setDisplayScale(double v) {
        displayScale.set(Math.min(3.0, Math.max(0.3, v)));
    }

    public String getResolution() {
        return resolution.get();
    }

    public void setResolution(String v) {
        resolution.set(v);
    }

    // ═════════════════════════════════════════════════════════════════════
    // Timer formatting
    // ═════════════════════════════════════════════════════════════════════
    public String getFormattedTime() {
        long totalSecs = (long) matchDurationMinutes.get() * 60;
        long display;
        if (countDown.get()) {
            display = Math.max(0, totalSecs - elapsedSeconds.get());
        } else {
            display = Math.min(totalSecs, elapsedSeconds.get());
        }
        long m = display / 60;
        long s = display % 60;
        return String.format("%02d:%02d", m, s);
    }

    public boolean isTimeExpired() {
        long totalSecs = (long) matchDurationMinutes.get() * 60;
        return elapsedSeconds.get() >= totalSecs;
    }

    // ═════════════════════════════════════════════════════════════════════
    // Full reset
    // ═════════════════════════════════════════════════════════════════════
    public void resetMatch() {
        setTimerRunning(false);
        setElapsedSeconds(0);
        setHomeScore(0);
        setAwayScore(0);
        setHomeFouls(0);
        setAwayFouls(0);
    }
}