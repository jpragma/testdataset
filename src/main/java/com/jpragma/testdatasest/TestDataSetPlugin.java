package com.jpragma.testdatasest;

import java.util.List;

public interface TestDataSetPlugin {
    void modifyColumns(List<String> columns);

    void modifyRow(List<Object> values);
}
