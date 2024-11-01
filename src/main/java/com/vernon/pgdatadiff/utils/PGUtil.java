package com.vernon.pgdatadiff.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.google.common.collect.Lists;
import com.vernon.pgdatadiff.core.DBDiffContext;
import com.vernon.pgdatadiff.model.DBSetting;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月30日 上午11:28:33
 */
@Slf4j
public class PGUtil {

    public static void compare(String srouceFile, String targetFile, String outputDir, String outputFile) {
        try {
            String outputFilePath = FileUtil.createFile(outputDir, outputFile);
            String command = String.format("java -jar apgdiff-2.7.0.jar --ignore-start-with --new=%s --old=%s > %s", srouceFile, targetFile, outputFilePath);

            log.info("compare command: " + command);
            // 执行命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令执行结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                FileUtil.echo(outputFilePath, line);
            }

            // 等待命令执行完成
            process.waitFor();

            // 关闭流
            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<String> builcCommand(String... command) {
        return Lists.newArrayList(command);
    }

    public static void dump(String dumpFilePath, DBSetting dbSetting, List<String> excludedTables) {
        String pgDumpExe = DBDiffContext.miscSetting.getPgDumpPath();

        Process p;
        ProcessBuilder pb;

        String host = dbSetting.getHost();
        String port = dbSetting.getPort().toString();
        String dbName = dbSetting.getDbName();
        String schemaName = dbSetting.getSchema();
        String username = dbSetting.getUsername();
        String password = dbSetting.getPassword();

        List<String> commands = PGUtil.builcCommand(pgDumpExe, // pg_dump路径
                "--host", host, // pghost
                "--port", port, // pgport
                "--username", username, // pg用户名
                "--no-password", // 不输入密码 通过环境变量带入
                "--verbose", "--format=p", "--schema-only", "--no-owner", "--no-privileges", "--no-tablespaces", "--file", dumpFilePath, // 导出路径
                "--dbname", dbName, // database名称
                "--schema", schemaName // 可以指定导出带有中文的schema
        );

//        "--exclude-table", "tax_classification_code_20240801bak",
//        "--exclude-table", "gksk_fbr_gs.license_share_config",
//        "--exclude-table", "gksk_fbr_gs.tasks_info"

        if (!ObjectUtils.isEmpty(excludedTables)) {
            for (String excludedTable : excludedTables) {
                commands.add("--exclude-table");
                commands.add(dbSetting.getSchema() + "." + excludedTable);
            }
        }

        log.info("Dump DB: " + commands.toString());
        pb = new ProcessBuilder(commands);

        try {
            final Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", password); // pg密码
            p = pb.start();
            final BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = r.readLine();
            while (line != null) {
                System.err.println(line);
                line = r.readLine();
            }
            r.close();
            p.waitFor();
            System.out.println(p.exitValue());
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
