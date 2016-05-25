package sqlartan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;

public class Sqlartan extends Application {

	private static Sqlartan instance;
	private Stage primaryStage;
	private BorderPane mainLayout;
	private SqlartanController controller;


	/**
	 * Get the instance
	 * @return
	 */
	public static Sqlartan getInstance() {
		if (instance == null) instance = new Sqlartan();
		return instance;
	}

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}


	/**
	 * Return the main controler
	 * @return SqlartanController
	 */
	public SqlartanController getController() {
		return controller;
	}
	/**
	 * The main entry point for all JavaFX applications.
	 * The start method is called after the init method has returned,
	 * and after the system is ready for the application to begin running.
	 *
	 * <p>
	 * NOTE: This method is called on the JavaFX Application Thread.
	 * </p>
	 *
	 * @param primaryStage the primary stage for this application, onto which
	 *                     the application scene can be set. The primary stage will be embedded in
	 *                     the browser if the application was launched as an applet.
	 *                     Applications may create other stages, if needed, but they will not be
	 *                     primary stages and will not be embedded in the browser.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
			Platform.runLater(() -> {
				Popup.error("Something went wrong", throwable.getMessage());
				throwable.printStackTrace();
			});
		});

		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("SQLartan");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Sqlartan.class.getResource("view/Sqlartan.fxml"));
		mainLayout = loader.load();

		primaryStage.setScene(new Scene(mainLayout));
		primaryStage.getIcons().add(new Image("sqlartan/assets/icon.png"));
		primaryStage.setMinHeight(640);
		primaryStage.setMinWidth(1135);
		primaryStage.show();

		controller = loader.getController();
		controller.setApp(this);
		instance = this;

	}
	public BorderPane getMainLayout() { return mainLayout; }
	public Stage getPrimaryStage() {
		return primaryStage;
	}
}
