package sqlartan.view.tabs;

import java.io.IOException;

/**
 * Created by julien on 24.05.16.
 */
public class ViewTabsController extends TabsController {

	protected void initialize() throws IOException {
		super.initialize();

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == displayTab) {
				displayTab.setContent(dataTableView.getTableView(structure));
			} else if (newTab == structureTab) {
				displayStructure();
			}
		});
	}
}
