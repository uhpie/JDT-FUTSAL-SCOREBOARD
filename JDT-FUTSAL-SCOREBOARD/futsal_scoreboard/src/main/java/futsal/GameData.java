package futsal;

import javafx.beans.property.*;
import javafx.scene.image.Image;

/**
 * Shared observable game state. Both the DisplayWindow and ControlPanel
 * bind to these properties so changes are reflected instantly everywhere.
 */
public class GameData {

    // ── Match Info ────────────────────────────────────────────────────────
    private final StringProperty leagueTitle = new SimpleStringProperty("FUTSAL LEAGUE 2026");
    private final IntegerProperty round = new SimpleIntegerProperty(1);
    private final IntegerProperty matchDurationMinutes = new SimpleIntegerProperty(20);
    private final IntegerProperty timeoutDurationMinutes = new SimpleIntegerProperty(1);

    // ── Timeout state (for DisplayWindow sync) ────────────────────────────
    private final BooleanProperty timeoutActive = new SimpleBooleanProperty(false);
    private final LongProperty timeoutRemainingSecs = new SimpleLongProperty(0);

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

    // ── Image file paths (for persistence / profile save-load) ────────────
    private String homeLogoPath = null;
    private String buzzerSoundPath = null;
    private String awayLogoPath = null;
    private String leagueLogoPath = null;
    private String backgroundImagePath = null;

    // ── Red card timer sync (for DisplayWindow) ────────────────────────────
    public static final int MAX_CARD_TIMERS = 20;
    private final LongProperty[] homeCardTimerSecs = buildTimerArray();
    private final LongProperty[] awayCardTimerSecs = buildTimerArray();

    private static LongProperty[] buildTimerArray() {
        LongProperty[] arr = new LongProperty[MAX_CARD_TIMERS];
        for (int i = 0; i < MAX_CARD_TIMERS; i++)
            arr[i] = new SimpleLongProperty(-1);
        return arr;
    }

    public LongProperty homeCardTimerSecsProperty(int idx) {
        return homeCardTimerSecs[idx];
    }

    public LongProperty awayCardTimerSecsProperty(int idx) {
        return awayCardTimerSecs[idx];
    }

    public long getHomeCardTimerSecs(int idx) {
        return homeCardTimerSecs[idx].get();
    }

    public long getAwayCardTimerSecs(int idx) {
        return awayCardTimerSecs[idx].get();
    }

    public void setHomeCardTimerSecs(int idx, long v) {
        homeCardTimerSecs[idx].set(v);
    }

    public void setAwayCardTimerSecs(int idx, long v) {
        awayCardTimerSecs[idx].set(v);
    }

    // ── Timer ─────────────────────────────────────────────────────────────
    private final LongProperty elapsedSeconds = new SimpleLongProperty(0);
    private final BooleanProperty timerRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty countDown = new SimpleBooleanProperty(true);

    // ── Display ───────────────────────────────────────────────────────────
    private final DoubleProperty bgOpacity = new SimpleDoubleProperty(0.28);
    private final DoubleProperty displayScale = new SimpleDoubleProperty(1.0);
    private final BooleanProperty showAggregate = new SimpleBooleanProperty(false);
    private final StringProperty resolution = new SimpleStringProperty("1280x720");

    // ── Penalty Shootout ──────────────────────────────────────────────────
    /**
     * Kick result values:
     * 0 = not yet taken (pending)
     * 1 = scored (goal)
     * 2 = missed
     */
    public static final int MAX_PENALTY_KICKS = 20;
    public static final int KICKS_PER_SET = 5;

    private final BooleanProperty showPenaltyScreen = new SimpleBooleanProperty(false);
    private final IntegerProperty homePenaltyScore = new SimpleIntegerProperty(0);
    private final IntegerProperty awayPenaltyScore = new SimpleIntegerProperty(0);
    private final IntegerProperty homePenaltyCount = new SimpleIntegerProperty(0);
    private final IntegerProperty awayPenaltyCount = new SimpleIntegerProperty(0);

    private final IntegerProperty[] homePenaltyKicks = buildKickArray();
    private final IntegerProperty[] awayPenaltyKicks = buildKickArray();

    private static IntegerProperty[] buildKickArray() {
        IntegerProperty[] arr = new IntegerProperty[MAX_PENALTY_KICKS];
        for (int i = 0; i < MAX_PENALTY_KICKS; i++)
            arr[i] = new SimpleIntegerProperty(0);
        return arr;
    }

    public BooleanProperty showPenaltyScreenProperty() {
        return showPenaltyScreen;
    }

    public boolean isShowPenaltyScreen() {
        return showPenaltyScreen.get();
    }

    public void setShowPenaltyScreen(boolean v) {
        showPenaltyScreen.set(v);
    }

    public IntegerProperty homePenaltyScoreProperty() {
        return homePenaltyScore;
    }

    public int getHomePenaltyScore() {
        return homePenaltyScore.get();
    }

    public void setHomePenaltyScore(int v) {
        homePenaltyScore.set(Math.max(0, v));
    }

    public IntegerProperty awayPenaltyScoreProperty() {
        return awayPenaltyScore;
    }

    public int getAwayPenaltyScore() {
        return awayPenaltyScore.get();
    }

    public void setAwayPenaltyScore(int v) {
        awayPenaltyScore.set(Math.max(0, v));
    }

    public IntegerProperty homePenaltyCountProperty() {
        return homePenaltyCount;
    }

    public int getHomePenaltyCount() {
        return homePenaltyCount.get();
    }

    public void setHomePenaltyCount(int v) {
        homePenaltyCount.set(Math.max(0, Math.min(MAX_PENALTY_KICKS, v)));
    }

    public IntegerProperty awayPenaltyCountProperty() {
        return awayPenaltyCount;
    }

    public int getAwayPenaltyCount() {
        return awayPenaltyCount.get();
    }

    public void setAwayPenaltyCount(int v) {
        awayPenaltyCount.set(Math.max(0, Math.min(MAX_PENALTY_KICKS, v)));
    }

    public IntegerProperty homePenaltyKickProperty(int idx) {
        return homePenaltyKicks[idx];
    }

    public IntegerProperty awayPenaltyKickProperty(int idx) {
        return awayPenaltyKicks[idx];
    }

    public int getHomePenaltyKick(int idx) {
        return homePenaltyKicks[idx].get();
    }

    public int getAwayPenaltyKick(int idx) {
        return awayPenaltyKicks[idx].get();
    }

    public void setHomePenaltyKick(int idx, int val) {
        homePenaltyKicks[idx].set(val);
    }

    public void setAwayPenaltyKick(int idx, int val) {
        awayPenaltyKicks[idx].set(val);
    }

    /** Add a penalty kick result for home (1=scored, 2=missed). */
    public void addHomePenaltyKick(int result) {
        int count = getHomePenaltyCount();
        if (count >= MAX_PENALTY_KICKS)
            return;
        setHomePenaltyKick(count, result);
        setHomePenaltyCount(count + 1);
        if (result == 1)
            setHomePenaltyScore(getHomePenaltyScore() + 1);
    }

    /** Remove the last home penalty kick. */
    public void undoHomePenaltyKick() {
        int count = getHomePenaltyCount();
        if (count <= 0)
            return;
        int lastResult = getHomePenaltyKick(count - 1);
        setHomePenaltyKick(count - 1, 0);
        setHomePenaltyCount(count - 1);
        if (lastResult == 1)
            setHomePenaltyScore(getHomePenaltyScore() - 1);
    }

    /** Add a penalty kick result for away (1=scored, 2=missed). */
    public void addAwayPenaltyKick(int result) {
        int count = getAwayPenaltyCount();
        if (count >= MAX_PENALTY_KICKS)
            return;
        setAwayPenaltyKick(count, result);
        setAwayPenaltyCount(count + 1);
        if (result == 1)
            setAwayPenaltyScore(getAwayPenaltyScore() + 1);
    }

    /** Remove the last away penalty kick. */
    public void undoAwayPenaltyKick() {
        int count = getAwayPenaltyCount();
        if (count <= 0)
            return;
        int lastResult = getAwayPenaltyKick(count - 1);
        setAwayPenaltyKick(count - 1, 0);
        setAwayPenaltyCount(count - 1);
        if (lastResult == 1)
            setAwayPenaltyScore(getAwayPenaltyScore() - 1);
    }

    /** Reset all penalty data. */
    public void resetPenalty() {
        setHomePenaltyCount(0);
        setAwayPenaltyCount(0);
        setHomePenaltyScore(0);
        setAwayPenaltyScore(0);
        for (int i = 0; i < MAX_PENALTY_KICKS; i++) {
            homePenaltyKicks[i].set(0);
            awayPenaltyKicks[i].set(0);
        }
    }

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

    public IntegerProperty timeoutDurationMinutesProperty() {
        return timeoutDurationMinutes;
    }

    public BooleanProperty timeoutActiveProperty() {
        return timeoutActive;
    }

    public LongProperty timeoutRemainingSecsProperty() {
        return timeoutRemainingSecs;
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

    public int getTimeoutDurationMinutes() {
        return timeoutDurationMinutes.get();
    }

    public void setTimeoutDurationMinutes(int v) {
        timeoutDurationMinutes.set(Math.max(1, v));
    }

    public boolean isTimeoutActive() {
        return timeoutActive.get();
    }

    public void setTimeoutActive(boolean v) {
        timeoutActive.set(v);
    }

    public long getTimeoutRemainingSecs() {
        return timeoutRemainingSecs.get();
    }

    public void setTimeoutRemainingSecs(long v) {
        timeoutRemainingSecs.set(Math.max(0, v));
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

    public String getLeagueLogoPath() {
        return leagueLogoPath;
    }

    public void setLeagueLogoPath(String v) {
        leagueLogoPath = v;
    }

    public Image getHomeLogo() {
        return homeLogo.get();
    }

    public void setHomeLogo(Image v) {
        homeLogo.set(v);
    }

    public String getBuzzerSoundPath() {
        return buzzerSoundPath;
    }

    public void setBuzzerSoundPath(String v) {
        buzzerSoundPath = v;
    }

    public String getHomeLogoPath() {
        return homeLogoPath;
    }

    public void setHomeLogoPath(String v) {
        homeLogoPath = v;
    }

    public Image getAwayLogo() {
        return awayLogo.get();
    }

    public void setAwayLogo(Image v) {
        awayLogo.set(v);
    }

    public String getAwayLogoPath() {
        return awayLogoPath;
    }

    public void setAwayLogoPath(String v) {
        awayLogoPath = v;
    }

    public Image getBackgroundImage() {
        return backgroundImage.get();
    }

    public void setBackgroundImage(Image v) {
        backgroundImage.set(v);
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String v) {
        backgroundImagePath = v;
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
        setTimeoutActive(false);
        setTimeoutRemainingSecs(0);
    }
}