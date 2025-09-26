// File: LocalFileLogger.java
package com.it.weblogclient.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalFileLogger {

    private static final String LOG_DIR = "logs/client";
    private static final String LOG_FILE_PREFIX = "weblog-fail-";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Object lock = new Object();

    static {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("无法创建日志目录: " + LOG_DIR);
        }
    }

    /**
     * 写入失败日志到本地文件（仿 Log4j 格式）
     */
    public static void logFailedBatch(String batchId, String logContent) {
        synchronized (lock) { // 防止多线程写冲突
            try {
                String dateStr = LocalDateTime.now().format(FILE_DATE_FORMAT);
                String fileName = LOG_FILE_PREFIX + dateStr + LOG_FILE_SUFFIX;
                Path logFile = Paths.get(LOG_DIR, fileName);

                // 检查文件大小，超过则归档
                if (Files.exists(logFile) && Files.size(logFile) > MAX_FILE_SIZE) {
                    rotateLog(logFile, dateStr);
                }

                try (BufferedWriter writer = Files.newBufferedWriter(logFile, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                    String formattedLog = formatLog(batchId, logContent);
                    writer.write(formattedLog);
                    writer.newLine();
                }
            } catch (Exception e) {
                System.err.println("写入本地失败日志文件失败: " + e.getMessage());
            }
        }
    }

    private static void rotateLog(Path logFile, String dateStr) throws IOException {
        int index = 1;
        Path rotatedFile;
        do {
            rotatedFile = logFile.resolveSibling(LOG_FILE_PREFIX + dateStr + "." + (index++) + LOG_FILE_SUFFIX);
        } while (Files.exists(rotatedFile));

        Files.move(logFile, rotatedFile);
        System.out.println("日志文件已归档: " + rotatedFile);
    }

    private static String formatLog(String batchId, String logContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("[BatchId: ").append(batchId).append("] - ");
        
        // 模拟 Log4j 多行堆栈
        String[] lines = logContent.split("\n");
        sb.append(lines[0]).append(System.lineSeparator());
        for (int i = 1; i < lines.length; i++) {
            sb.append("\t at ").append(lines[i].trim()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}