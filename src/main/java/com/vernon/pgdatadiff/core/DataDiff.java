package com.vernon.pgdatadiff.core;

import java.io.IOException;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.vernon.pgdatadiff.model.DataDiffConfigItem;
import com.vernon.pgdatadiff.pg.PostgresConnectionPool;
import com.vernon.pgdatadiff.utils.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午10:44:06
 */
@Slf4j
public class DataDiff {

    public void startDataWriter() {
        new Thread(new DataWriter()).start();
    }

    public void compareData() {
        DataComparer.compareData();
    }

    public void loadConf() throws IOException {
        DataDiffContext.currentWorkDir = System.getProperty("user.dir");
        log.info("current working dir is " + DataDiffContext.currentWorkDir);

//        String path = "/" + DataDiffContext.filePath;
//        InputStream configStr = getClass().getResourceAsStream(path);

        String path = DataDiffContext.currentWorkDir + System.getProperty("file.separator") + DataDiffContext.filePath;
        String configStr = FileUtil.readFile(path);

        if (configStr == null) {
            throw new RuntimeException("error to load config file:" + path);
        } else {
            JSONArray jsonArray = JSON.parseObject(configStr, JSONArray.class);

            for (Object object : jsonArray) {
                DataDiffConfigItem item = JSON.parseObject(object.toString(), DataDiffConfigItem.class);
                DataDiffContext.configMap.put(item.getKey(), item);
            }

            log.info("finsih to load config file...");

            PostgresConnectionPool.initDataSources(DataDiffContext.configMap);

            log.info("finsih to init datasource...");
        }
    }

}
