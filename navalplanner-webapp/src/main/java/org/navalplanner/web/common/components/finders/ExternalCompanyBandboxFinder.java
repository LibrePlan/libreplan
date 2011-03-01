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

package org.navalplanner.web.common.components.finders;

import java.util.List;

import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Implements all the methods needed to comply IBandboxFinder This is a finder
 * for {@link ExternalCompany}l in a {@link Bandbox}. Provides how many columns
 * for {@link ExternalCompany} will be shown, how to render
 * {@link ExternalCompany} object , how to do the matching, what text to show
 * when an element is selected, etc
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
public class ExternalCompanyBandboxFinder extends BandboxFinder implements
        IBandboxFinder {

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private final String headers[] = { "ID", "Name" };

    @Override
    @Transactional(readOnly = true)
    public List<ExternalCompany> getAll() {
        List<ExternalCompany> externalCompanies = externalCompanyDAO.getAll();
        initializeExternalCompanies(externalCompanies);
        return externalCompanies;
    }

    private void initializeExternalCompanies(
            List<ExternalCompany> externalCompanies) {
        for (ExternalCompany externalCompany : externalCompanies) {
            initializeExternalCompany(externalCompany);
        }
    }

    private void initializeExternalCompany(ExternalCompany externalCompany) {
        externalCompany.getName();
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        final ExternalCompany externalCompany = (ExternalCompany) obj;
        text = text.toLowerCase();
        return (externalCompany.getNif().toLowerCase().contains(
                text.toLowerCase()) || externalCompany.getName().toLowerCase()
                .contains(text));
    }

    @Override
    public String objectToString(Object obj) {
        return ((ExternalCompany) obj).getName();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return externalCompanyRenderer;
    }

    /**
     * Render for {@link ExternalCompany}
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    private final ListitemRenderer externalCompanyRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            ExternalCompany externalCompany = (ExternalCompany) data;

            item.setValue(data);

            final Listcell labelNif = new Listcell();
            labelNif.setLabel(externalCompany.getNif());
            labelNif.setParent(item);

            final Listcell labelName = new Listcell();
            labelName.setLabel(externalCompany.getName());
            labelName.setParent(item);
        }
    };
}
