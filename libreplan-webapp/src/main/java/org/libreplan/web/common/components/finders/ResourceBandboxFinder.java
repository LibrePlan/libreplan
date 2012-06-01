/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * This is a finder for {@link Resource Resources} in a {@link Bandbox}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Repository
public class ResourceBandboxFinder extends BandboxFinder implements IBandboxFinder {

    @Autowired
    private IResourceDAO resourceDAO;

    private final String headers[] = { _("Resource") };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> getAll() {
        return resourceDAO.findAll();
    }

    @Override
    public boolean entryMatchesText(Object obj, String text) {
        Resource resource = (Resource) obj;
        text = StringUtils.trim(text.toLowerCase());
        return checkContainsText(resource.getShortDescription(), text);
    }

    private boolean checkContainsText(String original, String text) {
        return original.toLowerCase().contains(text);
    }

    @Override
    public String objectToString(Object obj) {
        Resource resource = (Resource) obj;
        return resource.getShortDescription();
    }

    @Override
    public String[] getHeaders() {
        return headers.clone();
    }

    @Override
    public ListitemRenderer getItemRenderer() {
        return resourcesRenderer;
    }

    private final ListitemRenderer resourcesRenderer = new ListitemRenderer() {
        @Override
        public void render(Listitem item, Object data) {
            Resource resource = (Resource) data;
            item.setValue(data);

            item.appendChild(new Listcell(resource.getShortDescription()));
        }
    };
}
