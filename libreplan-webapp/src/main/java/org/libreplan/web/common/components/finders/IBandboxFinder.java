/*
 * This file is part of LibrePlan
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

package org.libreplan.web.common.components.finders;

import java.util.List;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListitemRenderer;

/**
 * Interface for providing, displaying and matching elements for a
 * {@link BandboxSearch}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IBandboxFinder {

    /**
     * Specify here how to do the matching between an object and input text
     *
     * @param entry
     * @param text
     * @return
     */
    boolean entryMatchesText(Object obj, String text);

    /**
     * Get list of {@link BaseEntity} to fill {@link BandboxSearch}
     *
     * Executed once only when {@link BandboxSearch} is rendered for the first
     * time
     *
     * @return
     */
    List<? extends BaseEntity> getAll();

    /**
     * Returns a {@link ListModel} of objects specified by concrete classes
     * which implement this interface
     *
     * @return
     */
    ListModel getModel();

    /**
     *
     * @param model
     */
    void setModel(ListModel model);

    /**
     * Provides headers for {@link BandboxSearch}
     *
     * @return
     */
    String[] getHeaders();

    /**
     * Returns a customize {@link ListitemRenderer}
     *
     * This renderer knows how to show objects in the list of elements
     *
     * @return
     */
    ListitemRenderer getItemRenderer();

    /**
     * Text displayed for each object in the list of elements
     *
     * @param value
     * @return
     */
    String objectToString(Object obj);

    /**
     * Reset the model
     */
    void resetModel();

}
