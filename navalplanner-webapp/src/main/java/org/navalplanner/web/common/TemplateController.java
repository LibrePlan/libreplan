/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.common.ITemplateModel.IOnFinished;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.navalplanner.web.security.SecurityUtils;
import org.navalplanner.web.users.bootstrap.MandatoryUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Window;

/**
 * Controller to manage UI operations from main template.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemplateController extends GenericForwardComposer {

    private static final Log LOG = LogFactory.getLog(TemplateController.class);

    @Autowired
    private ITemplateModel templateModel;

    @Autowired
    private IScenarioManager scenarioManager;

    private Window window;

    private IMessagesForUser windowMessages;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        if (templateModel.isScenariosVisible()) {
            window = (Window) comp.getFellow("changeScenarioWindow");
            windowMessages = new MessagesForUser(window
                .getFellow("messagesContainer"));
        }
    }

    public Scenario getScenario() {
        return scenarioManager.getCurrent();
    }

    public void changeScenario() throws SuspendNotAllowedException,
            InterruptedException {
        window.doModal();
    }

    public List<Scenario> getScenarios() {
        if (templateModel == null) {
            return Collections.emptyList();
        }
        return templateModel.getScenarios();
    }

    public String getCompanyLogoURL() {
        if (templateModel == null || templateModel.getCompanyLogoURL() == null) {
            return "";
        }
        return templateModel.getCompanyLogoURL().trim();
    }

    public void accept() {
        BandboxSearch scenarioBandboxSearch = (BandboxSearch) window
                .getFellow("scenarioBandboxSearch");
        Scenario scenario = (Scenario) scenarioBandboxSearch
                .getSelectedElement();

        templateModel.setScenario(SecurityUtils.getSessionUserLoginName(),
                scenario, new IOnFinished() {
                    @Override
                    public void onWithoutErrorFinish() {
                        window.setVisible(false);
                        Executions.sendRedirect("/");
                    }

                    @Override
                    public void errorHappened(Exception exceptionHappened) {
                        LOG.error("error doing reassignation",
                                exceptionHappened);
                        windowMessages.showMessage(Level.ERROR, _(
                                "error doing reassignation: {0}",
                                exceptionHappened));
                    }
                });
    }

    public void cancel() {
        window.setVisible(false);
    }

    public Boolean getScenariosVisible() {
        return (templateModel != null) && templateModel.isScenariosVisible();
    }

    public String getDefaultPasswdAdminVisible() {
        return notChangedPasswordWarningDisplayPropertyFor(MandatoryUser.ADMIN);
    }

    public String getDefaultPasswdUserVisible() {
        return notChangedPasswordWarningDisplayPropertyFor(MandatoryUser.USER);
    }

    public String getDefaultPasswdWsreaderVisible() {
        return notChangedPasswordWarningDisplayPropertyFor(MandatoryUser.WSREADER);
    }

    public String getDefaultPasswdWswriterVisible() {
        return notChangedPasswordWarningDisplayPropertyFor(MandatoryUser.WSWRITER);
    }

    private String notChangedPasswordWarningDisplayPropertyFor(
            MandatoryUser mandatoryUser) {
        return asDisplayProperty(templateModel
                .hasChangedDefaultPassword(mandatoryUser));
    }


    private String asDisplayProperty(boolean passwordChanged) {
        return passwordChanged ? "none" : "inline";
    }

    public String getDefaultPasswdVisible() {
        return asDisplayProperty(!templateModel
                .adminPasswordChangedAndSomeOtherNotChanged());
    }

    public String getIdAdminUser() {
        return templateModel.getIdUser(MandatoryUser.ADMIN.getLoginName());
    }

    public String getIdUser() {
        return templateModel.getIdUser(MandatoryUser.USER.getLoginName());
    }

    public String getIdWsreaderUser() {
        return templateModel.getIdUser(MandatoryUser.WSREADER.getLoginName());
    }

    public String getIdWswriterUser() {
        return templateModel.getIdUser(MandatoryUser.WSWRITER.getLoginName());
    }
}
