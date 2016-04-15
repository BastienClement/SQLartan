package sqlartan.view;

import javafx.beans.property.StringProperty;
import jdk.nashorn.internal.objects.ArrayBufferView;
import java.util.ArrayList;

/**
 * Created by julien on 13.04.16.
 */
public class testClass {
	ArrayList<StringProperty> tab;

	public testClass()
	{
		tab = new ArrayList<StringProperty>();
	}

	public void add(StringProperty sp)
	{
		tab.add(sp);
	}



}
