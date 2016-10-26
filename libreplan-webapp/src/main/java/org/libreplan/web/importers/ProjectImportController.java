package org.libreplan.web.importers;

import static org.libreplan.web.I18nHelper._;

import java.io.InputStream;
import java.util.List;

import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.importers.CalendarDTO;
import org.libreplan.importers.ICalendarImporter;
import org.libreplan.importers.IOrderImporter;
import org.libreplan.importers.OrderDTO;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;

/**
 * Controller for import projects
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
public class ProjectImportController extends GenericForwardComposer {

    public static final String BREADCRUMBS_SEPARATOR = "/common/img/migas_separacion.gif";

    /**
     * OrderImporter service.
     */
    private IOrderImporter orderImporterMPXJ;

    /**
     * CalendarImporter service.
     */
    private ICalendarImporter calendarImporterMPXJ;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Radio importCalendars;

    private Radio importTasks;

    private Radio importAll;

    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("projectImportController", this);

        injectsObjects();

        messages = new MessagesForUser(messagesContainer);
        setBreadCrumbs(comp);
    }

    private void injectsObjects() {
        calendarImporterMPXJ = (ICalendarImporter) SpringUtil.getBean("calendarImporterMPXJ");
        orderImporterMPXJ = (IOrderImporter) SpringUtil.getBean("orderImporterMPXJ");
    }

    private void setBreadCrumbs(Component comp) {
        Component breadCrumbs = comp.getPage().getFellow("breadcrumbs");

        if (breadCrumbs.getChildren() != null) {
            breadCrumbs.getChildren().clear();
        }

        breadCrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadCrumbs.appendChild(new Label(_("Planning")));
        breadCrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
        breadCrumbs.appendChild(new Label(_("Import project")));
    }
    /**
     * Method called when the onUpload event happens.
     *
     * Should be public!
     * Used in projectImport.zul
     *
     * @param media
     *            Media to be imported.
     */
    public void importProject(Media media) {

        String file = media.getName();

        if (checkFileFormat(file)) {

            if (importCalendars.isChecked()) {

                try {
                    importCalendar(media.getStreamData(), file);
                    messages.showMessage(Level.INFO, _(file + ": Calendar import successfully!"));
                } catch (InstanceNotFoundException e) {
                    messages.showMessage(Level.ERROR, _("Instance not found."));
                } catch (ValidationException e) {
                    messages.showMessage(Level.ERROR, e.getMessage());
                }

            } else if (importTasks.isChecked()) {
                importProject(media.getStreamData(), file);

                messages.showMessage(Level.INFO, _(file + ": Task import successfully!"));

            } else if (importAll.isChecked()) {

                try {
                    importAll(media.getStreamData(), file);
                    messages.showMessage(Level.INFO, _(file + ": Import successfully!"));
                } catch (InstanceNotFoundException e) {
                    messages.showMessage(Level.ERROR, _("Instance not found."));
                } catch (ValidationException e) {
                    messages.showMessage(Level.ERROR, e.getMessage());
                }

            } else {
                messages.showMessage(Level.WARNING, _("Select one of the options."));
            }

        } else {
            messages.showMessage(Level.ERROR, _("The only current supported formats are mpp and planner."));
        }

    }

    /**
     * Imports calendars, orders, task and dependencies from a InputStream.
     *
     * @param streamData
     *            InputStream with the data that is going to be imported.
     * @param file
     *            Name of the file that we want to import.
     */
    @Transactional
    private void importAll(InputStream streamData, String file) throws InstanceNotFoundException {

        List<CalendarDTO> calendarDTOs = calendarImporterMPXJ.getCalendarDTOs(streamData, file);

        List<BaseCalendar> baseCalendars = calendarImporterMPXJ.getBaseCalendars(calendarDTOs);

        calendarImporterMPXJ.storeBaseCalendars(baseCalendars);

        OrderDTO importData = calendarImporterMPXJ.getOrderDTO(file);

        Order order = orderImporterMPXJ.convertImportDataToOrder(importData, true);

        TaskGroup taskGroup = orderImporterMPXJ.createTask(importData, true);

        List<Dependency> dependencies = orderImporterMPXJ.createDependencies(importData);

        orderImporterMPXJ.storeOrder(order, taskGroup, dependencies);

    }

    /**
     * Imports the calendars from a InputStream.
     *
     * @param streamData
     *            InputStream with the data that is going to be imported.
     * @param file
     *            Name of the file that we want to import.
     */
    @Transactional
    private void importCalendar(InputStream streamData, String file) throws InstanceNotFoundException {

        List<CalendarDTO> calendarDTOs = calendarImporterMPXJ.getCalendarDTOs(streamData, file);

        List<BaseCalendar> baseCalendars = calendarImporterMPXJ.getBaseCalendars(calendarDTOs);

        calendarImporterMPXJ.storeBaseCalendars(baseCalendars);

    }

    /**
     * Imports an InputStream.
     *
     * @param streamData
     *            InputStream with the data that is going to be imported.
     * @param file
     *            Name of the file that we want to import.
     */
    @Transactional
    private void importProject(InputStream streamData, String file) {

        OrderDTO importData = orderImporterMPXJ.getImportData(streamData, file);

        Order order = orderImporterMPXJ.convertImportDataToOrder(importData, false);

        TaskGroup taskGroup = orderImporterMPXJ.createTask(importData, false);

        List<Dependency> dependencies = orderImporterMPXJ.createDependencies(importData);

        orderImporterMPXJ.storeOrder(order, taskGroup, dependencies);

    }

    /**
     * Checks that the file has the correct format (.mpp or .planner).
     *
     * @param file
     *            Name of the file.
     * @return boolean True if is correct, false if not.
     */
    private boolean checkFileFormat(String file) {
        return file.matches("(?i).*mpp") || file.matches("(?i).*planner");
    }

}
