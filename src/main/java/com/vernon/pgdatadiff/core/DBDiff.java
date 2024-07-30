package com.vernon.pgdatadiff.core;

import java.io.IOException;

import com.alibaba.fastjson2.JSON;
import com.vernon.pgdatadiff.core.ddl.SchemaComparer;
import com.vernon.pgdatadiff.core.dml.DataComparer;
import com.vernon.pgdatadiff.model.DataDiffSetting;
import com.vernon.pgdatadiff.pg.PostgresConnectionPool;
import com.vernon.pgdatadiff.utils.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午10:44:06
 */
@Slf4j
public class DBDiff {

    public void startDataWriter() {
        new Thread(new DataWriter()).start();
    }

    public void compareSchema() {
        SchemaComparer.compareSchema();
    }
    
    public void compareData() {
        DataComparer.compareData();
    }

    public void loadConf() throws IOException {
        DBDiffContext.currentWorkDir = System.getProperty("user.dir");
        log.info("current working dir is " + DBDiffContext.currentWorkDir);

//        String path = "/" + DataDiffContext.filePath;
//        InputStream configStr = getClass().getResourceAsStream(path);

        String path = DBDiffContext.currentWorkDir + System.getProperty("file.separator") + DBDiffContext.filePath;
        String configStr = FileUtil.readFile(path);

        if (configStr == null) {
            throw new RuntimeException("error to load config file:" + path);
        } else {
            DataDiffSetting dds = JSON.parseObject(configStr, DataDiffSetting.class);

            dds.getCompares().forEach(e -> {
                DBDiffContext.configMap.put(e.getKey(), e);
            });
            DBDiffContext.miscSetting = dds.getMisc();

            log.info("finsih to load config file...");

            PostgresConnectionPool.initDataSources(DBDiffContext.configMap);

            log.info("finsih to init datasource...");
        }
    }

}
