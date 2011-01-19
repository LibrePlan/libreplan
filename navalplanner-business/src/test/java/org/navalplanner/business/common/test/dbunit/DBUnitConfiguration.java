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

package org.navalplanner.business.common.test.dbunit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * The <code>DBUnitConfiguration</code> annotation defines class-level metadata used
 * to provide DBUnit configuration data to an instance of {@link DBUnitTestExecutionListener}.
 * </p>
 *
 * @author Bob McCune
 * @version 1.0, 4/1/2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface DBUnitConfiguration {

    /**
     * The DBUnit data set configuration file locations.
     *
     * @return the location of the DBUnit data set configuration files
     */
    String[] locations() default {};

    /**
     * The DBUnit DataSet type of the configuration files.  If not specified the default will be assumed to be
     * {@link org.dbunit.dataset.xml.XmlDataSet}.
     *
     * @return the data set type of the configuration files
     */
    Class type() default org.dbunit.dataset.xml.XmlDataSet.class;

}
