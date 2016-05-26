package sqlartan.view.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import sqlartan.Sqlartan;
import sqlartan.core.PersistentStructure;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.view.DataTableView;
import sqlartan.view.tabs.struct.TabStructure;
import sqlartan.view.tabs.struct.TableStructure;
import sqlartan.view.util.Popup;
import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Created by julien on 24.05.16.
 */
public abstract class TabsController<T extends TabStructure> {

	@FXML
	protected TableColumn<TableStructure, Number> colNo;
	@FXML
	protected TableColumn<TableStructure, String> colName;
	@FXML
	protected TableColumn<TableStructure, String> colType;
	@FXML
	protected TableColumn<TableStructure, String> colNull;
	@FXML
	protected TableColumn<TableStructure, String> colDefaultValue;
	@FXML
	protected TableColumn<TableStructure, String> colComment;

	protected PersistentStructure<?> structure;

	protected DataTableView dataTableView = new DataTableView();

	@FXML
	protected TabPane tabPane;

	@FXML
	protected Tab displayTab;

	@FXML
	protected Tab structureTab;

	@FXML
	protected Pane sqlPane;

	@FXML
	protected TableView<TableStructure> structureTable;


	@FXML
	protected void initialize() throws IOException {


		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		Pane pane = loader.load();
		sqlPane.getChildren().add(pane);

		pane.prefHeightProperty().bind(sqlPane.heightProperty());
		pane.prefWidthProperty().bind(sqlPane.widthProperty());


		/**
		 * Display the datas from the tableStructures in display tab only when he's active.
		 * Every time a new query is done.
		 */


		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		tabPane.getSelectionModel().clearSelection();

	}


	/**
	 * Display the structure, if the structure can't be displayed, a popup will ask the user if he want to drop it.
	 */
	protected void displayStructure() {
		ObservableList<TableStructure> tableStructures = FXCollections.observableArrayList();

		TableStructure.IDReset();
		try {
			tableStructures.addAll(structure.columns()
			                                .map(TableStructure::new)
			                                .toList());
		} catch (UncheckedSQLException e) {
			Platform.runLater(() -> {
				ButtonType ok = new ButtonType("Yes drop it");
				ButtonType cancel = new ButtonType("Cancel");
				Popup.warning("Error while display structure", e.getMessage(), ok, cancel)
				     .filter(d -> d == ok)
				     .ifPresent(d -> Sqlartan.getInstance().getController().dropStructure(structure));
				Sqlartan.getInstance().getController().selectTreeIndex(0);
			});
		}

		structureTable.setItems(tableStructures);
	}

	/**
	 * Display the data table
	 */
	protected void displayDataTable() {
		displayTab.setContent(dataTableView.getTableView(structure));

	}

	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}

	protected Callback<TableColumn<T, String>, TableCell<T, String>>
	actionButton(String label, BiConsumer<TableCell<T, String>, ActionEvent> action) {
		return param -> new TableCell<T, String>() {
			private Button btn = new Button(label);
			public void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
					setText(null);
				} else {
					btn.setOnAction(event -> action.accept(this, event));
					setGraphic(btn);
					setText(null);
				}
			}
		};
	}
}
