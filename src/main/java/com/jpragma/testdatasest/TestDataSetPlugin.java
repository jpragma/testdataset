package com.jpragma.testdatasest;

import java.util.List;

/**
 * Modifies list of columns and each row before they are applied to the database.
 * This is useful when you need populate VERSION or some audit columns and you don't
 * want to repeat yourself if every test.
 */
public interface TestDataSetPlugin {
    void modifyColumns(List<String> columns);

    void modifyRow(List<Object> values);
}
