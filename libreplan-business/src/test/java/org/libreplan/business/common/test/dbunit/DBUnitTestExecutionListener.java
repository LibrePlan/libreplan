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

package org.libreplan.business.common.test.dbunit;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * A Spring @{TestExecutionListener} used to integrate the functionality of
 * DBUnit.
 *
 * @author Bob McCune
 * @version 1.0
 */
public class DBUnitTestExecutionListener extends
        TransactionalTestExecutionListener {

    private static final Log logger = LogFactory
            .getLog(DBUnitTestExecutionListener.class);

    private static final String DEFAULT_DATASOURCE_NAME = "dataSource";
    private static final String TABLE_TYPES[] = { "TABLE", "ALIAS" };

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {

        super.beforeTestMethod(testContext);

        DataSource dataSource = getDataSource(testContext);
        Connection conn = DataSourceUtils.getConnection(dataSource);
        IDatabaseConnection dbUnitConn = getDBUnitConnection(conn);
        try {
            IDataSet dataSets[] = getDataSets(testContext);
            for (IDataSet dataSet : dataSets) {
                DatabaseOperation.CLEAN_INSERT.execute(dbUnitConn, dataSet);
                logger.info("Performed CLEAN_INSERT of IDataSet.");
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private DataSource getDataSource(TestContext context) {
        DataSource dataSource;
        Map beans = context.getApplicationContext().getBeansOfType(
                DataSource.class);
        if (beans.size() > 1) {
            dataSource = (DataSource) beans.get(DEFAULT_DATASOURCE_NAME);
            if (dataSource == null) {
                throw new NoSuchBeanDefinitionException(
                        "Unable to locate default data source.");
            }
        } else {
            dataSource = (DataSource) beans.values().iterator().next();
        }
        return dataSource;
    }

    private IDatabaseConnection getDBUnitConnection(Connection c)
            throws DatabaseUnitException
            {
        IDatabaseConnection conn = new DatabaseConnection(c);
        DatabaseConfig config = conn.getConfig();
        config.setFeature("http://www.dbunit.org/features/qualifiedTableNames",
                true);
        config.setProperty("http://www.dbunit.org/properties/tableType",
                TABLE_TYPES);
        return conn;
    }

    private IDataSet[] getDataSets(TestContext context) throws Exception {
        String dataFiles[] = getDataLocations(context);
        IDataSet dataSets[] = new IDataSet[dataFiles.length];
        for (int i = 0; i < dataFiles.length; i++) {
            Resource resource = new ClassPathResource(dataFiles[i]);
            Class clazz = getDataSetType(context);
            Constructor con = clazz.getConstructor(InputStream.class);
            dataSets[i] = (IDataSet) con.newInstance(resource.getInputStream());
        }
        return dataSets;
    }

    protected Class getDataSetType(TestContext context) {
        Class<?> testClass = context.getTestClass();
        DBUnitConfiguration config = testClass
                .getAnnotation(DBUnitConfiguration.class);
        return config.type();
    }

    private String[] getDataLocations(TestContext context) {
        Class<?> testClass = context.getTestClass();
        DBUnitConfiguration config = testClass
                .getAnnotation(DBUnitConfiguration.class);
        if (config == null) {
            throw new IllegalStateException("Test class '" + testClass
                    + " has is missing @DBUnitConfiguration annotation.");
        }
        if (config.locations().length == 0) {
            throw new IllegalStateException(
                    "@DBUnitConfiguration annotation doesn't specify any DBUnit configuration locations.");
        }
        return config.locations();
    }
}