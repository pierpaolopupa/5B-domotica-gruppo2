package it.gruppo2b.domotica.gui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Gui extends Application {

    private ProgressBar progressBar;
    private Label progressLabel;
    private Label titleLabel;

    @Override
    public void start(Stage primaryStage) {
        Image bgImage = new Image(
                getClass().getResource("/images/serenyaLogo.png").toExternalForm(),
                600, 350, false, true
        );

        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(600);
        bgView.setFitHeight(350);
        bgView.setPreserveRatio(false);

        titleLabel = new Label("SERENYA");
        titleLabel.setTextFill(Color.web("#FFFF"));
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
            Thread.sleep(40);
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

        Image bgImage = new Image(
                getClass().getResource("/images/sfondoBase.png").toExternalForm()
        );
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        Label mainLabel = new Label("Benvenuto in SERENYA!");
        mainLabel.setFont(Font.font("Segoe UI", 28));
        mainLabel.setTextFill(Color.web("#E0F7FF"));

        Label subLabel = new Label("Sistema Multiclient / Multiserver attivo");
        subLabel.setFont(Font.font("Segoe UI", 16));
        subLabel.setTextFill(Color.web("#B0D8FF"));

        VBox content = new VBox(10, mainLabel, subLabel);
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
    }
}

