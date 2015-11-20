package org.libreplan.web.dashboard;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Created
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 20.11.15.
 */

@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardControllerGlobal extends GenericForwardComposer {
    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
    }
}
