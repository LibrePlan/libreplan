package org.libreplan.web.orders.files;

import org.libreplan.business.orders.daos.IOrderFileDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderFile;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.IConfigurationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zkplus.spring.SpringUtil;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderFileModel implements IOrderFileModel {

    public static final String UNKNOWN_FILE_EXTENSION = "Unknown type";

    @Autowired
    private IOrderFileDAO fileDAO;

    private OrderFile orderFile;

    private IConfigurationModel configurationModel;

    @Override
    @Transactional
    public void confirmSave() {
        fileDAO.save(orderFile);
    }

    @Override
    public void setFileName(String name) {
        orderFile.setName(name);
    }

    @Override
    public void setFileType(String type) {
        orderFile.setType(type);
    }

    @Override
    public void setUploadDate(Date date) {
        orderFile.setDate(date);
    }

    @Override
    public void setUploader(User user) {
        orderFile.setUploader(user);
    }

    @Override
    public void setParent(OrderElement project) {
        orderFile.setParent(project);
    }

    @Override
    public void createNewFileObject() {
        orderFile = new OrderFile();
    }

    @Override
    @Transactional
    public List<OrderFile> getAll() {
        return fileDAO.getAll();
    }

    /**
     * This method is used to delete OrderFile and physical file asociated with it.
     * Also different cases can occur:
     *  - First case: there is no troubles and OrderFile is deleted from database
     *  and physical file is deleted from disc.
     *  - Second case: there is some troubles with deleting physical file from disc, but
     *  OrderFile is deleted from database.
     *
     * @param  file {@link OrderFile} that needs to be deleted.
     * @return true if file was deleted successfully.
     * @return false if file was not successfully deleted .
     */
    @Override
    @Transactional
    public boolean delete(OrderFile file) {
        fileDAO.delete(file);
        return deletePhysicalFile(file);
    }

    @Override
    @Transactional
    public List<OrderFile> findByParent(OrderElement parent) {
        return fileDAO.findByParent(parent);
    }

    @Override
    public OrderFile getOrderFile() {
        return orderFile;
    }

    private boolean deletePhysicalFile (OrderFile file) {

        try {
            configurationModel = (IConfigurationModel) SpringUtil.getBean("configurationModel");
            configurationModel.init();

            String projectCode = file.getParent().getCode();
            String directory = configurationModel.getRepositoryLocation() + "orders" + "/" + projectCode;
            File fileToDelete;

            if (UNKNOWN_FILE_EXTENSION.equals(file.getType())) {
                fileToDelete = new File(directory + "/" + file.getName());
            }
            else {
                fileToDelete = new File(directory + "/" + file.getName() + "." + file.getType());
            }

            return fileToDelete.delete();
        } catch (Exception ignored) {
            /*
             * org.zkoss.zk.ui.Execution("SpringUtil can be called only under ZK environment!") can occur if
             * ZK environment is not raised, this can occur in JUnit tests
             */
            return false;
        }
    }


}
