package com.vernon.pgdatadiff.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.vernon.pgdatadiff.core.DBDiffContext;
import com.vernon.pgdatadiff.model.DBSetting;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月30日 上午11:28:33
 */
@Slf4j
public class PGUtil {
//    public static void main(String[] args) {
//        try {
////            String command = "java -jar apgdiff-2.7.0.jar --ignore-start-with --new=20240730-150104057/gkskfbr_102_gs2xj/dump/targetDDL.sql --old=20240730-150104057/gkskfbr_102_gs2xj/dump/sourceDDL.sql > ./ddlDiff.sql";
////            String command = "java -jar apgdiff-2.7.0.jar --ignore-start-with --new=targetDDL.sql --old=sourceDDL.sql > ddlDiff.sql";
//
//            String command = "java -jar apgdiff-2.7.0.jar --ignore-start-with --new=%s --old=%s > %s";
//
//            // 执行命令
//            Process process = Runtime.getRuntime().exec(command);
//
//            // 读取命令执行结果
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//
//            // 等待命令执行完成
//            process.waitFor();
//
//            // 关闭流
//            reader.close();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public static void compare(String srouceFile, String targetFile, String outputDir, String outputFile) {
        try {
//          String command = "java -jar apgdiff-2.7.0.jar --ignore-start-with --new=20240730-150104057/gkskfbr_102_gs2xj/dump/targetDDL.sql --old=20240730-150104057/gkskfbr_102_gs2xj/dump/sourceDDL.sql > ./ddlDiff.sql";
//          String command = "java -jar apgdiff-2.7.0.jar --ignore-start-with --new=targetDDL.sql --old=sourceDDL.sql > ddlDiff.sql";
            String outputFilePath = FileUtil.createFile(outputDir, outputFile);
            String command = String.format("java -jar apgdiff-2.7.0.jar --ignore-start-with --new=%s --old=%s > %s", srouceFile, targetFile, outputFilePath);

            // 执行命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令执行结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
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

    public static void dump(String dumpFilePath, DBSetting dbSetting) {

//        String pgDumpExe = DataDiffContext.currentWorkDir + System.getProperty("file.separator") + "pg_dump.exe";
        String pgDumpExe = DBDiffContext.miscSetting.getPgDumpPath();
//        String pgDumpFile = DataDiffContext.currentWorkDir + System.getProperty("file.separator") + "test.sql";

        Process p;
        ProcessBuilder pb;
//        pb = new ProcessBuilder(pgDumpPath, // pg_dump路径
//                "--host", "10.31.248.102", // pghost
//                "--port", "5432", // pgport
//                "--username", "postgres", // pg用户名
//                "--no-password", // 不输入密码 通过环境变量带入
//                "--format", "plain", // 导出格式
//                "--blobs", // blob
//                "--schema-only",
//                "--no-owner",
//                "--no-privileges",
//                "--no-tablespaces",
//                "--format=p",
//                "--verbose", 
//                "--file", pgDumpFile, // 导出路径
//                "--dbname", "gksk_fbr_xj", // database名称
//                "--schema", "gksk_fbr_xj", // 可以指定导出带有中文的schema
//                "--encoding", "\"UTF8\""); // 编码格式
        String host = dbSetting.getHost();
        String port = dbSetting.getPort().toString();
        String dbName = dbSetting.getDbName();
        String schemaName = dbSetting.getSchema();
        String username = dbSetting.getUsername();
        String password = dbSetting.getPassword();

        pb = new ProcessBuilder(pgDumpExe, // pg_dump路径
                "--host", host, // pghost
                "--port", port, // pgport
                "--username", username, // pg用户名
                "--no-password", // 不输入密码 通过环境变量带入
                "--verbose", "--format=p", "--schema-only", "--no-owner", "--no-privileges", "--no-tablespaces", "--file", dumpFilePath, // 导出路径
                "--dbname", dbName, // database名称
                "--schema", schemaName // 可以指定导出带有中文的schema
        );

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
