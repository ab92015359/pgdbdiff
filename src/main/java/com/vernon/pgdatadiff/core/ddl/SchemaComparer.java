package com.vernon.pgdatadiff.core.ddl;

import java.util.Map.Entry;

import com.vernon.pgdatadiff.constants.SettingConstant;
import com.vernon.pgdatadiff.core.DBDiffContext;
import com.vernon.pgdatadiff.model.DataDiffConfigItem;
import com.vernon.pgdatadiff.utils.FileUtil;
import com.vernon.pgdatadiff.utils.PGUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月30日 下午1:39:44
 */
@Slf4j
public class SchemaComparer {
    public static void compareSchema() {
        generateDumpFiles();
    }

    private static void generateDumpFiles() {
        int totalConfigCount = DBDiffContext.configMap.size();
        int processedConfigCount = 0;

        String sourceDDLFilePath = "";
        String sourceDDLTempFilePath = "";
        String targetDDLFilePath = "";
        for (Entry<String, DataDiffConfigItem> entry : DBDiffContext.configMap.entrySet()) {
            if (!DBDiffContext.execCommands.contains(entry.getKey().toUpperCase()) && !DBDiffContext.execCommands.contains(SettingConstant.ALL_COMMAND)) {
                continue;
            }
            log.info(String.format("========== Start to process diff set for %s with progress(%s/%s) ==========", entry.getKey(), processedConfigCount + 1,
                    totalConfigCount));

            String dir = DBDiffContext.identifier + System.getProperty("file.separator") + entry.getKey() + System.getProperty("file.separator") + "dump";
            String sourceDDLFileName = "sourceDDL.sql";
            String sourceDDLTempFileName = "sourceDDLTemp.sql";

            sourceDDLFilePath = FileUtil.createFile(dir, sourceDDLFileName);
            sourceDDLTempFilePath = FileUtil.createFile(dir, sourceDDLTempFileName);

            String srouceSchema = entry.getValue().getValue().getSource().getSchema();
            String targetSchema = entry.getValue().getValue().getTarget().getSchema();

            if (srouceSchema.equals(targetSchema)) {
                PGUtil.dump(sourceDDLFilePath, entry.getValue().getValue().getSource(),
                        entry.getValue().getValue().getCompareOptions().getSchemaCompare().getExcluedTables());
            } else {
                PGUtil.dump(sourceDDLTempFilePath, entry.getValue().getValue().getSource(),
                        entry.getValue().getValue().getCompareOptions().getSchemaCompare().getExcluedTables());
                FileUtil.replaceFile(sourceDDLTempFilePath, sourceDDLFilePath, entry.getValue().getValue().getSource().getSchema(),
                        entry.getValue().getValue().getTarget().getSchema());
            }

            String targetDDLFileName = "targetDDL.sql";
            targetDDLFilePath = FileUtil.createFile(dir, targetDDLFileName);
            PGUtil.dump(targetDDLFilePath, entry.getValue().getValue().getTarget(),
                    entry.getValue().getValue().getCompareOptions().getSchemaCompare().getExcluedTables());

            PGUtil.compare(sourceDDLFilePath, targetDDLFilePath, DBDiffContext.identifier + System.getProperty("file.separator") + entry.getKey(),
                    "SchemaDiff.sql");

            processedConfigCount++;
        }

        log.info("finish to generate ddl.");
    }
}
