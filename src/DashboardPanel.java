import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private final ScheduleManager scheduleManager;
    private final User currentUser;

    private JLabel welcomeLabel;
    private JLabel dateTimeLabel;
    private JTextArea todayScheduleArea;
    private JLabel taskStatusLabel;

    public DashboardPanel(ScheduleManager scheduleManager, User currentUser) {
        this.scheduleManager = scheduleManager;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단 패널: 환영 메시지 및 날짜/시간
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(dateTimeLabel, BorderLayout.EAST);

        // 중앙 패널: 오늘 일정 및 할 일 현황
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1, 20, 20));

        // 오늘 일정 패널
        JPanel todaySchedulePanel = new JPanel(new BorderLayout());
        todaySchedulePanel.setBorder(BorderFactory.createTitledBorder("오늘의 일정"));
        todayScheduleArea = new JTextArea();
        todayScheduleArea.setEditable(false);
        todayScheduleArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(todayScheduleArea);
        todaySchedulePanel.add(scrollPane, BorderLayout.CENTER);

        // 할 일 현황 패널
        JPanel taskStatusPanel = new JPanel(new BorderLayout());
        taskStatusPanel.setBorder(BorderFactory.createTitledBorder("할 일 현황"));
        taskStatusLabel = new JLabel();
        taskStatusLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        taskStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taskStatusPanel.add(taskStatusLabel, BorderLayout.CENTER);

        centerPanel.add(todaySchedulePanel);
        centerPanel.add(taskStatusPanel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // 날짜/시간 실시간 업데이트 (1초마다)
        new Timer(1000, e -> updateDateTime()).start();

        // 데이터 로드
        updateDashboard();
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");
        dateTimeLabel.setText(sdf.format(new Date()));
    }

    public void updateDashboard() {
        // 환영 메시지 업데이트
        welcomeLabel.setText(currentUser.getName() + "님, 안녕하세요!");

        // 오늘 일정 업데이트
        updateTodaySchedules();

        // 할 일 현황 업데이트
        updateTaskStatus();

        // 날짜/시간 초기화
        updateDateTime();
    }

    private void updateTodaySchedules() {
        List<Schedule> todaySchedules = scheduleManager.getSchedulesForToday();
        if (todaySchedules.isEmpty()) {
            todayScheduleArea.setText("오늘 예정된 일정이 없습니다.");
        } else {
            String schedulesText = todaySchedules.stream()
                .map(s -> String.format("%s - %s: %s",
                        s.getStartTime().toLocalTime(),
                        s.getEndTime().toLocalTime(),
                        s.getTitle()))
                .collect(Collectors.joining("\n"));
            todayScheduleArea.setText(schedulesText);
        }
    }

    private void updateTaskStatus() {
        List<Task> allTasks = scheduleManager.getAllTasks();
        long completedTasks = allTasks.stream().filter(Task::isCompleted).count();
        long totalTasks = allTasks.size();
        taskStatusLabel.setText(String.format("총 %d개의 할 일 중 %d개 완료", totalTasks, completedTasks));
    }
} 