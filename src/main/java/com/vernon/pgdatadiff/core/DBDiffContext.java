package com.vernon.pgdatadiff.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vernon.pgdatadiff.constants.SettingConstant;
import com.vernon.pgdatadiff.model.DataDiffConfigItem;
import com.vernon.pgdatadiff.model.DataDiffMiscSetting;
import com.vernon.pgdatadiff.model.EchoObject;

import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月25日 下午10:52:49
 */
@Data
public class DBDiffContext {
    public static DataDiffMiscSetting miscSetting;
    public static Map<String, DataDiffConfigItem> configMap = Maps.newHashMap();
    
    public static String currentWorkDir;
    public static String identifier;
    public static Deque<EchoObject> echoQueue = new ArrayDeque<>();
    public static Boolean isFinished = false;

    public static Integer forkTaskCount = 1;
    public static Integer finishedForkTaskCount = 0;

    public static String filePath = "datadiffconfig.json";
    public static List<String> execCommands = Lists.newArrayList(SettingConstant.ALL_COMMAND);

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
        identifier = LocalDateTime.now().format(formatter);
    }

    public static void initForkCount() {
        forkTaskCount = 1;
        finishedForkTaskCount = 0;
    }
}
