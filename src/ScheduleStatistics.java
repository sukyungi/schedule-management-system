import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ScheduleStatistics {
    private ScheduleManager scheduleManager;
    private UserManager userManager;

    public ScheduleStatistics(ScheduleManager scheduleManager, UserManager userManager) {
        this.scheduleManager = scheduleManager;
        this.userManager = userManager;
    }

    public Map<String, Object> getStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 전체 일정 수
        List<Schedule> userSchedules = scheduleManager.getSchedulesByUserId(userId);
        stats.put("totalSchedules", userSchedules.size());
        
        // 완료된 일정 수
        long completedCount = userSchedules.stream()
            .filter(Schedule::isCompleted)
            .count();
        stats.put("completedSchedules", completedCount);
        
        // 완료율
        double completionRate = userSchedules.isEmpty() ? 0.0 : 
            (double) completedCount / userSchedules.size() * 100;
        stats.put("completionRate", completionRate);
        
        // 카테고리별 통계
        Map<String, Integer> categoryStats = scheduleManager.getCategoryStatistics(userId);
        stats.put("categoryStatistics", categoryStats);
        
        // 우선순위별 통계
        Map<Integer, Integer> priorityStats = scheduleManager.getPriorityStatistics(userId);
        stats.put("priorityStatistics", priorityStats);
        
        // 오늘의 일정 수
        List<Schedule> todaySchedules = scheduleManager.getSchedulesByDate(LocalDate.now());
        stats.put("todaySchedules", todaySchedules.size());
        
        // 이번 주 일정 수
        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        long weeklyCount = userSchedules.stream()
            .filter(schedule -> {
                LocalDate scheduleDate = schedule.getStartTime().toLocalDate();
                return !scheduleDate.isBefore(weekStart) && !scheduleDate.isAfter(weekEnd);
            })
            .count();
        stats.put("weeklySchedules", weeklyCount);
        
        return stats;
    }

    public List<Schedule> getUpcomingSchedules(String userId, int days) {
        return scheduleManager.getUpcomingSchedules(userId, days);
    }

    public List<Schedule> getOverdueSchedules(String userId) {
        return scheduleManager.getOverdueSchedules(userId);
    }

    public List<Schedule> getCompletedSchedules(String userId) {
        return scheduleManager.getCompletedSchedules(userId);
    }

    // 월별 통계
    public Map<String, Integer> getMonthlyStatistics(Integer year) {
        Map<String, Integer> monthlyStats = new HashMap<>();
        List<Schedule> allSchedules = scheduleManager.getAllSchedules();
        
        for (int month = 1; month <= 12; month++) {
            int count = 0;
            for (Schedule schedule : allSchedules) {
                if (schedule.getStartTime().getYear() == year && 
                    schedule.getStartTime().getMonthValue() == month) {
                    count++;
                }
            }
            monthlyStats.put(month + "월", count);
        }
        
        return monthlyStats;
    }

    // 주별 통계
    public Map<String, Integer> getWeeklyStatistics(Integer year, Integer month) {
        Map<String, Integer> weeklyStats = new HashMap<>();
        List<Schedule> allSchedules = scheduleManager.getAllSchedules();
        
        for (int week = 1; week <= 5; week++) {
            int count = 0;
            for (Schedule schedule : allSchedules) {
                if (schedule.getStartTime().getYear() == year && 
                    schedule.getStartTime().getMonthValue() == month) {
                    int weekOfMonth = (schedule.getStartTime().getDayOfMonth() - 1) / 7 + 1;
                    if (weekOfMonth == week) {
                        count++;
                    }
                }
            }
            weeklyStats.put(week + "주차", count);
        }
        
        return weeklyStats;
    }

    // 완료된 일정 통계
    public Map<String, Integer> getCompletedScheduleStatistics() {
        Map<String, Integer> completedStats = new HashMap<>();
        List<Schedule> allSchedules = scheduleManager.getAllSchedules();
        
        int completed = 0;
        int total = allSchedules.size();
        
        for (Schedule schedule : allSchedules) {
            if (schedule.isCompleted()) {
                completed++;
            }
        }
        
        completedStats.put("완료된 일정", completed);
        completedStats.put("전체 일정", total);
        completedStats.put("미완료 일정", total - completed);
        
        return completedStats;
    }

    // 카테고리별 통계
    public Map<String, Integer> getCategoryStatistics() {
        return scheduleManager.getCategoryStatistics(userManager.getCurrentUser().getUserId());
    }

    // 중요 일정 통계
    public Map<String, Integer> getImportantScheduleStatistics() {
        Map<String, Integer> importantStats = new HashMap<>();
        List<Schedule> allSchedules = scheduleManager.getAllSchedules();
        
        int important = 0;
        int normal = 0;
        
        for (Schedule schedule : allSchedules) {
            if (schedule.isImportant()) {
                important++;
            } else {
                normal++;
            }
        }
        
        importantStats.put("중요 일정", important);
        importantStats.put("일반 일정", normal);
        
        return importantStats;
    }

    // 완료율 (기간별)
    public double getCompletionRate(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Schedule> allSchedules = scheduleManager.getAllSchedules();
        int total = 0;
        int completed = 0;
        
        for (Schedule schedule : allSchedules) {
            if (schedule.getStartTime().isAfter(startDateTime) && 
                schedule.getStartTime().isBefore(endDateTime)) {
                total++;
                if (schedule.isCompleted()) {
                    completed++;
                }
            }
        }
        
        return total > 0 ? (double) completed / total * 100 : 0.0;
    }
} 