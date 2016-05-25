package sqlartan.view.tabs;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
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


	public TextField getValue(){
		return new TextField(value.getValue());
	}

	public void setValue(TextField tf){
		value = new SimpleStringProperty(tf.getText());
	}

	public void setValue(String str){
		value = new SimpleStringProperty(str);
	}
}
