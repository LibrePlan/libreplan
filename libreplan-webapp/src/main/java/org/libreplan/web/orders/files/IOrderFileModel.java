package org.libreplan.web.orders.files;

import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderFile;
import org.libreplan.business.users.entities.User;

import java.util.Date;
import java.util.List;

/**
 * Contract for {@link OrderFile}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public interface IOrderFileModel {

    void confirmSave();

    void setFileName(String name);

    void setFileType(String type);

    void setUploadDate(Date date);

    void setUploader(User user);

    void setParent(OrderElement project);

    void createNewFileObject();

    List<OrderFile> getAll();

    /**
     * This method is used to delete OrderFile and physical file asociated with it
     *
     * @param file {@link OrderFile} that need to be deleted
     * @return true if file was deleted successfully.
     * @return false if file was not deleted successfully.
     */
    boolean delete(OrderFile file);

    List<OrderFile> findByParent(OrderElement parent);

    OrderFile getOrderFile();
}
