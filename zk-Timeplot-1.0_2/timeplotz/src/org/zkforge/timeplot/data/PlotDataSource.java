package org.zkforge.timeplot.data;

public class PlotDataSource {
	private String dataSourceUri;

	private String separator = ",";

	private int dataSourceColumn = 1;

	public int getDataSourceColumn() {
		return dataSourceColumn;
	}

	public void setDataSourceColumn(int dataSourceColumn) {
		this.dataSourceColumn = dataSourceColumn;
	}

	public String getDataSourceUri() {
		return dataSourceUri;
	}

	public void setDataSourceUri(String dataSourceUri) {
		this.dataSourceUri = dataSourceUri;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
