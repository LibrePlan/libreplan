package org.navalplanner.business.common.test.dbunit;

import java.lang.annotation.*;

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
