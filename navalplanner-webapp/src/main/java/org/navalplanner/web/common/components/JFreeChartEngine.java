package org.navalplanner.web.common.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.JFreeChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.TickLabelEntity;
import org.jfree.chart.entity.TitleEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.zkoss.lang.Objects;
import org.zkoss.util.TimeZones;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Area;
import org.zkoss.zul.CategoryModel;
import org.zkoss.zul.Chart;
import org.zkoss.zul.ChartModel;
import org.zkoss.zul.XYModel;
import org.zkoss.zul.impl.ChartEngine;

public class JFreeChartEngine implements ChartEngine {

    private String _type;
    private ChartImpl _chartImpl;
    private transient boolean _threeD;
    private static final SimpleDateFormat _dateFormat = new SimpleDateFormat();
    private static Map _periodMap = new HashMap(10);
    static {
        _periodMap.put(Chart.MILLISECOND, org.jfree.data.time.Millisecond.class);
        _periodMap.put(Chart.SECOND, org.jfree.data.time.Second.class);
        _periodMap.put(Chart.MINUTE, org.jfree.data.time.Minute.class);
        _periodMap.put(Chart.HOUR, org.jfree.data.time.Hour.class);
        _periodMap.put(Chart.DAY, org.jfree.data.time.Day.class);
        _periodMap.put(Chart.WEEK, org.jfree.data.time.Week.class);
        _periodMap.put(Chart.MONTH, org.jfree.data.time.Month.class);
        _periodMap.put(Chart.QUARTER, org.jfree.data.time.Quarter.class);
        _periodMap.put(Chart.YEAR, org.jfree.data.time.Year.class);
    }

    private ChartImpl getChartImpl(Chart chart){
        if (Objects.equals(chart.getType(), _type) && _threeD == chart.isThreeD()) {
            return _chartImpl;
        }

        if ( Chart.TIME_SERIES.equals(chart.getType()) ){
            _chartImpl = new TimeSeriesChart();
        }else if ( Chart.BAR.equals(chart.getType())){
            _chartImpl = chart.isThreeD() ? new Bar3dChart() : new BarChart();
        }else {
            throw new RuntimeException("Unsupported chart type: " + chart.getType());
        }

        _threeD = chart.isThreeD();
        _type = chart.getType();
        return _chartImpl;
    }
    @Override
    public byte[] drawChart(Object data) {
        Chart chart = (Chart) data;
        ChartImpl impl = getChartImpl(chart);
        JFreeChart jfchart = impl.createChart(chart);

        Plot plot = (Plot) jfchart.getPlot();
        float alpha = (float)(((float)chart.getFgAlpha()) / 255);
        plot.setForegroundAlpha(alpha);

        alpha = (float)(((float)chart.getBgAlpha()) / 255);
        plot.setBackgroundAlpha(alpha);

        int[] bgRGB = chart.getBgRGB();
        if (bgRGB != null) {
            plot.setBackgroundPaint(new Color(bgRGB[0], bgRGB[1], bgRGB[2], chart.getBgAlpha()));
        }

        int[] paneRGB = chart.getPaneRGB();
        if (paneRGB != null) {
            jfchart.setBackgroundPaint(new Color(paneRGB[0], paneRGB[1], paneRGB[2], chart.getPaneAlpha()));
        }

        //since 3.6.3, JFreeChart 1.0.13 change default fonts which does not support Chinese, allow
        //developer to set font.

        //title font
        final Font tfont = chart.getTitleFont();
        if (tfont != null) {
            jfchart.getTitle().setFont(tfont);
        }

        //legend font
        final Font lfont = chart.getLegendFont();
        if (lfont != null) {
            jfchart.getLegend().setItemFont(lfont);
        }

        if (plot instanceof CategoryPlot) {
            final CategoryPlot cplot = (CategoryPlot) plot;
            cplot.setRangeGridlinePaint(new Color(0xc0, 0xc0, 0xc0));

            //Domain axis(x axis)
            final Font xlbfont = chart.getXAxisFont();
            final Font xtkfont = chart.getXAxisTickFont();
            if (xlbfont != null) {
                cplot.getDomainAxis().setLabelFont(xlbfont);
            }
            if (xtkfont != null) {
                cplot.getDomainAxis().setTickLabelFont(xtkfont);
            }

            //Range axis(y axis)
            final Font ylbfont = chart.getYAxisFont();
            final Font ytkfont = chart.getYAxisTickFont();
            if (ylbfont != null) {
                cplot.getRangeAxis().setLabelFont(ylbfont);
            }
            if (ytkfont != null) {
                cplot.getRangeAxis().setTickLabelFont(ytkfont);
            }
        } else if (plot instanceof XYPlot) {
            final XYPlot xyplot = (XYPlot) plot;
            xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            xyplot.setDomainGridlinePaint(Color.LIGHT_GRAY);

            //Domain axis(x axis)
            final Font xlbfont = chart.getXAxisFont();
            final Font xtkfont = chart.getXAxisTickFont();
            if (xlbfont != null) {
                xyplot.getDomainAxis().setLabelFont(xlbfont);
            }
            if (xtkfont != null) {
                xyplot.getDomainAxis().setTickLabelFont(xtkfont);
            }

            //Range axis(y axis)
            final Font ylbfont = chart.getYAxisFont();
            final Font ytkfont = chart.getYAxisTickFont();
            if (ylbfont != null) {
                xyplot.getRangeAxis().setLabelFont(ylbfont);
            }
            if (ytkfont != null) {
                xyplot.getRangeAxis().setTickLabelFont(ytkfont);
            }
        }

        //callbacks for each area
        ChartRenderingInfo jfinfo = new ChartRenderingInfo();
        BufferedImage bi = jfchart.createBufferedImage(chart.getIntWidth(), chart.getIntHeight(), Transparency.TRANSLUCENT, jfinfo);

        //remove old areas
        if (chart.getChildren().size() > 20)
            chart.invalidate(); //improve performance if too many chart
        chart.getChildren().clear();

        if (Events.isListened(chart, Events.ON_CLICK, false) || chart.isShowTooltiptext()) {
            int j = 0;
            String preUrl = null;
            for(Iterator it=jfinfo.getEntityCollection().iterator();it.hasNext();) {
                ChartEntity ce = ( ChartEntity ) it.next();
                final String url = ce.getURLText();

                //workaround JFreeChart's bug (skip replicate areas)
                if (url != null) {
                    if (preUrl == null) {
                        preUrl = url;
                    } else if (url.equals(preUrl)) { //start replicate, skip
                        break;
                    }
                }

                //1. JFreeChartEntity area cover the whole chart, will "mask" other areas
                //2. LegendTitle area cover the whole legend, will "mask" each legend
                //3. PlotEntity cover the whole chart plotting araa, will "mask" each bar/line/area
                if (!(ce instanceof JFreeChartEntity)
                && !(ce instanceof TitleEntity && ((TitleEntity)ce).getTitle() instanceof LegendTitle)
                && !(ce instanceof PlotEntity)) {
                    Area area = new Area();
                    area.setParent(chart);
                    area.setCoords(ce.getShapeCoords());
                    area.setShape(ce.getShapeType());
                    area.setId("area_"+chart.getId()+'_'+(j++));
                    if (chart.isShowTooltiptext() && ce.getToolTipText() != null) {
                        area.setTooltiptext(ce.getToolTipText());
                    }
                    area.setAttribute("url", ce.getURLText());
                    impl.render(chart, area, ce);
                    if (chart.getAreaListener() != null) {
                        try {
                            chart.getAreaListener().onRender(area, ce);
                        } catch (Exception ex) {
                            throw UiException.Aide.wrap(ex);
                        }
                    }
                }
            }
        }
        //clean up the "LEGEND_SEQ"
        //used for workaround LegendItemEntity.getSeries() always return 0
        //used for workaround TickLabelEntity no information
        chart.removeAttribute("LEGEND_SEQ");
        chart.removeAttribute("TICK_SEQ");

        try {
            //encode into png image format byte array
            return EncoderUtil.encode(bi, ImageFormat.PNG, true);
        } catch(java.io.IOException ex) {
            throw UiException.Aide.wrap(ex);
        }
    }

    private void decodeLegendInfo(Area area, LegendItemEntity info, Chart chart){
        if (info == null) return;

        final ChartModel model = chart.getModel();
        final int seq = ((Integer)chart.getAttribute("LEGEND_SEQ")).intValue();

        if (model instanceof CategoryModel){
            Comparable series = ((CategoryModel)model).getSeries(seq);
            area.setAttribute("series", series);
            if (chart.isShowTooltiptext() && info.getToolTipText() == null) {
                area.setTooltiptext(series.toString());
            }
        }else if (model instanceof XYModel){
            Comparable series = ((XYModel)model).getSeries(seq);
            area.setAttribute("series", series);
            if (chart.isShowTooltiptext() && info.getToolTipText() == null) {
                area.setTooltiptext(series.toString());
            }
        }
    }

    /**
     * decode XYItemEntity into key-value pair of Area's componentScope.
     */
    private void decodeXYInfo(Area area, XYItemEntity info, Chart chart) {
        if (info == null) {
            return;
        }
        TimeZone tz = chart.getTimeZone();
        if (tz == null) tz = TimeZones.getCurrent();

        XYDataset dataset = info.getDataset();
        int si = info.getSeriesIndex();
        int ii = info.getItem();

        area.setAttribute("series", dataset.getSeriesKey(si));

        if (dataset instanceof XYZDataset) {
            XYZDataset ds = (XYZDataset) dataset;
            area.setAttribute("x", ds.getX(si, ii));
            area.setAttribute("y", ds.getY(si, ii));
            area.setAttribute("z", ds.getZ(si, ii));
        } else {
            area.setAttribute("x", dataset.getX(si, ii));
            area.setAttribute("y", dataset.getY(si, ii));
        }

    }

    /**
     * decode CategoryItemEntity into key-value pair of Area's componentScope.
     */
    private void decodeCategoryInfo(Area area, CategoryItemEntity info) {
        if (info == null) {
            return;
        }

        CategoryDataset dataset = info.getDataset();
        Comparable category = info.getColumnKey();
        Comparable series = info.getRowKey();

        area.setAttribute("series", series);
        area.setAttribute("category", category);

        if (dataset instanceof GanttCategoryDataset) {
            final GanttCategoryDataset gd = (GanttCategoryDataset) dataset;
            area.setAttribute("start", gd.getStartValue(series, category));
            area.setAttribute("end", gd.getEndValue(series, category));
            area.setAttribute("percent", gd.getPercentComplete(series, category));
        } else {
            area.setAttribute("value", dataset.getValue(series, category));
        }
    }

    /**
     * decode TickLabelEntity into key-value pair of Area's componentScope.
     */
    private void decodeTickLabelInfo(Area area, TickLabelEntity info, Chart chart) {
        if (info == null) {
            return;
        }
        final ChartModel model = chart.getModel();
        final int seq = ((Integer)chart.getAttribute("TICK_SEQ")).intValue();

        if (model instanceof CategoryModel) {
            Comparable category = ((CategoryModel)model).getCategory(seq);
            area.setAttribute("category", category);
            if (chart.isShowTooltiptext() && info.getToolTipText() == null) {
                area.setTooltiptext(category.toString());
            }
        }
    }

    /**
     * transfer a CategoryModel into JFreeChart CategoryDataset.
     */
    private CategoryDataset CategoryModelToCategoryDataset(CategoryModel model) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final Iterator it = model.getKeys().iterator(); it.hasNext();) {
            final List key = (List) it.next();
            Comparable series = (Comparable) key.get(0);
            Comparable category = (Comparable) key.get(1);
            Number value = (Number) model.getValue(series, category);
            dataset.setValue(value, series, category);
        }
        return dataset;
    }

    /*
    * transfer a XYModel into JFreeChart XYSeriesCollection.
    */
   private XYDataset XYModelToXYDataset(XYModel model) {
       final XYSeriesCollection dataset = new XYSeriesCollection();
       for (final Iterator it = model.getSeries().iterator(); it.hasNext();) {
           final Comparable series = (Comparable) it.next();
           XYSeries xyser = new XYSeries(series, model.isAutoSort());
           final int size = model.getDataCount(series);
           for(int j = 0; j < size; ++j) {
               xyser.add(model.getX(series, j), model.getY(series, j), false);
           }
           dataset.addSeries(xyser);
       }
       return dataset;
   }

   /**
    * transfer a XYModel into JFreeChart TimeSeriesCollection.
    */
   private XYDataset XYModelToTimeDataset(XYModel model, Chart chart) {
       TimeZone tz = chart.getTimeZone();
       if (tz == null) tz = TimeZones.getCurrent();
       String p = chart.getPeriod();
       if (p == null) p = Chart.MILLISECOND;
       Class pclass = (Class) _periodMap.get(p);
       if (pclass == null) {
           throw new UiException("Unsupported period for Time Series chart: "+p);
       }
       final TimeSeriesCollection dataset = new TimeSeriesCollection(tz);

       for (final Iterator it = model.getSeries().iterator(); it.hasNext();) {
           final Comparable series = (Comparable) it.next();
           final org.jfree.data.time.TimeSeries tser =
               new org.jfree.data.time.TimeSeries(series);
               //new org.jfree.data.time.TimeSeries(series, pclass); //deprecated since JFreeChart 10.0.13
           final int size = model.getDataCount(series);
           for(int j = 0; j < size; ++j) {
               final RegularTimePeriod period = RegularTimePeriod.createInstance(
                   pclass, new Date(model.getX(series, j).longValue()), tz);
               tser.addOrUpdate(period, model.getY(series, j));
           }
           dataset.addSeries(tser);
       }
       return dataset;
   }


    private PlotOrientation getOrientation(String orient) {
        return "horizontal".equals(orient) ?
            PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
    }

    //-- Chart specific implementation --//
    /** base chart */
    abstract private class ChartImpl {
        abstract void render(Chart chart, Area area, ChartEntity info);
        abstract JFreeChart createChart(Chart chart);
    }


    private class TimeSeriesChart extends ChartImpl{

        @Override
        public void render(Chart chart, Area area, ChartEntity info) {
            if (info instanceof LegendItemEntity) {
                area.setAttribute("entity", "LEGEND");
                Integer seq = (Integer)chart.getAttribute("LEGEND_SEQ");
                seq = seq == null ? new Integer(0) : new Integer(seq.intValue()+1);
                chart.setAttribute("LEGEND_SEQ", seq);
                decodeLegendInfo(area, (LegendItemEntity)info, chart);
            } else if (info instanceof XYItemEntity) {
                area.setAttribute("entity", "DATA");
                decodeXYInfo(area, (XYItemEntity) info, chart);
            } else {
                area.setAttribute("entity", "TITLE");
                if (chart.isShowTooltiptext()) {
                    area.setTooltiptext(chart.getTitle());
                }
            }
        }

        @Override
        public JFreeChart createChart(Chart chart) {
            ChartModel model = (ChartModel) chart.getModel();
            if (!(model instanceof XYModel)) {
                throw new UiException("model must be a org.zkoss.zul.XYModel");
            }
            final JFreeChart jchart = ChartFactory.createTimeSeriesChart(
                chart.getTitle(),
                chart.getXAxis(),
                chart.getYAxis(),
                XYModelToTimeDataset((XYModel)model, chart),
                chart.isShowLegend(),
                chart.isShowTooltiptext(), true);
            setupDateAxis(jchart, chart);
            return jchart;
        }
    }

    private void setupDateAxis(JFreeChart jchart, Chart chart) {
        final Plot plot = jchart.getPlot();
        final DateAxis axisX = (DateAxis) ((XYPlot)plot).getDomainAxis();
        final TimeZone zone = chart.getTimeZone();
        if (zone != null) {
            axisX.setTimeZone(zone);
        }
        if (chart.getDateFormat() != null) {
            axisX.setDateFormatOverride(_dateFormat);
        }
    }



    private class BarChart extends ChartImpl{
        public void render(Chart chart, Area area, ChartEntity info) {
            if (info instanceof LegendItemEntity) {
                area.setAttribute("entity", "LEGEND");
                Integer seq = (Integer)chart.getAttribute("LEGEND_SEQ");
                seq = seq == null ? new Integer(0) : new Integer(seq.intValue()+1);
                chart.setAttribute("LEGEND_SEQ", seq);
                decodeLegendInfo(area, (LegendItemEntity)info, chart);
            } else if (info instanceof CategoryItemEntity) {
                area.setAttribute("entity", "DATA");
                decodeCategoryInfo(area, (CategoryItemEntity)info);
            } else if (info instanceof XYItemEntity) {
                area.setAttribute("entity", "DATA");
                decodeXYInfo(area, (XYItemEntity) info, chart);
            } else if (info instanceof TickLabelEntity) {
                area.setAttribute("entity", "CATEGORY");
                Integer seq = (Integer)chart.getAttribute("TICK_SEQ");
                seq = seq == null ? new Integer(0) : new Integer(seq.intValue()+1);
                chart.setAttribute("TICK_SEQ", seq);
                decodeTickLabelInfo(area, (TickLabelEntity) info, chart);
            } else {
                area.setAttribute("entity", "TITLE");
                if (chart.isShowTooltiptext()) {
                    area.setTooltiptext(chart.getTitle());
                }
            }
        }

        public JFreeChart createChart(Chart chart) {
            ChartModel model = (ChartModel) chart.getModel();
            if (model instanceof CategoryModel) {
                return ChartFactory.createBarChart(
                    chart.getTitle(),
                    chart.getXAxis(),
                    chart.getYAxis(),
                    CategoryModelToCategoryDataset((CategoryModel)model),
                    getOrientation(chart.getOrient()),
                    chart.isShowLegend(),
                    chart.isShowTooltiptext(), true);
            } else if (model instanceof XYModel) {
                return ChartFactory.createXYBarChart(
                        chart.getTitle(),
                        chart.getXAxis(),
                        false,
                        chart.getYAxis(),
                        (IntervalXYDataset) XYModelToXYDataset((XYModel)model),
                        getOrientation(chart.getOrient()),
                        chart.isShowLegend(),
                        chart.isShowTooltiptext(), true);
            }else{
                throw new UiException("The only supported model is org.zkoss.zul.CategoryModel");
                }
            }
       }

    /** bar3d chart */
    private class Bar3dChart extends BarChart {
        public JFreeChart createChart(Chart chart) {
            ChartModel model = (ChartModel) chart.getModel();
            if (!(model instanceof CategoryModel)) {
                throw new UiException("model must be a org.zkoss.zul.CategoryModel");
            }
            return ChartFactory.createBarChart3D(
                chart.getTitle(),
                chart.getXAxis(),
                chart.getYAxis(),
                CategoryModelToCategoryDataset((CategoryModel)model),
                getOrientation(chart.getOrient()),
                chart.isShowLegend(),
                chart.isShowTooltiptext(), true);
        }
    }


}
