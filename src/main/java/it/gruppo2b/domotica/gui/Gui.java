package it.gruppo2b.domotica.gui;

import it.gruppo2b.domotica.client.DisKinClient;
import it.gruppo2b.domotica.server.DisKinServer;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Gui extends Application {

    private ProgressBar progressBar;
    private Label progressLabel;
    private Label titleLabel;

    @Override
    public void start(Stage primaryStage) {
        Image bgImage = new Image(getClass().getResource("/images/serenyaLogo.png").toExternalForm(), 600, 350, false, true);
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(600);
        bgView.setFitHeight(350);
        bgView.setPreserveRatio(false);

        titleLabel = new Label("SERENYA");
        titleLabel.setTextFill(Color.web("#FFFFFF"));
        titleLabel.setFont(Font.font("Segoe UI Light", 64));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #55C1FF;");

        progressLabel = new Label("Caricamento... 0%");
        progressLabel.setTextFill(Color.WHITE);
        progressLabel.setFont(Font.font("Segoe UI", 14));

        VBox contentBox = new VBox(25, titleLabel, progressBar, progressLabel);
        contentBox.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(bgView, contentBox);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 600, 350);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        startLoading(primaryStage);
    }

    private void startLoading(Stage splashStage) {
        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Connessione al database...");
                simulateDatabaseConnection(20);

                updateMessage("Inizializzazione moduli...");
                simulateDatabaseConnection(40);

                updateMessage("Verifica sensori...");
                simulateDatabaseConnection(70);

                updateMessage("Avvio interfaccia...");
                simulateDatabaseConnection(100);

                return null;
            }
        };

        progressBar.progressProperty().bind(loadingTask.progressProperty());
        progressLabel.textProperty().bind(loadingTask.messageProperty());

        loadingTask.setOnSucceeded(e -> fadeOutSplash(splashStage));

        new Thread(loadingTask).start();
    }

    private void simulateDatabaseConnection(int targetProgress) throws InterruptedException {
        for (int i = 0; i <= targetProgress; i++) {
            Thread.sleep(30);
            double progress = i / 100.0;
            Platform.runLater(() -> progressBar.setProgress(progress));
            Platform.runLater(() -> progressLabel.setText("Caricamento... " + (int) (progress * 100) + "%"));
        }
    }

    private void fadeOutSplash(Stage splashStage) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), splashStage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            splashStage.close();
            showMainApp();
        });
        fadeOut.play();
    }

    private void showMainApp() {
        Stage mainStage = new Stage();

        Image bgImage = new Image(getClass().getResource("/images/sfondoBase.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        Label mainLabel = new Label("Benvenuto in SERENYA!");
        mainLabel.setFont(Font.font("Segoe UI", 28));
        mainLabel.setTextFill(Color.web("#E0F7FF"));

        Label subLabel = new Label("Sistema Multiclient / Multiserver attivo");
        subLabel.setFont(Font.font("Segoe UI", 16));
        subLabel.setTextFill(Color.web("#B0D8FF"));

        Button btnClient = styledNeoButton("Configura Client");
        Button btnServer = styledNeoButton("Configura Server");
        Button btnBoth = styledNeoButton("Configura Client + Server");

        HBox buttons = new HBox(20, btnClient, btnServer, btnBoth);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(12, mainLabel, subLabel, buttons);
        content.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(bgView, content);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 1280, 800);
        bgView.fitWidthProperty().bind(scene.widthProperty());
        bgView.fitHeightProperty().bind(scene.heightProperty());

        mainStage.setScene(scene);
        mainStage.setTitle("SERENYA - Sistema Multiclient/Multiserver");
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(600);
        mainStage.show();

        btnClient.setOnAction(e -> openClientWindow());
        btnServer.setOnAction(e -> openServerWindow());
        btnBoth.setOnAction(e -> openBothWindow());
    }

    private Button styledNeoButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Segoe UI Semibold", 18));
        b.setTextFill(Color.web("#E6F7FF"));

        b.setStyle("" +
                "-fx-background-radius: 18;" +
                "-fx-background-color: rgba(0,0,0,0.35);" +
                "-fx-padding: 14 30 14 30;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 2;" +
                "-fx-border-color: linear-gradient(#4FFBDF, #2B95FF);" +
                "-fx-effect: dropshadow(gaussian, rgba(40,160,255,0.45), 16, 0.4, 0, 0);" +
                "-fx-cursor: hand;"
        );

        Timeline breathing = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(b.opacityProperty(), 1.0),
                        new KeyValue(b.scaleXProperty(), 1.0),
                        new KeyValue(b.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.seconds(1.8),
                        new KeyValue(b.opacityProperty(), 0.88),
                        new KeyValue(b.scaleXProperty(), 1.03),
                        new KeyValue(b.scaleYProperty(), 1.03)
                )
        );
        breathing.setAutoReverse(true);
        breathing.setCycleCount(Animation.INDEFINITE);
        breathing.play();

        b.setOnMouseEntered(e -> {
            b.setStyle("" +
                    "-fx-background-radius: 18;" +
                    "-fx-background-color: rgba(0, 40, 70, 0.55);" +
                    "-fx-padding: 14 32 14 32;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-width: 3;" +
                    "-fx-border-color: linear-gradient(#7BFFFF, #4FC3FF);" +
                    "-fx-effect: dropshadow(gaussian, rgba(90,225,255,0.85), 28, 0.65, 0, 0);" +
                    "-fx-cursor: hand;"
            );
        });

        b.setOnMouseExited(e -> {
            b.setStyle("" +
                    "-fx-background-radius: 18;" +
                    "-fx-background-color: rgba(0,0,0,0.35);" +
                    "-fx-padding: 14 30 14 30;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-color: linear-gradient(#4FFBDF, #2B95FF);" +
                    "-fx-effect: dropshadow(gaussian, rgba(40,160,255,0.45), 16, 0.4, 0, 0);" +
                    "-fx-cursor: hand;"
            );
        });

        b.setOnMousePressed(e -> {
            b.setScaleX(0.92);
            b.setScaleY(0.92);
            b.setEffect(new DropShadow(25, Color.web("#34D1FF")));
        });

        b.setOnMouseReleased(e -> {
            b.setScaleX(1.0);
            b.setScaleY(1.0);
            b.setEffect(new DropShadow(16, Color.web("rgba(40,160,255,0.45)")));
        });

        return b;
    }

    private void openClientWindow() {
        Stage win = new Stage();
        win.setTitle("SERENYA - Client");
        Image bg = new Image(getClass().getResource("/images/sfondoBase.png").toExternalForm());
        ImageView bgView = new ImageView(bg);
        bgView.setPreserveRatio(false);

        TextArea console = createConsoleArea();
        console.appendText("Console client\n");

        TextField input = new TextField();
        input.setPromptText("Messaggio da inviare al server...");
        Button send = styledNeoButton("Invia");
        HBox sendBox = new HBox(10, input, send);
        sendBox.setAlignment(Pos.CENTER);

        VBox right = new VBox(10, new Label("Output"), console, sendBox);
        right.setPadding(new Insets(10));
        right.setPrefWidth(700);

        StackPane root = new StackPane(bgView, right);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 900, 600);
        bgView.fitWidthProperty().bind(scene.widthProperty());
        bgView.fitHeightProperty().bind(scene.heightProperty());

        win.setScene(scene);
        win.show();

        DisKinClient client = new DisKinClient("127.0.0.1", 5000, 5001);
        client.setListener((from, message) -> Platform.runLater(() -> console.appendText("[" + from + "] " + message + "\n")));
        client.connectTCP();
        client.initializeUDP();

        send.setOnAction(e -> {
            String txt = input.getText();
            if (txt == null || txt.isBlank()) return;
            client.sendTCP(txt);
            input.clear();
        });

        win.setOnCloseRequest(e -> client.close());
    }

    private void openServerWindow() {
        Stage win = new Stage();
        win.setTitle("SERENYA - Server");
        Image bg = new Image(getClass().getResource("/images/sfondoBase.png").toExternalForm());
        ImageView bgView = new ImageView(bg);
        bgView.setPreserveRatio(false);

        TextArea console = createConsoleArea();
        console.appendText("Console server\n");

        VBox right = new VBox(10, new Label("Entrate client (JSON)"), console);
        right.setPadding(new Insets(10));
        right.setPrefWidth(900);

        StackPane root = new StackPane(bgView, right);
        Scene scene = new Scene(root, 1000, 600);
        bgView.fitWidthProperty().bind(scene.widthProperty());
        bgView.fitHeightProperty().bind(scene.heightProperty());

        win.setScene(scene);
        win.show();

        DisKinServer server = new DisKinServer(5000, 5001, 8);
        server.setListener((from, message) -> Platform.runLater(() -> console.appendText("[" + from + "] " + message + "\n")));
        server.start();

        win.setOnCloseRequest(e -> server.stop());
    }

    private void openBothWindow() {
        TextInputDialog dialog = new TextInputDialog("2");
        dialog.setHeaderText("Quanti client vuoi creare automaticamente?");
        dialog.showAndWait().ifPresent(str -> {
            int n;
            try { n = Integer.parseInt(str); } catch (NumberFormatException ex) { n = 1; }

            Stage win = new Stage();
            win.setTitle("SERENYA - Client + Server");
            Image bg = new Image(getClass().getResource("/images/sfondoBase.png").toExternalForm());
            ImageView bgView = new ImageView(bg);
            bgView.setPreserveRatio(false);

            TextArea serverConsole = createConsoleArea();
            serverConsole.setPrefHeight(200);
            serverConsole.appendText("Server console\n");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            List<DisKinClient> clients = new ArrayList<>();
            AtomicInteger col = new AtomicInteger(0);
            for (int i = 0; i < n; i++) {
                TextArea cConsole = createConsoleArea();
                cConsole.setPrefSize(300, 200);
                cConsole.appendText("Client #" + (i+1) + "\n");
                grid.add(cConsole, col.getAndIncrement(), 0);

                DisKinClient client = new DisKinClient("127.0.0.1", 5000, 5001);
                int idx = i+1;
                client.setListener((from, message) -> Platform.runLater(() -> cConsole.appendText("[" + from + "] " + message + "\n")));
                client.connectTCP();
                client.initializeUDP();
                clients.add(client);
            }

            VBox right = new VBox(10, new Label("Server (mini)"), serverConsole);
            right.setPadding(new Insets(10));
            right.setPrefWidth(400);

            BorderPane main = new BorderPane();
            main.setCenter(grid);
            main.setRight(right);

            StackPane root = new StackPane(bgView, main);
            Scene scene = new Scene(root, 1200, 700);
            bgView.fitWidthProperty().bind(scene.widthProperty());
            bgView.fitHeightProperty().bind(scene.heightProperty());

            win.setScene(scene);
            win.show();

            DisKinServer server = new DisKinServer(5000, 5001, 8);
            server.setListener((from, message) -> Platform.runLater(() -> serverConsole.appendText("[" + from + "] " + message + "\n")));
            server.start();

            win.setOnCloseRequest(e -> {
                server.stop();
                clients.forEach(DisKinClient::close);
            });
        });
    }

    private TextArea createConsoleArea() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setStyle("-fx-control-inner-background:#051220; -fx-font-family: 'Consolas', monospace; -fx-highlight-fill: #55C1FF; -fx-highlight-text-fill: #000000; -fx-text-fill: #E0F7FF;");
        return ta;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
