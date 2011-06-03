package org.zkforge.timeplot.geometry;

public class DefaultTimeGeometry implements TimeGeometry {

	private static int count = 0;

	private int timeGeometryId = count++;//readonly

	private String axisColor;

	private String gridColor = "#000000";

	private float gridLineWidth = (float) 0.5;

	private String axisLabelsPlacement = "top";

	private int gridStep = 20;

	private int gridStepRange = 20;

	private int min = 0;

	private int max = 100;

	private String timeValuePosition = "bottom";

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

	public int getGridStep() {
		return gridStep;
	}

	public void setGridStep(int gridStep) {
		this.gridStep = gridStep;
	}

	public int getGridStepRange() {
		return gridStepRange;
	}

	public void setGridStepRange(int gridStepRange) {
		this.gridStepRange = gridStepRange;
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

	public String getTimeValuePosition() {
		return timeValuePosition;
	}

	public void setTimeValuePosition(String timeValuePosition) {
		this.timeValuePosition = timeValuePosition;
	}

	public int getTimeGeometryId() {
		return timeGeometryId;
	}
}
