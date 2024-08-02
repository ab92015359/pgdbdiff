package com.vernon.pgdatadiff.model;

import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午11:31:51
 */
@Data
public class CompareOptions {
    private DataCompare dataCompare;
    private SchemaCompare schemaCompare;
    private Integer batchSize = 1000;
    private Integer concurrent = 5;
}
