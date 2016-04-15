package sqlartan.view;

import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.core.Table;
import sqlartan.core.util.IterableStream;
import java.sql.SQLException;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	private Sqlartan sqlartan;

	public Database dataBase;

	private ObservableList<testClass> rows = FXCollections.observableArrayList();


	public void setApp(Sqlartan sqlartan)
	{
		this.sqlartan = sqlartan;
	}

	@FXML
	private TreeView<String> treeView;

	@FXML
	private TableView table;

	@FXML
	private void initialize() throws SQLException {

		dataBase = new Database("testdb.db");

		test();
	}


	void test() throws SQLException {
		TreeItem<String> mainTreeItem = new TreeItem<String> ("Bases de données"); // Hidden
		treeView.setShowRoot(false);

		TreeItem<String> dbTreeItem1 =new TreeItem<String>(dataBase.name());
		mainTreeItem.getChildren().add(dbTreeItem1);



		for (Table table : dataBase.tables())
		{
			dbTreeItem1.getChildren().add(new TreeItem<String>(table.name()));
		}

		TreeItem<String> dbTreeItem2 =new TreeItem<String>("Une autre base de données");


		treeView.setRoot(mainTreeItem);
		mainTreeItem.getChildren().add(dbTreeItem2);

		mainTreeItem.setExpanded(true);


		dbTreeItem2.getChildren().add(new TreeItem<String>("Une table"));
		dbTreeItem2.getChildren().add(new TreeItem<String>("Une vue"));



	}

	void test2()
	{
		table.setItems(rows);

		testClass tmp = rows.get(0);

		for (int i = 0; i < 10; ++i)
		{
			TableColumn<testClass, String> coll = new  TableColumn<testClass, String>();
			table.getColumns().add(coll);
			coll.setCellValueFactory(cellDate->cellDate.getValue().tab.get(i));

		}



	}

}
