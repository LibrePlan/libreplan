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
package org.zkoss.ganttz.timetracker.zoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SeveralModifiers implements IDetailItemModifier { 

    private final List<IDetailItemModifier> modifiers;

    public static IDetailItemModifier empty() {
        return new SeveralModifiers(Collections.emptyList());
    }

    public static IDetailItemModifier create(IDetailItemModifier... modifiers) {
        return new SeveralModifiers(Arrays.asList(modifiers));
    }

    public static IDetailItemModifier create(Collection<? extends IDetailItemModifier> modifiers) {
        return new SeveralModifiers(modifiers);
    }

    private SeveralModifiers(Collection<? extends IDetailItemModifier> modifiers) {
        Validate.noNullElements(modifiers);
        this.modifiers = new ArrayList<>(modifiers);
    }

    @Override
    public DetailItem applyModificationsTo(DetailItem item, ZoomLevel z) {
        DetailItem result = item;
        for (IDetailItemModifier each : modifiers) {
            result = each.applyModificationsTo(result, z);
        }

        return result;
    }

}
