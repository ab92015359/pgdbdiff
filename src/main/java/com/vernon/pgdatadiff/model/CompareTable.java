package com.vernon.pgdatadiff.model;

import java.util.List;

import lombok.Data;

/** 
* @author Vernon Chen
* @time 2024年7月25日 下午11:34:06 
*/
@Data
public class CompareTable {
    private String tableName;
    private List<String> tableKeyFields;
    private Integer batchSize;
    private Integer concurrent;
}
