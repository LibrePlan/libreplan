package org.zkforge.timeplot;

import java.util.Date;

import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkforge.timeplot.impl.TimeplotComponent;
import org.zkoss.lang.Objects;
import org.zkoss.xml.HTMLs;

public class Timeplot extends TimeplotComponent {

	private String _height = "150px";// default

	private String _width = "100%";// default



	public String getInnerAttrs() {
		final String attrs = super.getInnerAttrs();
		final StringBuffer sb = new StringBuffer(64);
		if (attrs != null) {
			sb.append(attrs);
		}

		
		return sb.toString();

	}

	public String getHeight() {
		return _height;
	}

	public void setHeight(String height) {
		if (!Objects.equals(_height, height)) {
			_height = height;
			smartUpdate("z.height", height);
		}
	}

	public String getWidth() {
		return _width;

	}

	public void setWidth(String width) {
		if (!Objects.equals(_width, width)) {
			_width = width;
			smartUpdate("z.width", width);
			// invalidate();
		}
	}


}
