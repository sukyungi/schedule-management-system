import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleManager {
    private Map<String, Schedule> schedules;
    private String currentUserId;
    private static ScheduleManager instance;

    private ScheduleManager() {
        schedules = new HashMap<>();
        loadSchedules();
    }

    public static ScheduleManager getInstance() {
        if (instance == null) {
            instance = new ScheduleManager();
        }
        return instance;
    }

    private void loadSchedules() {
        Map<String, Schedule> loadedSchedules = DataStorage.loadSchedules();
        if (loadedSchedules != null) {
            schedules = loadedSchedules;
        }
    }

    public void saveSchedules() {
        DataStorage.saveSchedules(schedules);
    }

    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
    }

    public void addSchedule(Schedule schedule) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (!schedule.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("다른 사용자의 일정을 추가할 수 없습니다.");
        }

        // 일정 중복 체크
        for (Schedule existingSchedule : getUserSchedules()) {
            if (schedule.isOverlapping(existingSchedule)) {
                throw new IllegalArgumentException("이미 예약된 시간과 겹칩니다.");
            }
        }

        schedules.put(schedule.getScheduleId(), schedule);
        schedule.scheduleReminder();
        saveSchedules();
    }

    public void updateSchedule(String scheduleId, Schedule updatedSchedule) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Schedule existingSchedule = schedules.get(scheduleId);
        if (existingSchedule == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }
        if (!existingSchedule.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("다른 사용자의 일정을 수정할 수 없습니다.");
        }

        // 일정 중복 체크 (자기 자신은 제외)
        for (Schedule schedule : getUserSchedules()) {
            if (!schedule.getScheduleId().equals(scheduleId) && 
                updatedSchedule.isOverlapping(schedule)) {
                throw new IllegalArgumentException("이미 예약된 시간과 겹칩니다.");
            }
        }

        existingSchedule.cancelReminder();
        schedules.put(scheduleId, updatedSchedule);
        updatedSchedule.scheduleReminder();
        saveSchedules();
    }

    public void deleteSchedule(String scheduleId) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Schedule schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }
        if (!schedule.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("다른 사용자의 일정을 삭제할 수 없습니다.");
        }

        schedule.cancelReminder();
        schedules.remove(scheduleId);
        saveSchedules();
    }

    public List<Schedule> getUserSchedules() {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return schedules.values().stream()
            .filter(schedule -> schedule.getUserId().equals(currentUserId) || 
                              schedule.canUserView(currentUserId))
            .collect(Collectors.toList());
    }

    public List<Schedule> getSchedulesByDate(LocalDate date) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            LocalDateTime startTime = schedule.getStartTime();
            if (startTime.toLocalDate().equals(date)) {
                result.add(schedule);
            }
        }
        return result;
    }

    public List<Schedule> getSchedulesByDateRange(LocalDateTime start, LocalDateTime end) {
        return schedules.values().stream()
            .filter(schedule -> 
                !schedule.getStartTime().isAfter(end) && 
                !schedule.getEndTime().isBefore(start))
            .collect(Collectors.toList());
    }

    public List<Schedule> getSchedulesByTag(String tag) {
        return schedules.values().stream()
            .filter(schedule -> schedule.getTags().contains(tag))
            .collect(Collectors.toList());
    }

    public List<Schedule> getImportantSchedules() {
        return schedules.values().stream()
            .filter(Schedule::isImportant)
            .collect(Collectors.toList());
    }

    public List<Schedule> getUpcomingSchedules(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);
        return schedules.values().stream()
            .filter(schedule -> 
                schedule.getStartTime().isAfter(now) && 
                schedule.getStartTime().isBefore(endTime))
            .collect(Collectors.toList());
    }

    public Map<String, List<Schedule>> getSchedulesByStatus() {
        return schedules.values().stream()
            .collect(Collectors.groupingBy(Schedule::getStatus));
    }

    public List<String> getAllTags() {
        return schedules.values().stream()
            .flatMap(schedule -> schedule.getTags().stream())
            .distinct()
            .collect(Collectors.toList());
    }

    public Schedule getSchedule(String scheduleId) {
        Schedule schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }
        if (!schedule.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("다른 사용자의 일정을 조회할 수 없습니다.");
        }
        return schedule;
    }

    public void shareSchedule(String scheduleId, String targetUserId, String permission) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Schedule schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }
        if (!schedule.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("다른 사용자의 일정을 공유할 수 없습니다.");
        }

        schedule.shareWithUser(targetUserId, permission);
        saveSchedules();
    }

    public void removeSharedUser(String scheduleId, String targetUserId) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Schedule schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("존재하지 않는 일정입니다.");
        }
        if (!schedule.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("다른 사용자의 일정 공유를 수정할 수 없습니다.");
        }

        schedule.removeSharedUser(targetUserId);
        saveSchedules();
    }

    public List<Schedule> getSharedSchedules() {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return schedules.values().stream()
            .filter(schedule -> !schedule.getUserId().equals(currentUserId) && 
                              schedule.canUserView(currentUserId))
            .collect(Collectors.toList());
    }

    public List<Schedule> getSharedSchedulesByUser(String userId) {
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return schedules.values().stream()
            .filter(schedule -> schedule.getUserId().equals(currentUserId) && 
                              schedule.canUserView(userId))
            .collect(Collectors.toList());
    }

    public String createSchedule(String title, String description, LocalDateTime startTime,
            LocalDateTime endTime, String location, String category, boolean isImportant) {
        String scheduleId = UUID.randomUUID().toString();
        Schedule schedule = new Schedule(scheduleId, title, description, startTime, endTime,
                location, category, isImportant, currentUserId);
        schedules.put(scheduleId, schedule);
        saveSchedules();
        return scheduleId;
    }

    public List<Schedule> getAllSchedules() {
        return new ArrayList<>(schedules.values());
    }

    public List<Schedule> getSchedulesByCategory(String category) {
        return schedules.values().stream()
                .filter(schedule -> schedule.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public void createSchedule(Schedule schedule) {
        schedules.put(schedule.getScheduleId(), schedule);
        saveSchedules();
    }

    public List<Schedule> getSchedules() {
        return new ArrayList<>(schedules.values());
    }

    public Schedule getScheduleByTitle(String title) {
        return schedules.values().stream()
            .filter(schedule -> schedule.getTitle().equals(title))
            .findFirst()
            .orElse(null);
    }

    public void updateSchedule(Schedule schedule) {
        schedules.put(schedule.getScheduleId(), schedule);
        saveSchedules();
    }

    public Map<String, Object> getScheduleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 전체 일정 수
        stats.put("totalSchedules", schedules.size());
        
        // 카테고리별 일정 수
        Map<String, Long> categoryCounts = schedules.values().stream()
            .collect(Collectors.groupingBy(
                Schedule::getCategory,
                Collectors.counting()
            ));
        stats.put("categoryCounts", categoryCounts);
        
        // 중요 일정 수
        long importantCount = schedules.values().stream()
            .filter(Schedule::isImportant)
            .count();
        stats.put("importantCount", importantCount);
        
        // 날짜별 일정 수
        Map<LocalDate, Long> dateCounts = schedules.values().stream()
            .collect(Collectors.groupingBy(
                schedule -> schedule.getStartTime().toLocalDate(),
                Collectors.counting()
            ));
        stats.put("dateCounts", dateCounts);
        
        return stats;
    }

    public List<Schedule> getSchedulesByPriority(int priority) {
        return schedules.values().stream()
            .filter(schedule -> schedule.getPriority() == priority)
            .collect(Collectors.toList());
    }

    public List<Schedule> searchSchedules(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return schedules.values().stream()
            .filter(schedule -> 
                schedule.getTitle().toLowerCase().contains(lowerKeyword) ||
                schedule.getDescription().toLowerCase().contains(lowerKeyword) ||
                schedule.getLocation().toLowerCase().contains(lowerKeyword) ||
                schedule.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword))
            )
            .collect(Collectors.toList());
    }

    public List<Schedule> getUpcomingSchedules(String userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(days);
        return schedules.values().stream()
            .filter(schedule -> 
                schedule.getUserId().equals(userId) &&
                !schedule.getStartTime().isBefore(now) &&
                !schedule.getStartTime().isAfter(end)
            )
            .sorted(Comparator.comparing(Schedule::getStartTime))
            .collect(Collectors.toList());
    }

    public List<Schedule> getOverdueSchedules(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return schedules.values().stream()
            .filter(schedule -> 
                schedule.getUserId().equals(userId) &&
                schedule.getEndTime().isBefore(now) &&
                !schedule.isCompleted()
            )
            .sorted(Comparator.comparing(Schedule::getEndTime))
            .collect(Collectors.toList());
    }

    public List<Schedule> getCompletedSchedules(String userId) {
        return schedules.values().stream()
            .filter(schedule -> 
                schedule.getUserId().equals(userId) &&
                schedule.isCompleted()
            )
            .sorted(Comparator.comparing(Schedule::getCompletedAt).reversed())
            .collect(Collectors.toList());
    }

    public List<Schedule> getSchedulesByUserId(String userId) {
        return schedules.values().stream()
            .filter(schedule -> schedule.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    public Map<String, Integer> getCategoryStatistics(String userId) {
        Map<String, Integer> categoryStats = new HashMap<>();
        List<Schedule> userSchedules = getSchedulesByUserId(userId);
        
        for (Schedule schedule : userSchedules) {
            String category = schedule.getCategory();
            categoryStats.put(category, categoryStats.getOrDefault(category, 0) + 1);
        }
        
        return categoryStats;
    }

    public Map<Integer, Integer> getPriorityStatistics(String userId) {
        Map<Integer, Integer> priorityStats = new HashMap<>();
        List<Schedule> userSchedules = getSchedulesByUserId(userId);
        
        for (Schedule schedule : userSchedules) {
            int priority = schedule.getPriority();
            priorityStats.put(priority, priorityStats.getOrDefault(priority, 0) + 1);
        }
        
        return priorityStats;
    }

    public double getCompletionRate(String userId) {
        List<Schedule> userSchedules = getSchedulesByUserId(userId);
        if (userSchedules.isEmpty()) {
            return 0.0;
        }
        
        long completedCount = userSchedules.stream()
            .filter(Schedule::isCompleted)
            .count();
            
        return (double) completedCount / userSchedules.size() * 100;
    }

    public void shareSchedule(String scheduleId, String targetUserId) {
        Schedule schedule = schedules.get(scheduleId);
        if (schedule != null) {
            schedule.addSharedUser(targetUserId);
            DataStorage.saveSchedules(schedules);
        }
    }

    public void unshareSchedule(String scheduleId, String targetUserId) {
        Schedule schedule = schedules.get(scheduleId);
        if (schedule != null) {
            schedule.removeSharedUser(targetUserId);
            DataStorage.saveSchedules(schedules);
        }
    }

    public List<Schedule> getSharedSchedules(String userId) {
        return schedules.values().stream()
            .filter(schedule -> schedule.getSharedWith().contains(userId))
            .collect(Collectors.toList());
    }

    public void completeSchedule(String scheduleId) {
        Schedule schedule = schedules.get(scheduleId);
        if (schedule != null) {
            schedule.setStatus("COMPLETED");
            schedule.setCompleted(true);
            saveSchedules();
        }
    }

    public List<Schedule> getSchedulesForToday() {
        if (currentUserId == null) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        return getUserSchedules().stream()
                .filter(schedule -> schedule.getStartTime().toLocalDate().equals(today))
                .collect(Collectors.toList());
    }

    public List<Task> getAllTasks() {
        if (currentUserId == null) {
            return Collections.emptyList();
        }
        // This is a placeholder. A proper implementation would load tasks 
        // associated with the current user.
        // For now, let's assume DataStorage can handle this.
        Map<String, Task> allTasks = DataStorage.loadTasks();
        if (allTasks == null) {
            return Collections.emptyList();
        }
        return allTasks.values().stream()
                .filter(task -> task.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }
} 