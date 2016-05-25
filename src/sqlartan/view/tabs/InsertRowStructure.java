package sqlartan.view.tabs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
	BooleanProperty nulle;




	public InsertRowStructure(Column column){
		name = new SimpleStringProperty(column.name());
		type = new SimpleStringProperty(column.type());
		value = new SimpleStringProperty();
		nulle = new SimpleBooleanProperty();
		nulle.addListener((observable, oldValue, newValue) -> {
			if (newValue)
			{
				value.setValue(null);
			}
		});

		value.addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				nulle.setValue(false);
			}
		});

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
