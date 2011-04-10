package org.zkforge.timeplot.geometry;

public interface TimeGeometry {
	public String getAxisColor();

	public void setAxisColor(String axisColor);

	public String getAxisLabelsPlacement();

	public void setAxisLabelsPlacement(String axisLabelsPlacement);

	public String getGridColor();

	public void setGridColor(String gridColor);

	public float getGridLineWidth();

	public void setGridLineWidth(float gridLineWidth);

	public int getGridStep();

	public void setGridStep(int gridStep);

	public int getGridStepRange();

	public void setGridStepRange(int gridStepRange);

	public int getMax();

	public void setMax(int max);

	public int getMin();

	public void setMin(int min);

	public String getTimeValuePosition();

	public void setTimeValuePosition(String timeValuePosition);

	public int getTimeGeometryId();
}
