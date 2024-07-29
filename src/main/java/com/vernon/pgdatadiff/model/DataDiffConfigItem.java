package com.vernon.pgdatadiff.model;

import lombok.Data;

/** 
* @author Vernon Chen
* @time 2024年7月25日 下午11:22:55 
*/
@Data
public class DataDiffConfigItem {
    private String key;
    private DataDiffCompareSetting value;
}
