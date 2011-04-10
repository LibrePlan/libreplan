package org.zkforge.timeplot.operator;

import org.zkforge.json.simple.JSONObject;

/**
 * @author gwx
 * 
 */
public class AverageOperator implements Operator {

	public AverageOperator() {
		params.put("size", new Integer(size));
	}

	private int size = 3;

	private JSONObject params = new JSONObject();

	public String getOperator() {
		//return "average= function(data, params)" +"{"+ "var size = (\"size\" in params) ? params.size : 30; var result = Timeplot.Math.movingAverage(data.values, size); return result;"+" }";
		return "Timeplot.Operator.average";
	}

	public String getParams() {
		return params.toString();

	}

	public void setSize(int size) {
		this.size = size;
		params.put("size", new Integer(size));
	}

	public int getSize() {
		return size;
	}
}
