package org.zkforge.timeplot.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.zkforge.json.simple.JSONObject;

public class PlotData {
	private final static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.US);

	private static int count = 0;

	private int id = count++;

	private Date time = new Date();

	private float value = 0;

	public String toString() {
		// TODO Auto-generated method stub
		JSONObject json = new JSONObject();
		json.put("id",String.valueOf( id));
		String formattedTime = sdf.format(time);
		json.put("time", formattedTime);
		json.put("value",String.valueOf( value));
		return json.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

}
