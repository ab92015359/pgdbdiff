package com.vernon.pgdatadiff;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.common.collect.Lists;
import com.vernon.pgdatadiff.core.DBDiff;
import com.vernon.pgdatadiff.core.DBDiffContext;

/**
 * @author liuyu
 */
@SpringBootApplication(scanBasePackages = { "com.vernon.pgdatadiff" })
@EnableScheduling
public class App {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(App.class).logStartupInfo(false).web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF).run(args);

        for (String arg : args) {
            System.out.println("Custom parameter: " + arg);
            if (arg.contains("-f")) {
                String[] argArray = arg.split("=");
                if (argArray.length == 2) {
                    DBDiffContext.filePath = argArray[1];
                }
            } else if (arg.contains("-c")) {
                String[] argArray = arg.split("=");
                if (argArray.length == 2) {
                    DBDiffContext.execCommands = Lists.newArrayList(argArray[1].toUpperCase().split(","));
                }
            }
        }

        DBDiff dbDiff = new DBDiff();
        dbDiff.loadConf();
        dbDiff.startDataWriter();
        if (DBDiffContext.miscSetting.getEnableSchemaDiff()) {
            dbDiff.compareSchema();
        }
        if (DBDiffContext.miscSetting.getEnableDataDiff()) {
            dbDiff.compareData();
        }
        DBDiffContext.isFinished = true;
    }

}
