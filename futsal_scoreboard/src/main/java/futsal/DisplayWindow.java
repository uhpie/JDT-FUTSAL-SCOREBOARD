package futsal;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DisplayWindow {

    // ── Colour palette (dark theme) ───────────────────────────────────────
    private static final String COL_WHITE = "#FFFFFF";
    private static final String COL_YELLOW = "#FFD700";
    private static final String COL_ORANGE = "#d87f13";
    private static final String COL_RED = "#c10c0c";
    private static final String COL_SUBTLE = "rgba(255,255,255,0.18)";

    // ── Base canvas dimensions ─────────────────────────────────────────────
    private static final double BASE_W = 1280;
    private static final double BASE_H = 720;
    private static final double LOGO_SIZE = 250;
    private static final double LOGO_BAR_H = 90;

    // ── Font sizes ─────────────────────────────────────────────────────────
    private static final double TITLE_FONT = 37;
    private static final double TEAM_FONT = 28;
    private static final double TIMER_FONT = 148;
    private static final double SCORE_FONT = 195;
    private static final double ROUND_NUM = 100;
    private static final double ROUND_LBL = 25;
    private static final double FOUL_NUM = 90;
    private static final double FOUL_LBL = 18;
    private static final double AGG_FONT = 50;
    private static final double AGG_HDR_FONT = 25;

    // ── State ──────────────────────────────────────────────────────────────
    private final GameData data;
    private Stage stage;
    private StackPane root;

    private Label leagueTitleLabel;
    private Label homeTeamLabel, awayTeamLabel;
    private ImageView homeLogoView, awayLogoView;
    private Label homePlaceholder, awayPlaceholder;
    private Label homeScoreLabel, awayScoreLabel;
    private Label timerLabel, roundLabel, aggLabel, aggHeaderLabel;
    private Label homeFoulsLabel, awayFoulsLabel;
    private ImageView bgImageView, leagueLogoView;
    private Region bgOverlay;
    private VBox contentCanvas;
    private Scale canvasScale;
    private AnimationTimer animTimer;
    private Timeline expiredBlink;

    public DisplayWindow(GameData data) {
        this.data = data;
        buildUI();
        bindData();
        startTimerEngine();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Stage / scene setup
    // ══════════════════════════════════════════════════════════════════════
    private void buildUI() {
        stage = new Stage();
        stage.setTitle("JDT Futsal Scoreboard — Display");

        // Dark background fill
        Region bg = new Region();
        bg.setStyle(
                "-fx-background-color: radial-gradient(center 30% 40%, radius 80%, #1A1A1A 0%, #111111 45%, #000000 100%);");

        // Optional user-supplied background image (shown at configured opacity)
        bgImageView = new ImageView();
        bgImageView.setPreserveRatio(false);
        bgImageView.setSmooth(true);
        bgImageView.setOpacity(data.getBgOpacity());
        bgImageView.setVisible(false);

        // Thin dark overlay so background image doesn't wash out text
        bgOverlay = new Region();
        bgOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.15);");

        // Canvas — fixed 1280×720 logical size, scaled to fill the window
        contentCanvas = buildCanvas();
        contentCanvas.setPrefWidth(BASE_W);
        contentCanvas.setPrefHeight(BASE_H);
        contentCanvas.setMinWidth(BASE_W);
        contentCanvas.setMinHeight(BASE_H);
        contentCanvas.setMaxWidth(BASE_W);
        contentCanvas.setMaxHeight(BASE_H);

        canvasScale = new Scale(1, 1, 0, 0);
        contentCanvas.getTransforms().add(canvasScale);

        root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().addAll(bg, bgImageView, bgOverlay, contentCanvas);
        StackPane.setAlignment(contentCanvas, Pos.TOP_LEFT);

        Scene scene = new Scene(root, 1280, 720, Color.BLACK);
        bg.prefWidthProperty().bind(scene.widthProperty());
        bg.prefHeightProperty().bind(scene.heightProperty());
        bgImageView.fitWidthProperty().bind(scene.widthProperty());
        bgImageView.fitHeightProperty().bind(scene.heightProperty());
        bgOverlay.prefWidthProperty().bind(scene.widthProperty());
        bgOverlay.prefHeightProperty().bind(scene.heightProperty());

        // Keep canvas scaled to fill window
        Runnable rescale = () -> {
            canvasScale.setX(scene.getWidth() / BASE_W);
            canvasScale.setY(scene.getHeight() / BASE_H);
        };
        scene.widthProperty().addListener((o, a, b) -> rescale.run());
        scene.heightProperty().addListener((o, a, b) -> rescale.run());
        stage.setOnShown(e -> rescale.run());

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case F11 -> stage.setFullScreen(!stage.isFullScreen());
                case ESCAPE -> stage.setFullScreen(false);
                default -> {
                }
            }
        });

        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.setFullScreenExitHint("Press F11 to exit fullscreen");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Canvas — VBox with main Pane + league logo bar
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildCanvas() {
        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_CENTER);

        // Main scoreboard pane (absolute positions)
        Pane main = new Pane();
        main.setPrefSize(BASE_W, BASE_H - LOGO_BAR_H);
        VBox.setVgrow(main, Priority.ALWAYS);
        buildAllElements(main);

        // League logo strip at the very bottom
        HBox logoBar = new HBox();
        logoBar.setAlignment(Pos.CENTER);
        logoBar.setPrefHeight(LOGO_BAR_H);
        logoBar.setStyle("-fx-background-color: transparent;");
        leagueLogoView = new ImageView();
        leagueLogoView.setFitHeight(190);
        leagueLogoView.setFitWidth(300);
        leagueLogoView.setPreserveRatio(true);
        leagueLogoView.setSmooth(true);
        leagueLogoView.setVisible(false);
        logoBar.getChildren().add(leagueLogoView);

        vbox.getChildren().addAll(main, logoBar);
        return vbox;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Absolute-positioned elements (all in a 1280 × 640 pane)
    // ══════════════════════════════════════════════════════════════════════
    private void buildAllElements(Pane p) {
        final double W = BASE_W; // 1280
        final double H = BASE_H - LOGO_BAR_H; // 640
        final double cx = W / 2.0; // 640 — horizontal centre

        // ── LEAGUE TITLE (very top, centred) ─────────────────────────────
        leagueTitleLabel = new Label(data.getLeagueTitle());
        leagueTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, TITLE_FONT));
        leagueTitleLabel.setTextFill(Color.web(COL_WHITE));
        leagueTitleLabel.setAlignment(Pos.CENTER);
        leagueTitleLabel.setTextAlignment(TextAlignment.CENTER);
        leagueTitleLabel.setPrefWidth(1200);
        leagueTitleLabel.setLayoutX(cx - 600);
        leagueTitleLabel.setLayoutY(10);

        // ── HOME TEAM NAME (top-left, above logo) ────────────────────────
        homeTeamLabel = new Label(data.getHomeTeamName());
        homeTeamLabel.setFont(Font.font("Arial", FontWeight.BOLD, TEAM_FONT));
        homeTeamLabel.setTextFill(Color.web(COL_WHITE));
        homeTeamLabel.setAlignment(Pos.CENTER);
        homeTeamLabel.setWrapText(true);
        homeTeamLabel.setTextAlignment(TextAlignment.CENTER);
        homeTeamLabel.setPrefWidth(240);
        homeTeamLabel.setLayoutX(42);
        homeTeamLabel.setLayoutY(72);

        // ── AWAY TEAM NAME (top-right, above logo) ───────────────────────
        awayTeamLabel = new Label(data.getAwayTeamName());
        awayTeamLabel.setFont(Font.font("Arial", FontWeight.BOLD, TEAM_FONT));
        awayTeamLabel.setTextFill(Color.web(COL_WHITE));
        awayTeamLabel.setAlignment(Pos.CENTER);
        awayTeamLabel.setWrapText(true);
        awayTeamLabel.setTextAlignment(TextAlignment.CENTER);
        awayTeamLabel.setPrefWidth(240);
        awayTeamLabel.setLayoutX(W - 280);
        awayTeamLabel.setLayoutY(72);

        // ── TIMER (large, top-centre) ─────────────────────────────────────
        timerLabel = new Label(data.getFormattedTime());
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, TIMER_FONT));
        timerLabel.setTextFill(Color.web(COL_YELLOW));
        timerLabel.setAlignment(Pos.CENTER);
        timerLabel.setPrefWidth(580);
        timerLabel.setLayoutX(cx - 290);
        timerLabel.setLayoutY(58);

        // ── AGGREGATE (below timer, optional) ────────────────────────────
        aggHeaderLabel = new Label("AGGREGATE");
        aggHeaderLabel.setFont(Font.font("Arial", FontWeight.BOLD, AGG_HDR_FONT));
        aggHeaderLabel.setTextFill(Color.web(COL_WHITE));
        aggHeaderLabel.setAlignment(Pos.CENTER);
        aggHeaderLabel.setPrefWidth(280);
        aggHeaderLabel.setLayoutX(cx - 140);
        aggHeaderLabel.setLayoutY(220);
        aggHeaderLabel.setVisible(data.isShowAggregate());

        aggLabel = new Label(data.getHomeAgg() + "-" + data.getAwayAgg());
        aggLabel.setFont(Font.font("Arial", FontWeight.BOLD, AGG_FONT));
        aggLabel.setTextFill(Color.web(COL_YELLOW));
        aggLabel.setAlignment(Pos.CENTER);
        aggLabel.setPrefWidth(280);
        aggLabel.setLayoutX(cx - 140);
        aggLabel.setLayoutY(250);
        aggLabel.setVisible(data.isShowAggregate());

        // ── ROUND label + number (centre, same level as scores) ──────────────
        Label roundHeader = new Label("ROUND");
        roundHeader.setFont(Font.font("Arial", FontWeight.BOLD, ROUND_LBL));
        roundHeader.setTextFill(Color.web(COL_WHITE));
        roundHeader.setAlignment(Pos.CENTER);
        roundHeader.setPrefWidth(200);
        roundHeader.setLayoutX(cx - 100);
        roundHeader.setLayoutY(330);

        roundLabel = new Label(String.valueOf(data.getRound()));
        roundLabel.setFont(Font.font("Arial", FontWeight.BOLD, ROUND_NUM));
        roundLabel.setTextFill(Color.web(COL_YELLOW));
        roundLabel.setAlignment(Pos.CENTER);
        roundLabel.setPrefWidth(200);
        roundLabel.setLayoutX(cx - 100);
        roundLabel.setLayoutY(360);

        // ── HOME LOGO (outer-left, vertically centred in main area) ───────
        StackPane homeLogoContainer = new StackPane();
        homeLogoContainer.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        homeLogoContainer.setMinSize(LOGO_SIZE, LOGO_SIZE);
        homeLogoContainer.setMaxSize(LOGO_SIZE, LOGO_SIZE);

        homePlaceholder = new Label("H");
        homePlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 80));
        homePlaceholder.setTextFill(Color.web(COL_SUBTLE));

        homeLogoView = new ImageView();
        homeLogoView.setFitWidth(LOGO_SIZE);
        homeLogoView.setFitHeight(LOGO_SIZE);
        homeLogoView.setPreserveRatio(true);
        homeLogoView.setSmooth(true);
        homeLogoView.setVisible(false);

        homeLogoContainer.getChildren().addAll(homePlaceholder, homeLogoView);
        homeLogoContainer.setLayoutX(33);
        homeLogoContainer.setLayoutY(165);

        // ── AWAY LOGO (outer-right) ───────────────────────────────────────
        StackPane awayLogoContainer = new StackPane();
        awayLogoContainer.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        awayLogoContainer.setMinSize(LOGO_SIZE, LOGO_SIZE);
        awayLogoContainer.setMaxSize(LOGO_SIZE, LOGO_SIZE);

        awayPlaceholder = new Label("A");
        awayPlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 80));
        awayPlaceholder.setTextFill(Color.web(COL_SUBTLE));

        awayLogoView = new ImageView();
        awayLogoView.setFitWidth(LOGO_SIZE);
        awayLogoView.setFitHeight(LOGO_SIZE);
        awayLogoView.setPreserveRatio(true);
        awayLogoView.setSmooth(true);
        awayLogoView.setVisible(false);

        awayLogoContainer.getChildren().addAll(awayPlaceholder, awayLogoView);
        awayLogoContainer.setLayoutX(990);
        awayLogoContainer.setLayoutY(165);

        // ── HOME SCORE (right of home logo, towards centre) ──────────────
        homeScoreLabel = new Label(String.valueOf(data.getHomeScore()));
        homeScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, SCORE_FONT));
        homeScoreLabel.setTextFill(Color.web(COL_WHITE));
        homeScoreLabel.setAlignment(Pos.CENTER);
        homeScoreLabel.setPrefWidth(240);
        homeScoreLabel.setMinWidth(240);
        homeScoreLabel.setMaxWidth(240);
        homeScoreLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        // Start just right of the home logo (20 + 200 + 20 gap = 240)
        homeScoreLabel.setLayoutX(270);
        homeScoreLabel.setLayoutY(350);

        // ── AWAY SCORE (left of away logo, towards centre) ────────────────
        awayScoreLabel = new Label(String.valueOf(data.getAwayScore()));
        awayScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, SCORE_FONT));
        awayScoreLabel.setTextFill(Color.web(COL_WHITE));
        awayScoreLabel.setAlignment(Pos.CENTER);
        awayScoreLabel.setPrefWidth(240);
        awayScoreLabel.setMinWidth(240);
        awayScoreLabel.setMaxWidth(240);
        awayScoreLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        // End just left of the away logo (W - 20 - 200 - 20 gap - 240 = 800)
        awayScoreLabel.setLayoutX(770);
        awayScoreLabel.setLayoutY(350);

        // ── HOME FOULS (bottom-left, centred under home logo) ────────────────
        Label homeFoulTitle = new Label("FOULS");
        homeFoulTitle.setFont(Font.font("Arial", FontWeight.BOLD, FOUL_LBL));
        homeFoulTitle.setTextFill(Color.web(COL_WHITE));
        homeFoulTitle.setAlignment(Pos.CENTER);
        homeFoulTitle.setPrefWidth(200);

        homeFoulsLabel = new Label(String.valueOf(data.getHomeFouls()));
        homeFoulsLabel.setFont(Font.font("Arial", FontWeight.BOLD, FOUL_NUM));
        homeFoulsLabel.setTextFill(Color.web(COL_ORANGE));
        homeFoulsLabel.setAlignment(Pos.CENTER);
        homeFoulsLabel.setPrefWidth(200);

        VBox homeFoulBlock = new VBox(0, homeFoulTitle, homeFoulsLabel);
        homeFoulBlock.setAlignment(Pos.CENTER);
        homeFoulBlock.setPrefWidth(200);
        homeFoulBlock.setLayoutX(60); // logo center(140) − halfWidth(100)
        homeFoulBlock.setLayoutY(H - 148);

        // ── AWAY FOULS (bottom-right, centred under away logo) ───────────────
        Label awayFoulTitle = new Label("FOULS");
        awayFoulTitle.setFont(Font.font("Arial", FontWeight.BOLD, FOUL_LBL));
        awayFoulTitle.setTextFill(Color.web(COL_WHITE));
        awayFoulTitle.setAlignment(Pos.CENTER);
        awayFoulTitle.setPrefWidth(200);

        awayFoulsLabel = new Label(String.valueOf(data.getAwayFouls()));
        awayFoulsLabel.setFont(Font.font("Arial", FontWeight.BOLD, FOUL_NUM));
        awayFoulsLabel.setTextFill(Color.web(COL_ORANGE));
        awayFoulsLabel.setAlignment(Pos.CENTER);
        awayFoulsLabel.setPrefWidth(200);

        VBox awayFoulBlock = new VBox(0, awayFoulTitle, awayFoulsLabel);
        awayFoulBlock.setAlignment(Pos.CENTER);
        awayFoulBlock.setPrefWidth(200);
        awayFoulBlock.setLayoutX(1020); // logo center(1140) − halfWidth(100)
        awayFoulBlock.setLayoutY(H - 148);

        // ── Add everything to the pane ────────────────────────────────────
        p.getChildren().addAll(
                leagueTitleLabel,
                homeTeamLabel, awayTeamLabel,
                timerLabel,
                aggHeaderLabel, aggLabel,
                roundHeader, roundLabel,
                homeLogoContainer, awayLogoContainer,
                homeScoreLabel, awayScoreLabel,
                homeFoulBlock, awayFoulBlock);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Resolution helper (called from ControlPanel)
    // ══════════════════════════════════════════════════════════════════════
    public void setResolution(String label) {
        int w, h;
        switch (label) {
            case "720p  (1280×720)" -> {
                w = 1280;
                h = 720;
            }
            case "1080p (1920×1080)" -> {
                w = 1920;
                h = 1080;
            }
            case "1440p (2560×1440)" -> {
                w = 2560;
                h = 1440;
            }
            case "4K    (3840×2160)" -> {
                w = 3840;
                h = 2160;
            }
            default -> {
                w = 1280;
                h = 720;
            }
        }
        stage.setFullScreen(false);
        stage.setWidth(w);
        stage.setHeight(h);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Data bindings
    // ══════════════════════════════════════════════════════════════════════
    private void bindData() {
        data.leagueTitleProperty().addListener((o, a, b) -> leagueTitleLabel.setText(b));
        data.homeTeamNameProperty().addListener((o, a, b) -> homeTeamLabel.setText(b));
        data.awayTeamNameProperty().addListener((o, a, b) -> awayTeamLabel.setText(b));
        data.homeScoreProperty().addListener((o, a, b) -> homeScoreLabel.setText(b.toString()));
        data.awayScoreProperty().addListener((o, a, b) -> awayScoreLabel.setText(b.toString()));
        data.homeFoulsProperty().addListener((o, a, b) -> homeFoulsLabel.setText(b.toString()));
        data.awayFoulsProperty().addListener((o, a, b) -> awayFoulsLabel.setText(b.toString()));
        data.roundProperty().addListener((o, a, b) -> roundLabel.setText(b.toString()));

        data.homeAggProperty().addListener((o, a, b) -> aggLabel.setText(data.getHomeAgg() + "-" + data.getAwayAgg()));
        data.awayAggProperty().addListener((o, a, b) -> aggLabel.setText(data.getHomeAgg() + "-" + data.getAwayAgg()));

        data.showAggregateProperty().addListener((o, a, b) -> {
            aggLabel.setVisible(b);
            aggHeaderLabel.setVisible(b);
        });

        data.homeLogoProperty().addListener((o, a, n) -> {
            if (n != null) {
                homeLogoView.setImage(n);
                homeLogoView.setVisible(true);
                homePlaceholder.setVisible(false);
            } else {
                homeLogoView.setImage(null);
                homeLogoView.setVisible(false);
                homePlaceholder.setVisible(true);
            }
        });
        data.awayLogoProperty().addListener((o, a, n) -> {
            if (n != null) {
                awayLogoView.setImage(n);
                awayLogoView.setVisible(true);
                awayPlaceholder.setVisible(false);
            } else {
                awayLogoView.setImage(null);
                awayLogoView.setVisible(false);
                awayPlaceholder.setVisible(true);
            }
        });
        data.leagueLogoProperty().addListener((o, a, n) -> {
            leagueLogoView.setImage(n);
            leagueLogoView.setVisible(n != null);
        });
        data.backgroundImageProperty().addListener((o, a, n) -> {
            bgImageView.setImage(n);
            bgImageView.setVisible(n != null);
        });
        data.bgOpacityProperty().addListener((o, a, n) -> bgImageView.setOpacity(n.doubleValue()));

        data.elapsedSecondsProperty().addListener((o, a, b) -> refreshTimer());
        data.matchDurationMinutesProperty().addListener((o, a, b) -> refreshTimer());
        data.countDownProperty().addListener((o, a, b) -> refreshTimer());
        data.timerRunningProperty().addListener((o, a, b) -> {
            if (!b)
                refreshTimer();
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Timer refresh & blink
    // ══════════════════════════════════════════════════════════════════════
    private void refreshTimer() {
        timerLabel.setText(data.getFormattedTime());
        if (data.isTimeExpired()) {
            timerLabel.setTextFill(Color.web(COL_RED));
            startExpiredBlink();
        } else {
            timerLabel.setTextFill(Color.web(COL_YELLOW));
            stopExpiredBlink();
        }
    }

    private void startTimerEngine() {
        animTimer = new AnimationTimer() {
            private long startNano = -1, startElapsed = 0;

            @Override
            public void handle(long now) {
                if (!data.isTimerRunning()) {
                    startNano = -1;
                    return;
                }
                if (startNano == -1) {
                    startNano = now;
                    startElapsed = data.getElapsedSeconds();
                    return;
                }
                long secs = startElapsed + (now - startNano) / 1_000_000_000L;
                if (data.isCountDown()) {
                    long max = (long) data.getMatchDurationMinutes() * 60;
                    if (secs >= max) {
                        data.setElapsedSeconds(max);
                        data.setTimerRunning(false);
                        return;
                    }
                }
                if (secs != data.getElapsedSeconds()) {
                    data.setElapsedSeconds(secs);
                    timerLabel.setText(data.getFormattedTime());
                }
            }
        };
        animTimer.start();
    }

    private void startExpiredBlink() {
        if (expiredBlink != null &&
                expiredBlink.getStatus() == javafx.animation.Animation.Status.RUNNING)
            return;
        expiredBlink = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timerLabel.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(500), new KeyValue(timerLabel.opacityProperty(), 0.2)),
                new KeyFrame(Duration.millis(1000), new KeyValue(timerLabel.opacityProperty(), 1.0)));
        expiredBlink.setCycleCount(Timeline.INDEFINITE);
        expiredBlink.play();
    }

    private void stopExpiredBlink() {
        if (expiredBlink != null)
            expiredBlink.stop();
        timerLabel.setOpacity(1.0);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════════════════════════════
    public void show() {
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }
}