package org.navalplanner.web.common;

import org.zkoss.zk.ui.Execution;

/**
 * It's used for retrieving the current {@link Execution} object <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ExecutorRetriever {

    public Execution getCurrent();

}
