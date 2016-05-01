package sqlartan.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import java.io.IOException;

/**
 * Projet : SQLartan
 * Créé le 01.05.2016.
 *
 * @author Adriano Ruberto
 */
public class TabsController {

	/**
	 * @param loader
	 * @param tab
	 */
	protected void addPane(FXMLLoader loader, Pane tab){
		try {
			Pane pane = loader.load();

			tab.getChildren().add(pane);

			pane.prefHeightProperty().bind(tab.heightProperty());
			pane.prefWidthProperty().bind(tab.widthProperty());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
