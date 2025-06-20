import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ScheduleGUI extends JFrame {
    private final ScheduleManager scheduleManager;
    private final UserManager userManager;
    private JPanel mainPanel;
    private CalendarPanel calendarPanel;
    private TaskListPanel taskListPanel;
    private StatisticsPanel statisticsPanel;
    private SettingsPanel settingsPanel;
    private DashboardPanel dashboardPanel;
    private NotificationManager notificationManager;
    private Timer timer;
    private User currentUser;
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public ScheduleGUI() {
        setTitle("일정 관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 매니저 초기화
        userManager = UserManager.getInstance();
        scheduleManager = ScheduleManager.getInstance();
        
        // 메인 패널 설정
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);
        
        // 알림 매니저 초기화
        notificationManager = NotificationManager.getInstance();
        
        // 로그인 패널 초기화
        initializeLoginPanel();
        
        // 초기 화면 설정
        showLoginPanel();
        
        // 타이머 설정 (1분마다 알림 체크)
        timer = new Timer(60000, _ -> {
            if (currentUser != null) {
                notificationManager.checkNotifications(scheduleManager);
            }
        });
        timer.start();
    }

    private void initializeContentPanels() {
        try {
            // 대시보드 패널
            dashboardPanel = new DashboardPanel(scheduleManager, currentUser);

            // 캘린더 패널
            calendarPanel = new CalendarPanel(this, scheduleManager, userManager);
            
            // 할 일 목록 패널
            taskListPanel = new TaskListPanel(this, scheduleManager);
            
            // 통계 패널
            statisticsPanel = new StatisticsPanel(this, scheduleManager, userManager);
            
            // 설정 패널
            settingsPanel = new SettingsPanel();
            
            System.out.println("모든 패널이 성공적으로 초기화되었습니다.");
        } catch (Exception e) {
            System.err.println("패널 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLoginPanel() {
        try {
            mainPanel.removeAll();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(loginPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
            setSize(400, 300);
            setLocationRelativeTo(null);
            setJMenuBar(null); // 로그인 화면에서는 메뉴바 제거
            System.out.println("로그인 패널이 표시되었습니다.");
        } catch (Exception e) {
            System.err.println("로그인 패널 표시 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showMainPanel() {
        try {
            // 패널 초기화
            initializeContentPanels();
            
            mainPanel.removeAll();
            mainPanel.setLayout(new BorderLayout());
            
            // 메뉴바 설정
            setJMenuBar(createMenuBar());
            
            // 상단 패널 (사용자 정보 및 로그아웃 버튼)
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel userLabel = new JLabel("사용자: " + currentUser.getName());
            userLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
            JButton logoutButton = new JButton("로그아웃");
            logoutButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
            logoutButton.addActionListener(_ -> {
                userManager.logout();
                currentUser = null;
                showLoginPanel();
            });
            topPanel.add(userLabel, BorderLayout.WEST);
            topPanel.add(logoutButton, BorderLayout.EAST);
            
            // 탭 패널
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
            
            // 탭에 패널 추가
            tabbedPane.addTab("대시보드", dashboardPanel);
            tabbedPane.addTab("캘린더", calendarPanel);
            tabbedPane.addTab("할 일 목록", taskListPanel);
            tabbedPane.addTab("통계", statisticsPanel);
            tabbedPane.addTab("설정", settingsPanel);
            
            // 탭 변경 시 대시보드 업데이트
            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedComponent() == dashboardPanel) {
                    dashboardPanel.updateDashboard();
                }
            });
            
            // 메인 패널에 컴포넌트 추가
            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(tabbedPane, BorderLayout.CENTER);
            
            // 패널 업데이트
            mainPanel.revalidate();
            mainPanel.repaint();
            
            // 창 크기 조정
            setSize(1000, 700);
            setLocationRelativeTo(null);
            
            System.out.println("메인 패널이 표시되었습니다.");
        } catch (Exception e) {
            System.err.println("메인 패널 표시 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "화면 전환 중 오류가 발생했습니다. 다시 로그인해주세요.");
            showLoginPanel();
        }
    }

    private void initializeLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 로그인 폼 컴포넌트
        JLabel titleLabel = new JLabel("일정 관리 시스템");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        
        JLabel usernameLabel = new JLabel("아이디:");
        usernameLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        
        JLabel passwordLabel = new JLabel("비밀번호:");
        passwordLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        
        JButton loginButton = new JButton("로그인");
        loginButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        JButton registerButton = new JButton("회원가입");
        registerButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        
        // 컴포넌트 배치
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 30, 10);
        loginPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        loginPanel.add(buttonPanel, gbc);
        
        // 로그인 버튼 이벤트
        loginButton.addActionListener(_ -> handleLogin());
        
        // 회원가입 버튼 이벤트
        registerButton.addActionListener(_ -> handleRegister());
    }

    private void handleLogin() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }
            
            if (userManager.login(username, password)) {
                System.out.println("로그인 성공 - 사용자: " + username);
                currentUser = userManager.getCurrentUser();
                showMainPanel();
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 올바르지 않습니다.");
            }
    }
        
    private void handleRegister() {
            String username = JOptionPane.showInputDialog(this, "아이디를 입력하세요:");
            if (username == null || username.trim().isEmpty()) return;
            
            String password = JOptionPane.showInputDialog(this, "비밀번호를 입력하세요:");
            if (password == null || password.trim().isEmpty()) return;
            
            String name = JOptionPane.showInputDialog(this, "이름을 입력하세요:");
            if (name == null || name.trim().isEmpty()) return;
            
            if (userManager.registerUser(username.trim(), password.trim(), name.trim())) {
                JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "회원가입에 실패했습니다.");
            }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        
        // 파일 메뉴
        JMenu fileMenu = new JMenu("파일");
        JMenuItem exitItem = new JMenuItem("종료");
        exitItem.addActionListener(_ -> System.exit(0));
        fileMenu.add(exitItem);
        
        // 일정 메뉴
        JMenu scheduleMenu = new JMenu("일정");
        JMenuItem addItem = new JMenuItem("일정 추가");
        addItem.addActionListener(_ -> {
            if (calendarPanel != null) {
                calendarPanel.showAddScheduleDialog();
            }
        });
        scheduleMenu.add(addItem);
        
        // 도움말 메뉴
        JMenu helpMenu = new JMenu("도움말");
        JMenuItem aboutItem = new JMenuItem("정보");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "일정 관리 시스템 v1.0\n\n" +
                    "개발자: Sukyung Lim\n" +
                    "© 2025 All rights reserved.",
                    "정보",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(scheduleMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "일정 관리 시스템 v1.0\n\n" +
            "개발자: Sukyung Lim\n" +
            "© 2025 All rights reserved.",
            "정보",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkNotifications() {
        if (userManager.getCurrentUser() != null) {
            List<Schedule> schedules = scheduleManager.getUserSchedules();
            for (Schedule schedule : schedules) {
                if (schedule.getReminderMinutes() > 0) {
                    LocalDateTime reminderTime = schedule.getStartTime()
                        .minus(schedule.getReminderMinutes(), ChronoUnit.MINUTES);
                    if (reminderTime.isBefore(LocalDateTime.now()) && 
                        reminderTime.plusMinutes(1).isAfter(LocalDateTime.now())) {
                        NotificationManager.getInstance().showNotificationPopup(
                            "일정 알림",
                            schedule.getTitle() + " 일정이 곧 시작됩니다."
                        );
                    }
                }
            }

            // TaskManager가 구현되지 않았으므로 주석 처리
            /*
            List<Task> tasks = taskManager.getUserTasks();
            for (Task task : tasks) {
                if (task.getDueDate() != null && 
                    task.getStatus() != Task.Status.COMPLETED &&
                    task.getDueDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    NotificationManager.getInstance().showNotificationPopup(
                        "태스크 마감 알림",
                        task.getTitle() + " 태스크의 마감이 1시간 이내입니다."
                    );
                }
            }
            */
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ScheduleGUI gui = new ScheduleGUI();
            gui.setVisible(true);
        });
    }
} 