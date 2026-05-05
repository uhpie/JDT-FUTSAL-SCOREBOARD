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
    private Label leagueLogoLabel, homeLogoLabel, awayLogoLabel, bgImageLabel;

    public ControlPanel(GameData data, Stage displayStage, DisplayWindow display) {
        this.data = data;
        this.displayStage = displayStage;
        this.display = display;
    }

    public void show(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("JDT Futsal — Referee Control Panel");

        final double BASE_W = 900, BASE_H = 560;

        // Fixed-size canvas
        BorderPane canvas = buildRoot();
        canvas.setStyle("-fx-background-color: " + BG_DARK + ";");
        canvas.setPrefSize(BASE_W, BASE_H);
        canvas.setMinSize(BASE_W, BASE_H);
        canvas.setMaxSize(BASE_W, BASE_H);

        // Scale transform — stretches canvas to fill window
        javafx.scene.transform.Scale sc = new javafx.scene.transform.Scale(1, 1, 0, 0);
        canvas.getTransforms().add(sc);

        // StackPane as scene root — freely resizable, black background
        StackPane outer = new StackPane();
        outer.setStyle("-fx-background-color: black;");
        outer.getChildren().add(canvas);
        // Align canvas to TOP_LEFT so scale origin (0,0) is correct
        StackPane.setAlignment(canvas, Pos.TOP_LEFT);

        Scene scene = new Scene(outer, BASE_W, BASE_H, Color.web(BG_DARK));

        // Rescale canvas whenever window size changes
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

    private BorderPane buildRoot() {
        BorderPane bp = new BorderPane();

        // TOP: title bar with Settings button
        bp.setTop(buildTitleBar());

        // CENTER: 3-column HBox
        HBox center = new HBox(0);
        center.setAlignment(Pos.CENTER);

        VBox homeCol = buildTeamColumn(true);
        VBox centerCol = buildCenterColumn();
        VBox awayCol = buildTeamColumn(false);

        HBox.setHgrow(homeCol, Priority.ALWAYS);
        HBox.setHgrow(centerCol, Priority.NEVER);
        HBox.setHgrow(awayCol, Priority.ALWAYS);

        center.getChildren().addAll(homeCol, centerCol, awayCol);
        bp.setCenter(center);

        // BOTTOM: fouls + cards bar
        bp.setBottom(buildBottomBar());

        return bp;
    }

    // ── Title bar ──────────────────────────────────────────────────────────
    private StackPane buildTitleBar() {
        Label title = new Label(data.getLeagueTitle());
        title.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(GOLD));
        data.leagueTitleProperty().addListener((o, a, b) -> title.setText(b));
        StackPane.setAlignment(title, Pos.CENTER);

        Button settingsBtn = new Button("⚙  Settings");
        settingsBtn.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        settingsBtn.setStyle("-fx-background-color: rgba(255,215,0,0.15); -fx-text-fill:" + GOLD
                + "; -fx-border-color: rgba(255,215,0,0.4); -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 16; -fx-cursor:hand;");
        settingsBtn.setOnAction(e -> openSettings());
        StackPane.setAlignment(settingsBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(settingsBtn, new Insets(0, 12, 0, 0));

        StackPane bar = new StackPane(title, settingsBtn);
        bar.setPrefHeight(50);
        bar.setStyle("-fx-background-color: transparent;");
        return bar;
    }

    // ── Team column ────────────────────────────────────────────────────────
    private VBox buildTeamColumn(boolean isHome) {
        VBox col = new VBox(16);
        col.setAlignment(Pos.CENTER);
        col.setPadding(new Insets(20, 10, 10, 10));

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
        scoreVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 90));
        scoreVal.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        scoreVal.setWrapText(false);
        scoreVal.setMinWidth(javafx.scene.control.Control.USE_PREF_SIZE);
        scoreVal.setTextFill(Color.web(TEXT_PRI));
        scoreVal.setAlignment(Pos.CENTER);
        scoreVal.setMinWidth(Region.USE_PREF_SIZE);
        scoreVal.setMaxWidth(200);
        if (isHome)
            data.homeScoreProperty().addListener((o, a, b) -> scoreVal.setText(b.toString()));
        else
            data.awayScoreProperty().addListener((o, a, b) -> scoreVal.setText(b.toString()));

        Button minus = scoreBtn("−", "#444444"), plus = scoreBtn("+", "#444444");
        minus.setOnAction(e -> {
            if (isHome)
                data.setHomeScore(data.getHomeScore() - 1);
            else
                data.setAwayScore(data.getAwayScore() - 1);
        });
        plus.setOnAction(e -> {
            if (isHome)
                data.setHomeScore(data.getHomeScore() + 1);
            else
                data.setAwayScore(data.getAwayScore() + 1);
        });

        HBox row = new HBox(12, minus, scoreVal, plus);
        row.setAlignment(Pos.CENTER);
        col.getChildren().addAll(nameLabel, row);
        return col;
    }

    // ── Center column ──────────────────────────────────────────────────────
    private VBox buildCenterColumn() {
        VBox col = new VBox(8);
        col.setAlignment(Pos.CENTER);
        col.setPadding(new Insets(10, 0, 10, 0));
        col.setMinWidth(380);
        col.setPrefWidth(380);
        col.setMaxWidth(380);

        cpTimerLabel = new Label(data.getFormattedTime());
        cpTimerLabel.setFont(Font.font("Helvetica", FontWeight.BOLD, 78));
        cpTimerLabel.setTextFill(Color.web(GOLD));
        cpTimerLabel.setAlignment(Pos.CENTER);
        cpTimerLabel.setMaxWidth(Double.MAX_VALUE);
        data.elapsedSecondsProperty().addListener((o, a, b) -> cpTimerLabel.setText(data.getFormattedTime()));
        data.matchDurationMinutesProperty().addListener((o, a, b) -> cpTimerLabel.setText(data.getFormattedTime()));
        data.countDownProperty().addListener((o, a, b) -> cpTimerLabel.setText(data.getFormattedTime()));

        Button startBtn = timerBtn("▶", GREEN), pauseBtn = timerBtn("⏸", ORANGE), resetBtn = timerBtn("↺", RED_BTN);
        startBtn.setOnAction(e -> data.setTimerRunning(true));
        pauseBtn.setOnAction(e -> data.setTimerRunning(false));
        resetBtn.setOnAction(e -> {
            data.setTimerRunning(false);
            data.setElapsedSeconds(0);
        });
        HBox timerBtns = new HBox(8, startBtn, pauseBtn, resetBtn);
        timerBtns.setAlignment(Pos.CENTER);

        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(200);
        sep.setStyle("-fx-background-color: rgba(255,215,0,0.25);");

        Label roundLbl = new Label("ROUND");
        roundLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        roundLbl.setTextFill(Color.web(TEXT_SEC));
        roundLbl.setAlignment(Pos.CENTER);
        roundLbl.setMaxWidth(Double.MAX_VALUE);

        Label roundVal = new Label(String.valueOf(data.getRound()));
        roundVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 64));
        roundVal.setTextFill(Color.web(TEXT_PRI));
        roundVal.setAlignment(Pos.CENTER);
        roundVal.setMinWidth(70);
        data.roundProperty().addListener((o, a, b) -> roundVal.setText(b.toString()));

        Button rM = scoreBtn("−", "#444444"), rP = scoreBtn("+", "#444444");
        rM.setOnAction(e -> data.setRound(data.getRound() - 1));
        rP.setOnAction(e -> data.setRound(data.getRound() + 1));
        HBox roundRow = new HBox(8, rM, roundVal, rP);
        roundRow.setAlignment(Pos.CENTER);

        ToggleButton aggToggle = new ToggleButton("AGG: OFF");
        aggToggle.setSelected(data.isShowAggregate());
        aggToggle.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        aggToggle.setStyle(
                "-fx-background-color:#1C1C1C; -fx-text-fill:rgba(255,255,255,0.5); -fx-border-color:rgba(255,255,255,0.15); -fx-border-radius:6; -fx-background-radius:6; -fx-padding:5 14; -fx-cursor:hand;");

        Label homeAggVal = new Label(String.valueOf(data.getHomeAgg()));
        homeAggVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 40));
        homeAggVal.setTextFill(Color.web(TEXT_PRI));
        homeAggVal.setMinWidth(55);
        homeAggVal.setAlignment(Pos.CENTER);
        data.homeAggProperty().addListener((o, a, b) -> homeAggVal.setText(b.toString()));

        Label aggDash = new Label("—");
        aggDash.setFont(Font.font("Helvetica", FontWeight.BOLD, 24));
        aggDash.setTextFill(Color.web(TEXT_SEC));

        Label awayAggVal = new Label(String.valueOf(data.getAwayAgg()));
        awayAggVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 40));
        awayAggVal.setTextFill(Color.web(TEXT_PRI));
        awayAggVal.setMinWidth(55);
        awayAggVal.setAlignment(Pos.CENTER);
        data.awayAggProperty().addListener((o, a, b) -> awayAggVal.setText(b.toString()));

        Button hAggM = scoreBtn("−", "#444444"), hAggP = scoreBtn("+", "#444444"), aAggM = scoreBtn("−", "#444444"),
                aAggP = scoreBtn("+", "#444444");
        hAggM.setOnAction(e -> data.setHomeAgg(data.getHomeAgg() - 1));
        hAggP.setOnAction(e -> data.setHomeAgg(data.getHomeAgg() + 1));
        aAggM.setOnAction(e -> data.setAwayAgg(data.getAwayAgg() - 1));
        aAggP.setOnAction(e -> data.setAwayAgg(data.getAwayAgg() + 1));

        HBox aggRow = new HBox(4, hAggM, hAggP, homeAggVal, aggDash, awayAggVal, aAggM, aAggP);
        aggRow.setPadding(new Insets(0, 10, 0, 10));
        aggRow.setAlignment(Pos.CENTER);
        aggRow.setVisible(data.isShowAggregate());
        aggRow.setManaged(data.isShowAggregate());

        Label aggLbl = new Label("AGGREGATE");
        aggLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 13));
        aggLbl.setTextFill(Color.web(TEXT_SEC));
        aggLbl.setAlignment(Pos.CENTER);
        aggLbl.setMaxWidth(Double.MAX_VALUE);
        aggLbl.setVisible(data.isShowAggregate());
        aggLbl.setManaged(data.isShowAggregate());

        aggToggle.setOnAction(e -> {
            boolean on = aggToggle.isSelected();
            data.setShowAggregate(on);
            aggToggle.setText(on ? "AGG: ON" : "AGG: OFF");
            aggToggle.setStyle(on ? "-fx-background-color:" + GOLD + "33; -fx-text-fill:" + GOLD + "; -fx-border-color:"
                    + GOLD
                    + "66; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:5 14; -fx-font-weight:bold; -fx-cursor:hand;"
                    : "-fx-background-color:#1C1C1C; -fx-text-fill:rgba(255,255,255,0.5); -fx-border-color:rgba(255,255,255,0.15); -fx-border-radius:6; -fx-background-radius:6; -fx-padding:5 14; -fx-cursor:hand;");
            aggRow.setVisible(on);
            aggRow.setManaged(on);
            aggLbl.setVisible(on);
            aggLbl.setManaged(on);
        });

        col.getChildren().addAll(cpTimerLabel, timerBtns, sep, roundLbl, roundRow, aggToggle, aggLbl, aggRow);
        return col;
    }

    // ── Bottom bar: fouls + cards ─────────────────────────────────────────
    private VBox buildBottomBar() {
        VBox outerBar = new VBox(0);
        outerBar.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // ── Fouls row ──
        BorderPane foulsBar = new BorderPane();
        foulsBar.setPadding(new Insets(10, 20, 6, 20));

        VBox homeF = buildFoulBlock(true);
        homeF.setAlignment(Pos.CENTER_LEFT);
        foulsBar.setLeft(homeF);
        BorderPane.setAlignment(homeF, Pos.CENTER_LEFT);

        Label foulsLbl = new Label("FOULS");
        foulsLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        foulsLbl.setTextFill(Color.web(TEXT_SEC));
        foulsLbl.setAlignment(Pos.CENTER);
        foulsBar.setCenter(foulsLbl);
        BorderPane.setAlignment(foulsLbl, Pos.CENTER);

        VBox awayF = buildFoulBlock(false);
        awayF.setAlignment(Pos.CENTER_RIGHT);
        foulsBar.setRight(awayF);
        BorderPane.setAlignment(awayF, Pos.CENTER_RIGHT);

        // ── Divider ──
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        // ── Cards row ──
        BorderPane cardsBar = new BorderPane();
        cardsBar.setPadding(new Insets(6, 20, 10, 20));

        VBox homeCards = buildCardBlock(true);
        homeCards.setAlignment(Pos.CENTER_LEFT);
        cardsBar.setLeft(homeCards);
        BorderPane.setAlignment(homeCards, Pos.CENTER_LEFT);

        Label cardsLbl = new Label("CARDS");
        cardsLbl.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        cardsLbl.setTextFill(Color.web(TEXT_SEC));
        cardsLbl.setAlignment(Pos.CENTER);
        cardsBar.setCenter(cardsLbl);
        BorderPane.setAlignment(cardsLbl, Pos.CENTER);

        VBox awayCards = buildCardBlock(false);
        awayCards.setAlignment(Pos.CENTER_RIGHT);
        cardsBar.setRight(awayCards);
        BorderPane.setAlignment(awayCards, Pos.CENTER_RIGHT);

        outerBar.getChildren().addAll(foulsBar, div, cardsBar);
        return outerBar;
    }

    private VBox buildCardBlock(boolean isHome) {
        Label yellowIcon = new Label("🟨");
        yellowIcon.setFont(Font.font(20));

        Label yellowVal = new Label(String.valueOf(isHome ? data.getHomeYellow() : data.getAwayYellow()));
        yellowVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 28));
        yellowVal.setTextFill(Color.web("#FFD700"));
        yellowVal.setMinWidth(30);
        yellowVal.setAlignment(Pos.CENTER);
        if (isHome)
            data.homeYellowProperty().addListener((o, a, b) -> yellowVal.setText(b.toString()));
        else
            data.awayYellowProperty().addListener((o, a, b) -> yellowVal.setText(b.toString()));

        Button yMinus = cardBtn("−", "#444444"), yPlus = cardBtn("+", "#444444");
        yMinus.setOnAction(e -> {
            if (isHome)
                data.setHomeYellow(data.getHomeYellow() - 1);
            else
                data.setAwayYellow(data.getAwayYellow() - 1);
        });
        yPlus.setOnAction(e -> {
            if (isHome)
                data.setHomeYellow(data.getHomeYellow() + 1);
            else
                data.setAwayYellow(data.getAwayYellow() + 1);
        });

        Label redIcon = new Label("🟥");
        redIcon.setFont(Font.font(20));

        Label redVal = new Label(String.valueOf(isHome ? data.getHomeRed() : data.getAwayRed()));
        redVal.setFont(Font.font("Helvetica", FontWeight.BOLD, 28));
        redVal.setTextFill(Color.web("#FF3B3B"));
        redVal.setMinWidth(30);
        redVal.setAlignment(Pos.CENTER);
        if (isHome)
            data.homeRedProperty().addListener((o, a, b) -> redVal.setText(b.toString()));
        else
            data.awayRedProperty().addListener((o, a, b) -> redVal.setText(b.toString()));

        Button rMinus = cardBtn("−", "#444444"), rPlus = cardBtn("+", "#444444");
        rMinus.setOnAction(e -> {
            if (isHome)
                data.setHomeRed(data.getHomeRed() - 1);
            else
                data.setAwayRed(data.getAwayRed() - 1);
        });
        rPlus.setOnAction(e -> {
            if (isHome)
                data.setHomeRed(data.getHomeRed() + 1);
            else
                data.setAwayRed(data.getAwayRed() + 1);
        });

        HBox yellowRow = new HBox(4, yellowIcon, yMinus, yellowVal, yPlus);
        yellowRow.setAlignment(Pos.CENTER_LEFT);
        HBox redRow = new HBox(4, redIcon, rMinus, redVal, rPlus);
        redRow.setAlignment(Pos.CENTER_LEFT);
        HBox both = new HBox(16, yellowRow, redRow);
        both.setAlignment(Pos.CENTER_LEFT);

        VBox block = new VBox(0, both);
        block.setAlignment(Pos.CENTER_LEFT);
        return block;
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

        Button minus = smallBtn("−", "#444444"), plus = smallBtn("+", "#444444"), reset = smallBtn("RESET", "#444444");
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
        VBox block = new VBox(2, row);
        block.setAlignment(Pos.CENTER_LEFT);
        return block;
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
        settingsStage.setTitle("⚙  Settings");
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
        Label header = new Label("⚙  SETTINGS");
        header.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));
        header.setTextFill(Color.web(GOLD));
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        Label copyright = new Label("© 2026 JDT Futsal Scoreboard. All rights reserved.");
        copyright.setFont(Font.font("Helvetica", 11));
        copyright.setTextFill(Color.web("rgba(255,255,255,0.25)"));
        copyright.setAlignment(Pos.CENTER);
        copyright.setMaxWidth(Double.MAX_VALUE);
        copyright.setPadding(new Insets(10, 0, 4, 0));
        VBox profileCard = buildProfileCard();
        root.getChildren().addAll(header, hRule(), profileCard, hRule(),
                buildMatchInfoCard(), buildTeamCard(true), buildTeamCard(false),
                buildTimerSettingsCard(), buildDisplayCard(), buildMatchControlCard(), copyright);
        return root;
    }

    private VBox buildProfileCard() {
        VBox card = card("💾  SETTINGS PROFILES");
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
        Button saveBtn = bigBtn("💾  Save", "#2EA043", null);
        saveBtn.setOnAction(e -> {
            String n = profileNameField.getText().trim();
            if (n.isEmpty())
                return;
            SettingsManager.save(data, n);
            refreshList.run();
            saveBtn.setText("✓  Saved!");
            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), ev -> saveBtn.setText("💾  Save")))
                    .play();
        });
        Button loadBtn = bigBtn("📂  Load", "#1F6FEB", null);
        loadBtn.setOnAction(e -> {
            String n = profileNameField.getText().trim();
            if (n.isEmpty())
                return;
            SettingsManager.load(data, n);
            // Refresh image labels to reflect loaded profile
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
            loadBtn.setText("✓  Loaded!");
            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), ev -> loadBtn.setText("📂  Load")))
                    .play();
        });
        Button deleteBtn = bigBtn("🗑  Delete", "#8B0000", null);
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
        VBox card = card("📋  MATCH INFO");
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
        Button llBtn = actionBtn("📁  Choose League Logo"), llClear = actionBtn("✕  Clear");
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
        VBox card = card(isHome ? "🏠  HOME TEAM" : "✈  AWAY TEAM");
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
        Button logoBtn = actionBtn("📁  Choose Logo"), clearLogo = actionBtn("✕  Clear");
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
        VBox card = card("⏱  TIMER SETTINGS");
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
        card.getChildren().addAll(formRow("Mode:", new HBox(12, cdRb, cuRb)), formRow("Duration (min):", durSpin));
        return card;
    }

    private VBox buildDisplayCard() {
        VBox card = card("🖥  DISPLAY");
        ComboBox<String> resBox = new ComboBox<>(FXCollections.observableArrayList("720p  (1280×720)",
                "1080p (1920×1080)", "1440p (2560×1440)", "4K    (3840×2160)"));
        resBox.setValue("720p  (1280×720)");
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
        Button bgBtn = actionBtn("📁  Choose Background"), bgClear = actionBtn("✕  Clear");
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
        Slider opSlider = new Slider(0.0, 1.0, data.getBgOpacity());
        opSlider.setStyle("-fx-accent:" + GOLD + ";");
        opSlider.valueProperty().addListener((o, a, b) -> data.setBgOpacity(b.doubleValue()));
        Button fsBtn = bigBtn("⛶  Fullscreen Display (F11)", BLUE, null);
        fsBtn.setMaxWidth(Double.MAX_VALUE);
        fsBtn.setOnAction(e -> displayStage.setFullScreen(!displayStage.isFullScreen()));
        Button secondBtn = actionBtn("🖥  Move to Second Screen / Projector");
        secondBtn.setMaxWidth(Double.MAX_VALUE);
        secondBtn.setOnAction(e -> moveToSecondScreen());
        VBox bgBox = new VBox(4, new HBox(8, bgBtn, bgClear), bgSelected);
        card.getChildren().addAll(formRow("Resolution:", resBox), formRow("Background:", bgBox),
                formRow("BG Opacity:", opSlider), fsBtn, secondBtn);
        return card;
    }

    private VBox buildMatchControlCard() {
        VBox card = card("⚠  MATCH CONTROL");
        Button nextRound = bigBtn("→  Next Round  (resets timer & fouls)", BLUE, null);
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
        Button reset = bigBtn("↺  Full Match Reset", RED_BTN, null);
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
        tf.setStyle(
                "-fx-background-color:#1C1C1C;" +
                        "-fx-text-fill:white;" +
                        "-fx-prompt-text-fill:rgba(255,255,255,0.75);" +
                        "-fx-border-color:rgba(255,255,255,0.12);" +
                        "-fx-border-radius:6;" +
                        "-fx-background-radius:6;" +
                        "-fx-padding:7 10;" +
                        "-fx-font-size:13;");
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
        b.setStyle(
                "-fx-background-color:#1C1C1C;-fx-text-fill:white;-fx-font-size:13;-fx-padding:7 14;-fx-background-radius:6;-fx-border-color:rgba(255,255,255,0.12);-fx-border-radius:6;-fx-cursor:hand;");
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
                + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:22;-fx-min-width:84;-fx-min-height:54;-fx-background-radius:10;-fx-cursor:hand;");
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

    /** Creates the small status label shown below an image-picker row. */
    private Label imagePickLabel(String currentPath) {
        boolean has = currentPath != null && !currentPath.isEmpty();
        Label lbl = new Label(has ? "✓  " + new File(currentPath).getName() : "No image selected");
        lbl.setFont(Font.font("Helvetica", 11));
        lbl.setTextFill(has ? Color.web(GREEN) : Color.web("rgba(255,255,255,0.35)"));
        lbl.setWrapText(true);
        return lbl;
    }

    private void setImagePickLabelSelected(Label lbl, String filename) {
        lbl.setText("✓  " + filename);
        lbl.setTextFill(Color.web(GREEN));
    }

    private void setImagePickLabelCleared(Label lbl) {
        lbl.setText("No image selected");
        lbl.setTextFill(Color.web("rgba(255,255,255,0.35)"));
    }

    private Alert confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Confirm");
        a.setHeaderText(null);
        return a;
    }
}