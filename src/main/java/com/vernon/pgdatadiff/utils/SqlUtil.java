package com.vernon.pgdatadiff.utils;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.vernon.pgdatadiff.model.CompareTable;

/**
 * @author Vernon Chen
 * @time 2024年7月26日 下午11:46:25
 */
public class SqlUtil {

    public static String[] buildInsert(CompareTable ct, Map<String, Object> sourceRowColumns) {
        String insertKeys = "";
        String insertValues = "";
        int i = 0;
        for (Entry<String, Object> sourceColumnEntry : sourceRowColumns.entrySet()) {
            if (i != 0) {
                insertKeys += ", ";
                insertValues += ", ";
            }
            insertKeys += "\"" + sourceColumnEntry.getKey() + "\"";
            Object value = sourceColumnEntry.getValue();
            if (value == null) {
                insertValues += "NULL";
            } else {
                if (value instanceof String) {
                    value = ((String) value).replaceAll("'", "''");
                    value = ((String) value).replaceAll("\\\\", "\\\\\\\\");
                }
                insertValues += "'" + value + "'";
            }
            i++;
        }
        return new String[] { insertKeys, insertValues };
    }

    public static String buildUpdate(CompareTable ct, Map<String, Object> diffRow) {
        String updateSql = "";
        int i = 0;
        for (Entry<String, Object> diffEntry : diffRow.entrySet()) {
            if (i != 0) {
                updateSql += ", ";
            }

            updateSql += "\"" + diffEntry.getKey() + "\" = ";
            Object value = diffEntry.getValue();
            if (value == null) {
                updateSql += "NULL";
            } else {
                if (value instanceof String) {
                    value = ((String) value).replaceAll("'", "''");
                    value = ((String) value).replaceAll("\\\\", "\\\\\\\\");
                }
                updateSql += "'" + value + "'";
            }

            i++;
        }
        return updateSql;
    }

    public static String buildWhere(CompareTable ct, Map<String, Object> whereMap) {
        String whereStr = "";
        int i = 0;
        for (Entry<String, Object> whereItem : whereMap.entrySet()) {
            if (i != 0) {
                whereStr += " AND ";
            }
            whereStr += "\"" + whereItem.getKey() + "\" = '" + whereItem.getValue() + "'";
            i++;
        }
        return whereStr;
    }

    public static String buildWheres(CompareTable ct, Map<String, Map<String, Object>> whereMap) {
        String whereStr = "";
        if (ct.getTableKeyFields().size() == 1) {
            whereStr += "\"" + ct.getTableKeyFields().get(0) + "\" in (";
            int i = 0;
            for (Entry<String, Map<String, Object>> whereItems : whereMap.entrySet()) {
                if (i != 0) {
                    whereStr += ", ";
                }
                for (Entry<String, Object> whereItem : whereItems.getValue().entrySet()) {

                    whereStr += "'" + whereItem.getValue() + "'";

                }
                i++;
            }
            whereStr += ")";
        } else {
            int j = 0;
            for (Entry<String, Map<String, Object>> whereItems : whereMap.entrySet()) {
                if (j != 0) {
                    whereStr += " OR (";
                }
                if (Strings.isNullOrEmpty(whereStr)) {
                    whereStr += "(";
                }
                whereStr += buildWhere(ct, whereItems.getValue());
                whereStr += ")";
                j++;
            }
        }
        return whereStr;
    }
}
