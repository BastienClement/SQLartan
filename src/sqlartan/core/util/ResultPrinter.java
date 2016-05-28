package sqlartan.core.util;

import sqlartan.core.GeneratedColumn;
import sqlartan.core.Result;
import sqlartan.core.ResultColumn;
import sqlartan.core.Row;
import sqlartan.core.stream.ImmutableList;
import java.util.ArrayList;

/**
 * Utility object to print a Result object as an ASCII table.
 */
abstract public class ResultPrinter {
	/**
	 * Prints the given Result object as an ASCII table.
	 *
	 * @param res the result object
	 */
	public static void print(Result res) {
		ImmutableList<ResultColumn> columns = res.columns();

		// Count the number of columns to display
		int column_count = columns.size();
		ArrayList<ArrayList<String>> rows = new ArrayList<>();

		// Add column titles
		ArrayList<String> titles = new ArrayList<>(column_count);
		for (GeneratedColumn col : columns) {
			titles.add(col.name());
		}
		rows.add(titles);

		// Fetch result data
		for (Row row : res) {
			ArrayList<String> values = new ArrayList<>(column_count);
			for (int i = 0; i < column_count; i++) {
				values.add(row.getString());
			}
			rows.add(values);
		}

		// Compute strings length
		int[] lengths = new int[column_count];
		int lengths_sum = 0;

		for (ArrayList<String> row : rows) {
			for (int i = 0; i < column_count; i++) {
				int col_length = row.get(i).length();
				if (col_length > lengths[i]) {
					lengths_sum += col_length - lengths[i];
					lengths[i] = col_length;
				}
			}
		}

		// Display the table
		boolean first = true;
		for (ArrayList<String> row : rows) {
			for (int i = 0; i < column_count; i++) {
				if (i != 0) System.out.print(" | ");
				System.out.print(pad(row.get(i), lengths[i], ' '));
			}

			if (first) {
				first = false;
				System.out.print("\n");
				int width = lengths_sum + 3 * (column_count - 1);
				System.out.print(pad("", width, '-'));
			}

			System.out.print("\n");
		}
	}

	/**
	 * Appends the padding char at the end of the string so that its
	 * lengths is equals to the requested length.
	 *
	 * @param str     the stream to pad
	 * @param width   the target width
	 * @param padding the char to use for padding
	 */
	private static String pad(String str, int width, char padding) {
		if (str.length() == width) return str;

		StringBuilder sb = new StringBuilder(width);
		sb.append(str);

		for (int i = width - str.length(); i > 0; i--) {
			sb.append(padding);
		}

		return sb.toString();
	}
}
