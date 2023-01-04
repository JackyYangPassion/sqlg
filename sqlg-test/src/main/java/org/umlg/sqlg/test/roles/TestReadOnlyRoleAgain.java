package org.umlg.sqlg.test.roles;

import org.junit.Assert;
import org.junit.Test;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="https://github.com/pietermartin">Pieter Martin</a>
 * Date: 2018/07/21
 */
public class TestReadOnlyRoleAgain {

    @Test
    public void test441() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:file:src/test/db2/sqlgraphdb", "SA", "");
        try (Statement statement = c.createStatement()) {
            statement.execute("CREATE TABLE \"PUBLIC\".\"V_A\" (\"ID\" BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, \"name\" LONGVARCHAR);");
            statement.execute("INSERT INTO \"PUBLIC\".\"V_A\" (\"name\") VALUES ('john');");
            statement.execute("INSERT INTO \"PUBLIC\".\"V_A\" (\"name\") VALUES ('joe');");
            c.commit();
            ResultSet rs = statement.executeQuery("select \"name\" from \"PUBLIC\".\"V_A\";");
            Set<String> names = new HashSet<>(Set.of("john", "joe"));
            while (rs.next()) {
                names.remove(rs.getString(1));
            }
            Assert.assertTrue(names.isEmpty());
            statement.execute("CREATE USER \"sqlgReadOnly\" PASSWORD 'sqlgReadOnly'");
            statement.execute("CREATE ROLE \"READ_ONLY\"");
            statement.execute("GRANT READ_ONLY TO \"sqlgReadOnly\"");
            statement.execute("GRANT SELECT ON TABLE \"PUBLIC\".\"V_A\" TO READ_ONLY");
            c.commit();
        } finally {
            c.close();
        }
        Connection readOnlyConnection = DriverManager.getConnection("jdbc:hsqldb:file:src/test/db2/sqlgraphdb", "sqlgReadOnly", "sqlgReadOnly");
        try (Statement statement = readOnlyConnection.createStatement()) {
            ResultSet rs = statement.executeQuery("select \"name\" from \"PUBLIC\".\"V_A\";");
            Set<String> names = new HashSet<>(Set.of("john", "joe"));
            while (rs.next()) {
                names.remove(rs.getString(1));
            }
            Assert.assertTrue(names.isEmpty());
            DatabaseMetaData metadata = readOnlyConnection.getMetaData();
            ResultSet schemaRs = metadata.getSchemas(null, "PUBLIC");
            boolean result = schemaRs.next();
            Assert.assertTrue(result);
        } finally {
            readOnlyConnection.close();
        }
    }
}
