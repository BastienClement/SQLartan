package sqlartan.core;

public class ResultColumn extends GeneratedColumn {
	private Result result;

	ResultColumn(Result result, Properties props) {
		super(props);
		this.result = result;
	}
}
