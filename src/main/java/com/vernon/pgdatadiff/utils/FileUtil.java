package com.vernon.pgdatadiff.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.logging.log4j.util.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月27日 下午5:22:26
 */
@Slf4j
public class FileUtil {
    public static void replaceFile(String filePath1, String filePath2, String str1, String str2) {
        File file = new File(filePath1);
        try (FileInputStream fis = new FileInputStream(file); InputStreamReader isr = new InputStreamReader(fis, "UTF-8"); BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.replaceAll(str1, str2);
                FileUtil.echo(filePath2, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filePath) {
        String content = Strings.EMPTY;
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file); InputStreamReader isr = new InputStreamReader(fis, "UTF-8"); BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                content += line + System.getProperty("line.separator");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String createFile(String directoryPath, String fileName) {
        // 创建目录
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                log.error("cannot create dir: " + directoryPath);
                throw new RuntimeException("cannot create dir: " + directoryPath);
            }
        }

        // 在目录中创建文件
        File file = new File(directory, fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static void echo(String filePath, String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(new String(line.getBytes("UTF-8"), "UTF-8"));
            writer.newLine(); // 写入一个新行字符
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void echo(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine(); // 写入一个新行字符
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
