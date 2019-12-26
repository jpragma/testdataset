package com.jpragma.dataloader;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DataLoaderBuilder {
    final DataLoader dataLoader;

    public DataLoaderBuilder(Supplier<Connection> connectionSupplier) {
        dataLoader  = new DataLoader(connectionSupplier);
    }

    public DataLoaderBuilder plugin(DataLoaderPlugin plugin) {
        dataLoader.plugins.add(plugin);
        return this;
    }

    public TableBuilder table(String name) {
        return new TableBuilder(this, name);
    }

    public static class TableBuilder {
        private final DataLoaderBuilder dataLoaderBuilder;
        private final Table table;

        private TableBuilder(DataLoaderBuilder builder, String name) {
            this.dataLoaderBuilder = builder;
            this.table = new Table();
            table.name = name;
        }

        public RowBuilder columns(String... names) {
            table.columns = new ArrayList<>(Arrays.asList(names));
            return new RowBuilder(this, table);
        }

        public TableBuilder andAnotherTable(String name) {
            saveCurrentTableDef();
            return new TableBuilder(dataLoaderBuilder, name);
        }

        public DataLoader build() {
            saveCurrentTableDef();
            return dataLoaderBuilder.dataLoader;
        }

        private void saveCurrentTableDef() {
            dataLoaderBuilder.dataLoader.tables.add(table);
        }
    }

    public static class RowBuilder {
        private final TableBuilder tableBuilder;
        private final Table table;

        private RowBuilder(TableBuilder tableBuilder, Table table) {
            this.tableBuilder = tableBuilder;
            this.table = table;
        }

        public RowBuilder row(Object... values) {
            validateSizeMatchesColumns(values.length, table.columns.size());
            table.rows.add(new ArrayList<>(Arrays.asList(values)));
            return new RowBuilder(tableBuilder, table);
        }

        private void validateSizeMatchesColumns(int numOfValues, int numOfCols) {
            if (numOfValues != numOfCols) {
                throw new IllegalArgumentException(numOfValues + " values provided, but " + numOfCols + " columns have been previously defined");
            }
        }

        public TableBuilder table(String name) {
            return tableBuilder.andAnotherTable(name);
        }

        public DataLoader build() {
            return tableBuilder.build();
        }
    }

    static class Table {
        String name;
        List<String> columns;
        List<List<Object>> rows = new ArrayList<>();
    }
}

