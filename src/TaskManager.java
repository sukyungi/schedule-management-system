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
        if (!task.getUserId().equals(currentUserId)) {
            throw new SecurityException("다른 사용자의 태스크를 추가할 수 없습니다.");
        }
        tasks.put(task.getTaskId(), task);
    }

    public void updateTask(String taskId, Task updatedTask) {
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
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId))
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByStatus(Task.Status status) {
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId) && task.getStatus() == status)
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByPriority(Task.Priority priority) {
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId) && task.getPriority() == priority)
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByDueDate(LocalDateTime date) {
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId) && 
                          task.getDueDate() != null &&
                          task.getDueDate().toLocalDate().equals(date.toLocalDate()))
            .collect(Collectors.toList());
    }

    public List<Task> getTasksByTag(String tag) {
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId) && 
                          task.getTags().contains(tag))
            .collect(Collectors.toList());
    }

    public List<Task> getImportantTasks() {
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId) && task.isImportant())
            .collect(Collectors.toList());
    }

    public List<Task> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId) && 
                          task.getDueDate() != null &&
                          task.getDueDate().isBefore(now) &&
                          task.getStatus() != Task.Status.COMPLETED)
            .collect(Collectors.toList());
    }

    public List<String> getAllTags() {
        return tasks.values().stream()
            .filter(task -> task.getUserId().equals(currentUserId))
            .flatMap(task -> task.getTags().stream())
            .distinct()
            .collect(Collectors.toList());
    }
} 