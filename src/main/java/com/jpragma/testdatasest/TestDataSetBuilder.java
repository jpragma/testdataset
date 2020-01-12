package com.jpragma.testdatasest;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Builder that provides DSL for creation of TestDataSet.
 * Start by calling plugin() or table()
 */
public class TestDataSetBuilder {
    final TestDataSet testDataSet;

    public TestDataSetBuilder(Supplier<Connection> connectionSupplier) {
        testDataSet = new TestDataSet(connectionSupplier);
    }

    /**
     * Plugins will be applied in the same order they added
     * @param plugin Plugin that modifies columns and rows
     * @return "this" for chaining calls
     */
    public TestDataSetBuilder plugin(TestDataSetPlugin plugin) {
        testDataSet.plugins.add(plugin);
        return this;
    }

    /**
     * Table to be populated
     * @param name table name
     * @return "this" for chaining calls
     */
    public TableBuilder table(String name) {
        return new TableBuilder(this, name);
    }

    public static class TableBuilder {
        private final TestDataSetBuilder testDataSetBuilder;
        private final Table table;

        private TableBuilder(TestDataSetBuilder builder, String name) {
            this.testDataSetBuilder = builder;
            this.table = new Table();
            table.name = name;
        }

        /**
         * List of columns to be populated
         * @param names names of columns
         * @return RowBuilder
         */
        public RowBuilder columns(String... names) {
            table.columns = new ArrayList<>(Arrays.asList(names));
            return new RowBuilder(this, table);
        }

        public TableBuilder andAnotherTable(String name) {
            saveCurrentTableDef();
            return new TableBuilder(testDataSetBuilder, name);
        }

        public TestDataSet build() {
            saveCurrentTableDef();
            return testDataSetBuilder.testDataSet;
        }

        private void saveCurrentTableDef() {
            testDataSetBuilder.testDataSet.tables.add(table);
        }
    }

    public static class RowBuilder {
        private final TableBuilder tableBuilder;
        private final Table table;

        private RowBuilder(TableBuilder tableBuilder, Table table) {
            this.tableBuilder = tableBuilder;
            this.table = table;
        }

        /**
         * @param values Values of individual row to be inserted
         * @return Another RowBuilder for chaining calls
         */
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

        /**
         * Creates ready to use TestDataSet
         * @return TestDataSet
         */
        public TestDataSet build() {
            return tableBuilder.build();
        }
    }

    static class Table {
        String name;
        List<String> columns;
        List<List<Object>> rows = new ArrayList<>();
    }
}

