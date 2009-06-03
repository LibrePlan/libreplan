package org.navalplanner.web.common.entrypoints;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

/**
 * Uses {@link Executions#getCurrent()} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class DefaultExecutorRetriever implements ExecutorRetriever {

    @Override
    public Execution getCurrent() {
        return Executions.getCurrent();
    }

}
