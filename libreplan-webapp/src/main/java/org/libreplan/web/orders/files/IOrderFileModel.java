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

    void delete(OrderFile file);

    List<OrderFile> findByParent(OrderElement parent);

    OrderFile getOrderFile();
}
