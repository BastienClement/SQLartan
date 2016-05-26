package sqlartan.view.tabs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sqlartan.core.Column;
import sqlartan.core.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 21.05.16.
 */
public class InsertRowStructure {
	private StringProperty name;
	private StringProperty type;
	private StringProperty value;
	private BooleanProperty nullable;
	private Type typed;


	public InsertRowStructure(Column column) {

		name = new SimpleStringProperty(column.name());
		type = new SimpleStringProperty(column.type());
		value = new SimpleStringProperty();
		nullable = new SimpleBooleanProperty();
		typed = column.affinity().type;


		nullable.addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				value.setValue(null);
			}
		});

		value.addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				nullable.setValue(false);
			}
		});
	}


	/**
	 * Value property getter
	 *
	 * @return property
	 */
	public StringProperty valueProperty() {
		return value;
	}


	/**
	 * Name property getter
	 *
	 * @return property
	 */
	public StringProperty nameProperty() {
		return name;
	}


	/**
	 * Type property getter
	 *
	 * @return property
	 */
	public StringProperty typeProperty() {
		return type;
	}


	/**
	 * Nulle property getter
	 *
	 * @return property
	 */
	public BooleanProperty nullableProperty() {
		return nullable;
	}


	/**
	 * Make an object table with the good typs for the sql insertion
	 *
	 * @param liste
	 * @return the object table
	 * @throws Exception
	 */
	public static Object[] toArray(ObservableList<InsertRowStructure> liste) throws Exception {
		List<Object> lk = new LinkedList<>();

		for (InsertRowStructure irs : liste) {
			Object obj;

			switch (irs.typed) {
				case Integer:
					obj = new Integer(irs.value.getValue());
					break;
				case Real:
					obj = new Double(irs.value.getValue());
					break;
				default:
					obj = irs.value.getValue();
					break;
			}

			lk.add(obj);
		}
		return lk.toArray();
	}
}
