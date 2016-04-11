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
		TreeItem<String> mainTreeItem = new TreeItem<String> ("Bases de données");
		TreeItem<String> dbTreeItem1 =new TreeItem<String>("Une base de données");
		TreeItem<String> dbTreeItem2 =new TreeItem<String>("Une autre base de données");


		treeView.setRoot(mainTreeItem);
		mainTreeItem.getChildren().add(dbTreeItem1);
		mainTreeItem.getChildren().add(dbTreeItem2);

		mainTreeItem.setExpanded(true);


		dbTreeItem1.getChildren().add(new TreeItem<String>("Une table"));
		dbTreeItem1.getChildren().add(new TreeItem<String>("Une vue"));


		dbTreeItem2.getChildren().add(new TreeItem<String>("Une table"));
		dbTreeItem2.getChildren().add(new TreeItem<String>("Une vue"));
		treeView.setShowRoot(false);



	}

}
