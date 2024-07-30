package com.vernon.pgdatadiff.model;

import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月30日 下午1:12:52
 */
@Data
public class DataDiffMiscSetting {
    private String pgDumpPath = "D:\\Program Files (x86)\\pgadmin-v7\\runtime\\pg_dump.exe";
    private Boolean enableSchemaDiff = false;
    private Boolean enableDataDiff = false;
}
