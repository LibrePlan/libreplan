package org.libreplan.web.orders.files;

import org.libreplan.business.orders.daos.IOrderFileDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderFile;
import org.libreplan.business.users.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Created by Vova Perebykivskyi <vova@libreplan-enterprise.com> on 12.24.2015.
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderFileModel implements IOrderFileModel {

    @Autowired
    private IOrderFileDAO fileDAO;

    private OrderFile orderFile;

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

    @Override
    @Transactional
    public void delete(OrderFile file){
        fileDAO.delete(file);
    }

    @Override
    @Transactional
    public List<OrderFile> findByParent(OrderElement parent) {
        return fileDAO.findByParent(parent);
    }

    public OrderFile getOrderFile() {
        return orderFile;
    }
}
