package com.jpragma.dataloader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DataLoader {
    final List<DataLoaderPlugin> plugins = new ArrayList<>();
    final List<DataLoaderBuilder.Table> tables = new ArrayList<>();
    final Supplier<Connection> connectionSupplier;

    DataLoader(Supplier<Connection> connectionSupplier) {
        // package-private constructor, only builder can create this class
        this.connectionSupplier = connectionSupplier;
    }

    public void load() {
        applyPlugins();
        tables.forEach(table -> {
            String sql = "INSERT INTO " + table.name
                    + " (" + join(table.columns, s -> s) + ")"
                    + " VALUES (" + join(table.columns, s -> "?") + ")";
            try {
                PreparedStatement stmt = connectionSupplier.get().prepareStatement(sql);
                table.rows.forEach(vals -> {
                    try {
                        for (int i = 0; i < vals.size(); i++) {
                            setStatementValue(stmt, i + 1, vals.get(i));
                        }
                        stmt.execute();
                        stmt.clearParameters();
                    } catch (SQLException e) {
                        toRuntime(e);
                    }
                });
            } catch (SQLException e) {
                toRuntime(e);
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private void setStatementValue(PreparedStatement stmt, int index, Object val) throws SQLException {
        if (val == null) {
            stmt.setObject(index, null);
        } else {
            Class clazz = val.getClass();
            if (clazz.equals(String.class)) {
                stmt.setString(index, (String) val);
            } else if (clazz.equals(Integer.class)) {
                stmt.setInt(index, (Integer) val);
            } else if (clazz.equals(Long.class)) {
                stmt.setLong(index, (Long) val);
            } else if (clazz.equals(Double.class)) {
                stmt.setDouble(index, (Double) val);
            } else if (clazz.equals(LocalDate.class)) {
                stmt.setDate(index, new java.sql.Date(toEpochMilli(((LocalDate) val).atStartOfDay())));
            } else if (clazz.equals(LocalDateTime.class)) {
                stmt.setDate(index, new java.sql.Date(toEpochMilli(((LocalDateTime) val))));
            } else {
                throw new IllegalArgumentException(clazz + " is not supported yet");
            }
        }
    }

    private long toEpochMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
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
