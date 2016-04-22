package sqlartan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sqlartan.core.Database;
import sqlartan.view.SqlartanController;

public class Sqlartan extends Application{

	private Stage primaryStage;
	private BorderPane mainLayout;

	SqlartanController controller;

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
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("SQLartan");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Sqlartan.class.getResource("view/Sqlartan.fxml"));
		mainLayout = loader.load();

		primaryStage.setScene(new Scene(mainLayout));
		primaryStage.getIcons().add(new Image("sqlartan/icon.png"));
		primaryStage.show();

		controller = loader.getController();
		controller.setApp(this);

	}

	public static void main(String[] args) {
		launch(args);
	}
}
