package com.vernon.pgdatadiff.core.dml;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.springframework.util.ObjectUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vernon.pgdatadiff.constants.SettingConstant;
import com.vernon.pgdatadiff.constants.SqlConstant;
import com.vernon.pgdatadiff.core.DBDiffContext;
import com.vernon.pgdatadiff.core.dml.async.InsertDataAsyncProcess;
import com.vernon.pgdatadiff.core.dml.async.UpdateDataAsyncProcess;
import com.vernon.pgdatadiff.dao.DataOperationDao;
import com.vernon.pgdatadiff.enums.DsEnum;
import com.vernon.pgdatadiff.model.CompareTable;
import com.vernon.pgdatadiff.model.DataDiffConfigItem;
import com.vernon.pgdatadiff.model.EchoObject;
import com.vernon.pgdatadiff.utils.FileUtil;
import com.vernon.pgdatadiff.utils.SqlUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月26日 上午11:01:50
 */
@Slf4j
public class DataComparer {

    public static void compareData() {
        int totolRowCount = 0;
        int totalConfigCount = DBDiffContext.configMap.size();
        int processedConfigCount = 0;
        for (Entry<String, DataDiffConfigItem> entry : DBDiffContext.configMap.entrySet()) {
            if (!DBDiffContext.execCommands.contains(entry.getKey().toUpperCase()) && !DBDiffContext.execCommands.contains(SettingConstant.ALL_COMMAND)) {
                continue;
            }
            log.info(String.format("========== Start to process diff set for %s with progress(%s/%s) ==========", entry.getKey(), processedConfigCount + 1,
                    totalConfigCount));
            int totalTableCount = entry.getValue().getValue().getCompareOptions().getDataCompare().getTables().size();
            int processedTableCount = 0;
            for (CompareTable ct : entry.getValue().getValue().getCompareOptions().getDataCompare().getTables()) {
                log.info(String.format("========== Start to compare %s's table %s with progress(%s/%s) ==========", entry.getKey(), ct.getTableName(),
                        processedTableCount + 1, totalTableCount));
                String sourceSchema = entry.getValue().getValue().getSource().getSchema();
                Map<String, Map<String, Object>> sourceIdMap = DataOperationDao.loadIds(entry.getKey(), DsEnum.SOURCE, sourceSchema, ct);
                String targetSchema = entry.getValue().getValue().getTarget().getSchema();
                Map<String, Map<String, Object>> targetIdMap = DataOperationDao.loadIds(entry.getKey(), DsEnum.TARGET, targetSchema, ct);

                Map<String, Map<String, Object>> onlySourceMap = Maps.newHashMap();
                Map<String, Map<String, Object>> bothMap = Maps.newHashMap();
                Map<String, Map<String, Object>> onlyTargetMap = Maps.newHashMap();
                for (String sourceId : sourceIdMap.keySet()) {
                    if (targetIdMap.containsKey(sourceId)) {
                        bothMap.put(sourceId, sourceIdMap.get(sourceId));
                    } else {
                        onlySourceMap.put(sourceId, sourceIdMap.get(sourceId));
                    }
                }
                for (String targetId : targetIdMap.keySet()) {
                    if (!sourceIdMap.containsKey(targetId)) {
                        onlyTargetMap.put(targetId, targetIdMap.get(targetId));
                    }
                }
                totolRowCount += onlySourceMap.size() + bothMap.size() + onlyTargetMap.size();
                generateSql(entry.getKey(), entry.getValue(), ct, onlySourceMap, bothMap, onlyTargetMap);

                processedTableCount++;
            }
            processedConfigCount++;
        }

        log.info("finish to generate sql with totle count " + totolRowCount);
        DBDiffContext.isFinished = true;
    }

    private static void generateSql(String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct, Map<String, Map<String, Object>> onlySourceMap,
            Map<String, Map<String, Object>> bothMap, Map<String, Map<String, Object>> onlyTargetSet) {
        log.debug(String.format("start to generate insert sql for talbe %s, count %s.", ct.getTableName(), onlySourceMap.size()));
        if (!ObjectUtils.isEmpty(onlySourceMap)) {
            generateInsertSql(configKey, dataDiffConfigItem, ct, onlySourceMap);
        }
        log.debug("finish to generate insert sql.");

        log.debug(String.format("start to generate update sql for talbe %s, count %s.", ct.getTableName(), bothMap.size()));
        if (!ObjectUtils.isEmpty(bothMap)) {
            generateUpdateSql(configKey, dataDiffConfigItem, ct, bothMap);
        }
        log.debug("finish to generate update sql.");

        log.debug(String.format("start to generate delete sql for talbe %s, count %s.", ct.getTableName(), onlyTargetSet.size()));
        if (!ObjectUtils.isEmpty(onlyTargetSet)) {
            generateDeleteSql(configKey, dataDiffConfigItem, ct, onlyTargetSet);
        }
        log.debug("finish to generate delete sql.");
    }

//    private static void generateInsertSqlSync(String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct,
//            Map<String, Map<String, Object>> onlySourceMap) {
//        String filePath = FileUtil.createFile(DataDiffContext.identifier, configKey + ".sql");
//        String sql = "INSERT INTO \"%s\".\"%s\" (%s) values (%s)";
//
//        Map<String, Map<String, Object>> sourceDatas = DataOperationDao.loadDatas(configKey, DsEnum.SOURCE,
//                dataDiffConfigItem.getValue().getSource().getSchema(), ct, onlySourceMap);
//        for (Entry<String, Map<String, Object>> sourceRowEntry : sourceDatas.entrySet()) {
//            String[] insertSql = SqlUtil.buildInsert(ct, sourceRowEntry.getValue());
//            FileUtil.echo(filePath, String.format(sql, dataDiffConfigItem.getValue().getTarget().getSchema(), ct.getTableName(), insertSql[0], insertSql[1]));
//        }
//    }

    private static void generateInsertSql(String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct,
            Map<String, Map<String, Object>> onlySourceMap) {
        DBDiffContext.initForkCount();
        Integer poolSize = Optional.fromNullable(ct.getConcurrent()).or(dataDiffConfigItem.getValue().getCompareOptions().getConcurrent());
        ForkJoinPool pool = new ForkJoinPool(poolSize);
        ForkJoinTask<Integer> task = new InsertDataAsyncProcess(Lists.newArrayList(onlySourceMap.keySet()), configKey, dataDiffConfigItem, ct, onlySourceMap);
        pool.submit(task);

        try {
            if (task.get() != onlySourceMap.size()) {
                log.error(String.format("error to generate insert sql with number %s/%s.", task.get(), onlySourceMap.size()));
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    private static void generateUpdateSqlSync(String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct, Map<String, Map<String, Object>> bothMap) {
//        String filePath = FileUtil.createFile(DataDiffContext.identifier, configKey + ".sql");
//        String sql = "UPDATE \"%s\".\"%s\" SET %s WHERE %s";
//        Map<String, Map<String, Object>> sourceDatas = DataOperationDao.loadDatas(configKey, DsEnum.SOURCE,
//                dataDiffConfigItem.getValue().getSource().getSchema(), ct, bothMap);
//        Map<String, Map<String, Object>> targetDatas = DataOperationDao.loadDatas(configKey, DsEnum.TARGET,
//                dataDiffConfigItem.getValue().getTarget().getSchema(), ct, bothMap);
//
//        for (Entry<String, Map<String, Object>> sourceRowEntry : sourceDatas.entrySet()) {
//            String id = sourceRowEntry.getKey();
//            Map<String, Object> sourceRowColumns = sourceRowEntry.getValue();
//            Map<String, Object> targetRowColumns = targetDatas.get(id);
//
//            Map<String, Object> diffColumn = Maps.newHashMap();
//            for (Entry<String, Object> sourceColumnEntry : sourceRowColumns.entrySet()) {
//                String columnKey = sourceColumnEntry.getKey();
//                Object sourceValue = sourceColumnEntry.getValue();
//                Object targetValue = targetRowColumns.get(columnKey);
//                if (sourceValue == null && targetValue == null) {
//                    continue;
//                } else if (sourceValue == null && targetValue != null) {
//                    diffColumn.put(columnKey, sourceValue);
//                } else if (sourceValue != null && targetValue == null) {
//                    diffColumn.put(columnKey, sourceValue);
//                } else if (!sourceValue.equals(targetValue)) {
//                    diffColumn.put(columnKey, sourceValue);
//                } else {
//                    continue;
//                }
//            }
//
//            if (!ObjectUtils.isEmpty(diffColumn)) {
//                FileUtil.echo(filePath, String.format(sql, dataDiffConfigItem.getValue().getTarget().getSchema(), ct.getTableName(),
//                        SqlUtil.buildUpdate(ct, diffColumn), SqlUtil.buildWhere(ct, bothMap.get(id))));
//            }
//        }
//    }

    private static void generateUpdateSql(String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct, Map<String, Map<String, Object>> bothMap) {
        DBDiffContext.initForkCount();
        Integer poolSize = Optional.fromNullable(ct.getConcurrent()).or(dataDiffConfigItem.getValue().getCompareOptions().getConcurrent());
        ForkJoinPool pool = new ForkJoinPool(poolSize);
        ForkJoinTask<Integer> task = new UpdateDataAsyncProcess(Lists.newArrayList(bothMap.keySet()), configKey, dataDiffConfigItem, ct, bothMap);
        pool.submit(task);

        try {
            if (task.get() != bothMap.size()) {
                log.error(String.format("error to generate update sql with number %s/%s.", task.get(), bothMap.size()));
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void generateDeleteSql(String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct,
            Map<String, Map<String, Object>> onlyTargetSet) {
        String filePath = FileUtil.createFile(DBDiffContext.identifier + System.getProperty("file.separator") + configKey, "DataDiff.sql");
        for (Entry<String, Map<String, Object>> entry : onlyTargetSet.entrySet()) {
            DBDiffContext.echoQueue.offer(EchoObject.builder().filePath(filePath).content(String.format(SqlConstant.DELETE_SQL,
                    dataDiffConfigItem.getValue().getTarget().getSchema(), ct.getTableName(), SqlUtil.buildWhere(ct, entry.getValue()))).build());
        }
    }

}
