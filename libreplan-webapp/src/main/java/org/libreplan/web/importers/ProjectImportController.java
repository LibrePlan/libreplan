package org.libreplan.web.importers;

import static org.libreplan.web.I18nHelper._;

import java.io.InputStream;
import java.util.List;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.importers.IOrderImporter;
import org.libreplan.importers.OrderDTO;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for import projects
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
public class ProjectImportController extends GenericForwardComposer {

    /**
     * OrderImporter service.
     */
    private IOrderImporter orderImporterMPXJ;

    private IMessagesForUser messages;

    private Component messagesContainer;

    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("projectImportController", this);
        messages = new MessagesForUser(messagesContainer);
    }

    /**
     * Method called when the onUpload event happens.
     *
     * @param Media
     *            Media to be imported.
     */
    public void importProject(Media media) {

        String file = media.getName();

        if (checkFileFormat(file)) {

            importProject(media.getStreamData(), file);

            messages.showMessage(Level.INFO, _(file + ": Import successfully!"));

        } else {
            messages.showMessage(Level.ERROR,
                    _("The only current suported formats are mpp and planner."));
        }

    }

    /**
     * Imports an InputStream.
     *
     * @param streamData
     *            InputStream with the data that is going to be imported.
     * @param file
     *            Name of the file that we want to import.
     * @return boolean True if the streamData was imported, false if not.
     */
    @Transactional
    private void importProject(InputStream streamData, String file) {

        OrderDTO importData = orderImporterMPXJ.getImportData(streamData, file);

        Order order = orderImporterMPXJ.convertImportDataToOrder(importData);

        TaskGroup taskGroup = orderImporterMPXJ.createTask(importData);

        List<Dependency> dependencies = orderImporterMPXJ
                .createDependencies(importData);

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

        if (file.matches("(?i).*mpp") | file.matches("(?i).*planner")) {
            return true;
        }

        return false;
    }

}
