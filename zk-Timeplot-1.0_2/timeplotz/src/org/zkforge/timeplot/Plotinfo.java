package org.zkforge.timeplot;

import java.util.ArrayList;

import org.zkforge.timeline.Timeline;
import org.zkforge.timeline.data.OccurEvent;
import org.zkforge.timeplot.data.PlotData;
import org.zkforge.timeplot.data.PlotDataSource;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkforge.timeplot.impl.TimeplotComponent;
import org.zkforge.timeplot.operator.Operator;
import org.zkoss.lang.Objects;
import org.zkoss.xml.HTMLs;
import org.zkoss.zk.au.out.AuScript;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;

public class Plotinfo extends TimeplotComponent {

	private ListModel _dataModel;

	private ArrayList _dataList = new ArrayList();

	private ListModel _eventModel;

	private ArrayList _eventList = new ArrayList();

	private transient ListDataListener _dataListener;

	private transient ListDataListener _eventListener;

	private String fillColor;



	private String dotColor;

	private String lineColor;

	private float lineWidth = (float) 1.0;

	private float eventLineWidth = (float) 1.0;

	private float dotRadius = (float) 2.0;

	private boolean showValues = false;

	private boolean roundValues = true;

	private int valuesOpacity = 75;

	private int bubbleWidth = 300;

	private int bubbleHeight = 200;
	
	private PlotDataSource pds=null;


	private Operator operator = null;

	private String eventSourceUri=null;

	private ValueGeometry _valueGeometry = null;

	private TimeGeometry _timeGeometry = null;

	public String getInnerAttrs() {
		final String attrs = super.getInnerAttrs();
		final StringBuffer sb = new StringBuffer(64);
		if (attrs != null) {
			sb.append(attrs);
		}
		HTMLs.appendAttribute(sb, "z.pid", getParent().getUuid());
		if (lineColor != null)
			HTMLs.appendAttribute(sb, "z.lineColor", lineColor);
		if (fillColor != null)
			HTMLs.appendAttribute(sb, "z.fillColor", fillColor);
		if (dotColor != null)
			HTMLs.appendAttribute(sb, "z.dotColor", dotColor);
		HTMLs.appendAttribute(sb, "z.lineWidth", String.valueOf(lineWidth));
		HTMLs.appendAttribute(sb, "z.eventLineWidth", String
				.valueOf(eventLineWidth));
		HTMLs.appendAttribute(sb, "z.dotRadius", String.valueOf(dotRadius));
		HTMLs.appendAttribute(sb, "z.showValues", showValues);
		HTMLs.appendAttribute(sb, "z.roundValues", roundValues);
		HTMLs.appendAttribute(sb, "z.valuesOpacity", valuesOpacity);
		HTMLs.appendAttribute(sb, "z.bubbleWidth", bubbleWidth);
		HTMLs.appendAttribute(sb, "z.bubbleHeight", bubbleHeight);

		if (operator != null) {
			HTMLs.appendAttribute(sb, "z.operator", operator.getOperator());
			String params = operator.getParams();
			if (params != null)
				HTMLs.appendAttribute(sb, "z.operatorParams", params);
		}
		if (pds != null){
			HTMLs.appendAttribute(sb, "z.separator", pds.getSeparator());
			HTMLs.appendAttribute(sb, "z.dataSourceColumn", pds.getDataSourceColumn());
			HTMLs.appendAttribute(sb, "z.dataSourceUri",pds.getDataSourceUri());
		}
		if (eventSourceUri != null)
			HTMLs.appendAttribute(sb, "z.eventSourceUri", eventSourceUri);

		if (_valueGeometry != null) {
			HTMLs.appendAttribute(sb, "z.valueGeometryDefined", true);
			HTMLs.appendAttribute(sb, "z.valueGeometryType", _valueGeometry
					.toString());
			HTMLs.appendAttribute(sb, "z.valueGeometry.id", _valueGeometry
					.getValueGeometryId());
			HTMLs.appendAttribute(sb, "z.valueGeometry.axisColor",
					_valueGeometry.getAxisColor());
			HTMLs.appendAttribute(sb, "z.valueGeometry.axisLabelsPlacement",
					_valueGeometry.getAxisLabelsPlacement());
			HTMLs.appendAttribute(sb, "z.valueGeometry.gridColor",
					_valueGeometry.getGridColor());
			HTMLs.appendAttribute(sb, "z.valueGeometry.gridLineWidth", String
					.valueOf(_valueGeometry.getGridLineWidth()));
			HTMLs.appendAttribute(sb, "z.valueGeometry.gridSpacing", String
					.valueOf(_valueGeometry.getGridSpacing()));

			HTMLs.appendAttribute(sb, "z.valueGeometry.gridType",
					_valueGeometry.getGridType());
			HTMLs.appendAttribute(sb, "z.valueGeometry.gridShortSize", String
					.valueOf(_valueGeometry.getGridShortSize()));
			HTMLs.appendAttribute(sb, "z.valueGeometry.min", String
					.valueOf(_valueGeometry.getMin()));
			HTMLs.appendAttribute(sb, "z.valueGeometry.max", String
					.valueOf(_valueGeometry.getMax()));

		}

		if (_timeGeometry != null) {
			HTMLs.appendAttribute(sb, "z.timeGeometryDefined", true);
			HTMLs.appendAttribute(sb, "z.timeGeometry.id", _timeGeometry
					.getTimeGeometryId());
			HTMLs.appendAttribute(sb, "z.timeGeometry.axisColor", _timeGeometry
					.getAxisColor());
			HTMLs.appendAttribute(sb, "z.timeGeometry.axisLabelsPlacement",
					_timeGeometry.getAxisLabelsPlacement());
			HTMLs.appendAttribute(sb, "z.timeGeometry.gridColor", _timeGeometry
					.getGridColor());
			HTMLs.appendAttribute(sb, "z.timeGeometry.gridLineWidth", String
					.valueOf(_timeGeometry.getGridLineWidth()));
			HTMLs.appendAttribute(sb, "z.timeGeometry.gridGridStep", String
					.valueOf(_timeGeometry.getGridStep()));

			HTMLs.appendAttribute(sb, "z.timeGeometry.gridStepRange", String
					.valueOf(_timeGeometry.getGridStepRange()));
			HTMLs.appendAttribute(sb, "z.timeGeometry.min", String
					.valueOf(_timeGeometry.getMin()));
			HTMLs.appendAttribute(sb, "z.timeGeometry.max", String
					.valueOf(_timeGeometry.getMax()));

		}
		return sb.toString();

	}

	public int getBubbleHeight() {
		return bubbleHeight;
	}

	public void setBubbleHeight(int bubbleHeight) {
		if (this.bubbleHeight != bubbleHeight) {
			this.bubbleHeight = bubbleHeight;
			smartUpdate("z.bubbleHeight", this.bubbleHeight);
		}
	}

	public int getBubbleWidth() {
		return bubbleWidth;
	}

	public void setBubbleWidth(int bubbleWidth) {
		if (this.bubbleWidth != bubbleWidth) {
			this.bubbleWidth = bubbleWidth;
			smartUpdate("z.bubbleWidth", this.bubbleWidth);
			//invalidate();
		}
	}


	
	public PlotDataSource getPlotDataSource() {
		return pds;
	}

	public void setPlotDataSource(PlotDataSource pds) {
		if (!Objects.equals(this.pds, pds)) {
			this.pds = pds;
			smartUpdate("z.separator",this.pds.getSeparator());
			smartUpdate("z.dataSourceColumn",this.pds.getDataSourceColumn());
			smartUpdate("z.dataSourceUri", this.pds.getDataSourceUri());
			//invalidate();
		}
	}

	public String getDotColor() {
		return dotColor;
	}

	public void setDotColor(String dotColor) {
		if (!Objects.equals(this.dotColor, dotColor)) {
			this.dotColor = dotColor;
			//System.out.println(this.dotColor);
			 smartUpdate("z.dotColor", this.dotColor);
			//invalidate();
		}
	}

	public float getDotRadius() {
		return dotRadius;
	}

	public void setDotRadius(float dotRadius) {
		if (this.dotRadius != dotRadius) {
			this.dotRadius = dotRadius;
			smartUpdate("z.dotRadius",String.valueOf(this.dotRadius));
			//invalidate();
		}
	}

	public float getEventLineWidth() {
		return eventLineWidth;
	}

	public void setEventLineWidth(float eventLineWidth) {
		if (this.eventLineWidth != eventLineWidth) {
			this.eventLineWidth = eventLineWidth;
			smartUpdate("z.eventLineWidth",String.valueOf(this.eventLineWidth));
		}
	}

	public String getEventSourceUri() {
		return eventSourceUri;
	}

	public void setEventSourceUri(String eventSourceUri) {
		if (!Objects.equals(this.eventSourceUri, eventSourceUri)) {
			this.eventSourceUri = eventSourceUri;
			smartUpdate("z.eventSourceUri", this.eventSourceUri);
			
		}
	}
	
	public void repaint(){
		smartUpdate("z.repaint",true);	
		
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		if (!Objects.equals(this.fillColor, fillColor)) {
			this.fillColor = fillColor;
			smartUpdate("z.fillColor", this.fillColor);
			//invalidate();
		}
	}

	public String getLineColor() {
		return lineColor;
	}

	public void setLineColor(String lineColor) {
		if (!Objects.equals(this.lineColor , lineColor)) {
			this.lineColor = lineColor;
			
			smartUpdate("z.lineColor", this.lineColor);
			//invalidate();
		}
	}

	public float getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(float lineWidth) {
		if (this.lineWidth != lineWidth) {
			this.lineWidth = lineWidth;
			smartUpdate("z.lineWidth",String.valueOf(this.lineWidth));
			//invalidate();
		}
	}

	public boolean isRoundValues() {
		return roundValues;
	}

	public void setRoundValues(boolean roundValues) {
		if (this.roundValues != roundValues) {
			this.roundValues = roundValues;
			smartUpdate("z.roundValues",this.roundValues);
		}
	}

	public boolean isShowValues() {
		return showValues;
	}

	public void setShowValues(boolean showValues) {
		if (this.showValues != showValues) {
			this.showValues = showValues;
			smartUpdate("z.showValues", this.showValues);
			//invalidate();
		}
	}

	public int getValuesOpacity() {
		return valuesOpacity;
	}

	public void setValuesOpacity(int valuesOpacity) {
		if (this.valuesOpacity != valuesOpacity) {
			this.valuesOpacity = valuesOpacity;
			smartUpdate("z.valuesOpacity", this.valuesOpacity);
			//invalidate();
		}
	}

	public void addPlotEvent(OccurEvent oe) {
		response("addPlotEvent" + oe.getId(), new AuScript(this,
				"zkPlotinfo.addPlotEvent(\"" + getUuid() + "\"" + ","
						+ oe.toString() + ")"));
	}

	public void modifyPlotEvent(OccurEvent oe) {
		response("modifyPlotEvent" + oe.getId(), new AuScript(this,
				"zkPlotinfo.modifyPlotEvent(\"" + getUuid() + "\"" + ","
						+ oe.toString() + ")"));
	}

	public void removePlotEvent(OccurEvent oe) {
		response("removePlotEvent" + oe.getId(), new AuScript(this,
				"zkPlotinfo.removePlotEvent(\"" + getUuid() + "\"" + ","
						+ oe.toString() + ")"));
	}

	public void addPlotData(PlotData pd) {
		response("addPlotData" + pd.getId(), new AuScript(this,
				"zkPlotinfo.addPlotData(\"" + getUuid() + "\"" + ","
						+ pd.toString() + ")"));
	}

	public void modifyPlotData(PlotData pd) {
		response("modifyPlotData" + pd.getId(), new AuScript(this,
				"zkPlotinfo.modifyPlotData(\"" + getUuid() + "\"" + ","
						+ pd.toString() + ")"));
	}

	public void removePlotData(PlotData pd) {
		response("removePlotData" + pd.getId(), new AuScript(this,
				"zkPlotinfo.removePlotData(\"" + getUuid() + "\"" + ","
						+ pd.toString() + ")"));
	}

	public ListModel getDataModel() {
		return _dataModel;
	}

	public void setDataModel(ListModel dataModel) {
		if (dataModel == null)
			return;
		if (_dataModel != null)
			_dataModel.removeListDataListener(_dataListener);
		_dataModel = dataModel;

		if (_dataModel != null) {
			_dataListener = new ListDataListener() {
				public void onChange(ListDataEvent event) {
					// TODO Auto-generated method stub
					onListDataChange(event);
				}
			};
			_dataModel.addListDataListener(_dataListener);
		}

	}

	protected void onListDataChange(ListDataEvent event) {

		int lower = event.getIndex0();
		int upper = event.getIndex1();
		switch (event.getType()) {
		case ListDataEvent.INTERVAL_ADDED:
			for (int i = lower; i <= upper; i++) {

				PlotData pd = (PlotData) _dataModel.getElementAt(i);

				this.addPlotData(pd);
				_dataList.add(pd);
			}
			break;
		case ListDataEvent.INTERVAL_REMOVED:
			for (int i = upper; i >= lower; i--) {
				PlotData pd = (PlotData) _dataList.get(i);
				_dataList.remove(i);
				this.removePlotData(pd);
			}
			break;
		case ListDataEvent.CONTENTS_CHANGED:
			for (int i = lower; i <= upper; i++) {
				PlotData pd = (PlotData) _dataModel.getElementAt(i);
				_dataList.set(i, pd);
				this.modifyPlotData(pd);
			}
			break;

		}

	}

	public ListModel getEventModel() {
		return _eventModel;
	}

	public void setEventModel(ListModel eventModel) {
		if (eventModel == null)
			return;
		if (_eventModel != null)
			_eventModel.removeListDataListener(_eventListener);
		_eventModel = eventModel;

		if (_eventModel != null) {
			_eventListener = new ListDataListener() {
				public void onChange(ListDataEvent event) {
					// TODO Auto-generated method stub
					onListEventChange(event);
				}
			};
			_eventModel.addListDataListener(_eventListener);
		}

	}

	protected void onListEventChange(ListDataEvent event) {

		int lower = event.getIndex0();
		int upper = event.getIndex1();
		switch (event.getType()) {
		case ListDataEvent.INTERVAL_ADDED:
			for (int i = lower; i <= upper; i++) {

				OccurEvent oe = (OccurEvent) _eventModel.getElementAt(i);

				this.addPlotEvent(oe);
				_eventList.add(oe);
			}
			break;
		case ListDataEvent.INTERVAL_REMOVED:
			for (int i = upper; i >= lower; i--) {
				OccurEvent oe = (OccurEvent) _eventList.get(i);
				_eventList.remove(i);
				this.removePlotEvent(oe);
			}
			break;
		case ListDataEvent.CONTENTS_CHANGED:
			for (int i = lower; i <= upper; i++) {
				OccurEvent oe = (OccurEvent) _eventModel.getElementAt(i);
				_eventList.set(i, oe);
				this.modifyPlotEvent(oe);
			}
			break;

		}

	}



	public TimeGeometry getTimeGeometry() {
		return _timeGeometry;
	}

	public void setTimeGeometry(TimeGeometry timeGeometry) {
		if (!Objects.equals(_timeGeometry, timeGeometry)) {
			_timeGeometry = timeGeometry;

			//invalidate();
		}

	}

	public ValueGeometry getValueGeometry() {
		return _valueGeometry;
	}

	public void setValueGeometry(ValueGeometry valueGeometry) {
		if (!Objects.equals(_valueGeometry, valueGeometry)) {
			_valueGeometry = valueGeometry;
			//invalidate();
		}
	}


	/**
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 *            the operator to set
	 */
	public void setOperator(Operator operator) {
		if (!Objects.equals(this.operator, operator)) {
			this.operator = operator;
			invalidate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.zkoss.zk.ui.AbstractComponent#invalidate() //
	 */
	// @Override
	public void invalidate() {
		// TODO Auto-generated method stub
		super.invalidate();
		if (getParent() != null)
			getParent().invalidate();
	}

	public void setParent(Component parent) {
		if (parent != null && !(parent instanceof Timeplot))
			throw new UiException("Unsupported parent for plotinfo: " + parent);
		super.setParent(parent);
	}

}