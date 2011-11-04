package org.libreplan.web.subcontract;

import java.util.List;

import org.libreplan.business.externalcompanies.daos.ICustomerComunicationDAO;
import org.libreplan.business.externalcompanies.entities.CustomerComunication;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/subcontract/customerComunication.zul")
public class CustomerComunicationModel implements ICustomerComunicationModel{

    @Autowired
    private ICustomerComunicationDAO customerComunicationDAO;
    
    private FilterCustomerComunicationEnum currentFilter = FilterCustomerComunicationEnum.NOT_REVIEWED;

    @Override
    @Transactional
    public void confirmSave(CustomerComunication customerComunication){
        customerComunicationDAO.save(customerComunication);
    }

    @Override
    @Transactional
    public List<CustomerComunication> getCustomerAllComunications(){
        List<CustomerComunication> list = customerComunicationDAO.getAll();
        forceLoadAssociatedOrder(list);
        return list;
    }

    @Override
    @Transactional
    public List<CustomerComunication> getCustomerComunicationWithoutReviewed(){
        List<CustomerComunication> list = customerComunicationDAO.getAllNotReviewed();
        forceLoadAssociatedOrder(list);
        return list;
    }

    private void forceLoadAssociatedOrder(List<CustomerComunication> customerComunicationList){
        if (customerComunicationList != null) {
            for (CustomerComunication customerComunication : customerComunicationList) {
                customerComunication.getOrder().getName();
            }
        }
    }

    @Override
    public void setCurrentFilter(FilterCustomerComunicationEnum currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override
    public FilterCustomerComunicationEnum getCurrentFilter() {
        return currentFilter;
    }
}
