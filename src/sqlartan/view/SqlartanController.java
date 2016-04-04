package sqlartan.view;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	@FXML
	private TreeView<String> treeView;

	@FXML
	private void initialize()
	{
		test();
	}


	void test()
	{
		TreeItem<String> mainTreeItem = new TreeItem<String> ("Une base de donnee");
		treeView.setRoot(mainTreeItem);

		mainTreeItem.setExpanded(true);

		mainTreeItem.getChildren().add(new TreeItem<String>("Une table"));
		mainTreeItem.getChildren().add(new TreeItem<String>("Une vue"));

	}

}
