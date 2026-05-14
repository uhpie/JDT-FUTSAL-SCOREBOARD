package futsal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;
import java.io.File;

public class ControlPanel {

    private static final String BG_DARK = "#000000";
    private static final String GOLD = "#FFD700";
    private static final String GREEN = "#2EA043";
    private static final String RED_BTN = "#8B0000";
    private static final String BLUE = "#1F6FEB";
    private static final String TEXT_PRI = "#FFFFFF";
    private static final String TEXT_SEC = "rgba(255,255,255,0.50)";
    private static final String ORANGE = "#FF8C00";

    private final GameData data;
    private final Stage displayStage;
    private final DisplayWindow display;
    private Stage stage;
    private Stage settingsStage;
    private Label cpTimerLabel;
    private VBox homeCardTimersBox, awayCardTimersBox;
    private final java.util.List<javafx.animation.Timeline> allCardTimers = new java.util.ArrayList<>();
    private int homeCardCount = 0;
    private int awayCardCount = 0;
    private Label leagueLogoLabel, homeLogoLabel, awayLogoLabel, bgImageLabel, buzzerSoundLabel;

    public ControlPanel(GameData data, Stage displayStage, DisplayWindow display) {
        this.data = data;
        this.displayStage = displayStage;
        this.display = display;
    }

    public void show(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("JDT Futsal \u2014 Referee Control Panel");

        final double BASE_W = 1100, BASE_H = 720;

        StackPane canvas = buildRoot();
        canvas.setStyle("-fx-background-color: " + BG_DARK + ";");
        canvas.setPrefSize(BASE_W, BASE_H);
        canvas.setMinSize(BASE_W, BASE_H);
        canvas.setMaxSize(BASE_W, BASE_H);

        javafx.scene.transform.Scale sc = new javafx.scene.transform.Scale(1, 1, 0, 0);
        canvas.getTransforms().add(sc);

        StackPane outer = new StackPane();
        outer.setStyle("-fx-background-color: black;");
        outer.getChildren().add(canvas);
        StackPane.setAlignment(canvas, Pos.TOP_LEFT);

        Scene scene = new Scene(outer, BASE_W, BASE_H, Color.web(BG_DARK));

        Runnable rescale = () -> {
            double sw = scene.getWidth(), sh = scene.getHeight();
            sc.setX(sw / BASE_W);
            sc.setY(sh / BASE_H);
        };
        scene.widthProperty().addListener((o, a, b) -> rescale.run());
        scene.heightProperty().addListener((o, a, b) -> rescale.run());
        stage.setOnShown(e2 -> rescale.run());

        scene.setOnKeyPressed(e2 -> {
            if (e2.getCode() == javafx.scene.input.KeyCode.F11)
                stage.setFullScreen(!stage.isFullScreen());
        });

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(400);
        stage.setMinHeight(280);
        stage.setFullScreenExitHint("Press F11 to exit fullscreen");
        stage.show();
    }

    private StackPane buildRoot() {
        BorderPane bp = new BorderPane();
        bp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bp.setTop(buildTitleBar());

        HBox center = new HBox(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10, 14, 8, 14));

        VBox homeCol = buildTeamColumn(true);
        VBox centerCol = buildCenterColumn();
        VBox awayCol = buildTeamColumn(false);

        homeCol.setPrefWidth(0);
        awayCol.setPrefWidth(0);
        HBox.setHgrow(homeCol, Priority.ALWAYS);
        HBox.setHgrow(centerCol, Priority.NEVER);
        HBox.setHgrow(awayCol, Priority.ALWAYS);

        center.getChildren().addAll(homeCol, centerCol, awayCol);
        bp.setCenter(center);
        bp.setBottom(buildBottomBar());

        // ── Penalty overlay ──
        VBox penaltyOverlay = buildPenaltyPanel();
        penaltyOverlay.setVisible(data.isShowPenaltyScreen());
        penaltyOverlay.setMouseTransparent(!data.isShowPenaltyScreen());

        data.showPenaltyScreenProperty().addListener((o, a, show) -> {
            penaltyOverlay.setVisible(show);
            penaltyOverlay.setMouseTransparent(!show);
        });

        StackPane root = new StackPane(bp, penaltyOverlay);
        root.setStyle("-fx-background-color: transparent;");
        return root;
    }

    // ── Title bar ──────────────────────────────────────────────────────────
    private HBox buildTitleBar() {
        Label title = new Label(data.getLeagueTitle());
        title.setFont(Font.font("Helvetica", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(GOLD));
        title.setAlignment(Pos.CENTER);

        data.leagueTitleProperty().addListener((o, a, b) -> title.setText(b));

        // ── PENALTY button (header, top-right) ──
        String penOffStyle = "-fx-background-color:#2B1800; -fx-text-fill:" + ORANGE
                + "; -fx-border-color:" + ORANGE + "99; -fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-padding:7 18; -fx-cursor:hand; -fx-border-width:2; -fx-font-weight:bold; -fx-font-size:13;";
        String penOnStyle = "-fx-background-color:" + ORANGE + "; -fx-text-fill:#000000; "
                + "-fx-border-color:" + ORANGE + "; -fx-border-radius:8; -fx-background-radius:8; "
                + "-fx-padding:7 18; -fx-cursor:hand; -fx-font-weight:bold; -fx-font-size:13; -fx-border-width:2;";
        Button penaltyBtn = new Button(data.isShowPenaltyScreen() ? "\u26bd  EXIT PENALTY" : "\u26bd  PENALTY");
        penaltyBtn.setStyle(data.isShowPenaltyScreen() ? penOnStyle : penOffStyle);
        penaltyBtn.setOnAction(e -> data.setShowPenaltyScreen(!data.isShowPenaltyScreen()));
        data.showPenaltyScreenProperty().addListener((o, a, b) -> {
            penaltyBtn.setStyle(b ? penOnStyle : penOffStyle);
            penaltyBtn.setText(b ? "\u26bd  EXIT PENALTY" : "\u26bd  PENALTY");
        });

        // ── SETTINGS button ──
        Button settingsBtn = new Button("\u2699  SETTINGS");
        settingsBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        settingsBtn.setStyle("-fx-background-color:rgba(255,215,0,0.14); -fx-text-fill:" + GOLD
                + "; -fx-border-color:rgba(255,215,0,0.45); -fx-border-radius:8;"
                + "-fx-background-radius:8; -fx-padding:7 18; -fx-cursor:hand; -fx-font-weight:bold;");
        settingsBtn.setOnAction(e -> openSettings());

        HBox rightButtons = new HBox(10, penaltyBtn, settingsBtn);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);

        // Left spacer equal width to rightButtons so title stays centered
        Region leftSpacer = new Region();
        leftSpacer.prefWidthProperty().bind(rightButtons.widthProperty());

        StackPane titlePane = new StackPane(title);
        StackPane.setAlignment(title, Pos.CENTER);
        HBox.setHgrow(titlePane, Priority.ALWAYS);

        HBox bar = new HBox(leftSpacer, titlePane, rightButtons);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10, 14, 8, 14));
        bar.setStyle("-fx-background-color:transparent;"
                + "-fx-border-color:rgba(255,255,255,0.08); -fx-border-width:0 0 1 0;");
        return bar;
    }

    // ── Team column (rounded dark card like FutsalApp) ────────────────────
    private VBox buildTeamColumn(boolean isHome) {
        VBox col = new VBox(14);
        col.setAlignment(Pos.TOP_CENTER);
        col.setPadding(new Insets(22, 22, 22, 22));
        col.setStyle(
                "-fx-background-color:#101010;"
                        + "-fx-background-radius:20;"
                        + "-fx-border-color:rgba(255,255,255,0.07);"
                        + "-fx-border-radius:20;"
                        + "-fx-border-width:1;");
        VBox.setVgrow(col, Priority.ALWAYS);

        Label nameLabel = new Label(isHome ? data.getHomeTeamName() : data.getAwayTeamName());
        nameLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 26));
        nameLabel.setTextFill(Color.web(TEXT_PRI));
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        if (isHome)
            data.homeTeamNameProperty().addListener((o, a, b) -> nameLabel.setText(b));
        else
            data.awayTeamNameProperty().addListener((o, a, b) -> nameLabel.setText(b));

        Label scoreVal = new Label(String.valueOf(isHome ? data.getHomeScore() : data.getAwayScore()));
        scoreVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 72));
        scoreVal.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        scoreVal.setWrapText(false);
        scoreVal.setTextFill(Color.web(TEXT_PRI));
        scoreVal.setAlignment(Pos.CENTER);
        scoreVal.setMaxWidth(200);
        if (isHome)
            data.homeScoreProperty().addListener((o, a, b) -> scoreVal.setText(b.toString()));
        else
            data.awayScoreProperty().addListener((o, a, b) -> scoreVal.setText(b.toString()));

        Button minus = scoreBtn("\u2212", "#2A2A2A"), plus = scoreBtn("+", "#2A2A2A");
        minus.setOnAction(e -> {
            if (isHome) {
                data.setHomeScore(data.getHomeScore() - 1);
                data.setHomeAgg(Math.max(0, data.getHomeAgg() - 1));
            } else {
                data.setAwayScore(data.getAwayScore() - 1);
                data.setAwayAgg(Math.max(0, data.getAwayAgg() - 1));
            }
        });
        plus.setOnAction(e -> {
            if (isHome) {
                data.setHomeScore(data.getHomeScore() + 1);
                data.setHomeAgg(data.getHomeAgg() + 1);
            } else {
                data.setAwayScore(data.getAwayScore() + 1);
                data.setAwayAgg(data.getAwayAgg() + 1);
            }
        });

        HBox scoreRow = new HBox(14, minus, scoreVal, plus);
        scoreRow.setAlignment(Pos.CENTER);

        VBox timersBox = new VBox(6);
        timersBox.setAlignment(Pos.CENTER);
        timersBox.setPickOnBounds(false);
        if (isHome)
            homeCardTimersBox = timersBox;
        else
            awayCardTimersBox = timersBox;
        VBox.setVgrow(timersBox, Priority.ALWAYS);

        col.getChildren().addAll(nameLabel, scoreRow, timersBox);
        return col;
    }

    // ── Center column (FutsalApp style) ────────────────────────────────────
    private VBox buildCenterColumn() {
        VBox col = new VBox(8);
        col.setAlignment(Pos.TOP_CENTER);
        col.setPadding(new Insets(8, 6, 8, 6));
        col.setMinWidth(340);
        col.setPrefWidth(340);
        col.setMaxWidth(340);

        // ── Big clock ──
        cpTimerLabel = new Label(data.getFormattedTime());
        cpTimerLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 82));
        cpTimerLabel.setTextFill(Color.web(GOLD));
        cpTimerLabel.setAlignment(Pos.CENTER);
        cpTimerLabel.setMaxWidth(Double.MAX_VALUE);
        data.elapsedSecondsProperty().addListener((o, a, b) -> cpTimerLabel.setText(data.getFormattedTime()));
        data.matchDurationMinutesProperty().addListener((o, a, b) -> cpTimerLabel.setText(data.getFormattedTime()));
        data.countDownProperty().addListener((o, a, b) -> cpTimerLabel.setText(data.getFormattedTime()));

        data.timerRunningProperty().addListener((o, a, running) -> {
            for (javafx.animation.Timeline tl : allCardTimers) {
                if (running)
                    tl.play();
                else
                    tl.stop();
            }
        });

        // ── Timeout display label (shown when active) ──
        Label toDisplayLbl = new Label("TIMEOUT   01:00");
        toDisplayLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 16));
        toDisplayLbl.setTextFill(Color.web("#60A5FA"));
        toDisplayLbl.setAlignment(Pos.CENTER);
        toDisplayLbl.setMaxWidth(Double.MAX_VALUE);
        toDisplayLbl.setVisible(false);
        toDisplayLbl.setManaged(false);

        // ── 3 action buttons in a row: START/PAUSE | RESET | TIMEOUT ──
        Button startPauseBtn = new Button("\u25b6  START");
        startPauseBtn.setMaxWidth(Double.MAX_VALUE);
        startPauseBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));
        startPauseBtn.setStyle("-fx-background-color:" + GREEN + "; -fx-text-fill:white;"
                + " -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:14 0; -fx-cursor:hand;");
        HBox.setHgrow(startPauseBtn, Priority.ALWAYS);

        Button resetBtn = new Button("\u21ba  RESET");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));
        resetBtn.setStyle("-fx-background-color:" + RED_BTN + "; -fx-text-fill:white;"
                + " -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:14 0; -fx-cursor:hand;");
        HBox.setHgrow(resetBtn, Priority.ALWAYS);

        Button timeoutBtn = new Button("TIMEOUT");
        timeoutBtn.setMaxWidth(Double.MAX_VALUE);
        timeoutBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));
        timeoutBtn.setStyle("-fx-background-color:#1C3A5F; -fx-text-fill:white;"
                + " -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:14 0; -fx-cursor:hand;");
        HBox.setHgrow(timeoutBtn, Priority.ALWAYS);

        HBox timerBtns = new HBox(6, startPauseBtn, resetBtn, timeoutBtn);
        timerBtns.setMaxWidth(Double.MAX_VALUE);

        // Toggle start/pause
        data.timerRunningProperty().addListener((o, a, running) -> {
            if (running) {
                startPauseBtn.setText("\u23f8  PAUSE");
                startPauseBtn.setStyle("-fx-background-color:#7C4A00; -fx-text-fill:#FCA53A;"
                        + " -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:14 0; -fx-cursor:hand;");
            } else {
                startPauseBtn.setText("\u25b6  START");
                startPauseBtn.setStyle("-fx-background-color:" + GREEN + "; -fx-text-fill:white;"
                        + " -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:14 0; -fx-cursor:hand;");
            }
        });
        startPauseBtn.setOnAction(e -> data.setTimerRunning(!data.isTimerRunning()));
        resetBtn.setOnAction(e -> {
            data.setTimerRunning(false);
            data.setElapsedSeconds(0);
        });

        // ── Timeout countdown logic ──
        javafx.animation.Timeline[] timeoutTimeline = { null };
        long[] timeoutRemaining = { 0 };
        Runnable updateToDisplay = () -> {
            long s = timeoutRemaining[0];
            toDisplayLbl.setText(String.format("TIMEOUT   %02d:%02d", s / 60, s % 60));
        };
        Runnable startTimeoutCountdown = () -> {
            if (timeoutTimeline[0] != null)
                timeoutTimeline[0].stop();
            timeoutTimeline[0] = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), ev -> {
                        if (timeoutRemaining[0] > 0) {
                            timeoutRemaining[0]--;
                            updateToDisplay.run();
                            data.setTimeoutRemainingSecs(timeoutRemaining[0]);
                        }
                        if (timeoutRemaining[0] <= 0) {
                            timeoutTimeline[0].stop();
                            data.setTimeoutActive(false);
                            // Do NOT auto-resume — referee clicks START when ready
                            data.setTimerRunning(false);
                            toDisplayLbl.setVisible(false);
                            toDisplayLbl.setManaged(false);
                        }
                    }));
            timeoutTimeline[0].setCycleCount(javafx.animation.Animation.INDEFINITE);
            timeoutTimeline[0].play();
        };
        timeoutBtn.setOnAction(e -> {
            data.setTimerRunning(false);
            timeoutRemaining[0] = (long) data.getTimeoutDurationMinutes() * 60;
            updateToDisplay.run();
            data.setTimeoutActive(true);
            data.setTimeoutRemainingSecs(timeoutRemaining[0]);
            toDisplayLbl.setVisible(true);
            toDisplayLbl.setManaged(true);
            startTimeoutCountdown.run();
        });

        // ── Gold separator ──
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color:rgba(255,215,0,0.25);");

        // ── Round block (label above number) ──
        Label roundLbl = new Label("ROUND");
        roundLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 11));
        roundLbl.setTextFill(Color.web(TEXT_SEC));
        roundLbl.setAlignment(Pos.CENTER);
        roundLbl.setMaxWidth(Double.MAX_VALUE);

        Label roundVal = new Label(String.valueOf(data.getRound()));
        roundVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 38));
        roundVal.setTextFill(Color.web(TEXT_PRI));
        roundVal.setAlignment(Pos.CENTER);
        roundVal.setMinWidth(55);
        data.roundProperty().addListener((o, a, b) -> roundVal.setText(b.toString()));

        Button rM = smallBtn("\u2212", "#2A2A2A"), rP = smallBtn("+", "#2A2A2A");
        rM.setOnAction(e -> data.setRound(data.getRound() - 1));
        rP.setOnAction(e -> data.setRound(data.getRound() + 1));

        HBox roundBtns = new HBox(6, rM, roundVal, rP);
        roundBtns.setAlignment(Pos.CENTER);
        VBox roundRow = new VBox(2, roundLbl, roundBtns);
        roundRow.setAlignment(Pos.CENTER);

        // ── Aggregate section (improved full-width) ──
        String aggOffSty = "-fx-background-color:#1C1C1C; -fx-text-fill:rgba(255,255,255,0.55);"
                + " -fx-border-color:rgba(255,255,255,0.15); -fx-border-radius:6; -fx-background-radius:6;"
                + " -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:12; -fx-font-weight:bold;";
        String aggOnSty = "-fx-background-color:" + GOLD + "33; -fx-text-fill:" + GOLD + ";"
                + " -fx-border-color:" + GOLD + "66; -fx-border-radius:6; -fx-background-radius:6;"
                + " -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:12; -fx-font-weight:bold;";

        ToggleButton aggToggle = new ToggleButton(data.isShowAggregate() ? "AGG: ON" : "AGG: OFF");
        aggToggle.setSelected(data.isShowAggregate());
        aggToggle.setMaxWidth(Double.MAX_VALUE);
        aggToggle.setStyle(data.isShowAggregate() ? aggOnSty : aggOffSty);

        Label homeAggVal = new Label(String.valueOf(data.getHomeAgg()));
        homeAggVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 32));
        homeAggVal.setTextFill(Color.web(TEXT_PRI));
        homeAggVal.setMinWidth(46);
        homeAggVal.setAlignment(Pos.CENTER);
        data.homeAggProperty().addListener((o, a, b) -> homeAggVal.setText(b.toString()));

        Label aggDash = new Label("\u2014");
        aggDash.setFont(Font.font("Helvetica", FontWeight.BOLD, 22));
        aggDash.setTextFill(Color.web(TEXT_SEC));

        Label awayAggVal = new Label(String.valueOf(data.getAwayAgg()));
        awayAggVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 32));
        awayAggVal.setTextFill(Color.web(TEXT_PRI));
        awayAggVal.setMinWidth(46);
        awayAggVal.setAlignment(Pos.CENTER);
        data.awayAggProperty().addListener((o, a, b) -> awayAggVal.setText(b.toString()));

        Button hAggM = cardBtn("\u2212", "#2A2A2A"), hAggP = cardBtn("+", "#2A2A2A"),
                aAggM = cardBtn("\u2212", "#2A2A2A"), aAggP = cardBtn("+", "#2A2A2A");
        hAggM.setOnAction(e -> data.setHomeAgg(data.getHomeAgg() - 1));
        hAggP.setOnAction(e -> data.setHomeAgg(data.getHomeAgg() + 1));
        aAggM.setOnAction(e -> data.setAwayAgg(data.getAwayAgg() - 1));
        aAggP.setOnAction(e -> data.setAwayAgg(data.getAwayAgg() + 1));

        HBox aggValRow = new HBox(5, hAggM, homeAggVal, hAggP, aggDash, aAggM, awayAggVal, aAggP);
        aggValRow.setAlignment(Pos.CENTER);
        aggValRow.setVisible(data.isShowAggregate());
        aggValRow.setManaged(data.isShowAggregate());

        Label aggHeader = new Label("AGGREGATE");
        aggHeader.setFont(Font.font("Helvetica", FontWeight.BOLD, 10));
        aggHeader.setTextFill(Color.web(TEXT_SEC));
        aggHeader.setAlignment(Pos.CENTER);
        aggHeader.setMaxWidth(Double.MAX_VALUE);
        aggHeader.setVisible(data.isShowAggregate());
        aggHeader.setManaged(data.isShowAggregate());

        VBox aggBox = new VBox(4, aggToggle, aggHeader, aggValRow);
        aggBox.setAlignment(Pos.CENTER);
        aggBox.setMaxWidth(Double.MAX_VALUE);

        aggToggle.setOnAction(e -> {
            boolean on = aggToggle.isSelected();
            data.setShowAggregate(on);
            aggToggle.setText(on ? "AGG: ON" : "AGG: OFF");
            aggToggle.setStyle(on ? aggOnSty : aggOffSty);
            aggValRow.setVisible(on);
            aggValRow.setManaged(on);
            aggHeader.setVisible(on);
            aggHeader.setManaged(on);
        });
        // Sync toggle when profile loaded externally
        data.showAggregateProperty().addListener((o, a, b) -> {
            aggToggle.setSelected(b);
            aggToggle.setText(b ? "AGG: ON" : "AGG: OFF");
            aggToggle.setStyle(b ? aggOnSty : aggOffSty);
            aggValRow.setVisible(b);
            aggValRow.setManaged(b);
            aggHeader.setVisible(b);
            aggHeader.setManaged(b);
        });

        // ── Countdown mode toggle ──
        Button cdToggle = new Button(data.isCountDown() ? "MODE: COUNT DOWN \u25bc" : "MODE: COUNT UP \u25b2");
        cdToggle.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        cdToggle.setMaxWidth(Double.MAX_VALUE);
        cdToggle.setStyle("-fx-background-color:#1C1C1C; -fx-text-fill:rgba(255,255,255,0.70);"
                + " -fx-border-color:rgba(255,255,255,0.15); -fx-border-radius:6; -fx-background-radius:6;"
                + " -fx-padding:7 14; -fx-cursor:hand;");
        cdToggle.setOnAction(e -> {
            data.setCountDown(!data.isCountDown());
            cdToggle.setText(data.isCountDown() ? "MODE: COUNT DOWN \u25bc" : "MODE: COUNT UP \u25b2");
        });
        data.countDownProperty()
                .addListener((o, a, b) -> cdToggle.setText(b ? "MODE: COUNT DOWN \u25bc" : "MODE: COUNT UP \u25b2"));

        // ── Buzzer ──
        Button buzzerBtn = new Button("\uD83D\uDD0A  BUZZER");
        buzzerBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
        buzzerBtn.setMaxWidth(Double.MAX_VALUE);
        buzzerBtn.setStyle("-fx-background-color:" + RED_BTN + "; -fx-text-fill:white;"
                + " -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:18 0; -fx-cursor:hand;");
        buzzerBtn.setOnAction(e -> {
            String buzPath = data.getBuzzerSoundPath();
            if (buzPath != null && !buzPath.isEmpty() && new File(buzPath).exists()) {
                new Thread(() -> {
                    try {
                        // Try javax.sound.sampled first (works for WAV)
                        File soundFile = new File(buzPath);
                        javax.sound.sampled.AudioInputStream ais = javax.sound.sampled.AudioSystem
                                .getAudioInputStream(soundFile);
                        javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                        clip.open(ais);
                        clip.addLineListener(evt -> {
                            if (evt.getType() == javax.sound.sampled.LineEvent.Type.STOP)
                                clip.close();
                        });
                        clip.start();
                    } catch (Exception ex) {
                        // Fallback: use OS media player (supports MP3 and all formats)
                        try {
                            String os = System.getProperty("os.name").toLowerCase();
                            ProcessBuilder pb;
                            if (os.contains("win")) {
                                // PowerShell MediaPlayer — supports MP3, WAV, WMA etc
                                String escaped = buzPath.replace("'", "\'");
                                pb = new ProcessBuilder("powershell", "-WindowStyle", "Hidden",
                                        "-Command",
                                        "Add-Type -AssemblyName presentationCore;" +
                                                "$mp=New-Object system.windows.media.mediaplayer;" +
                                                "$mp.Open([uri]'" + escaped + "');" +
                                                "$mp.Play();Start-Sleep 10;$mp.Stop()");
                            } else if (os.contains("mac")) {
                                pb = new ProcessBuilder("afplay", buzPath);
                            } else {
                                pb = new ProcessBuilder("ffplay", "-nodisp", "-autoexit", buzPath);
                            }
                            pb.start();
                        } catch (Exception ex2) {
                            java.awt.Toolkit.getDefaultToolkit().beep();
                        }
                    }
                }).start();
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });

        col.getChildren().addAll(
                cpTimerLabel, toDisplayLbl, timerBtns,
                sep, roundRow, aggBox, cdToggle, buzzerBtn);
        return col;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Penalty control panel overlay
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildPenaltyPanel() {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: #050505;");
        panel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ── Header bar ──
        Label header = new Label("\u26bd  PENALTY SHOOTOUT CONTROL");
        header.setFont(Font.font("Helvetica", FontWeight.BOLD, 22));
        header.setTextFill(Color.web(ORANGE));
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(Double.MAX_VALUE);

        Button exitPenBtn = new Button("\u2715  Exit Penalty Mode");
        exitPenBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        exitPenBtn.setStyle("-fx-background-color:#2A2A2A; -fx-text-fill:rgba(255,255,255,0.80); "
                + "-fx-border-color:rgba(255,255,255,0.18); -fx-border-radius:8; -fx-background-radius:8;"
                + "-fx-padding:7 22; -fx-cursor:hand;");
        exitPenBtn.setOnAction(e -> data.setShowPenaltyScreen(false));

        HBox titleRow = new HBox(header);
        titleRow.setAlignment(Pos.CENTER);
        titleRow.setPadding(new Insets(14, 20, 4, 20));

        HBox exitRow = new HBox(exitPenBtn);
        exitRow.setAlignment(Pos.CENTER);
        exitRow.setPadding(new Insets(2, 20, 10, 20));

        VBox headerBar = new VBox(0, titleRow, exitRow);
        headerBar.setStyle(
                "-fx-background-color: #0D0D0D; -fx-border-color: rgba(255,140,0,0.30); -fx-border-width: 0 0 2 0;");

        // ── Main controls area ──
        HBox teamsRow = new HBox(20);
        teamsRow.setPadding(new Insets(18, 20, 10, 20));
        teamsRow.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(teamsRow, Priority.ALWAYS);

        VBox homePanel = buildTeamPenaltyPanel(true);
        VBox awayPanel = buildTeamPenaltyPanel(false);

        // ── Center: reset + status ──
        VBox centerControls = new VBox(14);
        centerControls.setAlignment(Pos.CENTER);
        centerControls.setMinWidth(160);
        centerControls.setMaxWidth(160);

        Label vsLbl = new Label("VS");
        vsLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 36));
        vsLbl.setTextFill(Color.web("rgba(255,255,255,0.15)"));
        vsLbl.setAlignment(Pos.CENTER);
        vsLbl.setMaxWidth(Double.MAX_VALUE);

        Button resetPenBtn = new Button("\u21ba  Reset Penalty");
        resetPenBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        resetPenBtn.setMaxWidth(Double.MAX_VALUE);
        resetPenBtn.setStyle("-fx-background-color:" + RED_BTN + "; -fx-text-fill:white;"
                + "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:10 14; -fx-cursor:hand;");
        resetPenBtn.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Reset all penalty kicks?", ButtonType.OK,
                    ButtonType.CANCEL);
            a.setTitle("Confirm");
            a.setHeaderText(null);
            a.showAndWait().ifPresent(b -> {
                if (b == ButtonType.OK)
                    data.resetPenalty();
            });
        });

        centerControls.getChildren().addAll(vsLbl, resetPenBtn);

        HBox.setHgrow(homePanel, Priority.ALWAYS);
        HBox.setHgrow(awayPanel, Priority.ALWAYS);
        teamsRow.getChildren().addAll(homePanel, centerControls, awayPanel);

        // ── Kicks taken summary bar ──
        HBox summaryBar = new HBox();
        summaryBar.setAlignment(Pos.CENTER);
        summaryBar.setPadding(new Insets(8, 20, 12, 20));
        summaryBar.setStyle(
                "-fx-background-color:#0D0D0D; -fx-border-color:rgba(255,255,255,0.08); -fx-border-width:1 0 0 0;");

        Label summaryLbl = new Label();
        summaryLbl.setFont(Font.font("Helvetica", 13));
        summaryLbl.setTextFill(Color.web("rgba(255,255,255,0.50)"));
        summaryLbl.setAlignment(Pos.CENTER);
        summaryLbl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(summaryLbl, Priority.ALWAYS);

        Runnable updateSummary = () -> {
            int hc = data.getHomePenaltyCount(), ac = data.getAwayPenaltyCount();
            int hs = data.getHomePenaltyScore(), as = data.getAwayPenaltyScore();
            summaryLbl.setText(String.format(
                    "%s: %d goal(s) from %d kick(s)     |     %s: %d goal(s) from %d kick(s)",
                    data.getHomeTeamName(), hs, hc,
                    data.getAwayTeamName(), as, ac));
        };
        updateSummary.run();
        data.homePenaltyCountProperty().addListener((o, a, b) -> updateSummary.run());
        data.awayPenaltyCountProperty().addListener((o, a, b) -> updateSummary.run());
        data.homePenaltyScoreProperty().addListener((o, a, b) -> updateSummary.run());
        data.awayPenaltyScoreProperty().addListener((o, a, b) -> updateSummary.run());
        data.homeTeamNameProperty().addListener((o, a, b) -> updateSummary.run());
        data.awayTeamNameProperty().addListener((o, a, b) -> updateSummary.run());

        summaryBar.getChildren().add(summaryLbl);

        panel.getChildren().addAll(headerBar, teamsRow, summaryBar);
        return panel;
    }

    private VBox buildTeamPenaltyPanel(boolean isHome) {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(14));
        panel.setStyle("-fx-background-color:#111111; -fx-background-radius:12;"
                + "-fx-border-color:rgba(255,140,0,0.20); -fx-border-radius:12; -fx-border-width:1;");

        // ── Team name ──
        Label nameLbl = new Label(isHome ? data.getHomeTeamName() : data.getAwayTeamName());
        nameLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
        nameLbl.setTextFill(Color.web(TEXT_PRI));
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setMaxWidth(Double.MAX_VALUE);
        if (isHome)
            data.homeTeamNameProperty().addListener((o, a, b) -> nameLbl.setText(b));
        else
            data.awayTeamNameProperty().addListener((o, a, b) -> nameLbl.setText(b));

        // ── Penalty score ──
        Label scoreHeader = new Label("PENALTY GOALS");
        scoreHeader.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        scoreHeader.setTextFill(Color.web("rgba(255,255,255,0.45)"));
        scoreHeader.setAlignment(Pos.CENTER);
        scoreHeader.setMaxWidth(Double.MAX_VALUE);

        Label scoreLbl = new Label(String.valueOf(isHome ? data.getHomePenaltyScore() : data.getAwayPenaltyScore()));
        scoreLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 80));
        scoreLbl.setTextFill(Color.web(ORANGE));
        scoreLbl.setAlignment(Pos.CENTER);
        scoreLbl.setMaxWidth(Double.MAX_VALUE);
        if (isHome)
            data.homePenaltyScoreProperty().addListener((o, a, b) -> scoreLbl.setText(b.toString()));
        else
            data.awayPenaltyScoreProperty().addListener((o, a, b) -> scoreLbl.setText(b.toString()));

        // ── Kick count ──
        Label kickCountLbl = new Label();
        kickCountLbl.setFont(Font.font("Helvetica", 13));
        kickCountLbl.setTextFill(Color.web("rgba(255,255,255,0.50)"));
        kickCountLbl.setAlignment(Pos.CENTER);
        kickCountLbl.setMaxWidth(Double.MAX_VALUE);
        Runnable updateKickCount = () -> {
            int count = isHome ? data.getHomePenaltyCount() : data.getAwayPenaltyCount();
            int score = isHome ? data.getHomePenaltyScore() : data.getAwayPenaltyScore();
            kickCountLbl.setText(score + " scored / " + (count - score) + " missed / " + count + " total");
        };
        updateKickCount.run();
        if (isHome) {
            data.homePenaltyCountProperty().addListener((o, a, b) -> updateKickCount.run());
            data.homePenaltyScoreProperty().addListener((o, a, b) -> updateKickCount.run());
        } else {
            data.awayPenaltyCountProperty().addListener((o, a, b) -> updateKickCount.run());
            data.awayPenaltyScoreProperty().addListener((o, a, b) -> updateKickCount.run());
        }

        // ── Separator ──
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.10);");

        // ── Action buttons ──
        Label actionsHdr = new Label("RECORD KICK RESULT");
        actionsHdr.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        actionsHdr.setTextFill(Color.web("rgba(255,255,255,0.40)"));
        actionsHdr.setAlignment(Pos.CENTER);
        actionsHdr.setMaxWidth(Double.MAX_VALUE);

        Button scoredBtn = new Button("\u2705  SCORED");
        scoredBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 18));
        scoredBtn.setMaxWidth(Double.MAX_VALUE);
        scoredBtn.setStyle("-fx-background-color:#163020; -fx-text-fill:#2EA043; "
                + "-fx-border-color:#2EA043; -fx-border-radius:10; -fx-background-radius:10;"
                + "-fx-padding:14 10; -fx-cursor:hand; -fx-border-width:2;");
        scoredBtn.setOnAction(e -> {
            if (isHome) {
                data.addHomePenaltyKick(1);
                data.setHomeScore(data.getHomeScore() + 1); // update main scoreboard
                data.setHomeAgg(data.getHomeAgg() + 1); // update aggregate
            } else {
                data.addAwayPenaltyKick(1);
                data.setAwayScore(data.getAwayScore() + 1);
                data.setAwayAgg(data.getAwayAgg() + 1);
            }
        });

        Button missedBtn = new Button("\u274c  MISSED");
        missedBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 18));
        missedBtn.setMaxWidth(Double.MAX_VALUE);
        missedBtn.setStyle("-fx-background-color:#301616; -fx-text-fill:#FF3B3B; "
                + "-fx-border-color:#FF3B3B; -fx-border-radius:10; -fx-background-radius:10;"
                + "-fx-padding:14 10; -fx-cursor:hand; -fx-border-width:2;");
        missedBtn.setOnAction(e -> {
            if (isHome)
                data.addHomePenaltyKick(2);
            else
                data.addAwayPenaltyKick(2);
        });

        // Lock/unlock buttons based on kick count vs KICKS_PER_SET (5)
        String lockedStyle = "-fx-background-color:#1A1A1A; -fx-text-fill:rgba(255,255,255,0.25); "
                + "-fx-border-color:rgba(255,255,255,0.10); -fx-border-radius:10; -fx-background-radius:10;"
                + "-fx-padding:14 10; -fx-cursor:default; -fx-border-width:2;";
        String scoredActiveStyle = "-fx-background-color:#163020; -fx-text-fill:#2EA043; "
                + "-fx-border-color:#2EA043; -fx-border-radius:10; -fx-background-radius:10;"
                + "-fx-padding:14 10; -fx-cursor:hand; -fx-border-width:2;";
        String missedActiveStyle = "-fx-background-color:#301616; -fx-text-fill:#FF3B3B; "
                + "-fx-border-color:#FF3B3B; -fx-border-radius:10; -fx-background-radius:10;"
                + "-fx-padding:14 10; -fx-cursor:hand; -fx-border-width:2;";

        Runnable updateLock = () -> {
            int count = isHome ? data.getHomePenaltyCount() : data.getAwayPenaltyCount();
            boolean locked = count >= GameData.KICKS_PER_SET;
            scoredBtn.setDisable(locked);
            missedBtn.setDisable(locked);
            scoredBtn.setText(locked ? "\uD83D\uDD12  FULL (5/5)" : "\u2705  SCORED");
            missedBtn.setText(locked ? "\uD83D\uDD12  FULL (5/5)" : "\u274c  MISSED");
            scoredBtn.setStyle(locked ? lockedStyle : scoredActiveStyle);
            missedBtn.setStyle(locked ? lockedStyle : missedActiveStyle);
        };
        updateLock.run(); // apply initial state
        if (isHome) {
            data.homePenaltyCountProperty().addListener((o, a, b) -> updateLock.run());
        } else {
            data.awayPenaltyCountProperty().addListener((o, a, b) -> updateLock.run());
        }

        Button undoBtn = new Button("\u21a9  UNDO LAST KICK");
        undoBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        undoBtn.setMaxWidth(Double.MAX_VALUE);
        undoBtn.setStyle("-fx-background-color:#222; -fx-text-fill:rgba(255,255,255,0.70); "
                + "-fx-border-color:rgba(255,255,255,0.15); -fx-border-radius:8; -fx-background-radius:8;"
                + "-fx-padding:8 10; -fx-cursor:hand;");
        undoBtn.setOnAction(e -> {
            if (isHome) {
                int prevCount = data.getHomePenaltyCount();
                if (prevCount > 0) {
                    int lastKick = data.getHomePenaltyKick(prevCount - 1);
                    data.undoHomePenaltyKick();
                    if (lastKick == 1) { // was scored — reverse score + agg
                        data.setHomeScore(Math.max(0, data.getHomeScore() - 1));
                        data.setHomeAgg(Math.max(0, data.getHomeAgg() - 1));
                    }
                }
            } else {
                int prevCount = data.getAwayPenaltyCount();
                if (prevCount > 0) {
                    int lastKick = data.getAwayPenaltyKick(prevCount - 1);
                    data.undoAwayPenaltyKick();
                    if (lastKick == 1) {
                        data.setAwayScore(Math.max(0, data.getAwayScore() - 1));
                        data.setAwayAgg(Math.max(0, data.getAwayAgg() - 1));
                    }
                }
            }
        });

        panel.getChildren().addAll(
                nameLbl, scoreHeader, scoreLbl, kickCountLbl,
                sep, actionsHdr, scoredBtn, missedBtn, undoBtn);
        return panel;
    }

    // ── Bottom bar: fouls + yellow + red ──────────────────────────────────
    private VBox buildBottomBar() {
        HBox bar = new HBox(0);
        bar.setPadding(new Insets(10, 14, 12, 14));
        bar.setStyle("-fx-background-color:transparent;"
                + "-fx-border-color:rgba(255,255,255,0.08); -fx-border-width:1 0 0 0;");

        HBox homeBar = buildMergedBlock(true);
        HBox awayBar = buildMergedBlock(false);
        HBox.setHgrow(homeBar, Priority.ALWAYS);
        HBox.setHgrow(awayBar, Priority.ALWAYS);

        Region mid = new Region();
        mid.setMinWidth(20);

        bar.getChildren().addAll(homeBar, mid, awayBar);
        VBox outerBar = new VBox(bar);
        outerBar.setPickOnBounds(false);
        return outerBar;
    }

    private HBox buildMergedBlock(boolean isHome) {
        // ── FOULS ──
        Label foulTitle = new Label("FOULS");
        foulTitle.setFont(Font.font("Helvetica", FontWeight.BOLD, 9));
        foulTitle.setTextFill(Color.web(ORANGE));
        foulTitle.setAlignment(Pos.CENTER);

        Label foulVal = new Label(String.valueOf(isHome ? data.getHomeFouls() : data.getAwayFouls()));
        foulVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 32));
        foulVal.setTextFill(Color.web(ORANGE));
        foulVal.setMinWidth(42);
        foulVal.setAlignment(Pos.CENTER);
        if (isHome)
            data.homeFoulsProperty().addListener((o, a, b) -> foulVal.setText(b.toString()));
        else
            data.awayFoulsProperty().addListener((o, a, b) -> foulVal.setText(b.toString()));

        Button fMinus = smallBtn("\u2212", "#2A2A2A"), fPlus = smallBtn("+", "#2A2A2A"),
                fReset = smallBtn("RESET", "#2A2A2A");
        fMinus.setOnAction(e -> {
            if (isHome)
                data.setHomeFouls(data.getHomeFouls() - 1);
            else
                data.setAwayFouls(data.getAwayFouls() - 1);
        });
        fPlus.setOnAction(e -> {
            if (isHome)
                data.setHomeFouls(data.getHomeFouls() + 1);
            else
                data.setAwayFouls(data.getAwayFouls() + 1);
        });
        fReset.setOnAction(e -> {
            if (isHome)
                data.setHomeFouls(0);
            else
                data.setAwayFouls(0);
        });

        HBox fBtns = isHome ? new HBox(4, fReset, fMinus, foulVal, fPlus)
                : new HBox(4, fMinus, foulVal, fPlus, fReset);
        fBtns.setAlignment(Pos.CENTER);
        VBox foulBlock = new VBox(2, foulTitle, fBtns);
        foulBlock.setAlignment(Pos.CENTER);

        // ── RED card ──
        Label rTitle = new Label("RED");
        rTitle.setFont(Font.font("Helvetica", FontWeight.BOLD, 9));
        rTitle.setTextFill(Color.web("#FF4444"));
        rTitle.setAlignment(Pos.CENTER);

        Label rIcon = new Label("  ");
        rIcon.setStyle(
                "-fx-background-color:#DC2626; -fx-min-width:16; -fx-min-height:22; -fx-max-width:16; -fx-max-height:22; -fx-background-radius:3;");
        Label rVal = new Label(String.valueOf(isHome ? data.getHomeRed() : data.getAwayRed()));
        rVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 28));
        rVal.setTextFill(Color.web("#FF4444"));
        rVal.setMinWidth(34);
        rVal.setAlignment(Pos.CENTER);
        if (isHome)
            data.homeRedProperty().addListener((o, a, b) -> rVal.setText(b.toString()));
        else
            data.awayRedProperty().addListener((o, a, b) -> rVal.setText(b.toString()));
        Button rMinus = cardBtn("\u2212", "#2A2A2A"), rPlus = cardBtn("+", "#2A2A2A");
        rMinus.setOnAction(e -> {
            if (isHome && data.getHomeRed() <= 0)
                return;
            if (!isHome && data.getAwayRed() <= 0)
                return;
            if (isHome)
                data.setHomeRed(data.getHomeRed() - 1);
            else
                data.setAwayRed(data.getAwayRed() - 1);
            removeLastCardTimer(isHome);
            rPlus.setDisable(false);
        });
        rPlus.setOnAction(e -> {
            VBox timersBox = isHome ? homeCardTimersBox : awayCardTimersBox;
            if (timersBox != null && timersBox.getChildren().size() >= 2)
                return;
            if (isHome)
                data.setHomeRed(data.getHomeRed() + 1);
            else
                data.setAwayRed(data.getAwayRed() + 1);
            addCardTimer(isHome, 2, rPlus);
            if (timersBox != null && timersBox.getChildren().size() >= 2)
                rPlus.setDisable(true);
        });
        HBox rRow = new HBox(4, rIcon, rMinus, rVal, rPlus);
        rRow.setAlignment(Pos.CENTER);
        VBox redBlock = new VBox(2, rTitle, rRow);
        redBlock.setAlignment(Pos.CENTER);

        // Home = foul | red, Away = red | foul
        HBox row = isHome
                ? new HBox(14, foulBlock, redBlock)
                : new HBox(14, redBlock, foulBlock);
        row.setAlignment(isHome ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        HBox.setHgrow(row, Priority.ALWAYS);
        return row;
    }

    private VBox buildCardBlock(boolean isHome) {
        Label redIcon = new Label("  ");
        redIcon.setStyle(
                "-fx-background-color:#FF3B3B; -fx-min-width:22; -fx-min-height:22; -fx-max-width:22; -fx-max-height:22; -fx-background-radius:4;");
        Label redVal = new Label(String.valueOf(isHome ? data.getHomeRed() : data.getAwayRed()));
        redVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 28));
        redVal.setTextFill(Color.web("#FF3B3B"));
        redVal.setMinWidth(30);
        redVal.setAlignment(Pos.CENTER);
        if (isHome)
            data.homeRedProperty().addListener((o, a, b) -> redVal.setText(b.toString()));
        else
            data.awayRedProperty().addListener((o, a, b) -> redVal.setText(b.toString()));
        Button rMinus = cardBtn("\u2212", "#444444"), rPlus = cardBtn("+", "#444444");
        rMinus.setOnAction(e -> {
            if (isHome && data.getHomeRed() <= 0)
                return;
            if (!isHome && data.getAwayRed() <= 0)
                return;
            if (isHome)
                data.setHomeRed(data.getHomeRed() - 1);
            else
                data.setAwayRed(data.getAwayRed() - 1);
            removeLastCardTimer(isHome);
            rPlus.setDisable(false);
        });
        rPlus.setOnAction(e -> {
            VBox tb = isHome ? homeCardTimersBox : awayCardTimersBox;
            if (tb != null && tb.getChildren().size() >= 2)
                return;
            if (isHome)
                data.setHomeRed(data.getHomeRed() + 1);
            else
                data.setAwayRed(data.getAwayRed() + 1);
            addCardTimer(isHome, 2, rPlus);
            if (tb != null && tb.getChildren().size() >= 2)
                rPlus.setDisable(true);
        });
        HBox redRow = new HBox(4, redIcon, rMinus, redVal, rPlus);
        redRow.setAlignment(Pos.CENTER_LEFT);
        return new VBox(0, redRow);
    }

    private VBox buildFoulBlock(boolean isHome) {
        Label val = new Label(String.valueOf(isHome ? data.getHomeFouls() : data.getAwayFouls()));
        val.setFont(Font.font("Helvetica", FontWeight.BOLD, 36));
        val.setTextFill(Color.web(ORANGE));
        val.setMinWidth(45);
        val.setAlignment(Pos.CENTER);
        if (isHome)
            data.homeFoulsProperty().addListener((o, a, b) -> val.setText(b.toString()));
        else
            data.awayFoulsProperty().addListener((o, a, b) -> val.setText(b.toString()));
        Button minus = smallBtn("\u2212", "#444444"), plus = smallBtn("+", "#444444"),
                reset = smallBtn("RESET", "#444444");
        minus.setOnAction(e -> {
            if (isHome)
                data.setHomeFouls(data.getHomeFouls() - 1);
            else
                data.setAwayFouls(data.getAwayFouls() - 1);
        });
        plus.setOnAction(e -> {
            if (isHome)
                data.setHomeFouls(data.getHomeFouls() + 1);
            else
                data.setAwayFouls(data.getAwayFouls() + 1);
        });
        reset.setOnAction(e -> {
            if (isHome)
                data.setHomeFouls(0);
            else
                data.setAwayFouls(0);
        });
        HBox row = new HBox(6, minus, val, plus, reset);
        row.setAlignment(Pos.CENTER_LEFT);
        return new VBox(2, row);
    }

    // ═════════════════════════════════════════════════════════════════════
    // Settings
    // ═════════════════════════════════════════════════════════════════════
    private void openSettings() {
        if (settingsStage != null && settingsStage.isShowing()) {
            settingsStage.toFront();
            return;
        }
        settingsStage = new Stage();
        settingsStage.setTitle("\u2699  Settings");
        settingsStage.initOwner(stage);
        settingsStage.initModality(Modality.NONE);
        ScrollPane scroll = new ScrollPane(buildSettingsContent());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#0A0A0A; -fx-background-color:#0A0A0A;");
        settingsStage.setScene(new Scene(scroll, 500, 720, Color.web("#0A0A0A")));
        settingsStage.show();
    }

    private VBox buildSettingsContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#0A0A0A;");
        Label header = new Label("\u2699  SETTINGS");
        header.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
        header.setTextFill(Color.web(GOLD));
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        Label copyright = new Label("\u00a9 2026 JDT Futsal. All rights reserved.");
        copyright.setFont(Font.font("Helvetica", 11));
        copyright.setTextFill(Color.web("rgba(255,255,255,0.25)"));
        copyright.setAlignment(Pos.CENTER);
        copyright.setMaxWidth(Double.MAX_VALUE);
        copyright.setPadding(new Insets(10, 0, 4, 0));
        root.getChildren().addAll(header, hRule(), buildProfileCard(), hRule(),
                buildMatchInfoCard(), buildTeamCard(true), buildTeamCard(false),
                buildTimerSettingsCard(), buildBuzzerCard(), buildDisplayCard(), buildMatchControlCard(), copyright);
        return root;
    }

    private VBox buildProfileCard() {
        VBox card = card("\uD83D\uDCBE  SETTINGS PROFILES");
        TextField profileNameField = styledField("default");
        profileNameField.setPromptText("Profile name...");
        ListView<String> profileList = new ListView<>();
        profileList.setPrefHeight(120);
        profileList.setStyle(
                "-fx-background-color:#1C1C1C; -fx-control-inner-background:#1C1C1C; -fx-text-fill:white; -fx-border-color:rgba(255,255,255,0.12); -fx-border-radius:6;");
        profileList.getItems().addAll(SettingsManager.listProfiles());
        Runnable refreshList = () -> profileList.getItems().setAll(SettingsManager.listProfiles());
        profileList.setOnMouseClicked(e -> {
            String sel = profileList.getSelectionModel().getSelectedItem();
            if (sel != null)
                profileNameField.setText(sel);
        });
        Button saveBtn = bigBtn("\uD83D\uDCBE  Save", "#2EA043", null);
        saveBtn.setOnAction(e -> {
            String n = profileNameField.getText().trim();
            if (n.isEmpty())
                return;
            SettingsManager.save(data, n);
            refreshList.run();
            saveBtn.setText("\u2713  Saved!");
            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2),
                            ev -> saveBtn.setText("\uD83D\uDCBE  Save")))
                    .play();
        });
        Button loadBtn = bigBtn("\uD83D\uDCC2  Load", "#1F6FEB", null);
        loadBtn.setOnAction(e -> {
            String n = profileNameField.getText().trim();
            if (n.isEmpty())
                return;
            SettingsManager.load(data, n);
            if (leagueLogoLabel != null) {
                String lp = data.getLeagueLogoPath();
                if (lp != null && !lp.isEmpty())
                    setImagePickLabelSelected(leagueLogoLabel, new File(lp).getName());
                else
                    setImagePickLabelCleared(leagueLogoLabel);
            }
            if (homeLogoLabel != null) {
                String hp = data.getHomeLogoPath();
                if (hp != null && !hp.isEmpty())
                    setImagePickLabelSelected(homeLogoLabel, new File(hp).getName());
                else
                    setImagePickLabelCleared(homeLogoLabel);
            }
            if (awayLogoLabel != null) {
                String ap = data.getAwayLogoPath();
                if (ap != null && !ap.isEmpty())
                    setImagePickLabelSelected(awayLogoLabel, new File(ap).getName());
                else
                    setImagePickLabelCleared(awayLogoLabel);
            }
            if (bgImageLabel != null) {
                String bp = data.getBackgroundImagePath();
                if (bp != null && !bp.isEmpty())
                    setImagePickLabelSelected(bgImageLabel, new File(bp).getName());
                else
                    setImagePickLabelCleared(bgImageLabel);
            }
            loadBtn.setText("\u2713  Loaded!");
            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2),
                            ev -> loadBtn.setText("\uD83D\uDCC2  Load")))
                    .play();
        });
        Button deleteBtn = bigBtn("\uD83D\uDDD1  Delete", "#8B0000", null);
        deleteBtn.setOnAction(e -> {
            String n = profileNameField.getText().trim();
            if (n.isEmpty())
                return;
            SettingsManager.delete(n);
            refreshList.run();
            profileNameField.setText("");
        });
        HBox btnRow = new HBox(8, saveBtn, loadBtn, deleteBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox.setHgrow(loadBtn, Priority.ALWAYS);
        card.getChildren().addAll(formRow("Profile Name:", profileNameField), profileList, btnRow);
        return card;
    }

    private VBox buildMatchInfoCard() {
        VBox card = card("\uD83D\uDCCB  MATCH INFO");
        TextField leagueField = styledField(data.getLeagueTitle());
        leagueField.textProperty().addListener((o, a, b) -> {
            if (!b.equals(data.getLeagueTitle()))
                data.setLeagueTitle(b);
        });
        data.leagueTitleProperty().addListener((o, a, b) -> {
            if (!b.equals(leagueField.getText()))
                leagueField.setText(b);
        });
        Spinner<Integer> roundSpin = numSpinner(1, 99, data.getRound(), data::setRound);
        data.roundProperty().addListener((o, a, b) -> {
            if (b.intValue() != roundSpin.getValue())
                roundSpin.getValueFactory().setValue(b.intValue());
        });
        Button llBtn = actionBtn("\uD83D\uDCC1  Choose League Logo"), llClear = actionBtn("\u2715  Clear");
        llClear.setStyle(llClear.getStyle() + "-fx-background-color:" + RED_BTN + ";");
        leagueLogoLabel = imagePickLabel(data.getLeagueLogoPath());
        Label llSelected = leagueLogoLabel;
        llBtn.setOnAction(e -> {
            File f = imagePicker("Select League Logo");
            if (f != null) {
                data.setLeagueLogo(new Image(f.toURI().toString()));
                data.setLeagueLogoPath(f.getAbsolutePath());
                setImagePickLabelSelected(llSelected, f.getName());
            }
        });
        llClear.setOnAction(e -> {
            data.setLeagueLogo(null);
            data.setLeagueLogoPath(null);
            setImagePickLabelCleared(llSelected);
        });
        VBox llBox = new VBox(4, new HBox(8, llBtn, llClear), llSelected);
        card.getChildren().addAll(formRow("League / Event Title:", leagueField), formRow("Round Number:", roundSpin),
                formRow("League Logo:", llBox));
        return card;
    }

    private VBox buildTeamCard(boolean isHome) {
        VBox card = card(isHome ? "\uD83C\uDFE0  HOME TEAM" : "\u2708  AWAY TEAM");
        TextField nameField = styledField(isHome ? data.getHomeTeamName() : data.getAwayTeamName());
        nameField.textProperty().addListener((o, a, b) -> {
            if (isHome) {
                if (!b.equals(data.getHomeTeamName()))
                    data.setHomeTeamName(b);
            } else {
                if (!b.equals(data.getAwayTeamName()))
                    data.setAwayTeamName(b);
            }
        });
        if (isHome)
            data.homeTeamNameProperty().addListener((o, a, b) -> {
                if (!b.equals(nameField.getText()))
                    nameField.setText(b);
            });
        else
            data.awayTeamNameProperty().addListener((o, a, b) -> {
                if (!b.equals(nameField.getText()))
                    nameField.setText(b);
            });
        Button logoBtn = actionBtn("\uD83D\uDCC1  Choose Logo"), clearLogo = actionBtn("\u2715  Clear");
        clearLogo.setStyle(clearLogo.getStyle() + "-fx-background-color:" + RED_BTN + ";");
        String initLogoPath = isHome ? data.getHomeLogoPath() : data.getAwayLogoPath();
        Label logoSelected = imagePickLabel(initLogoPath);
        if (isHome)
            homeLogoLabel = logoSelected;
        else
            awayLogoLabel = logoSelected;
        logoBtn.setOnAction(e -> {
            File f = imagePicker("Select Logo");
            if (f != null) {
                Image img = new Image(f.toURI().toString());
                if (isHome) {
                    data.setHomeLogo(img);
                    data.setHomeLogoPath(f.getAbsolutePath());
                } else {
                    data.setAwayLogo(img);
                    data.setAwayLogoPath(f.getAbsolutePath());
                }
                setImagePickLabelSelected(logoSelected, f.getName());
            }
        });
        clearLogo.setOnAction(e -> {
            if (isHome) {
                data.setHomeLogo(null);
                data.setHomeLogoPath(null);
            } else {
                data.setAwayLogo(null);
                data.setAwayLogoPath(null);
            }
            setImagePickLabelCleared(logoSelected);
        });
        VBox logoBox = new VBox(4, new HBox(8, logoBtn, clearLogo), logoSelected);
        card.getChildren().addAll(formRow("Team Name:", nameField), formRow("Logo:", logoBox));
        return card;
    }

    private VBox buildTimerSettingsCard() {
        VBox card = card("\u23f1  TIMER SETTINGS");
        ToggleGroup mg = new ToggleGroup();
        RadioButton cdRb = styledRb("Count Down", mg), cuRb = styledRb("Count Up", mg);
        cdRb.setSelected(data.isCountDown());
        cuRb.setSelected(!data.isCountDown());
        cdRb.setOnAction(e -> data.setCountDown(true));
        cuRb.setOnAction(e -> data.setCountDown(false));
        data.countDownProperty().addListener((o, a, b) -> {
            cdRb.setSelected(b);
            cuRb.setSelected(!b);
        });
        Spinner<Integer> durSpin = numSpinner(1, 120, data.getMatchDurationMinutes(), v -> {
            data.setMatchDurationMinutes(v);
            data.setElapsedSeconds(0);
        });
        data.matchDurationMinutesProperty().addListener((o, a, b) -> {
            if (b.intValue() != durSpin.getValue())
                durSpin.getValueFactory().setValue(b.intValue());
        });
        Spinner<Integer> timeoutSpin = numSpinner(1, 60, data.getTimeoutDurationMinutes(),
                v -> data.setTimeoutDurationMinutes(v));
        data.timeoutDurationMinutesProperty().addListener((o, a, b) -> {
            if (b.intValue() != timeoutSpin.getValue())
                timeoutSpin.getValueFactory().setValue(b.intValue());
        });
        card.getChildren().addAll(formRow("Mode:", new HBox(12, cdRb, cuRb)), formRow("Duration (min):", durSpin),
                formRow("Timeout (min):", timeoutSpin));
        return card;
    }

    private VBox buildDisplayCard() {
        VBox card = card("\uD83D\uDDA5  DISPLAY");
        ComboBox<String> resBox = new ComboBox<>(FXCollections.observableArrayList(
                "720p  (1280\u00d7720)", "1080p (1920\u00d71080)", "1440p (2560\u00d71440)", "4K    (3840\u00d72160)"));
        resBox.setValue("720p  (1280\u00d7720)");
        resBox.setMaxWidth(Double.MAX_VALUE);
        resBox.setStyle(
                "-fx-background-color:#1C1C1C; -fx-text-fill:white; -fx-font-size:14; -fx-border-color:rgba(255,255,255,0.4); -fx-border-radius:6; -fx-background-radius:6;");
        resBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setTextFill(Color.WHITE);
                setFont(Font.font("Helvetica", FontWeight.BOLD, 14));
                setStyle("-fx-background-color:#1C1C1C;");
            }
        });
        resBox.setOnAction(e -> display.setResolution(resBox.getValue()));
        Button bgBtn = actionBtn("\uD83D\uDCC1  Choose Background"), bgClear = actionBtn("\u2715  Clear");
        bgClear.setStyle(bgClear.getStyle() + "-fx-background-color:" + RED_BTN + ";");
        bgImageLabel = imagePickLabel(data.getBackgroundImagePath());
        Label bgSelected = bgImageLabel;
        bgBtn.setOnAction(e -> {
            File f = imagePicker("Select Background");
            if (f != null) {
                data.setBackgroundImage(new Image(f.toURI().toString()));
                data.setBackgroundImagePath(f.getAbsolutePath());
                setImagePickLabelSelected(bgSelected, f.getName());
            }
        });
        bgClear.setOnAction(e -> {
            data.setBackgroundImage(null);
            data.setBackgroundImagePath(null);
            setImagePickLabelCleared(bgSelected);
        });
        VBox bgBox = new VBox(4, new HBox(8, bgBtn, bgClear), bgSelected);
        Slider opSlider = new Slider(0.0, 1.0, data.getBgOpacity());
        opSlider.setStyle("-fx-accent:" + GOLD + ";");
        opSlider.valueProperty().addListener((o, a, b) -> data.setBgOpacity(b.doubleValue()));
        Button fsBtn = bigBtn("\u26f6  Fullscreen Display (F11)", BLUE, null);
        fsBtn.setMaxWidth(Double.MAX_VALUE);
        fsBtn.setOnAction(e -> displayStage.setFullScreen(!displayStage.isFullScreen()));
        Button secondBtn = actionBtn("\uD83D\uDDA5  Move to Second Screen / Projector");
        secondBtn.setMaxWidth(Double.MAX_VALUE);
        secondBtn.setOnAction(e -> moveToSecondScreen());
        card.getChildren().addAll(formRow("Resolution:", resBox), formRow("Background:", bgBox),
                formRow("BG Opacity:", opSlider), fsBtn, secondBtn);
        return card;
    }

    private VBox buildBuzzerCard() {
        VBox card = card("\uD83D\uDD0A  BUZZER SOUND");
        Button chooseBtn = actionBtn("\uD83D\uDCC1  Choose Sound File");
        Button clearBtn = actionBtn("\u2715  Clear");
        clearBtn.setStyle(clearBtn.getStyle() + "-fx-background-color:" + RED_BTN + ";");
        buzzerSoundLabel = imagePickLabel(data.getBuzzerSoundPath());
        // Override the empty text to say "sound" not "image"
        if (data.getBuzzerSoundPath() == null || data.getBuzzerSoundPath().isEmpty())
            buzzerSoundLabel.setText("No sound selected");
        chooseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Buzzer Sound");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Audio Files", "*.wav", "*.mp3", "*.aac", "*.ogg", "*.flac", "*.m4a"));
            Stage owner = (settingsStage != null && settingsStage.isShowing()) ? settingsStage : stage;
            File f = fc.showOpenDialog(owner);
            if (f != null) {
                data.setBuzzerSoundPath(f.getAbsolutePath());
                setImagePickLabelSelected(buzzerSoundLabel, f.getName());
            }
        });
        clearBtn.setOnAction(e -> {
            data.setBuzzerSoundPath(null);
            buzzerSoundLabel.setText("No sound selected");
            buzzerSoundLabel.setTextFill(javafx.scene.paint.Color.web("rgba(255,255,255,0.35)"));
        });
        VBox fileBox = new VBox(4, new HBox(8, chooseBtn, clearBtn), buzzerSoundLabel);
        card.getChildren().addAll(formRow("Sound File:", fileBox));
        return card;
    }

    private VBox buildMatchControlCard() {
        VBox card = card("\u26a0  MATCH CONTROL");
        Button nextRound = bigBtn("\u2192  Next Round  (resets timer & fouls)", BLUE, null);
        nextRound.setMaxWidth(Double.MAX_VALUE);
        nextRound.setOnAction(e -> confirm("Advance to next round?").showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                data.setTimerRunning(false);
                data.setElapsedSeconds(0);
                data.setHomeFouls(0);
                data.setAwayFouls(0);
                data.setRound(data.getRound() + 1);
            }
        }));
        Button reset = bigBtn("\u21ba  Full Match Reset", RED_BTN, null);
        reset.setMaxWidth(Double.MAX_VALUE);
        reset.setOnAction(e -> confirm("Reset the ENTIRE match?").showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                data.resetMatch();
                data.setRound(1);
            }
        }));
        card.getChildren().addAll(nextRound, reset);
        return card;
    }

    private void moveToSecondScreen() {
        ObservableList<Screen> screens = Screen.getScreens();
        if (screens.size() < 2) {
            new Alert(Alert.AlertType.INFORMATION, "Only one screen detected.", ButtonType.OK).showAndWait();
            return;
        }
        Screen target = null;
        for (Screen s : screens) {
            if (!s.equals(Screen.getPrimary())) {
                target = s;
                break;
            }
        }
        if (target == null)
            target = screens.get(1);
        javafx.geometry.Rectangle2D b = target.getVisualBounds();
        displayStage.setX(b.getMinX());
        displayStage.setY(b.getMinY());
        displayStage.setWidth(b.getWidth());
        displayStage.setHeight(b.getHeight());
        displayStage.setFullScreen(true);
    }

    private File imagePicker(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        Stage owner = (settingsStage != null && settingsStage.isShowing()) ? settingsStage : stage;
        return fc.showOpenDialog(owner);
    }

    private VBox card(String title) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(GOLD));
        lbl.setPadding(new Insets(0, 0, 6, 0));
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle(
                "-fx-background-color:#141414;-fx-background-radius:10;-fx-border-color:rgba(255,255,255,0.10);-fx-border-width:1;-fx-border-radius:10;");
        box.getChildren().add(lbl);
        return box;
    }

    private HBox formRow(String lbl, javafx.scene.Node ctrl) {
        Label l = secondaryLabel(lbl);
        l.setMinWidth(150);
        HBox row = new HBox(10, l, ctrl);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(ctrl, Priority.ALWAYS);
        return row;
    }

    private TextField styledField(String v) {
        TextField tf = new TextField(v);
        tf.setStyle("-fx-background-color:#1C1C1C;-fx-text-fill:white;-fx-prompt-text-fill:rgba(255,255,255,0.75);"
                + "-fx-border-color:rgba(255,255,255,0.12);-fx-border-radius:6;-fx-background-radius:6;"
                + "-fx-padding:7 10;-fx-font-size:13;");
        return tf;
    }

    private Spinner<Integer> numSpinner(int min, int max, int init, java.util.function.Consumer<Integer> onChange) {
        Spinner<Integer> sp = new Spinner<>(min, max, init);
        sp.setEditable(true);
        sp.getEditor().setStyle("-fx-background-color:#1C1C1C;-fx-text-fill:white;");
        sp.setStyle("-fx-background-color:#1C1C1C;-fx-border-color:rgba(255,255,255,0.12);-fx-border-radius:6;");
        sp.valueProperty().addListener((o, a, b) -> {
            if (b != null)
                onChange.accept(b);
        });
        return sp;
    }

    private Button bigBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + (fg != null ? fg : "white")
                + ";-fx-font-weight:bold;-fx-font-size:14;-fx-padding:10 18;-fx-background-radius:8;-fx-cursor:hand;");
        return b;
    }

    private Button actionBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#1C1C1C;-fx-text-fill:white;-fx-font-size:13;-fx-padding:7 14;"
                + "-fx-background-radius:6;-fx-border-color:rgba(255,255,255,0.12);-fx-border-radius:6;-fx-cursor:hand;");
        return b;
    }

    private Button scoreBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color
                + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:26;-fx-min-width:58;-fx-min-height:58;-fx-background-radius:10;-fx-cursor:hand;");
        return b;
    }

    private Button timerBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color
                + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:18;-fx-min-width:64;-fx-min-height:38;-fx-background-radius:10;-fx-cursor:hand;");
        return b;
    }

    private Button cardBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color
                + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:14;-fx-min-width:30;-fx-min-height:30;-fx-background-radius:6;-fx-padding:2 6;-fx-cursor:hand;");
        return b;
    }

    private Button smallBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color
                + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13;-fx-min-height:36;-fx-background-radius:8;-fx-padding:5 10;-fx-cursor:hand;");
        return b;
    }

    private Label secondaryLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Helvetica", 13));
        l.setTextFill(Color.web("rgba(255,255,255,0.5)"));
        return l;
    }

    private RadioButton styledRb(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setFont(Font.font("Helvetica", 13));
        rb.setTextFill(Color.web(TEXT_PRI));
        return rb;
    }

    private Region hRule() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color:rgba(255,255,255,0.10);");
        return r;
    }

    private Label imagePickLabel(String currentPath) {
        boolean has = currentPath != null && !currentPath.isEmpty();
        Label lbl = new Label(has ? "\u2713  " + new java.io.File(currentPath).getName() : "No image selected");
        lbl.setFont(Font.font("Helvetica", 11));
        lbl.setTextFill(has ? Color.web(GREEN) : Color.web("rgba(255,255,255,0.35)"));
        lbl.setWrapText(true);
        return lbl;
    }

    private void setImagePickLabelSelected(Label lbl, String filename) {
        lbl.setText("\u2713  " + filename);
        lbl.setTextFill(Color.web(GREEN));
    }

    private void setImagePickLabelCleared(Label lbl) {
        lbl.setText("No image selected");
        lbl.setTextFill(Color.web("rgba(255,255,255,0.35)"));
    }

    private void addCardTimer(boolean isHome, int minutes, Button rPlusBtn) {
        VBox timersBox = isHome ? homeCardTimersBox : awayCardTimersBox;
        if (timersBox == null)
            return;
        int slot = timersBox.getChildren().size();
        int cardNum = isHome ? ++homeCardCount : ++awayCardCount;
        long[] remaining = { (long) minutes * 60 };
        if (isHome)
            data.setHomeCardTimerSecs(slot, remaining[0]);
        else
            data.setAwayCardTimerSecs(slot, remaining[0]);
        Label timerLbl = new Label(String.format("%02d:%02d", remaining[0] / 60, remaining[0] % 60));
        timerLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 26));
        timerLbl.setTextFill(Color.web("#FF3B3B"));
        timerLbl.setMinWidth(80);
        timerLbl.setAlignment(Pos.CENTER);
        Label numLbl = new Label("#" + cardNum);
        numLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        numLbl.setTextFill(Color.web("rgba(255,255,255,0.6)"));
        Label redBox = new Label("  ");
        redBox.setStyle(
                "-fx-background-color:#FF3B3B; -fx-min-width:16; -fx-min-height:16; -fx-max-width:16; -fx-max-height:16; -fx-background-radius:3;");
        HBox timerRow = new HBox(8, redBox, numLbl, timerLbl);
        timerRow.setAlignment(Pos.CENTER);
        timerRow.setPadding(new Insets(4, 10, 4, 10));
        timerRow.setStyle(
                "-fx-background-color:#1A0000;-fx-background-radius:6;-fx-border-color:rgba(255,59,59,0.35);-fx-border-radius:6;-fx-border-width:1;");
        boolean[] expired = { false };
        javafx.animation.Timeline[] tlHolder = { null };
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), ev -> {
                    if (expired[0])
                        return;
                    remaining[0]--;
                    if (remaining[0] <= 0) {
                        expired[0] = true;
                        if (isHome)
                            data.setHomeCardTimerSecs(slot, -1);
                        else
                            data.setAwayCardTimerSecs(slot, -1);
                        javafx.application.Platform.runLater(() -> {
                            timersBox.getChildren().remove(timerRow);
                            if (tlHolder[0] != null) {
                                tlHolder[0].stop();
                                allCardTimers.remove(tlHolder[0]);
                            }
                            renumberCardTimers(timersBox);
                            if (rPlusBtn != null)
                                rPlusBtn.setDisable(false); // slot freed
                        });
                    } else {
                        timerLbl.setText(String.format("%02d:%02d", remaining[0] / 60, remaining[0] % 60));
                        if (isHome)
                            data.setHomeCardTimerSecs(slot, remaining[0]);
                        else
                            data.setAwayCardTimerSecs(slot, remaining[0]);
                    }
                }));
        tl.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        tlHolder[0] = tl;
        if (data.isTimerRunning())
            tl.play();
        allCardTimers.add(tl);
        timersBox.getChildren().add(timerRow);
    }

    private void removeLastCardTimer(boolean isHome) {
        VBox timersBox = isHome ? homeCardTimersBox : awayCardTimersBox;
        if (timersBox == null || timersBox.getChildren().isEmpty())
            return;
        int slot = timersBox.getChildren().size() - 1;
        if (isHome)
            data.setHomeCardTimerSecs(slot, -1);
        else
            data.setAwayCardTimerSecs(slot, -1);
        if (!allCardTimers.isEmpty()) {
            int lastIdx = allCardTimers.size() - 1;
            allCardTimers.get(lastIdx).stop();
            allCardTimers.remove(lastIdx);
        }
        if (isHome && homeCardCount > 0)
            homeCardCount--;
        else if (!isHome && awayCardCount > 0)
            awayCardCount--;
        timersBox.getChildren().remove(timersBox.getChildren().size() - 1);
        renumberCardTimers(timersBox);
    }

    private void renumberCardTimers(VBox timersBox) {
        if (timersBox == null)
            return;
        for (int i = 0; i < timersBox.getChildren().size(); i++) {
            if (timersBox.getChildren().get(i) instanceof javafx.scene.layout.HBox row) {
                if (row.getChildren().size() >= 2 && row.getChildren().get(1) instanceof Label numLbl)
                    numLbl.setText("#" + (i + 1));
            }
        }
    }

    private Alert confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Confirm");
        a.setHeaderText(null);
        return a;
    }
}