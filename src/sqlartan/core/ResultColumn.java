package sqlartan.core;

import java.util.*;

/**
 * TODO
 */
public class ResultColumn extends GeneratedColumn {
	/**
	 * References to the Result instance
	 */
	private Result result;

	/**
	 * TODO
	 */
	private int index;

	/**
	 * TODO
	 */
	private Set<ResultColumn> updateKeys;

	ResultColumn(Result result, int index, Properties props) {
		super(props);
		this.result = result;
		this.index = index;
	}

	/**
	 *
	 * @return
	 */
	public int index() {
		return index;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Set<ResultColumn> updateKeys() {
		if (updateKeys == null) {
			updateKeys = new HashSet<>();
			Map<String, ResultColumn> seen = new HashMap<>();

			result.columns().view()
			      .peek(col -> {
				      col.sourceColumn().ifPresent(source -> {
					      seen.put(source.name(), col);
					      if (source.unique()) {
						      updateKeys.add(col);
					      }
				      });
			      })
			      .mapOptional(GeneratedColumn::sourceTable)
			      .distinct()
			      .forEach(table -> {
				      try {
					      table.primaryKey().get().columns().stream()
					           .map(seen::get)
					           .peek(col -> {
						           if (col == null) throw new NoSuchElementException();
					           })
					           .forEach(updateKeys::add);
				      } catch (NoSuchElementException ignored) {}
			      });
		}

		return updateKeys;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ResultColumn) {
			ResultColumn col = (ResultColumn) obj;
			return sourceColumn().equals(col.sourceColumn());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return sourceColumn().map(col -> Objects.hash(ResultColumn.class, col.hashCode()))
		                     .orElseGet(this::hashCode);
	}
}
