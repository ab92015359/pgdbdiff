package com.vernon.pgdatadiff.constants;

/**
 * @author Vernon Chen
 * @time 2024年7月27日 下午11:39:31
 */
public class SqlConstant {
    public static final String INSERT_SQL = "INSERT INTO \"%s\".\"%s\" (%s) values (%s);";
    public static final String UPDATE_SQL = "UPDATE \"%s\".\"%s\" SET %s WHERE %s;";
    public static final String DELETE_SQL = "DELETE FROM \"%s\".\"%s\" WHERE %s;";
}
