package org.zkforge.timeplot.geometry;

public class LogarithmicValueGeometry implements ValueGeometry {

	public String toString() {
		// TODO Auto-generated method stub
		return "LogarithmicValueGeometry";
	}

	private String axisColor;

	private String gridColor = "#000000";

	private float gridLineWidth = (float) 0.5;

	private String axisLabelsPlacement = "left";

	private int gridSpacing = 50;

	private String gridType = "short";

	private int gridShortSize = 40;

	private int min = 0;

	private int max = 110;

	public String getAxisColor() {
		return axisColor;
	}

	public void setAxisColor(String axisColor) {
		this.axisColor = axisColor;
	}

	public String getAxisLabelsPlacement() {
		return axisLabelsPlacement;
	}

	public void setAxisLabelsPlacement(String axisLabelsPlacement) {
		this.axisLabelsPlacement = axisLabelsPlacement;
	}

	public String getGridColor() {
		return gridColor;
	}

	public void setGridColor(String gridColor) {
		this.gridColor = gridColor;
	}

	public float getGridLineWidth() {
		return gridLineWidth;
	}

	public void setGridLineWidth(float gridLineWidth) {
		this.gridLineWidth = gridLineWidth;
	}

	public int getGridShortSize() {
		return gridShortSize;
	}

	public void setGridShortSize(int gridShortSize) {
		this.gridShortSize = gridShortSize;
	}

	public int getGridSpacing() {
		return gridSpacing;
	}

	public void setGridSpacing(int gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	public String getGridType() {
		return gridType;
	}

	public void setGridType(String gridType) {
		this.gridType = gridType;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getValueGeometryId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
