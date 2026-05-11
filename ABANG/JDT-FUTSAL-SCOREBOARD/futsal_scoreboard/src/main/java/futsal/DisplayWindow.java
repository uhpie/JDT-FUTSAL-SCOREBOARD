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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DisplayWindow {

    private static final String BK_DARK = "#000000";
    private static final String BK_MID = "#111111";
    private static final String BK_LIGHT = "#1A1A1A";
    private static final String COL_WHITE = "#FFFFFF";
    private static final String COL_DIM = "rgba(255,255,255,0.45)";
    private static final String COL_RED = "#FF3B3B";
    private static final String COL_ORANGE = "#FF8C00";
    private static final String COL_GOLD = "#FFD700";
    private static final String COL_TIMEOUT = "#3A9FFF";
    private static final String COL_GREEN = "#2EA043";

    private static final double BASE_W = 1280;
    private static final double BASE_H = 720;
    private static final double LOGO_SIZE = 240;
    private static final double SCORE_FONT = 160;
    private static final double TEAM_FONT = 30;
    private static final double TIMER_FONT = 100;
    private static final double ROUND_NUM = 100;
    private static final double ROUND_LBL = 22;
    private static final double FOUL_NUM = 80;
    private static final double FOUL_LBL = 14;
    private static final double TITLE_FONT = 26;

    private final GameData data;
    private Stage stage;
    private StackPane root;

    // Main scoreboard labels
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

    // Penalty screen
    private VBox penaltyCanvas;
    private Label penHomeScoreLabel, penAwayScoreLabel;
    private Label penHomeTotalLabel, penAwayTotalLabel;
    private Label penHomeTeamLabel, penAwayTeamLabel;
    private ImageView penHomeLogoView, penAwayLogoView;
    private Label penHomePlaceholder, penAwayPlaceholder;
    private Label penAggLabel, penAggHeaderLabel;
    private ImageView penLeagueLogoView;
    private Pane penaltyKickGrid;

    public DisplayWindow(GameData data) {
        this.data = data;
        buildUI();
        bindData();
        startTimerEngine();
    }

    private void buildUI() {
        stage = new Stage();
        stage.setTitle("JDT Futsal Scoreboard \u2014 Display");

        Region bg = new Region();
        bg.setStyle("-fx-background-color: radial-gradient(center 30% 40%, radius 80%, "
                + BK_LIGHT + " 0%, " + BK_MID + " 45%, " + BK_DARK + " 100%);");
        Region diag1 = new Region();
        diag1.setStyle("-fx-background-color: rgba(255,255,255,0.03);");
        diag1.setRotate(28);
        diag1.setPrefSize(700, 2200);
        Region diag2 = new Region();
        diag2.setStyle("-fx-background-color: rgba(0,0,0,0.10);");
        diag2.setRotate(-22);
        diag2.setPrefSize(900, 2200);

        bgImageView = new ImageView();
        bgImageView.setPreserveRatio(false);
        bgImageView.setSmooth(true);
        bgImageView.setOpacity(data.getBgOpacity());
        bgImageView.setVisible(false);

        bgOverlay = new Region();
        bgOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.15);");

        contentCanvas = buildMainCanvas();
        contentCanvas.setPrefWidth(BASE_W);
        contentCanvas.setPrefHeight(BASE_H);
        contentCanvas.setMinWidth(BASE_W);
        contentCanvas.setMinHeight(BASE_H);
        contentCanvas.setMaxWidth(BASE_W);
        contentCanvas.setMaxHeight(BASE_H);

        penaltyCanvas = buildPenaltyCanvas();
        penaltyCanvas.setPrefWidth(BASE_W);
        penaltyCanvas.setPrefHeight(BASE_H);
        penaltyCanvas.setMinWidth(BASE_W);
        penaltyCanvas.setMinHeight(BASE_H);
        penaltyCanvas.setMaxWidth(BASE_W);
        penaltyCanvas.setMaxHeight(BASE_H);
        penaltyCanvas.setVisible(false);

        canvasScale = new Scale(1, 1, 0, 0);
        contentCanvas.getTransforms().add(canvasScale);
        penaltyCanvas.getTransforms().add(canvasScale);

        root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().addAll(bg, diag1, diag2, bgImageView, bgOverlay, contentCanvas, penaltyCanvas);
        StackPane.setAlignment(contentCanvas, Pos.TOP_LEFT);
        StackPane.setAlignment(penaltyCanvas, Pos.TOP_LEFT);

        Scene scene = new Scene(root, 1280, 720, Color.BLACK);
        bg.prefWidthProperty().bind(scene.widthProperty());
        bg.prefHeightProperty().bind(scene.heightProperty());
        bgImageView.fitWidthProperty().bind(scene.widthProperty());
        bgImageView.fitHeightProperty().bind(scene.heightProperty());
        bgOverlay.prefWidthProperty().bind(scene.widthProperty());
        bgOverlay.prefHeightProperty().bind(scene.heightProperty());

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
    // Main scoreboard canvas
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildMainCanvas() {
        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_CENTER);

        Pane main = new Pane();
        main.setPrefSize(BASE_W, BASE_H - 80);
        VBox.setVgrow(main, Priority.ALWAYS);

        buildAllElements(main);

        HBox logoBar = new HBox();
        logoBar.setAlignment(Pos.CENTER);
        logoBar.setPrefHeight(80);
        logoBar.setStyle("-fx-background-color: transparent;");
        leagueLogoView = new ImageView();
        leagueLogoView.setFitHeight(70);
        leagueLogoView.setFitWidth(300);
        leagueLogoView.setPreserveRatio(true);
        leagueLogoView.setSmooth(true);
        leagueLogoView.setVisible(false);
        logoBar.getChildren().add(leagueLogoView);

        vbox.getChildren().addAll(main, logoBar);
        return vbox;
    }

    private void buildAllElements(Pane p) {
        double W = BASE_W, H = BASE_H - 80;
        double cx = W / 2;

        leagueTitleLabel = new Label(data.getLeagueTitle());
        leagueTitleLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, TITLE_FONT));
        leagueTitleLabel.setTextFill(Color.web(COL_WHITE));
        leagueTitleLabel.setAlignment(Pos.CENTER);
        leagueTitleLabel.setPrefWidth(600);
        leagueTitleLabel.setLayoutX(cx - 300);
        leagueTitleLabel.setLayoutY(18);

        homeTeamLabel = new Label(data.getHomeTeamName());
        homeTeamLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 26));
        homeTeamLabel.setTextFill(Color.web(COL_WHITE));
        homeTeamLabel.setAlignment(Pos.CENTER);
        homeTeamLabel.setWrapText(true);
        homeTeamLabel.setTextAlignment(TextAlignment.CENTER);
        homeTeamLabel.setPrefWidth(LOGO_SIZE);
        homeTeamLabel.setLayoutX(30);
        homeTeamLabel.setLayoutY(334);

        awayTeamLabel = new Label(data.getAwayTeamName());
        awayTeamLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 26));
        awayTeamLabel.setTextFill(Color.web(COL_WHITE));
        awayTeamLabel.setAlignment(Pos.CENTER);
        awayTeamLabel.setWrapText(true);
        awayTeamLabel.setTextAlignment(TextAlignment.CENTER);
        awayTeamLabel.setPrefWidth(LOGO_SIZE);
        awayTeamLabel.setLayoutX(W - LOGO_SIZE - 30);
        awayTeamLabel.setLayoutY(334);

        timerLabel = new Label(data.getFormattedTime());
        timerLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, TIMER_FONT));
        timerLabel.setTextFill(Color.web(COL_GOLD));
        timerLabel.setAlignment(Pos.CENTER);
        timerLabel.setPrefWidth(400);
        timerLabel.setLayoutX(cx - 200);
        timerLabel.setLayoutY(60);

        aggHeaderLabel = new Label("AGG");
        aggHeaderLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 18));
        aggHeaderLabel.setTextFill(Color.web(COL_DIM));
        aggHeaderLabel.setAlignment(Pos.CENTER);
        aggHeaderLabel.setPrefWidth(300);
        aggHeaderLabel.setLayoutX(cx - 150);
        aggHeaderLabel.setLayoutY(180);
        aggHeaderLabel.setVisible(false);

        aggLabel = new Label(data.getHomeAgg() + "  \u2014  " + data.getAwayAgg());
        aggLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 44));
        aggLabel.setTextFill(Color.web(COL_WHITE));
        aggLabel.setAlignment(Pos.CENTER);
        aggLabel.setPrefWidth(300);
        aggLabel.setLayoutX(cx - 150);
        aggLabel.setLayoutY(202);
        aggLabel.setVisible(false);

        Label roundHeader = new Label("ROUND");
        roundHeader.setFont(Font.font("Helvetica", FontWeight.BOLD, ROUND_LBL));
        roundHeader.setTextFill(Color.web(COL_DIM));
        roundHeader.setAlignment(Pos.CENTER);
        roundHeader.setPrefWidth(200);
        roundHeader.setLayoutX(cx - 100);
        roundHeader.setLayoutY(265);

        roundLabel = new Label(String.valueOf(data.getRound()));
        roundLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, ROUND_NUM));
        roundLabel.setTextFill(Color.web(COL_WHITE));
        roundLabel.setAlignment(Pos.CENTER);
        roundLabel.setPrefWidth(200);
        roundLabel.setLayoutX(cx - 100);
        roundLabel.setLayoutY(290);

        StackPane homeLogoContainer = new StackPane();
        homeLogoContainer.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        homeLogoContainer.setMinSize(LOGO_SIZE, LOGO_SIZE);
        homeLogoContainer.setMaxSize(LOGO_SIZE, LOGO_SIZE);
        homePlaceholder = new Label("H");
        homePlaceholder.setFont(Font.font("Helvetica", FontWeight.BOLD, 80));
        homePlaceholder.setTextFill(Color.web("rgba(255,255,255,0.18)"));
        homeLogoView = new ImageView();
        homeLogoView.setFitWidth(LOGO_SIZE);
        homeLogoView.setFitHeight(LOGO_SIZE);
        homeLogoView.setPreserveRatio(true);
        homeLogoView.setSmooth(true);
        homeLogoView.setVisible(false);
        homeLogoContainer.getChildren().addAll(homePlaceholder, homeLogoView);
        homeLogoContainer.setLayoutX(30);
        homeLogoContainer.setLayoutY(82);

        StackPane awayLogoContainer = new StackPane();
        awayLogoContainer.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        awayLogoContainer.setMinSize(LOGO_SIZE, LOGO_SIZE);
        awayLogoContainer.setMaxSize(LOGO_SIZE, LOGO_SIZE);
        awayPlaceholder = new Label("A");
        awayPlaceholder.setFont(Font.font("Helvetica", FontWeight.BOLD, 80));
        awayPlaceholder.setTextFill(Color.web("rgba(255,255,255,0.18)"));
        awayLogoView = new ImageView();
        awayLogoView.setFitWidth(LOGO_SIZE);
        awayLogoView.setFitHeight(LOGO_SIZE);
        awayLogoView.setPreserveRatio(true);
        awayLogoView.setSmooth(true);
        awayLogoView.setVisible(false);
        awayLogoContainer.getChildren().addAll(awayPlaceholder, awayLogoView);
        awayLogoContainer.setLayoutX(W - LOGO_SIZE - 30);
        awayLogoContainer.setLayoutY(82);

        homeScoreLabel = new Label(String.valueOf(data.getHomeScore()));
        homeScoreLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, SCORE_FONT));
        homeScoreLabel.setTextFill(Color.web(COL_WHITE));
        homeScoreLabel.setAlignment(Pos.CENTER);
        homeScoreLabel.setPrefWidth(220);
        homeScoreLabel.setMinWidth(220);
        homeScoreLabel.setMaxWidth(220);
        homeScoreLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        homeScoreLabel.setLayoutX(30 + LOGO_SIZE + 5);
        homeScoreLabel.setLayoutY(240);

        awayScoreLabel = new Label(String.valueOf(data.getAwayScore()));
        awayScoreLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, SCORE_FONT));
        awayScoreLabel.setTextFill(Color.web(COL_WHITE));
        awayScoreLabel.setAlignment(Pos.CENTER);
        awayScoreLabel.setPrefWidth(220);
        awayScoreLabel.setMinWidth(220);
        awayScoreLabel.setMaxWidth(220);
        awayScoreLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        awayScoreLabel.setLayoutX(W - LOGO_SIZE - 30 - 220 - 5);
        awayScoreLabel.setLayoutY(240);

        Label homeFoulTitle = new Label("FOULS");
        homeFoulTitle.setFont(Font.font("Helvetica", FontWeight.BOLD, FOUL_LBL));
        homeFoulTitle.setTextFill(Color.web(COL_DIM));
        homeFoulsLabel = new Label("0");
        homeFoulsLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, FOUL_NUM));
        homeFoulsLabel.setTextFill(Color.web(COL_ORANGE));
        VBox homeFoulBlock = new VBox(0, homeFoulTitle, homeFoulsLabel);
        homeFoulBlock.setAlignment(Pos.CENTER);
        homeFoulBlock.setPrefWidth(220);
        homeFoulBlock.setLayoutX(380);
        homeFoulBlock.setLayoutY(430);

        Label awayFoulTitle = new Label("FOULS");
        awayFoulTitle.setFont(Font.font("Helvetica", FontWeight.BOLD, FOUL_LBL));
        awayFoulTitle.setTextFill(Color.web(COL_DIM));
        awayFoulsLabel = new Label("0");
        awayFoulsLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, FOUL_NUM));
        awayFoulsLabel.setTextFill(Color.web(COL_ORANGE));
        VBox awayFoulBlock = new VBox(0, awayFoulTitle, awayFoulsLabel);
        awayFoulBlock.setAlignment(Pos.CENTER);
        awayFoulBlock.setPrefWidth(220);
        awayFoulBlock.setLayoutX(680);
        awayFoulBlock.setLayoutY(430);

        VBox homeCardTimerDisplay = buildCardTimerDisplay(true);
        homeCardTimerDisplay.setLayoutX(90);
        homeCardTimerDisplay.setLayoutY(465);

        VBox awayCardTimerDisplay = buildCardTimerDisplay(false);
        awayCardTimerDisplay.setLayoutX(W - LOGO_SIZE - 30 + 55);
        awayCardTimerDisplay.setLayoutY(465);

        p.getChildren().addAll(
                leagueTitleLabel, homeTeamLabel, awayTeamLabel,
                timerLabel, aggHeaderLabel, aggLabel, roundHeader, roundLabel,
                homeLogoContainer, awayLogoContainer,
                homeScoreLabel, awayScoreLabel,
                homeFoulBlock, awayFoulBlock,
                homeCardTimerDisplay, awayCardTimerDisplay);
    }

    private VBox buildCardTimerDisplay(boolean isHome) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < Math.min(2, GameData.MAX_CARD_TIMERS); i++) {
            final int slot = i;

            Label redBox = new Label("  ");
            redBox.setStyle("-fx-background-color:#FF3B3B;"
                    + "-fx-min-width:12;-fx-min-height:12;-fx-max-width:12;-fx-max-height:12;"
                    + "-fx-background-radius:3;");

            Label timeLbl = new Label("02:00");
            timeLbl.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            timeLbl.setTextFill(Color.web("#FF3B3B"));
            timeLbl.setMinWidth(90);

            HBox row = new HBox(8, redBox, timeLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 10, 4, 10));
            row.setStyle("-fx-background-color:#1A0000;-fx-background-radius:6;"
                    + "-fx-border-color:rgba(255,59,59,0.35);-fx-border-radius:6;-fx-border-width:1;");
            row.setVisible(false);
            row.setManaged(false);

            javafx.beans.property.LongProperty timerProp = isHome
                    ? data.homeCardTimerSecsProperty(slot)
                    : data.awayCardTimerSecsProperty(slot);
            timerProp.addListener((o, a, newVal) -> {
                long secs = newVal.longValue();
                if (secs < 0) {
                    row.setVisible(false);
                    row.setManaged(false);
                } else {
                    row.setVisible(true);
                    row.setManaged(true);
                    timeLbl.setText(String.format("%02d:%02d", secs / 60, secs % 60));
                    timeLbl.setTextFill(secs == 0 ? Color.web(COL_GOLD) : Color.web("#FF3B3B"));
                }
            });

            box.getChildren().add(row);
        }
        return box;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Penalty shootout screen
    // ══════════════════════════════════════════════════════════════════════
    private VBox buildPenaltyCanvas() {
        VBox vbox = new VBox(0);
        vbox.setAlignment(Pos.TOP_CENTER);

        Pane p = new Pane();
        p.setPrefSize(BASE_W, BASE_H - 80);
        VBox.setVgrow(p, Priority.ALWAYS);
        buildPenaltyElements(p);

        HBox logoBar = new HBox();
        logoBar.setAlignment(Pos.CENTER);
        logoBar.setPrefHeight(80);
        logoBar.setStyle("-fx-background-color: transparent;");
        penLeagueLogoView = new ImageView();
        penLeagueLogoView.setFitHeight(70);
        penLeagueLogoView.setFitWidth(300);
        penLeagueLogoView.setPreserveRatio(true);
        penLeagueLogoView.setSmooth(true);
        penLeagueLogoView.setVisible(false);
        logoBar.getChildren().add(penLeagueLogoView);

        vbox.getChildren().addAll(p, logoBar);
        return vbox;
    }

    private void buildPenaltyElements(Pane p) {
        double W = BASE_W;
        double H = BASE_H - 80; // 640
        double cx = W / 2;

        // ── "PENALTY SHOOTOUT" title ──
        Label title = new Label("PENALTY SHOOTOUT");
        title.setFont(Font.font("Helvetica", FontWeight.BOLD, 52));
        title.setTextFill(Color.web(COL_GOLD));
        title.setAlignment(Pos.CENTER);
        title.setPrefWidth(W);
        title.setLayoutX(0);
        title.setLayoutY(8);

        // ── Home logo (same size/position as main scoreboard) ──
        StackPane homeLogoSp = new StackPane();
        homeLogoSp.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        homeLogoSp.setMinSize(LOGO_SIZE, LOGO_SIZE);
        homeLogoSp.setMaxSize(LOGO_SIZE, LOGO_SIZE);
        penHomePlaceholder = new Label("H");
        penHomePlaceholder.setFont(Font.font("Helvetica", FontWeight.BOLD, 80));
        penHomePlaceholder.setTextFill(Color.web("rgba(255,255,255,0.18)"));
        penHomeLogoView = new ImageView();
        penHomeLogoView.setFitWidth(LOGO_SIZE);
        penHomeLogoView.setFitHeight(LOGO_SIZE);
        penHomeLogoView.setPreserveRatio(true);
        penHomeLogoView.setSmooth(true);
        penHomeLogoView.setVisible(false);
        homeLogoSp.getChildren().addAll(penHomePlaceholder, penHomeLogoView);
        homeLogoSp.setLayoutX(30);
        homeLogoSp.setLayoutY(82);

        // ── Away logo (same size/position as main scoreboard) ──
        StackPane awayLogoSp = new StackPane();
        awayLogoSp.setPrefSize(LOGO_SIZE, LOGO_SIZE);
        awayLogoSp.setMinSize(LOGO_SIZE, LOGO_SIZE);
        awayLogoSp.setMaxSize(LOGO_SIZE, LOGO_SIZE);
        penAwayPlaceholder = new Label("A");
        penAwayPlaceholder.setFont(Font.font("Helvetica", FontWeight.BOLD, 80));
        penAwayPlaceholder.setTextFill(Color.web("rgba(255,255,255,0.18)"));
        penAwayLogoView = new ImageView();
        penAwayLogoView.setFitWidth(LOGO_SIZE);
        penAwayLogoView.setFitHeight(LOGO_SIZE);
        penAwayLogoView.setPreserveRatio(true);
        penAwayLogoView.setSmooth(true);
        penAwayLogoView.setVisible(false);
        awayLogoSp.getChildren().addAll(penAwayPlaceholder, penAwayLogoView);
        awayLogoSp.setLayoutX(W - LOGO_SIZE - 30);
        awayLogoSp.setLayoutY(82);

        // ── Home team name (below home logo) ──
        penHomeTeamLabel = new Label(data.getHomeTeamName());
        penHomeTeamLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 26));
        penHomeTeamLabel.setTextFill(Color.web(COL_WHITE));
        penHomeTeamLabel.setAlignment(Pos.CENTER);
        penHomeTeamLabel.setWrapText(true);
        penHomeTeamLabel.setTextAlignment(TextAlignment.CENTER);
        penHomeTeamLabel.setPrefWidth(LOGO_SIZE);
        penHomeTeamLabel.setLayoutX(30);
        penHomeTeamLabel.setLayoutY(334);

        // ── Away team name (below away logo) ──
        penAwayTeamLabel = new Label(data.getAwayTeamName());
        penAwayTeamLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 26));
        penAwayTeamLabel.setTextFill(Color.web(COL_WHITE));
        penAwayTeamLabel.setAlignment(Pos.CENTER);
        penAwayTeamLabel.setWrapText(true);
        penAwayTeamLabel.setTextAlignment(TextAlignment.CENTER);
        penAwayTeamLabel.setPrefWidth(LOGO_SIZE);
        penAwayTeamLabel.setLayoutX(W - LOGO_SIZE - 30);
        penAwayTeamLabel.setLayoutY(334);

        // ── Combined total score: homeScore + homePenaltyScore ──
        penHomeTotalLabel = new Label(String.valueOf(data.getHomeScore()));
        penHomeTotalLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 180));
        penHomeTotalLabel.setTextFill(Color.web(COL_WHITE));
        penHomeTotalLabel.setAlignment(Pos.CENTER);
        penHomeTotalLabel.setPrefWidth(240);
        penHomeTotalLabel.setMinWidth(240);
        penHomeTotalLabel.setMaxWidth(240);
        penHomeTotalLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        penHomeTotalLabel.setLayoutX(240);
        penHomeTotalLabel.setLayoutY(340);

        penAwayTotalLabel = new Label(String.valueOf(data.getAwayScore()));
        penAwayTotalLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 180));
        penAwayTotalLabel.setTextFill(Color.web(COL_WHITE));
        penAwayTotalLabel.setAlignment(Pos.CENTER);
        penAwayTotalLabel.setPrefWidth(240);
        penAwayTotalLabel.setMinWidth(240);
        penAwayTotalLabel.setMaxWidth(240);
        penAwayTotalLabel.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        penAwayTotalLabel.setLayoutX(800);
        penAwayTotalLabel.setLayoutY(340);

        // ── AGG (center between scores, only visible when showAggregate) ──
        penAggHeaderLabel = new Label("AGG");
        penAggHeaderLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 18));
        penAggHeaderLabel.setTextFill(Color.web(COL_DIM));
        penAggHeaderLabel.setAlignment(Pos.CENTER);
        penAggHeaderLabel.setPrefWidth(200);
        penAggHeaderLabel.setLayoutX(cx - 100);
        penAggHeaderLabel.setLayoutY(375);
        penAggHeaderLabel.setVisible(data.isShowAggregate());

        penAggLabel = new Label(data.getHomeAgg() + "  \u2014  " + data.getAwayAgg());
        penAggLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 40));
        penAggLabel.setTextFill(Color.web(COL_WHITE));
        penAggLabel.setAlignment(Pos.CENTER);
        penAggLabel.setPrefWidth(200);
        penAggLabel.setLayoutX(cx - 100);
        penAggLabel.setLayoutY(398);
        penAggLabel.setVisible(data.isShowAggregate());

        // ── Kick grid (dynamic, redrawn on every kick change) ──
        // Keep penHomeScoreLabel/penAwayScoreLabel as hidden dummies for compat
        penHomeScoreLabel = new Label();
        penHomeScoreLabel.setVisible(false);
        penAwayScoreLabel = new Label();
        penAwayScoreLabel.setVisible(false);

        penaltyKickGrid = new Pane();
        penaltyKickGrid.setPrefSize(W, H);
        penaltyKickGrid.setLayoutX(0);
        penaltyKickGrid.setLayoutY(0);
        buildPenaltyKickGrid();

        p.getChildren().addAll(
                title,
                homeLogoSp, awayLogoSp,
                penHomeTeamLabel, penAwayTeamLabel,
                penHomeTotalLabel, penAwayTotalLabel,
                penAggHeaderLabel, penAggLabel,
                penHomeScoreLabel, penAwayScoreLabel,
                penaltyKickGrid);
    }

    private void buildPenaltyKickGrid() {
        penaltyKickGrid.getChildren().clear();

        int homeCount = data.getHomePenaltyCount();
        int awayCount = data.getAwayPenaltyCount();

        // Always show exactly 1 set of 5 kicks — no set 2, 3 etc.
        int setsToShow = 1;
        int slotsToShow = GameData.KICKS_PER_SET;
        boolean multiSet = false;

        double kickSize = 55, kickGap = 9;
        double setGap = 26;
        double setWidth = GameData.KICKS_PER_SET * kickSize + (GameData.KICKS_PER_SET - 1) * kickGap;

        drawTeamKicks(true, homeCount, slotsToShow, 290, 148, kickSize, kickGap, setGap, setWidth, multiSet);
        drawTeamKicks(false, awayCount, slotsToShow, 680, 148, kickSize, kickGap, setGap, setWidth, multiSet);
    }

    private void drawTeamKicks(boolean isHome, int takenCount, int slotsToShow,
            double startX, double startY,
            double kickSize, double kickGap, double setGap, double setWidth, boolean multiSet) {

        int numSets = (int) Math.ceil((double) slotsToShow / GameData.KICKS_PER_SET);

        for (int s = 0; s < numSets; s++) {
            double setX = startX + s * (setWidth + setGap);

            // Show SET label + separator only when multiple sets exist
            if (multiSet) {
                Label setLbl = new Label("SET " + (s + 1));
                setLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
                setLbl.setTextFill(Color.web("rgba(255,255,255,0.38)"));
                setLbl.setLayoutX(setX + setWidth / 2 - 18);
                setLbl.setLayoutY(startY - 20);
                penaltyKickGrid.getChildren().add(setLbl);

                if (s > 0) {
                    Region sep = new Region();
                    sep.setPrefSize(1, 90);
                    sep.setStyle("-fx-background-color: rgba(255,255,255,0.14);");
                    sep.setLayoutX(setX - setGap / 2.0);
                    sep.setLayoutY(startY - 6);
                    penaltyKickGrid.getChildren().add(sep);
                }
            }

            for (int k = 0; k < GameData.KICKS_PER_SET; k++) {
                int idx = s * GameData.KICKS_PER_SET + k;
                if (idx >= slotsToShow)
                    break;

                double kx = setX + k * (kickSize + kickGap);
                double ky = startY;

                int result = (idx < GameData.MAX_PENALTY_KICKS)
                        ? (isHome ? data.getHomePenaltyKick(idx) : data.getAwayPenaltyKick(idx))
                        : 0;
                boolean taken = idx < takenCount;

                drawKickSquare(kx, ky, kickSize, idx + 1, result, taken);
            }
        }
    }

    /**
     * Draws a numbered square kick indicator.
     * result: 0=pending, 1=scored (green), 2=missed (red)
     */
    private void drawKickSquare(double x, double y, double size, int kickNum, int result, boolean taken) {
        Rectangle rect = new Rectangle(x, y, size, size);
        rect.setArcWidth(9);
        rect.setArcHeight(9);
        rect.setStrokeWidth(2.5);

        Color numColor;
        if (!taken) {
            rect.setFill(Color.web("rgba(255,255,255,0.05)"));
            rect.setStroke(Color.web("rgba(255,255,255,0.18)"));
            rect.getStrokeDashArray().addAll(5.0, 4.0);
            numColor = Color.web("rgba(255,255,255,0.22)");
        } else if (result == 1) {
            rect.setFill(Color.web("#163020"));
            rect.setStroke(Color.web(COL_GREEN));
            numColor = Color.web(COL_GREEN);
        } else {
            rect.setFill(Color.web("#301616"));
            rect.setStroke(Color.web(COL_RED));
            numColor = Color.web(COL_RED);
        }

        Label numLbl = new Label(String.valueOf(kickNum));
        numLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, size * 0.46));
        numLbl.setTextFill(numColor);
        numLbl.setPrefSize(size, size);
        numLbl.setLayoutX(x);
        numLbl.setLayoutY(y);
        numLbl.setAlignment(Pos.CENTER);

        penaltyKickGrid.getChildren().addAll(rect, numLbl);
    }

    public void setResolution(String label) {
        int w, h;
        switch (label) {
            case "720p  (1280\u00d7720)" -> {
                w = 1280;
                h = 720;
            }
            case "1080p (1920\u00d71080)" -> {
                w = 1920;
                h = 1080;
            }
            case "1440p (2560\u00d71440)" -> {
                w = 2560;
                h = 1440;
            }
            case "4K    (3840\u00d72160)" -> {
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

    private void bindData() {
        // ── Main scoreboard ──
        data.leagueTitleProperty().addListener((o, a, b) -> leagueTitleLabel.setText(b));
        data.homeTeamNameProperty().addListener((o, a, b) -> {
            homeTeamLabel.setText(b);
            penHomeTeamLabel.setText(b);
        });
        data.awayTeamNameProperty().addListener((o, a, b) -> {
            awayTeamLabel.setText(b);
            penAwayTeamLabel.setText(b);
        });
        data.homeScoreProperty().addListener((o, a, b) -> homeScoreLabel.setText(b.toString()));
        data.awayScoreProperty().addListener((o, a, b) -> awayScoreLabel.setText(b.toString()));
        data.homeFoulsProperty().addListener((o, a, b) -> homeFoulsLabel.setText(b.toString()));
        data.awayFoulsProperty().addListener((o, a, b) -> awayFoulsLabel.setText(b.toString()));
        data.roundProperty().addListener((o, a, b) -> roundLabel.setText(b.toString()));
        data.homeAggProperty()
                .addListener((o, a, b) -> aggLabel.setText(data.getHomeAgg() + "  \u2014  " + data.getAwayAgg()));
        data.awayAggProperty()
                .addListener((o, a, b) -> aggLabel.setText(data.getHomeAgg() + "  \u2014  " + data.getAwayAgg()));
        data.showAggregateProperty().addListener((o, a, b) -> {
            aggLabel.setVisible(b);
            aggHeaderLabel.setVisible(b);
        });

        data.homeLogoProperty().addListener((o, a, n) -> {
            if (n != null) {
                homeLogoView.setImage(n);
                homeLogoView.setVisible(true);
                homePlaceholder.setVisible(false);
                penHomeLogoView.setImage(n);
                penHomeLogoView.setVisible(true);
                penHomePlaceholder.setVisible(false);
            } else {
                homeLogoView.setImage(null);
                homeLogoView.setVisible(false);
                homePlaceholder.setVisible(true);
                penHomeLogoView.setImage(null);
                penHomeLogoView.setVisible(false);
                penHomePlaceholder.setVisible(true);
            }
        });
        data.awayLogoProperty().addListener((o, a, n) -> {
            if (n != null) {
                awayLogoView.setImage(n);
                awayLogoView.setVisible(true);
                awayPlaceholder.setVisible(false);
                penAwayLogoView.setImage(n);
                penAwayLogoView.setVisible(true);
                penAwayPlaceholder.setVisible(false);
            } else {
                awayLogoView.setImage(null);
                awayLogoView.setVisible(false);
                awayPlaceholder.setVisible(true);
                penAwayLogoView.setImage(null);
                penAwayLogoView.setVisible(false);
                penAwayPlaceholder.setVisible(true);
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
        data.timeoutActiveProperty().addListener((o, a, b) -> refreshTimer());
        data.timeoutRemainingSecsProperty().addListener((o, a, b) -> {
            if (data.isTimeoutActive())
                refreshTimer();
        });

        // ── Penalty screen toggle ──
        data.showPenaltyScreenProperty().addListener((o, a, show) -> {
            contentCanvas.setVisible(!show);
            penaltyCanvas.setVisible(show);
        });

        // ── Penalty screen: combined total labels (mainScore + penaltyScore) ──
        // Penalty total = homeScore directly (same variable as main scoreboard)
        data.homeScoreProperty().addListener((o, a, b) -> penHomeTotalLabel.setText(b.toString()));
        data.awayScoreProperty().addListener((o, a, b) -> penAwayTotalLabel.setText(b.toString()));

        // ── Penalty screen AGG ──
        Runnable updatePenAgg = () -> penAggLabel.setText(data.getHomeAgg() + "  \u2014  " + data.getAwayAgg());
        data.homeAggProperty().addListener((o, a, b) -> updatePenAgg.run());
        data.awayAggProperty().addListener((o, a, b) -> updatePenAgg.run());
        data.showAggregateProperty().addListener((o, a, b) -> {
            penAggHeaderLabel.setVisible(b);
            penAggLabel.setVisible(b);
        });

        // ── Penalty screen: league logo ──
        data.leagueLogoProperty().addListener((o, a, n) -> {
            penLeagueLogoView.setImage(n);
            penLeagueLogoView.setVisible(n != null);
        });

        // ── Penalty kick grid — rebuild whenever any kick changes ──
        Runnable rebuildGrid = this::buildPenaltyKickGrid;
        data.homePenaltyCountProperty().addListener((o, a, b) -> rebuildGrid.run());
        data.awayPenaltyCountProperty().addListener((o, a, b) -> rebuildGrid.run());
        for (int i = 0; i < GameData.MAX_PENALTY_KICKS; i++) {
            data.homePenaltyKickProperty(i).addListener((o, a, b) -> rebuildGrid.run());
            data.awayPenaltyKickProperty(i).addListener((o, a, b) -> rebuildGrid.run());
        }
    }

    private void refreshTimer() {
        if (data.isTimeoutActive()) {
            long secs = data.getTimeoutRemainingSecs();
            timerLabel.setText(String.format("%02d:%02d", secs / 60, secs % 60));
            timerLabel.setTextFill(Color.web(COL_TIMEOUT));
            stopExpiredBlink();
        } else {
            timerLabel.setText(data.getFormattedTime());
            if (data.isTimeExpired()) {
                timerLabel.setTextFill(Color.web(COL_RED));
                startExpiredBlink();
            } else {
                timerLabel.setTextFill(Color.web(COL_GOLD));
                stopExpiredBlink();
            }
        }
    }

    private void startTimerEngine() {
        animTimer = new AnimationTimer() {
            private long startNano = -1, startElapsed = 0;

            @Override
            public void handle(long now) {
                if (!data.isTimerRunning() || data.isTimeoutActive()) {
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
        if (expiredBlink != null && expiredBlink.getStatus() == javafx.animation.Animation.Status.RUNNING)
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

    public void show() {
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }
}