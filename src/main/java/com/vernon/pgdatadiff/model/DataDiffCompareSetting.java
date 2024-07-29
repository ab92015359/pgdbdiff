package com.vernon.pgdatadiff.model;

import lombok.Data;

/** 
* @author Vernon Chen
* @time 2024年7月25日 下午11:30:39 
*/
@Data
public class DataDiffCompareSetting {
    private DBSetting source;
    private DBSetting target;
    private CompareOptions compareOptions;
}
