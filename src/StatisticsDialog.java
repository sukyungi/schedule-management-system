import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.Map;

public class StatisticsDialog extends JDialog {
    private ScheduleStatistics statistics;
    private JTabbedPane tabbedPane;
    private JComboBox<Integer> yearCombo;
    private JComboBox<Integer> monthCombo;

    public StatisticsDialog(Frame owner, ScheduleStatistics statistics) {
        super(owner, "일정 통계", true);
        this.statistics = statistics;

        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(owner);

        // 상단 패널 (연도/월 선택)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (탭)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        add(tabbedPane, BorderLayout.CENTER);

        // 초기 데이터 로드
        loadStatistics();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 연도 선택
        yearCombo = new JComboBox<>();
        int currentYear = LocalDateTime.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 5; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(currentYear);
        yearCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 월 선택
        monthCombo = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month);
        }
        monthCombo.setSelectedItem(LocalDateTime.now().getMonthValue());
        monthCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 새로고침 버튼
        JButton refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        refreshButton.addActionListener(e -> loadStatistics());

        panel.add(new JLabel("연도:"));
        panel.add(yearCombo);
        panel.add(new JLabel("월:"));
        panel.add(monthCombo);
        panel.add(refreshButton);

        return panel;
    }

    private void loadStatistics() {
        tabbedPane.removeAll();

        // 월별 통계
        JPanel monthlyPanel = createTablePanel(statistics.getMonthlyStatistics((Integer) yearCombo.getSelectedItem()));
        tabbedPane.addTab("월별 통계", monthlyPanel);

        // 주별 통계
        JPanel weeklyPanel = createTablePanel(statistics.getWeeklyStatistics(
            (Integer) yearCombo.getSelectedItem(),
            (Integer) monthCombo.getSelectedItem()));
        tabbedPane.addTab("주별 통계", weeklyPanel);

        // 완료된 일정 통계
        JPanel completedPanel = createTablePanel(statistics.getCompletedScheduleStatistics());
        tabbedPane.addTab("완료된 일정", completedPanel);

        // 카테고리별 통계
        JPanel categoryPanel = createTablePanel(statistics.getCategoryStatistics());
        tabbedPane.addTab("카테고리별 통계", categoryPanel);

        // 중요 일정 통계
        JPanel importantPanel = createTablePanel(statistics.getImportantScheduleStatistics());
        tabbedPane.addTab("중요 일정", importantPanel);

        // 완료율 패널
        JPanel completionRatePanel = createCompletionRatePanel();
        tabbedPane.addTab("완료율", completionRatePanel);
    }

    private JPanel createTablePanel(Map<String, Integer> data) {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columnNames = {"항목", "수량"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        data.forEach((key, value) -> {
            model.addRow(new Object[]{key, value});
        });

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCompletionRatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 기간 선택
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        endDateSpinner.setEditor(endEditor);

        JButton calculateButton = new JButton("완료율 계산");
        calculateButton.addActionListener(e -> {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            LocalDateTime startDateTime = startDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
            LocalDateTime endDateTime = endDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();

            double completionRate = statistics.getCompletionRate(startDateTime, endDateTime);
            JOptionPane.showMessageDialog(this,
                String.format("선택한 기간의 완료율: %.1f%%", completionRate),
                "완료율",
                JOptionPane.INFORMATION_MESSAGE);
        });

        controlPanel.add(new JLabel("시작일:"));
        controlPanel.add(startDateSpinner);
        controlPanel.add(new JLabel("종료일:"));
        controlPanel.add(endDateSpinner);
        controlPanel.add(calculateButton);

        panel.add(controlPanel, BorderLayout.NORTH);
        return panel;
    }
} 