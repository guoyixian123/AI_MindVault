package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取概览统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 待回复问诊数
        Integer pendingPosts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM consultation_post WHERE status = 'PENDING'", Integer.class);
        stats.put("pendingPosts", pendingPosts != null ? pendingPosts : 0);

        // 待确认预约数
        Integer pendingAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE status = 'PENDING'", Integer.class);
        stats.put("pendingAppointments", pendingAppointments != null ? pendingAppointments : 0);

        // 用户统计
        Integer totalUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE role = 'USER'", Integer.class);
        stats.put("totalUsers", totalUsers != null ? totalUsers : 0);

        Integer totalDoctors = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE role = 'DOCTOR'", Integer.class);
        stats.put("totalDoctors", totalDoctors != null ? totalDoctors : 0);

        // 预约状态统计
        Integer confirmedAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE status = 'CONFIRMED'", Integer.class);
        stats.put("confirmedAppointments", confirmedAppointments != null ? confirmedAppointments : 0);

        Integer completedAppointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE status = 'COMPLETED'", Integer.class);
        stats.put("completedAppointments", completedAppointments != null ? completedAppointments : 0);

        return ApiResponse.success(stats);
    }

    /**
     * 获取近7日问诊趋势
     */
    @GetMapping("/consultation-trend")
    public ApiResponse<List<Map<String, Object>>> getConsultationTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();

        // 查询近7天每天的问诊数量
        String sql = """
                SELECT DATE(FROM_UNIXTIME(created_at / 1000)) as date, COUNT(*) as count
                FROM consultation_post
                WHERE created_at >= (UNIX_TIMESTAMP() - 7 * 86400) * 1000
                GROUP BY DATE(FROM_UNIXTIME(created_at / 1000))
                ORDER BY date
                """;

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 填充近7天的数据
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -6);

            Map<String, Integer> dateCountMap = new HashMap<>();
            for (Map<String, Object> row : results) {
                String date = row.get("date").toString();
                Integer count = ((Number) row.get("count")).intValue();
                dateCountMap.put(date, count);
            }

            String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
            for (int i = 0; i < 7; i++) {
                Map<String, Object> item = new HashMap<>();
                String dateStr = String.format("%tF", cal);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

                item.put("day", weekDays[dayOfWeek]);
                item.put("date", dateStr);
                item.put("value", dateCountMap.getOrDefault(dateStr, 0));
                trend.add(item);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            // 如果查询失败，返回近7天的空数据
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -6);
            String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
            for (int i = 0; i < 7; i++) {
                Map<String, Object> item = new HashMap<>();
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
                item.put("day", weekDays[dayOfWeek]);
                item.put("date", String.format("%tF", cal));
                item.put("value", 0);
                trend.add(item);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        return ApiResponse.success(trend);
    }

    /**
     * 获取科室问诊排行
     */
    @GetMapping("/department-rank")
    public ApiResponse<List<Map<String, Object>>> getDepartmentRank() {
        String sql = """
                SELECT d.name, COUNT(cp.id) as count
                FROM department d
                LEFT JOIN consultation_post cp ON d.id = cp.department_id
                GROUP BY d.id, d.name
                ORDER BY count DESC
                LIMIT 5
                """;

        try {
            List<Map<String, Object>> rank = jdbcTemplate.queryForList(sql);
            return ApiResponse.success(rank);
        } catch (Exception e) {
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 获取预约状态分布
     */
    @GetMapping("/appointment-distribution")
    public ApiResponse<Map<String, Object>> getAppointmentDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        String sql = """
                SELECT status, COUNT(*) as count
                FROM appointment
                GROUP BY status
                """;

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> row : results) {
                String status = (String) row.get("status");
                Integer count = ((Number) row.get("count")).intValue();
                distribution.put(status, count);
            }
        } catch (Exception e) {
            distribution.put("PENDING", 0);
            distribution.put("CONFIRMED", 0);
            distribution.put("COMPLETED", 0);
            distribution.put("CANCELLED", 0);
        }

        return ApiResponse.success(distribution);
    }
}
