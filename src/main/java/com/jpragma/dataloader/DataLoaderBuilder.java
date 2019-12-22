package com.jpragma.dataloader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DataLoaderBuilder {
    DataLoader dataLoader = new DataLoader();

    DataLoaderBuilder plugin(DataLoaderPlugin plugin) {
        dataLoader.plugins.add(plugin);
        return this;
    }

    TableBuilder table(String name) {
        return new TableBuilder(this, name);
    }
}

class TableBuilder {
    private final DataLoaderBuilder dataLoaderBuilder;
    private final Table table;

    public TableBuilder(DataLoaderBuilder builder, String name) {
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

    private boolean saveCurrentTableDef() {
        return dataLoaderBuilder.dataLoader.tables.add(table);
    }
}

class RowBuilder {
    private final TableBuilder tableBuilder;
    private final Table table;

    RowBuilder(TableBuilder tableBuilder, Table table) {
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

class DataLoader {
    List<DataLoaderPlugin> plugins = new ArrayList<>();
    List<Table> tables = new ArrayList<>();
    void execute(Supplier<Connection> connectionSupplier) {
        applyPlugins();
        tables.forEach(table -> {
            String sql = "INSERT INTO " + table.name
                    + " (" + join(table.columns, s -> s) + ")"
                    + " VALUES (" + join(table.columns, s -> "?") + ")";
            try {
                PreparedStatement stmt = connectionSupplier.get().prepareStatement(sql);
            } catch (SQLException e) {
                toRuntime(e);
            }
        });
    }

    private void toRuntime(SQLException e) {
        throw new RuntimeException(e);
    }

    private String join(List<String> columns, UnaryOperator<String> transformer) {
        return columns.stream().map(transformer).collect(Collectors.joining(","));
    }

    private void applyPlugins() {
        tables.forEach(table -> plugins.forEach(plugin -> {
            plugin.modifyColumns(table.columns);
            table.rows.forEach(plugin::modifyRow);
        }));
    }
}

class Table {
    String name;
    List<String> columns;
    List<List<Object>> rows = new ArrayList<>();
}
