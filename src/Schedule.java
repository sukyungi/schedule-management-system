import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;

    private String scheduleId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String category;
    private boolean isImportant;
    private String status; // "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int reminderMinutes;
    private String color; // 일정 색상 (UI에서 구분용)
    private Map<String, String> sharedUsers; // 공유된 사용자 목록
    private boolean isRecurring; // 반복 여부
    private String recurrenceType; // 반복 유형 (DAILY, WEEKLY, MONTHLY, YEARLY)
    private int recurrenceInterval; // 반복 간격
    private LocalDateTime recurrenceEndDate; // 반복 종료일
    private Set<LocalDateTime> exceptionDates; // 반복 예외 날짜
    private Set<String> tags;
    private Set<String> sharedWith;
    private RecurrenceType recurrenceTypeEnum;
    private LocalDateTime recurrenceEnd;
    private int priority;
    private boolean isCompleted;
    private LocalDateTime completedAt;
    private List<SubTask> subTasks;

    public enum RecurrenceType {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    public static class SubTask {
        private String id;
        private String title;
        private boolean isCompleted;
        private LocalDateTime completedAt;

        public SubTask(String id, String title) {
            this.id = id;
            this.title = title;
            this.isCompleted = false;
        }

        // Getters and setters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { 
            this.isCompleted = completed;
            this.completedAt = completed ? LocalDateTime.now() : null;
        }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    public Schedule(String scheduleId, String title, String description, LocalDateTime startTime, 
                   LocalDateTime endTime, String location, String category, boolean isImportant,
                   String userId) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.category = category;
        this.isImportant = isImportant;
        this.status = "예정";
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.reminderMinutes = 30; // 기본 알림 시간 30분
        this.color = "#4A90E2"; // 기본 색상
        this.sharedUsers = new HashMap<>();
        this.isRecurring = false;
        this.recurrenceType = null;
        this.recurrenceInterval = 1;
        this.recurrenceEndDate = null;
        this.exceptionDates = new HashSet<>();
        this.tags = new HashSet<>();
        this.sharedWith = new HashSet<>();
        this.recurrenceTypeEnum = RecurrenceType.NONE;
        this.priority = 0;
        this.isCompleted = false;
        this.subTasks = new ArrayList<>();
    }

    private String generateScheduleId() {
        return "SCH" + UUID.randomUUID().toString().substring(0, 8);
    }

    // Getters and Setters
    public String getScheduleId() {
        return scheduleId;
    }
    
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isImportant() {
        return isImportant;
    }
    
    public void setImportant(boolean important) {
        this.isImportant = important;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) {
        if (isValidStatus(status)) {
            this.status = status;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("유효하지 않은 상태입니다.");
        }
    }
    
    public String getUserId() { return userId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) {
        if (reminderMinutes < 0) {
            throw new IllegalArgumentException("알림 시간은 0분 이상이어야 합니다.");
        }
        this.reminderMinutes = reminderMinutes;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getColor() { return color; }
    public void setColor(String color) {
        this.color = color;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isValidStatus(String status) {
        return status.equals("SCHEDULED") || 
               status.equals("IN_PROGRESS") || 
               status.equals("COMPLETED") || 
               status.equals("CANCELLED");
    }

    public long getDuration() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    public boolean isOverlapping(Schedule other) {
        return !(this.endTime.isBefore(other.startTime) || 
                this.startTime.isAfter(other.endTime));
    }

    public void scheduleReminder() {
        NotificationManager.getInstance().scheduleNotification(
            scheduleId,
            title,
            startTime,
            reminderMinutes
        );
    }

    public void cancelReminder() {
        NotificationManager.getInstance().cancelNotification(scheduleId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s - %s) %s",
            scheduleId, title, startTime, endTime, 
            isImportant ? "(중요)" : "");
    }

    // 공유 관련 메서드
    public Map<String, String> getSharedUsers() {
        return new HashMap<>(sharedUsers);
    }

    public void shareWithUser(String userId, String permission) {
        if (!isValidPermission(permission)) {
            throw new IllegalArgumentException("유효하지 않은 권한입니다.");
        }
        sharedUsers.put(userId, permission);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeSharedUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 유효하지 않습니다.");
        }
        sharedWith.remove(userId);
        this.updatedAt = LocalDateTime.now();
    }

    public String getUserPermission(String userId) {
        return sharedUsers.getOrDefault(userId, "NONE");
    }

    public boolean canUserEdit(String userId) {
        return this.userId.equals(userId) || 
               "WRITE".equals(sharedUsers.get(userId));
    }

    public boolean canUserView(String userId) {
        return this.userId.equals(userId) || 
               sharedUsers.containsKey(userId);
    }

    private boolean isValidPermission(String permission) {
        return permission.equals("READ") || permission.equals("WRITE");
    }

    // 반복 관련 메서드
    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        this.isRecurring = recurring;
        this.updatedAt = LocalDateTime.now();
    }

    public void setRecurrenceType(String type) {
        if (!isValidRecurrenceType(type)) {
            throw new IllegalArgumentException("유효하지 않은 반복 유형입니다.");
        }
        this.recurrenceType = type;
        this.recurrenceTypeEnum = RecurrenceType.valueOf(type);
        this.updatedAt = LocalDateTime.now();
    }

    public int getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public void setRecurrenceInterval(int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("반복 간격은 1 이상이어야 합니다.");
        }
        this.recurrenceInterval = interval;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(LocalDateTime endDate) {
        if (endDate != null && endDate.isBefore(startTime)) {
            throw new IllegalArgumentException("반복 종료일은 시작일 이후여야 합니다.");
        }
        this.recurrenceEndDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

    public Set<LocalDateTime> getExceptionDates() {
        return exceptionDates;
    }

    public void addExceptionDate(LocalDateTime date) {
        if (date != null) {
            exceptionDates.add(date);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeExceptionDate(LocalDateTime date) {
        exceptionDates.remove(date);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExceptionDate(LocalDateTime date) {
        return exceptionDates.contains(date);
    }

    public List<LocalDateTime> getRecurrenceDates() {
        if (!isRecurring || recurrenceTypeEnum == RecurrenceType.NONE) {
            return new ArrayList<>();
        }

        List<LocalDateTime> dates = new ArrayList<>();
        LocalDateTime current = startTime;
        ChronoUnit unit = getChronoUnit(recurrenceTypeEnum);

        while (current.isBefore(recurrenceEndDate) || current.isEqual(recurrenceEndDate)) {
            if (!exceptionDates.contains(current)) {
                dates.add(current);
            }
            current = current.plus(recurrenceInterval, unit);
        }

        return dates;
    }

    private ChronoUnit getChronoUnit(RecurrenceType type) {
        switch (type) {
            case DAILY: return ChronoUnit.DAYS;
            case WEEKLY: return ChronoUnit.WEEKS;
            case MONTHLY: return ChronoUnit.MONTHS;
            case YEARLY: return ChronoUnit.YEARS;
            default: throw new IllegalArgumentException("유효하지 않은 반복 유형입니다.");
        }
    }

    public LocalDateTime getNextRecurrenceDate() {
        if (!isRecurring || recurrenceTypeEnum == RecurrenceType.NONE) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = startTime;
        ChronoUnit unit = getChronoUnit(recurrenceTypeEnum);

        while (next.isBefore(now)) {
            next = next.plus(recurrenceInterval, unit);
        }

        while (exceptionDates.contains(next)) {
            next = next.plus(recurrenceInterval, unit);
        }

        return next.isAfter(recurrenceEndDate) ? null : next;
    }

    private boolean isValidRecurrenceType(String type) {
        try {
            RecurrenceType.valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    public void setRecurrence(RecurrenceType type, LocalDateTime end) {
        this.recurrenceTypeEnum = type;
        this.recurrenceEnd = end;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceTypeEnum;
    }

    public LocalDateTime getRecurrenceEnd() {
        return recurrenceEnd;
    }

    public void setPriority(int priority) {
        this.priority = Math.max(0, Math.min(5, priority)); // 0-5 사이의 값으로 제한
    }

    public int getPriority() {
        return priority;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void addSubTask(SubTask subTask) {
        this.subTasks.add(subTask);
    }

    public void removeSubTask(String subTaskId) {
        this.subTasks.removeIf(st -> st.getId().equals(subTaskId));
    }

    public List<SubTask> getSubTasks() {
        return Collections.unmodifiableList(subTasks);
    }

    public double getProgress() {
        if (subTasks.isEmpty()) {
            return isCompleted ? 100.0 : 0.0;
        }
        long completedCount = subTasks.stream().filter(SubTask::isCompleted).count();
        return (double) completedCount / subTasks.size() * 100;
    }

    public List<Schedule> getRecurringInstances(LocalDateTime start, LocalDateTime end) {
        if (recurrenceTypeEnum == RecurrenceType.NONE) {
            return Collections.singletonList(this);
        }

        List<Schedule> instances = new ArrayList<>();
        LocalDateTime current = startTime;
        
        while (!current.isAfter(end) && (recurrenceEnd == null || !current.isAfter(recurrenceEnd))) {
            if (!current.isBefore(start)) {
                Schedule instance = new Schedule(
                    scheduleId + "_" + current.toString(),
                    title,
                    description,
                    current,
                    current.plus(endTime.toEpochSecond(java.time.ZoneOffset.UTC) - startTime.toEpochSecond(java.time.ZoneOffset.UTC), java.time.temporal.ChronoUnit.SECONDS),
                    location,
                    category,
                    isImportant,
                    userId
                );
                instance.setColor(color);
                instance.tags.addAll(tags);
                instance.setPriority(priority);
                instance.setRecurring(true);
                instance.setRecurrenceType(recurrenceType);
                instance.setRecurrenceEndDate(recurrenceEnd);
                instances.add(instance);
            }

            switch (recurrenceTypeEnum) {
                case DAILY:
                    current = current.plusDays(1);
                    break;
                case WEEKLY:
                    current = current.plusWeeks(1);
                    break;
                case MONTHLY:
                    current = current.plusMonths(1);
                    break;
                case YEARLY:
                    current = current.plusYears(1);
                    break;
            }
        }

        return instances;
    }

    public void addSharedUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 유효하지 않습니다.");
        }
        sharedWith.add(userId);
        this.updatedAt = LocalDateTime.now();
    }

    public Set<String> getSharedWith() {
        return new HashSet<>(sharedWith);
    }

    public List<String> getSharedWithUsers() {
        return new ArrayList<>(sharedWith);
    }
} 