import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private Map<String, Task> tasks;
    private String currentUserId;

    public TaskManager() {
        this.tasks = new HashMap<>();
    }

    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
    }

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("태스크가 null입니다.");
        }
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (!task.getUserId().equals(currentUserId)) {
            throw new SecurityException("다른 사용자의 태스크를 추가할 수 없습니다.");
        }
        tasks.put(task.getTaskId(), task);
    }

    public void updateTask(String taskId, Task updatedTask) {
        if (taskId == null || updatedTask == null) {
            throw new IllegalArgumentException("태스크 ID 또는 업데이트할 태스크가 null입니다.");
        }
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Task existingTask = tasks.get(taskId);
        if (existingTask == null) {
            throw new IllegalArgumentException("존재하지 않는 태스크입니다.");
        }
        if (!existingTask.getUserId().equals(currentUserId)) {
            throw new SecurityException("다른 사용자의 태스크를 수정할 수 없습니다.");
        }
        tasks.put(taskId, updatedTask);
    }

    public void deleteTask(String taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("태스크 ID가 null입니다.");
        }
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("존재하지 않는 태스크입니다.");
        }
        if (!task.getUserId().equals(currentUserId)) {
            throw new SecurityException("다른 사용자의 태스크를 삭제할 수 없습니다.");
        }
        tasks.remove(taskId);
    }

    public Task getTask(String taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("태스크 ID가 null입니다.");
        }
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("존재하지 않는 태스크입니다.");
        }
        if (!task.getUserId().equals(currentUserId)) {
            throw new SecurityException("다른 사용자의 태스크를 조회할 수 없습니다.");
        }
        return task;
    }

    public List<Task> getUserTasks() {
        if (currentUserId == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && task.getUserId().equals(currentUserId))
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByStatus(Task.Status status) {
        if (currentUserId == null || status == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && task.getStatus() == status)
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByPriority(Task.Priority priority) {
        if (currentUserId == null || priority == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && task.getPriority() == priority)
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByDueDate(LocalDateTime date) {
        if (currentUserId == null || date == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && 
                          task.getDueDate() != null &&
                          task.getDueDate().toLocalDate().equals(date.toLocalDate()))
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByTag(String tag) {
        if (currentUserId == null || tag == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && 
                          task.getTags() != null && task.getTags().contains(tag))
            .collect(Collectors.toList());
    }

    public List<Task> getImportantTasks() {
        if (currentUserId == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && task.isImportant())
            .collect(Collectors.toList());
    }

    public List<Task> getOverdueTasks() {
        if (currentUserId == null) {
            return new ArrayList<>();
        }
        LocalDateTime now = LocalDateTime.now();
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && 
                          task.getDueDate() != null &&
                          task.getDueDate().isBefore(now) &&
                          task.getStatus() != Task.Status.COMPLETED)
            .collect(Collectors.toList());
    }

    public List<String> getAllTags() {
        if (currentUserId == null) {
            return new ArrayList<>();
        }
        return tasks.values().stream()
            .filter(task -> task != null && task.getUserId() != null && 
                          task.getUserId().equals(currentUserId) && task.getTags() != null)
            .flatMap(task -> task.getTags().stream())
            .distinct()
            .collect(Collectors.toList());
    }
} 