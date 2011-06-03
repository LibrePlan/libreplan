package org.zkforge.timeplot.geometry;

/**
 * @author gwx
 * 
 */
public interface ValueGeometry {
	public static final String LEFT = "left";

	public static final String RIGHT = "right";

	public static final String TOP = "top";

	public static final String BOTTOM = "bottom";

	public static final String SHORT = "short";

	public String getAxisColor();

	public void setAxisColor(String axisColor);

	public String getAxisLabelsPlacement();

	public void setAxisLabelsPlacement(String axisLabelsPlacement);

	public String getGridColor();

	public void setGridColor(String gridColor);

	public float getGridLineWidth();

	public void setGridLineWidth(float gridLineWidth);

	public int getGridShortSize();

	public void setGridShortSize(int gridShortSize);

	public int getGridSpacing();

	public void setGridSpacing(int gridSpacing);

	public String getGridType();

	public void setGridType(String gridType);

	public int getMax();

	public void setMax(int max);

	public int getMin();

	public void setMin(int min);
	public int getValueGeometryId();
}
