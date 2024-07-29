package com.vernon.pgdatadiff.model;

import lombok.Data;

/** 
* @author Vernon Chen
* @time 2024年7月25日 下午11:29:09 
*/
@Data
public class DBSetting {
    private String url;
    private String username;
    private String password;
    private String schema;
}
