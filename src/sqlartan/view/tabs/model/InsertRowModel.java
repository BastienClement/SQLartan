package sqlartan.view.tabs.model;


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
 * Projet : SQLartan
 * Créé le 21.05.16.
 *
 * @author Julien leroy
 *
 * TODO
 */
public class InsertRowModel {
	public final StringProperty name;
	public final StringProperty type;
	public final StringProperty value;
	public final BooleanProperty nullable;
	private final Type typed;

	public InsertRowModel(Column column) {
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
	 * Make an object table with the good typs for the sql insertion
	 *
	 * @param liste
	 * @return the object table
	 * @throws Exception
	 */
	public static Object[] toArray(ObservableList<InsertRowModel> liste) throws Exception {
		List<Object> lk = new LinkedList<>();

		for (InsertRowModel irs : liste) {
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
