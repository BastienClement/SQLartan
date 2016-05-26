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
	private BooleanProperty nulle;
	private Type typeT;




	public InsertRowStructure(Column column){

		name = new SimpleStringProperty(column.name());
		type = new SimpleStringProperty(column.type());
		value = new SimpleStringProperty();
		nulle = new SimpleBooleanProperty();
		typeT = column.affinity().type;


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

	/**
	 * Value property getter
	 *
	 * @return property
	 */
	public StringProperty valueProperty(){
		return value;
	}


	/**
	 * Name property getter
	 *
	 * @return property
	 */
	public StringProperty nameProperty(){
		return name;
	}

	/**
	 * Type property getter
	 *
	 * @return property
	 */
	public StringProperty typeProperty(){
		return type;
	}


	/**
	 * Nulle property getter
	 *
	 * @return property
	 */
	public BooleanProperty nulleProperty(){
		return nulle;
	}






	/**
	 * Make an object table with the good typs for the sql insertion
	 *
	 * @param liste
	 * @return  the object table
	 * @throws Exception
	 */
	public static Object[] getAsArray(ObservableList<InsertRowStructure> liste) throws Exception{
		List<Object> lk = new LinkedList<>();

		for (InsertRowStructure irs: liste){
			Object obj;

			switch (irs.typeT){

				case Integer:
					obj = new Integer(irs.value.getValue());
					break;
				case Real:
					obj = new Double(irs.value.getValue());
					break;
				case Text:
					obj = irs.value.getValue();
					break;
				default:
					obj = "NULL";
					break;
			}

			lk.add(obj);
		}
		return lk.toArray();
	}
}
