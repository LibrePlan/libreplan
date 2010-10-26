/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.resources.entities;
import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.Arrays;
import java.util.List;

/**
 * This class defines some criterion types known a priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public enum PredefinedCriterionTypes implements ICriterionType<Criterion> {

   WORK_RELATIONSHIP(_("WORK_RELATIONSHIP"), "Relationship of the resource with the enterprise ",false, false,true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return WorkingRelationship.getCriterionNames();
        }
    },
    LOCATION_GROUP(_("LOCATION_GROUP"), "Location where the workers work",
            false, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    MACHINE_LOCATION_GROUP(_("MACHINE LOCATION_GROUP"),
            "Location where there are machines", false, true, true,
            ResourceEnum.MACHINE) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    LEAVE(_("LEAVE"), "Leave",false, false, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return LeaveCriterions.getCriterionNames();
        }
    },
    TRAINING(_("TRAINING"), "Training courses and labor training",true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    JOB(_("JOB"),"Job",true, true, true, ResourceEnum.WORKER) {
        @Override
        public List<String> getPredefined() {
            return Arrays.asList();
        }
    },
    CATEGORY(_("CATEGORY"),"Professional category",true, true, true, ResourceEnum.WORKER) {
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
}
