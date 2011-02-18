package org.navalplanner.web.common.components;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;

import org.zkoss.lang.Objects;
import org.zkoss.util.Locales;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.ext.render.DynamicMedia;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.Utils;
import org.zkoss.zul.impl.XulElement;

public class JasperreportComponent extends Div{

    private static final String TASK_PDF = "pdf";
    private static final String TASK_HTML = "html";
    private static final String TASK_ODT = "odt";

    private static final String MEDIA_TYPE_PDF = "aplication/pdf";
    private static final String MEDIA_TYPE_HTML = "text/html";
    private static final String MEDIA_TYPE_ODT = "application/vnd.oasis.opendocument.text";

    private static final String IMAGE_DIR = "img/";

    private Locale _locale;
    private JRDataSource _datasource;
    private Map _parameters;
    private Map _imageMap;
    private String _type;
    private String _mediaType;
    private String _src;
    private Media _media;
    private int _medver;

    public JasperreportComponent(){}

    public void setType(String type){
        if (!Objects.equals(_type, type)) {
            _type = type;

            if (_type == TASK_PDF) _mediaType = MEDIA_TYPE_PDF;
            if (_type == TASK_HTML) _mediaType = MEDIA_TYPE_HTML;
            if (_type == TASK_ODT) _mediaType = MEDIA_TYPE_ODT;

            clearCachedData();
        }
    }

    public String getType(){
        return _type;
    }

    public String getMediaType(){
        return _mediaType;
    }

    public void setParameters(Map parameters) {
        if (!Objects.equals(_parameters, parameters)) {
            _parameters = parameters;
            clearCachedData();
        }
    }

    public Map getParameters(){
        return _parameters;
    }

    public void setDatasource(JRDataSource dataSource) {
        if (!Objects.equals(_datasource, dataSource)) {
            _datasource = dataSource;
            clearCachedData();
        }
    }

    public JRDataSource getDatasource(){
        return _datasource;
    }

    public void setSrc(String reportName){
        if (!reportName.endsWith(".jasper")) {
            reportName += ".jasper";
        }

        if( !Objects.equals(_src, reportName)){
            _src = reportName;
            clearCachedData();
        }
    }

    public String getSrc(){
        return _src;
    }

    public String getReportUrl(){
        Execution exec = Executions.getCurrent();

        return exec.getScheme() + "://" + exec.getServerName() + ":" + exec.getServerPort() +
                getEncodedSrc();
    }

    public String getEncodedSrc() {
        if (_src == null) {
            final Desktop dt = Executions.getCurrent().getDesktop();
            return  dt != null ? dt.getExecution().encodeURL("~./img/spacer.gif"):  "";
        } else {
            StringTokenizer st = new StringTokenizer(_src, ".");
            return Utils.getDynamicMediaURI(this, _medver++, st.nextToken(),
                        _type.equals("jxl") ? "xls": _type);
        }
    }

    public void setLocale(Locale locale) {
        if (!Objects.equals(_locale, locale)) {
            _locale = locale;
            clearCachedData();
        }

    }

    public Locale getLocale(){
        return _locale;
    }

    // -- ComponentCtrl --//
    public Object getExtraCtrl() {
        return new ExtraCtrl();
    }

    /**
     * A utility class to implement {@link #getExtraCtrl}. It is used only by
     * component developers.
     */
    private class ExtraCtrl extends XulElement.ExtraCtrl
    implements DynamicMedia{
        // -- DynamicMedia --//
        public Media getMedia(String pathInfo) {
            int indexOfImg = pathInfo.lastIndexOf(IMAGE_DIR);

            // path has IMAGE_DIR, it may be an image.
            if (indexOfImg >= 0) {
                String imageName = pathInfo.substring(indexOfImg
                        + IMAGE_DIR.length());

                // response file path has ".", it's not a image file
                if (imageName.indexOf(".") < 0) {
                    return getImage(imageName);
                }
            }
            return doReport();
        }
    }

    public Media doReport(){
        if(_media != null){
            return _media;
        }

        InputStream is = null;

        try{
            // get template file
            final Execution exec = Executions.getCurrent();
            is = exec.getDesktop().getWebApp()
                    .getResourceAsStream(exec.toAbsoluteURI(_src, false));
            if (is == null) {// try to load by class loader
                is = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(_src);
                if (is == null) {// try to load by file
                    File fl = new File(_src);
                    if (!fl.exists())
                        throw new RuntimeException("resource for " + _src
                                + " not found.");

                    is = new FileInputStream(fl);
                }
            }

            Map params;
            Map exportPara = null; // the exporter parameters which user set

            if (_parameters==null)
                params = new HashMap();
            else {
                params = _parameters;
                exportPara = (Map) params.remove("exportParameter");
            }

            if (_locale != null)
                params.put(JRParameter.REPORT_LOCALE, _locale);
            else if (!params.containsKey(JRParameter.REPORT_LOCALE))
                params.put(JRParameter.REPORT_LOCALE, Locales.getCurrent());

            // fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(is,
                    params,
                    _datasource != null ? _datasource: new JREmptyDataSource());

            // export one type of report
            if (TASK_PDF.equals(_type)) {

                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

                JRExporter exporter = new JRPdfExporter();
                if (exportPara != null)
                    exporter.setParameters(exportPara);
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, arrayOutputStream);
                exporter.exportReport();

                arrayOutputStream.close();

                return _media = new AMedia("report.pdf", "pdf", _mediaType,
                        arrayOutputStream.toByteArray());

            }else if (TASK_HTML.equals(_type)) {

                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

                JRExporter exporter = new JRHtmlExporter();
                if (exportPara != null)
                    exporter.setParameters(exportPara);
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, arrayOutputStream);

                // set IMAGES_MAP parameter to prepare get backward IMAGE_MAP parameter
                exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, new HashMap());
                exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, IMAGE_DIR);
                exporter.exportReport();

                arrayOutputStream.close();

                _imageMap = (Map)exporter.getParameter(JRHtmlExporterParameter.IMAGES_MAP);
                return _media = new AMedia("report.html", "html", _mediaType,
                        arrayOutputStream.toByteArray());

            }else if (TASK_ODT.equals(_type)) {

                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

                JRExporter exporter = new JROdtExporter();
                if (exportPara != null)
                    exporter.setParameters(exportPara);
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, arrayOutputStream);
                exporter.exportReport();

                arrayOutputStream.close();

                return _media = new AMedia("report.odt", "odt",
                        _mediaType, arrayOutputStream.toByteArray());

            }  else {
                throw new RuntimeException("Type: " + _type
                        + " is not supported in JasperReports.");
            }

        }catch(Exception e)
        {
            throw UiException.Aide.wrap(e);
        }finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Do nothing
                }
            }
        }
    }

    /**
     * When output file type is HTML, return image in AMedia
    */
    private AMedia getImage(String imageName) {
        byte[] imageBytes = (byte[])_imageMap.get(imageName);
        return new AMedia(imageName, "", "image/gif", imageBytes);
    }

    private void clearCachedData(){
        _media = null;
        _imageMap = null;
    }

}
