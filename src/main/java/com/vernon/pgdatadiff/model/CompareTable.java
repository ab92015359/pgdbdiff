package com.vernon.pgdatadiff.model;

import lombok.Data;

import java.util.List;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午11:34:06
 */
@Data
public class CompareTable {
    private String tableName;
    private List<String> tableKeyFields;
    private List<String> excluededUpdateFields;
    private List<ReplaceField> replaceFields;
    private Integer batchSize;
    private Integer concurrent;

    @Data
    public class ReplaceField {
        /***
        * 需要替换的列
        **/
        private String columnKey;
        /***
         * 目标值（被替换值）
         **/
        private String targetValue;
        /***
         * 替换值
         **/
        private String replaceValue;
    }
}
