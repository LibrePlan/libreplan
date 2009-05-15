package org.navalplanner.business.resources.bootstrap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class CriterionsBootstrap implements IDataBootstrap {

    private static final Log LOG = LogFactory.getLog(CriterionsBootstrap.class);

    @Autowired
    private ResourceService resourceService;

    @Override
    public void loadRequiredData() {
        LOG.info("TODO: load criterions");
        assert resourceService != null;
    }

}
