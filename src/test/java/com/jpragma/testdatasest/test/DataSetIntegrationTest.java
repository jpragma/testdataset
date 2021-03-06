package com.jpragma.testdatasest.test;

import com.jpragma.testdatasest.TestDataSet;
import com.jpragma.testdatasest.TestDataSetBuilder;
import com.jpragma.testdatasest.TestDataSetPlugin;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataSetIntegrationTest {

    @Test
    void dataSetCreatesProperStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement stmt1 = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(stmt1);

        TestDataSet tds = new TestDataSetBuilder(() -> mockConnection)
                .plugin(new DummyPlugin())
                .table("DEPARTMENT")
                .columns("DEP_ID", "NAME")
                .row(11, "CS")
                .table("STUDENT")
                .columns("ID", "NAME", "DEP_ID", "SCORE", "DOB")
                .row(1, "John Doe", 11, 92.2, LocalDate.parse("1995-06-12"))
                .row(2, "Jane Roe", 11, 98.3, LocalDate.parse("2000-01-23"))
                .build();
        tds.load();
        verify(mockConnection).prepareStatement("INSERT INTO DEPARTMENT (DEP_ID,NAME,VERSION) VALUES (?,?,?)");
        verify(mockConnection).prepareStatement("INSERT INTO STUDENT (ID,NAME,DEP_ID,SCORE,DOB,VERSION) VALUES (?,?,?,?,?,?)");
    }

    @Test
    void insertDataIntoH2Table() throws SQLException {
        Connection connection = inMemoryDataSource().getConnection();
        executeDDL(connection);
        new TestDataSetBuilder(() -> connection)
                .table("CUSTOMER")
                .columns("CUSTOMER_ID", "NAME", "DOB", "ACCOUNT_BALANCE", "CREATED_ON", "RANK")
                .row(1, "John Doe", LocalDate.parse("1980-05-14"), 187.78, LocalDate.parse("2019-12-22").atTime(14, 11), null)
                .build()
                .load();
        assertInsertedData(connection);
    }

    private void assertInsertedData(Connection connection) throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM CUSTOMER");
        assertThat(rs.next()).isTrue();
        assertThat(rs.getInt("CUSTOMER_ID")).isEqualTo(1);
        assertThat(rs.getString("NAME")).isEqualTo("John Doe");
        assertThat(rs.getDate("DOB").toLocalDate()).isEqualTo(LocalDate.parse("1980-05-14"));
        assertThat(rs.getDouble("ACCOUNT_BALANCE")).isEqualTo(187.78);
        assertThat(rs.getObject("RANK")).isNull();
    }

    private void executeDDL(Connection connection) throws SQLException {
        String ddl = "CREATE TABLE CUSTOMER (CUSTOMER_ID INT, NAME VARCHAR(100), DOB DATE, ACCOUNT_BALANCE DOUBLE, CREATED_ON TIMESTAMP, RANK INT NULL)";
        connection.createStatement().execute(ddl);
    }

    private DataSource inMemoryDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:");
        return dataSource;
    }

    static class DummyPlugin implements TestDataSetPlugin {
        @Override
        public void modifyColumns(List<String> columns) {
            columns.add("VERSION");
        }

        @Override
        public void modifyRow(List<Object> values) {
            values.add(0);
        }
    }


}


