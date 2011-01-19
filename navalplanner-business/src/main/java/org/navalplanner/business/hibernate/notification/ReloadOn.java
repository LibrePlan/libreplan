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
package org.navalplanner.business.hibernate.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Óscar González Fernández
 *
 */
public class ReloadOn {

    public static ReloadOn onChangeOf(Class<?>... klasses) {
        return onChangeOf(Arrays.asList(klasses));
    }

    public static ReloadOn onChangeOf(Collection<Class<?>> klasses) {
        return new ReloadOn(klasses);
    }

    private final List<Class<?>> classes;

    private ReloadOn(Collection<? extends Class<?>> classes) {
        this.classes = new ArrayList<Class<?>>(classes);
    }

    public List<Class<?>> getClassesOnWhichToReload() {
        return classes;
    }
}
