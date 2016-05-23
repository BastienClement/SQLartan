package sqlartan.view.tabs;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sqlartan.core.Column;

/**
 * Created by julien on 21.05.16.
 */
public class InsertRowStructure {
	StringProperty name;
	StringProperty type;
	StringProperty value;


	public InsertRowStructure(Column column){
		this.name = new SimpleStringProperty(column.name());
		this.type = new SimpleStringProperty(column.type());
		this.value = new SimpleStringProperty();

	}

	public void setValue(String str){
		value = new SimpleStringProperty(str);
	}
}
