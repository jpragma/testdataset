package com.jpragma.dataloader;

import java.util.List;

public interface DataLoaderPlugin {
    void modifyColumns(List<String> columns);

    void modifyRow(List<Object> values);
}
