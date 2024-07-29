package com.vernon.pgdatadiff.pg;

import java.sql.Connection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.vernon.pgdatadiff.enums.DsEnum;
import com.vernon.pgdatadiff.model.DBSetting;
import com.vernon.pgdatadiff.model.DataDiffConfigItem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Vernon Chen
 * @time 2024年7月26日 上午12:00:00
 */
public class PostgresConnectionPool {
    private static final Map<String, HikariDataSource> dsMap = Maps.newHashMap();

    private static void createDataSource(String dsKey, DBSetting setting) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(setting.getUrl());
        config.setUsername(setting.getUsername());
        config.setPassword(setting.getPassword());
        config.addDataSourceProperty("poolName", "PostgreSQL_HikariCP");
        config.addDataSourceProperty("maximumPoolSize", 50);
        config.setConnectionTimeout(600000);

        dsMap.put(dsKey, new HikariDataSource(config));
    }

    public static void initDataSources(Map<String, DataDiffConfigItem> configMap) {
        for (Entry<String, DataDiffConfigItem> entry : configMap.entrySet()) {
            createDataSource(entry.getKey() + ":" + DsEnum.SOURCE, entry.getValue().getValue().getSource());
            createDataSource(entry.getKey() + ":" + DsEnum.TARGET, entry.getValue().getValue().getTarget());
        }
    }

    public static Connection getConnection(String configKey, DsEnum dsEnum) throws Exception {
        String dsKey = configKey + ":" + dsEnum;
        if (dsMap.containsKey(dsKey)) {
            return dsMap.get(dsKey).getConnection();
        }
        throw new Exception(String.format("找不到【%s】的数据源", dsKey));
    }

}
