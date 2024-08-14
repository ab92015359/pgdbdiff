package com.vernon.pgdatadiff.dao;

import com.google.common.collect.Maps;
import com.vernon.pgdatadiff.enums.DsEnum;
import com.vernon.pgdatadiff.model.CompareTable;
import com.vernon.pgdatadiff.pg.PostgresConnectionPool;
import com.vernon.pgdatadiff.utils.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author Vernon Chen
 * @time 2024年7月26日 上午11:06:40
 */
@Slf4j
public class DataOperationDao {

    public static Map<String, Map<String, Object>> loadIds(String configKey, DsEnum dsEnum, String schema, CompareTable ct) {
        String sql = String.format("SELECT %s FROM \"%s\".\"%s\";", StringUtils.join(ct.getTableKeyFields(), ", "), schema, ct.getTableName());
        return doQuery(configKey, dsEnum, ct, sql);
    }

    public static Map<String, Map<String, Object>> loadDatas(String configKey, DsEnum dsEnum, String schema, CompareTable ct,
            Map<String, Map<String, Object>> whereMap) {
        String whereStr = SqlUtil.buildWheres(ct, whereMap);
        String sql = String.format("SELECT * FROM \"%s\".\"%s\" WHERE %s;", schema, ct.getTableName(), whereStr);
        return doQuery(configKey, dsEnum, ct, sql);
    }

    private static Map<String, Map<String, Object>> doQuery(String configKey, DsEnum dsEnum, CompareTable ct, String sql) {
        log.debug(String.format("exec SQL for %s %s : %s", configKey, dsEnum, sql));

        Map<String, Map<String, Object>> result = Maps.newLinkedHashMap();
        try (Connection connection = PostgresConnectionPool.getConnection(configKey, dsEnum);
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
//              如果要处理所有列，可以遍历所有的列
                Map<String, Object> resultMap = Maps.newLinkedHashMap();
                Map<String, Object> idMap = Maps.newHashMap();
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnKey = resultSet.getMetaData().getColumnName(i);
                    resultMap.put(columnKey, resultSet.getObject(i));
                    if (ct.getTableKeyFields().contains(columnKey)) {
                        idMap.put(columnKey, resultSet.getObject(i));
                    }
                }
                result.put(idMap.toString(), resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
