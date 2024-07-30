package com.vernon.pgdatadiff.model;

import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午11:29:09
 */
@Data
public class DBSetting {
    private String host;
    private Integer port;
    private String dbName;
    private String schema;
    private String username;
    private String password;
//    private String url;
//    private String username;
//    private String password;
//    private String schema;

    public String getUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s?currentSchema=%s", host, port, dbName, schema);
    }
}
