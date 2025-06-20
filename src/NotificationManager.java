import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NotificationManager {
    private static NotificationManager instance;
    private JFrame notificationFrame;
    private ScheduledExecutorService scheduler;
    private Map<String, ScheduledFuture<?>> notificationTasks;
    private java.util.List<NotificationListener> listeners;
    private Map<String, java.util.List<Notification>> notifications;
    private javax.swing.Timer checkTimer;
    private JPanel notificationPanel;
    private static final int CHECK_INTERVAL = 60000; // 1분마다 체크
    private static final int NOTIFICATION_DISPLAY_TIME = 5000; // 5초 동안 표시

    public static class Notification {
        private String id;
        private String title;
        private String message;
        private LocalDateTime time;
        private NotificationType type;
        private boolean isRead;

        public enum NotificationType {
            SCHEDULE_START,
            SCHEDULE_END,
            TASK_DUE,
            TASK_OVERDUE,
            SHARED_SCHEDULE,
            SYSTEM
        }

        public Notification(String id, String title, String message, LocalDateTime time, NotificationType type) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.time = time;
            this.type = type;
            this.isRead = false;
        }

        // Getters and setters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public LocalDateTime getTime() { return time; }
        public NotificationType getType() { return type; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }

    private NotificationManager() {
        notificationFrame = new JFrame();
        notificationFrame.setUndecorated(true);
        notificationFrame.setAlwaysOnTop(true);
        notificationFrame.setType(Window.Type.UTILITY);
        
        notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        notificationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        notificationFrame.add(scrollPane);
        
        // 알림 창 위치 설정 (화면 우측 상단)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        notificationFrame.setLocation(screenSize.width - 350, 50);

        scheduler = Executors.newScheduledThreadPool(1);
        notificationTasks = new ConcurrentHashMap<>();
        listeners = new ArrayList<>();
        notifications = new HashMap<>();
        initializeNotificationFrame();
        startCheckTimer();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public interface NotificationListener {
        void onNotificationReceived(String message);
    }

    public void addNotificationListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeNotificationListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void scheduleNotification(String taskId, String title, LocalDateTime deadline, int reminderMinutes) {
        // 이미 예약된 알림이 있다면 취소
        cancelNotification(taskId);

        LocalDateTime notificationTime = deadline.minus(reminderMinutes, ChronoUnit.MINUTES);
        long delay = ChronoUnit.MILLIS.between(LocalDateTime.now(), notificationTime);

        if (delay > 0) {
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                String message = String.format("'%s'의 마감이 %d분 후입니다!", title, reminderMinutes);
                notifyListeners(message);
                showNotificationPopup(title, message);
            }, delay, TimeUnit.MILLISECONDS);

            notificationTasks.put(taskId, future);
        }
    }

    public void cancelNotification(String taskId) {
        ScheduledFuture<?> future = notificationTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
    }

    private void notifyListeners(String message) {
        for (NotificationListener listener : listeners) {
            listener.onNotificationReceived(message);
        }
    }

    public void showNotificationPopup(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void initializeNotificationFrame() {
        notificationFrame = new JFrame();
        notificationFrame.setUndecorated(true);
        notificationFrame.setAlwaysOnTop(true);
        notificationFrame.setType(Window.Type.UTILITY);
        
        notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        notificationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        notificationFrame.add(scrollPane);
        
        // 알림 창 위치 설정 (화면 우측 상단)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        notificationFrame.setLocation(screenSize.width - 350, 50);
    }

    private void startCheckTimer() {
        checkTimer = new javax.swing.Timer(CHECK_INTERVAL, _ -> checkNotifications());
        checkTimer.start();
    }

    public void checkNotifications() {
        ScheduleManager scheduleManager = ScheduleManager.getInstance();
        checkNotifications(scheduleManager);
    }

    public void checkNotifications(ScheduleManager scheduleManager) {
        if (scheduleManager == null) return;

        String userId = UserManager.getInstance().getCurrentUser().getUserId();
        if (userId == null) return;

        LocalDateTime now = LocalDateTime.now();
        
        // 다가오는 일정 체크
        java.util.List<Schedule> upcomingSchedules = scheduleManager.getUpcomingSchedules(userId, 1);
        for (Schedule schedule : upcomingSchedules) {
            long minutesUntilStart = ChronoUnit.MINUTES.between(now, schedule.getStartTime());
            if (minutesUntilStart <= 30 && minutesUntilStart > 0) {
                addNotification(userId, new Notification(
                    UUID.randomUUID().toString(),
                    "다가오는 일정",
                    schedule.getTitle() + "이(가) " + minutesUntilStart + "분 후에 시작됩니다.",
                    now,
                    Notification.NotificationType.SCHEDULE_START
                ));
            }
        }

        // 마감 임박 일정 체크
        java.util.List<Schedule> overdueSchedules = scheduleManager.getOverdueSchedules(userId);
        for (Schedule schedule : overdueSchedules) {
            addNotification(userId, new Notification(
                UUID.randomUUID().toString(),
                "마감 임박",
                schedule.getTitle() + "의 마감 시간이 지났습니다.",
                now,
                Notification.NotificationType.SCHEDULE_END
            ));
        }

        // 공유된 일정 체크
        java.util.List<Schedule> sharedSchedules = scheduleManager.getSharedSchedules(userId);
        for (Schedule schedule : sharedSchedules) {
            addNotification(userId, new Notification(
                UUID.randomUUID().toString(),
                "공유된 일정",
                schedule.getTitle() + "이(가) 공유되었습니다.",
                now,
                Notification.NotificationType.SHARED_SCHEDULE
            ));
        }
    }

    public void addNotification(String userId, Notification notification) {
        notifications.computeIfAbsent(userId, _ -> new ArrayList<>()).add(notification);
        showNotification(notification);
    }

    private void showNotification(Notification notification) {
        JPanel notificationCard = new JPanel();
        notificationCard.setLayout(new BorderLayout());
        notificationCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JLabel titleLabel = new JLabel(notification.getTitle());
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        JLabel messageLabel = new JLabel(notification.getMessage());
        messageLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        
        notificationCard.add(titleLabel, BorderLayout.NORTH);
        notificationCard.add(messageLabel, BorderLayout.CENTER);
        
        notificationPanel.add(notificationCard);
        notificationPanel.revalidate();
        notificationPanel.repaint();
        
        notificationFrame.pack();
        notificationFrame.setVisible(true);
        
        // 일정 시간 후 알림 제거
        javax.swing.Timer timer = new javax.swing.Timer(NOTIFICATION_DISPLAY_TIME, _ -> {
            notificationPanel.remove(notificationCard);
            notificationPanel.revalidate();
            notificationPanel.repaint();
            if (notificationPanel.getComponentCount() == 0) {
                notificationFrame.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public java.util.List<Notification> getNotifications(String userId) {
        return notifications.getOrDefault(userId, new ArrayList<>());
    }

    public java.util.List<Notification> getUnreadNotifications(String userId) {
        return notifications.getOrDefault(userId, new ArrayList<>()).stream()
            .filter(notification -> !notification.isRead())
            .collect(Collectors.toList());
    }

    public void markAsRead(String userId, String notificationId) {
        notifications.getOrDefault(userId, new ArrayList<>()).stream()
            .filter(notification -> notification.getId().equals(notificationId))
            .findFirst()
            .ifPresent(notification -> notification.setRead(true));
    }

    public void markAllAsRead(String userId) {
        notifications.getOrDefault(userId, new ArrayList<>())
            .forEach(notification -> notification.setRead(true));
    }

    public void clearNotifications(String userId) {
        notifications.remove(userId);
    }

    public void stop() {
        if (checkTimer != null) {
            checkTimer.stop();
        }
    }
} 