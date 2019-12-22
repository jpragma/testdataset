package com.jpragma.dataloader;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataLoaderBuilderTest {

    @Test
    void dataLoaderCreatesProperStatement() throws SQLException {
        DataLoader loader = new DataLoaderBuilder()
                .plugin(new DummyPlugin())
                .table("DEPARTMENT")
                .columns("DEP_ID", "NAME")
                .row(11, "CS")
                .table("STUDENT")
                .columns("ID", "NAME", "DEP_ID", "SCORE", "DOB")
                .row(1, "John Doe", 11, 92.2, LocalDate.parse("1995-06-12"))
                .row(2, "Jane Roe", 11, 98.3, LocalDate.parse("2000-01-23"))
                .build();
        Connection mockConnection = mock(Connection.class);
        PreparedStatement stmt1 = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(stmt1);
        loader.execute(() -> mockConnection);
        assertLoader(loader);
        verify(mockConnection).prepareStatement("INSERT INTO DEPARTMENT (DEP_ID,NAME,VERSION) VALUES (?,?,?)");
        verify(mockConnection).prepareStatement("INSERT INTO STUDENT (ID,NAME,DEP_ID,SCORE,DOB,VERSION) VALUES (?,?,?,?,?,?)");
    }

    private void assertLoader(DataLoader loader) {
        assertThat(loader.plugins).hasSize(1);
        assertThat(loader.plugins.get(0)).isInstanceOf(DummyPlugin.class);

        assertThat(loader.tables).hasSize(2);

        Table tbl1 = loader.tables.get(0);
        assertThat(tbl1.name).isEqualTo("DEPARTMENT");
        assertThat(tbl1.columns).containsExactly("DEP_ID", "NAME", "VERSION");
        assertThat(tbl1.rows).hasSize(1);
        assertThat(tbl1.rows.get(0)).containsExactly(11, "CS", 0);

        Table tbl2 = loader.tables.get(1);
        assertThat(tbl2.name).isEqualTo("STUDENT");
        assertThat(tbl2.columns).containsExactly("ID", "NAME", "DEP_ID", "SCORE", "DOB", "VERSION");
        assertThat(tbl2.rows).hasSize(2);
    }

    static class DummyPlugin implements DataLoaderPlugin {
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


