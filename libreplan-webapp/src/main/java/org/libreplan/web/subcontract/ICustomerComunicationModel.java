package org.libreplan.web.subcontract;

import java.util.List;

import org.libreplan.business.externalcompanies.entities.CustomerComunication;

public interface ICustomerComunicationModel {

    void confirmSave(CustomerComunication customerComunication);

    List<CustomerComunication> getCustomerComunicationWithoutReviewed();

    List<CustomerComunication> getCustomerAllComunications();

    void setCurrentFilter(FilterCustomerComunicationEnum currentFilter);

    FilterCustomerComunicationEnum getCurrentFilter();

}
