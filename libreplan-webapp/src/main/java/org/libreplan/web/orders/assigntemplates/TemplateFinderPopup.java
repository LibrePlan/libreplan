/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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
package org.libreplan.web.orders.assigntemplates;

import static org.libreplan.web.I18nHelper._;

import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.templates.entities.OrderTemplate;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Popup;

/**
 * Pop-up to choose the template to create a task from.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class TemplateFinderPopup extends
        HtmlMacroComponent {

    private Component finderPlaceholder;
    private Popup popup;
    private IOnResult onResult;
    private BandboxSearch bandboxSearch;
    private Button acceptButton;
    private Button cancelButton;
    private Caption caption;

    public interface IOnResult<T extends OrderElementTemplate> {
        public void found(T template);
    }

    /**
     * @param ref
     *            this is passed to {@link Popup#open(Component, String)}
     * @param position
     *            this is pased to {@link Popup#open(Component, String)}
     * @param onResult
     * @see Popup#open(Component, String)
     */
    public void openForSubElemenetCreation(Component ref, String position,
            IOnResult<OrderElementTemplate> onResult) {
        this.onResult = onResult;
        setupPopUp(ref, position, "templatesEligibleForSubElement");
    }

    /**
     * @param ref
     *            this is passed to {@link Popup#open(Component, String)}
     * @param position
     *            this is pased to {@link Popup#open(Component, String)}
     * @param onResult
     * @see Popup#open(Component, String)
     */
    public void openForOrderCreation(Component ref, String position,
            IOnResult<OrderTemplate> onResult) {
        this.onResult = onResult;
        setupPopUp(ref, position, "templatesEligibleForOrder");
    }

    private void setupPopUp(Component ref, String position,
            String finderName) {
        if (bandboxSearch != null) {
            finderPlaceholder.removeChild(bandboxSearch);
        }
        bandboxSearch = new BandboxSearch();
        bandboxSearch.setFinder(finderName);
        bandboxSearch.setWidthBandbox("300px");
        bandboxSearch.setWidthListbox("600px");
        finderPlaceholder.appendChild(bandboxSearch);
        bandboxSearch.afterCompose();
        popup.open(ref, position);
        bandboxSearch.foucusOnInput();
    }

    private void onAccept() {
        Object selectedElement = bandboxSearch.getSelectedElement();
        if (selectedElement != null) {
            onResult.found((OrderElementTemplate) selectedElement);
        }
        popup.close();
    }

    private void onCancel() {
        popup.close();
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        acceptButton = (Button) getFellow("acceptButton");
        acceptButton.setLabel(_("Create task"));
        acceptButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                onAccept();
            }
        });
        cancelButton = (Button) getFellow("cancelButton");
        cancelButton.setLabel(_("Cancel"));
        cancelButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                onCancel();
            }
        });
        finderPlaceholder = getFellow("finderPlaceholder");
        popup = (Popup) getFellow("finderPopup");
        caption = (Caption) getFellow("finderCaption");
        caption.setLabel(_("Choosing Template"));
    }

}
