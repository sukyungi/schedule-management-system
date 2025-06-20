import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {
    public enum Priority {
        LOW("낮음"),
        MEDIUM("중간"),
        HIGH("높음"),
        URGENT("긴급");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        TODO("대기중"),
        IN_PROGRESS("진행중"),
        COMPLETED("완료");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 체크리스트 아이템 클래스
    public static class ChecklistItem {
        private String id;
        private String text;
        private boolean completed;
        private LocalDateTime completedAt;

        public ChecklistItem(String text) {
            this.id = UUID.randomUUID().toString();
            this.text = text;
            this.completed = false;
        }

        // Getters and Setters
        public String getId() { return id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { 
            this.completed = completed;
            this.completedAt = completed ? LocalDateTime.now() : null;
        }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    private String taskId;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private List<String> tags;
    private boolean isImportant;
    private List<ChecklistItem> checklistItems; // 체크리스트 아이템들

    public Task(String title, String description, Priority priority, LocalDateTime dueDate, String userId) {
        this.taskId = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = Status.TODO;
        this.dueDate = dueDate;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.tags = new ArrayList<>();
        this.isImportant = false;
        this.checklistItems = new ArrayList<>();
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }
    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }
    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUserId() { return userId; }
    public List<String> getTags() { return tags; }
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            this.updatedAt = LocalDateTime.now();
        }
    }
    public void removeTag(String tag) {
        if (tags.remove(tag)) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    public boolean isImportant() { return isImportant; }
    public void setImportant(boolean important) {
        this.isImportant = important;
        this.updatedAt = LocalDateTime.now();
    }

    // 체크리스트 관련 메서드들
    public List<ChecklistItem> getChecklistItems() { return checklistItems; }
    
    public void addChecklistItem(String text) {
        ChecklistItem item = new ChecklistItem(text);
        checklistItems.add(item);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeChecklistItem(String itemId) {
        checklistItems.removeIf(item -> item.getId().equals(itemId));
        this.updatedAt = LocalDateTime.now();
    }
    
    public void toggleChecklistItem(String itemId) {
        for (ChecklistItem item : checklistItems) {
            if (item.getId().equals(itemId)) {
                item.setCompleted(!item.isCompleted());
                this.updatedAt = LocalDateTime.now();
                break;
            }
        }
    }
    
    // 진행률 계산
    public double getProgressPercentage() {
        if (checklistItems.isEmpty()) {
            // 체크리스트가 없으면 상태로 진행률 계산
            switch (status) {
                case TODO: return 0.0;
                case IN_PROGRESS: return 50.0;
                case COMPLETED: return 100.0;
                default: return 0.0;
            }
        } else {
            // 체크리스트 기반 진행률 계산
            long completedCount = checklistItems.stream()
                .filter(ChecklistItem::isCompleted)
                .count();
            return (double) completedCount / checklistItems.size() * 100.0;
        }
    }
    
    public int getCompletedChecklistCount() {
        return (int) checklistItems.stream()
            .filter(ChecklistItem::isCompleted)
            .count();
    }
    
    public int getTotalChecklistCount() {
        return checklistItems.size();
    }
    
    // 마감일 임박 확인
    public boolean isDueSoon() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        return dueDate.isBefore(tomorrow) && !isCompleted();
    }
    
    // 마감일 지남 확인
    public boolean isOverdue() {
        if (dueDate == null) return false;
        return dueDate.isBefore(LocalDateTime.now()) && !isCompleted();
    }

    public boolean isCompleted() {
        return this.status == Status.COMPLETED;
    }

    @Override
    public String toString() {
        return String.format("%s (우선순위: %s, 상태: %s, 진행률: %.1f%%, 마감일: %s)",
            title, priority.getDisplayName(), status.getDisplayName(),
            getProgressPercentage(),
            dueDate != null ? dueDate.toString() : "없음");
    }
} 