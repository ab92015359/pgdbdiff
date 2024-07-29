package com.vernon.pgdatadiff.core.async;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RecursiveTask;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vernon.pgdatadiff.constants.SqlConstant;
import com.vernon.pgdatadiff.core.DataDiffContext;
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
public class InsertDataAsyncProcess extends RecursiveTask<Integer> {

    private static final long serialVersionUID = -3557901866093860412L;

    private List<String> segPartition;
    private String configKey;
    private DataDiffConfigItem dataDiffConfigItem;
    private CompareTable ct;
    private Map<String, Map<String, Object>> dataMap;

    private String filePath;

    public InsertDataAsyncProcess(List<String> segPartition, String configKey, DataDiffConfigItem dataDiffConfigItem, CompareTable ct,
            Map<String, Map<String, Object>> dataMap) {
        super();
        this.segPartition = segPartition;
        this.configKey = configKey;
        this.dataDiffConfigItem = dataDiffConfigItem;
        this.ct = ct;
        this.dataMap = dataMap;

        this.filePath = FileUtil.createFile(DataDiffContext.identifier, configKey + ".sql");
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

            Map<String, Map<String, Object>> sourceDatas = DataOperationDao.loadDatas(configKey, DsEnum.SOURCE,
                    dataDiffConfigItem.getValue().getSource().getSchema(), ct, segDataMap);
            for (Entry<String, Map<String, Object>> sourceRowEntry : sourceDatas.entrySet()) {
                String[] insertSql = SqlUtil.buildInsert(ct, sourceRowEntry.getValue());
                DataDiffContext.echoQueue.offer(EchoObject.builder().filePath(this.filePath).content(String.format(SqlConstant.INSERT_SQL,
                        dataDiffConfigItem.getValue().getTarget().getSchema(), ct.getTableName(), insertSql[0], insertSql[1])).build());
            }

            DataDiffContext.finishedForkTaskCount++;
            log.info(String.format("finish to process table %s, count %s, progress(%s/%s).", ct.getTableName(), segPartition.size(),
                    DataDiffContext.finishedForkTaskCount, DataDiffContext.forkTaskCount));
            return segPartition.size();
        }

        List<List<String>> segPartitions = Lists.partition(segPartition, segPartition.size() / 2 + 1);

        DataDiffContext.forkTaskCount++;
        log.debug(String.format("split table %s count %s to %s and %s", ct.getTableName(), segPartition.size(), segPartitions.get(0).size(),
                segPartitions.get(1).size()));
        InsertDataAsyncProcess task1 = new InsertDataAsyncProcess(segPartitions.get(0), configKey, dataDiffConfigItem, ct, dataMap);
        InsertDataAsyncProcess task2 = new InsertDataAsyncProcess(segPartitions.get(1), configKey, dataDiffConfigItem, ct, dataMap);
        // 执行任务
        task1.fork();
        task2.fork();
        // 获取任务执行的结果
        return task1.join() + task2.join();
    }

}
