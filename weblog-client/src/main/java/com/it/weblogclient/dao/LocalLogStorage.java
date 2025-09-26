package com.it.weblogclient.dao;

import com.it.weblogclient.domain.msg.WebLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalLogStorage {

    private static final String DB_URL = "jdbc:sqlite:logs_cache.db";

    static {
        initTable();
    }

    private static void initTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = """
                CREATE TABLE IF NOT EXISTS web_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    heap_used INTEGER,
                    heap_max INTEGER,
                    error_msg TEXT,
                    stack_trace TEXT,
                    create_time TEXT,
                    sent_status INTEGER DEFAULT 0
                )
                """;
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("初始化数据库失败: " + e.getMessage());
        }
    }

    public static void saveLog(WebLog log) {
        System.out.println("保存日志到sqlite...");
        String sql = "INSERT INTO web_log(heap_used, heap_max, error_msg, stack_trace, create_time, sent_status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, log.getHeapMemoryUsed());
            pstmt.setLong(2, log.getHeapMemoryMax());
            pstmt.setString(3, log.getErrorMessage());
            pstmt.setString(4, log.getErrorStack());
            pstmt.setString(5, log.getOccurTime().toString());
            pstmt.setInt(6, log.getSendStatus() != null ? log.getSendStatus() : 0);

            pstmt.executeUpdate();

            // 获取自增ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("保存日志失败: " + e.getMessage());
        }
    }

    public static List<WebLog> getUnsentLogs() {
        List<WebLog> logs = new ArrayList<>();
        String sql = "SELECT id, heap_used, heap_max, error_msg, stack_trace, create_time FROM web_log WHERE sent_status = 0 ORDER BY create_time ASC LIMIT 50";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                WebLog log = new WebLog();
                log.setId(rs.getInt("id"));
                log.setHeapMemoryUsed(rs.getLong("heap_used"));
                log.setHeapMemoryMax(rs.getLong("heap_max"));
                log.setErrorMessage(rs.getString("error_msg"));
                log.setErrorStack(rs.getString("stack_trace"));
                log.setOccurTime(LocalDateTime.parse(rs.getString("create_time")));
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("查询未发送日志失败: " + e.getMessage());
        }
        return logs;
    }

    public static void markAsSent(int id) {
        String sql = "UPDATE web_log SET sent_status = 1 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("标记日志为已发送失败: " + e.getMessage());
        }
    }

    /**
     * 标记日志为等待ack确认
     * @param ids
     */
    public static void markAsSentWait(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 构建 IN 查询的占位符：?,?,?,...
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

        String sql = "UPDATE web_log SET sent_status = 2 WHERE id IN (" + placeholders + ")";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置每个 ? 的值
            for (int i = 0; i < ids.size(); i++) {
                pstmt.setInt(i + 1, ids.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            System.out.println("成功标记 " + affectedRows + " 条日志为在等待ack");

        } catch (SQLException e) {
            System.err.println("批量标记日志为已发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void markAsSentBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 构建 IN 查询的占位符：?,?,?,...
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

        String sql = "UPDATE web_log SET sent_status = 1 WHERE id IN (" + placeholders + ")";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置每个 ? 的值
            for (int i = 0; i < ids.size(); i++) {
                pstmt.setInt(i + 1, ids.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            System.out.println("成功标记 " + affectedRows + " 条日志为已发送");

        } catch (SQLException e) {
            System.err.println("批量标记日志为已发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteLog(int id) {
        String sql = "DELETE FROM web_log WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除日志失败: " + e.getMessage());
        }
    }

    // 删除已发送的旧日志（更安全）
    public static int deleteSentLogsOlderThanDays(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        String cutoffStr = cutoffTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "DELETE FROM web_log WHERE sent_status = 1 AND create_time < ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cutoffStr);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("清理已发送旧日志失败: " + e.getMessage());
            return 0;
        }
    }
}