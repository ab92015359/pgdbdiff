package com.vernon.pgdatadiff.model;

import java.util.List;

import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午11:32:24
 */
@Data
public class DataCompare {
    private List<CompareTable> includedTables;
}
