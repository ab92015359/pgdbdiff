package com.vernon.pgdatadiff.core.dml.async;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

import org.springframework.util.ObjectUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vernon.pgdatadiff.constants.SqlConstant;
import com.vernon.pgdatadiff.core.DBDiffContext;
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
 * @time 2024年7月27日 下午10:22:32
 */
@Slf4j
public class UpdateDataAsyncProcess extends RecursiveTask<Integer> {

    private static final long serialVersionUID = 6831855513952588873L;

    private List<String> segPartition;
    private String configKey;
    private DataDiffConfigItem dataDiffConfigItem;
    private CompareTable ct;
    private Map<String, Map<String, Object>> dataMap;
    private String filePath;

    public UpdateDataAsyncProcess(List<String> segPartition, String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct,
            Map<String, Map<String, Object>> dataMap) {
        super();
        this.segPartition = segPartition;
        this.configKey = configKey;
        this.dataDiffConfigItem = dataDiffConfigItem;
        this.ct = ct;
        this.dataMap = dataMap;

        this.filePath = FileUtil.createFile(DBDiffContext.identifier + System.getProperty("file.separator") + configKey, "DataDiff.sql");
    }

    @Override
    protected Integer compute() {
        Integer batchSize = Optional.fromNullable(ct.getBatchSize()).or(dataDiffConfigItem.getValue().getCompareOptions().getBatchSize());
        if (segPartition.size() <= batchSize) {
            log.debug(String.format("start to process table %s, count %s.", ct.getTableName(), segPartition.size()));

            Map<String, Map<String, Object>> segDataMap = Maps.newHashMap();
            segPartition.forEach(e -> {
                segDataMap.put(e, dataMap.get(e));
            });

            log.debug("start to get data from DB for " + segPartition.size());
            CompletableFuture<Map<String, Map<String, Object>>> sourceCF = CompletableFuture.supplyAsync(() -> {
                return DataOperationDao.loadDatas(configKey, DsEnum.SOURCE, dataDiffConfigItem.getValue().getSource().getSchema(), ct, segDataMap);
            });
            CompletableFuture<Map<String, Map<String, Object>>> targetCF = CompletableFuture.supplyAsync(() -> {
                return DataOperationDao.loadDatas(configKey, DsEnum.TARGET, dataDiffConfigItem.getValue().getTarget().getSchema(), ct, segDataMap);
            });

            Map<String, Map<String, Object>> sourceDatas;
            Map<String, Map<String, Object>> targetDatas;
            try {
                sourceDatas = sourceCF.get();
                targetDatas = targetCF.get();
            } catch (InterruptedException | ExecutionException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }

//            log.debug("start to get data from DB for " + segPartition.size());
//            Map<String, Map<String, Object>> sourceDatas = DataOperationDao.loadDatas(configKey, DsEnum.SOURCE,
//                    dataDiffConfigItem.getValue().getSource().getSchema(), ct, segDataMap);
//            log.debug("finsih to get data from source DB for " + segPartition.size());
//            Map<String, Map<String, Object>> targetDatas = DataOperationDao.loadDatas(configKey, DsEnum.TARGET,
//                    dataDiffConfigItem.getValue().getTarget().getSchema(), ct, segDataMap);
//            log.debug("finsih to get data from target DB for " + segPartition.size());

            for (Entry<String, Map<String, Object>> sourceRowEntry : sourceDatas.entrySet()) {
                String id = sourceRowEntry.getKey();
                Map<String, Object> sourceRowColumns = sourceRowEntry.getValue();
                Map<String, Object> targetRowColumns = targetDatas.get(id);

                Map<String, Object> diffColumn = Maps.newHashMap();
                for (Entry<String, Object> sourceColumnEntry : sourceRowColumns.entrySet()) {
                    String columnKey = sourceColumnEntry.getKey();
                    Object sourceValue = sourceColumnEntry.getValue();
                    Object targetValue = targetRowColumns.get(columnKey);
                    if (sourceValue == null && targetValue == null) {
                        continue;
                    } else if (sourceValue == null && targetValue != null) {
                        diffColumn.put(columnKey, sourceValue);
                    } else if (sourceValue != null && targetValue == null) {
                        diffColumn.put(columnKey, sourceValue);
                    } else if (!sourceValue.equals(targetValue)) {
                        diffColumn.put(columnKey, sourceValue);
                    } else {
                        continue;
                    }
                }

                if (!ObjectUtils.isEmpty(diffColumn)) {
                    DBDiffContext.echoQueue.offer(EchoObject.builder().filePath(this.filePath)
                            .content(String.format(SqlConstant.UPDATE_SQL, dataDiffConfigItem.getValue().getTarget().getSchema(), ct.getTableName(),
                                    SqlUtil.buildUpdate(ct, diffColumn), SqlUtil.buildWhere(ct, segDataMap.get(id))))
                            .build());
                }
            }

            DBDiffContext.finishedForkTaskCount++;
            log.info(String.format("finish to process table %s, count %s, progress(%s/%s).", ct.getTableName(), segPartition.size(),
                    DBDiffContext.finishedForkTaskCount, DBDiffContext.forkTaskCount));
            return segPartition.size();
        }

        List<List<String>> segPartitions = Lists.partition(segPartition, segPartition.size() / 2 + 1);

        DBDiffContext.forkTaskCount++;
        log.debug(String.format("split table %s count %s to %s and %s", ct.getTableName(), segPartition.size(), segPartitions.get(0).size(),
                segPartitions.get(1).size()));
        UpdateDataAsyncProcess task1 = new UpdateDataAsyncProcess(segPartitions.get(0), configKey, dataDiffConfigItem, ct, dataMap);
        UpdateDataAsyncProcess task2 = new UpdateDataAsyncProcess(segPartitions.get(1), configKey, dataDiffConfigItem, ct, dataMap);
        // 执行任务
        task1.fork();
        task2.fork();
        // 获取任务执行的结果
        return task1.join() + task2.join();

    }

}
