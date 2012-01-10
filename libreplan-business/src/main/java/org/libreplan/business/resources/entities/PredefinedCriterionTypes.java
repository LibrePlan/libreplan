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

package org.libreplan.business.resources.entities;
import static org.libreplan.business.i18n.I18nHelper._;

import java.util.Arrays;
import java.util.List;

import org.libreplan.business.common.Registry;

/**
 * This class defines some criterion types known a priori<br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Ignacio Díaz Teijido <ignacio.diaz@cafedered.com>
 */
public enum PredefinedCriterionTypes implements ICriterionType<Criterion> {

    LOCATION(_("LOCATION"), "Worker location",
            false, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return LocationCriteria.getCriterionNames();
        }
    },
    CATEGORY(_("CATEGORY"), "Professional category", true, true, true,
            ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return CategoryCriteria.getCriterionNames();
        }
    },
    SKILL(_("SKILL"), "Worker skill", true, true, true,
            ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    };

    private final String description;

    private final boolean allowHierarchy;

    private final boolean allowSimultaneousCriterionsPerResource;

    private final boolean enabled;

    private final ResourceEnum resource;

    private PredefinedCriterionTypes(String name, String description, boolean allowHierarchy,
            boolean allowSimultaneousCriterionsPerResource,
            boolean enabled,
            ResourceEnum resource) {
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource = allowSimultaneousCriterionsPerResource;
        this.description = description;
        this.enabled = enabled;
        this.resource = resource;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean allowHierarchy() {
        return allowHierarchy;
    }

    @Override
    public boolean isAllowSimultaneousCriterionsPerResource() {
        return allowSimultaneousCriterionsPerResource;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean isImmutable() {
        return !this.enabled;
    }

    @Override
    public Criterion createCriterion(String name) {
        return Criterion.create(name, CriterionType.fromPredefined(this));
    }

    @Override
    public Criterion createCriterionWithoutNameYet() {
        return createCriterion("");
    }

    @Override
    public boolean contains(ICriterion criterion) {
        if (criterion instanceof Criterion) {
            Criterion c = (Criterion) criterion;
            return CriterionType.asCriterionType(this).equals(c.getType());
        }

        return false;
    }

    @Override
    public boolean criterionCanBeRelatedTo(Class<? extends Resource> klass) {
        return resource.isAssignableFrom(klass);
    }

    public abstract List<String> getPredefined();

    public CriterionType getCriterionType() {
        return Registry.getCriterionTypeDAO().findByName(getName());
    }

}
