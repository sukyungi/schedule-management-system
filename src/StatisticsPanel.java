import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsPanel extends JPanel {
    private final ScheduleManager scheduleManager;
    private final UserManager userManager;
    private final ScheduleGUI parent;
    private JTabbedPane tabbedPane;
    private JPanel overviewPanel;
    private JPanel dailySummaryPanel;
    private JPanel weeklyReviewPanel;
    private JPanel monthlyReportPanel;
    private JPanel categoryPanel;
    private JPanel priorityPanel;
    private JPanel completionPanel;
    private JPanel timelinePanel;
    private JPanel productivityPanel;
    private JComboBox<String> periodComboBox;
    private JComboBox<String> viewTypeComboBox;

    public StatisticsPanel(ScheduleGUI parent, ScheduleManager scheduleManager, UserManager userManager) {
        this.parent = parent;
        this.scheduleManager = scheduleManager;
        this.userManager = userManager;
        
        setLayout(new BorderLayout());
        initializeComponents();
        updateStatistics();
    }

    private void initializeComponents() {
        // 상단 컨트롤 패널
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("통계 설정"));
        
        // 기간 선택
        periodComboBox = new JComboBox<>(new String[]{"오늘", "이번 주", "이번 달", "올해", "전체"});
        periodComboBox.addActionListener(_ -> updateStatistics());
        
        // 보기 타입 선택
        viewTypeComboBox = new JComboBox<>(new String[]{"차트", "테이블"});
        viewTypeComboBox.addActionListener(_ -> updateStatistics());
        
        controlPanel.add(new JLabel("기간:"));
        controlPanel.add(periodComboBox);
        controlPanel.add(new JLabel("보기:"));
        controlPanel.add(viewTypeComboBox);
        
        // 새로고침 버튼
        JButton refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(_ -> updateStatistics());
        controlPanel.add(refreshButton);
        
        // 탭 패널
        tabbedPane = new JTabbedPane();
        
        // 개요 패널
        overviewPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        overviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 일일 요약 패널
        dailySummaryPanel = new JPanel(new BorderLayout());
        dailySummaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 주간 리뷰 패널
        weeklyReviewPanel = new JPanel(new BorderLayout());
        weeklyReviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 월간 리포트 패널
        monthlyReportPanel = new JPanel(new BorderLayout());
        monthlyReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 카테고리 패널
        categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 우선순위 패널
        priorityPanel = new JPanel(new BorderLayout());
        priorityPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 완료율 패널
        completionPanel = new JPanel(new BorderLayout());
        completionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 타임라인 패널
        timelinePanel = new JPanel(new BorderLayout());
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 생산성 분석 패널
        productivityPanel = new JPanel(new BorderLayout());
        productivityPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 탭 추가
        tabbedPane.addTab("개요", overviewPanel);
        tabbedPane.addTab("일일 요약", dailySummaryPanel);
        tabbedPane.addTab("주간 리뷰", weeklyReviewPanel);
        tabbedPane.addTab("월간 리포트", monthlyReportPanel);
        tabbedPane.addTab("카테고리", categoryPanel);
        tabbedPane.addTab("우선순위", priorityPanel);
        tabbedPane.addTab("완료율", completionPanel);
        tabbedPane.addTab("타임라인", timelinePanel);
        tabbedPane.addTab("생산성 분석", productivityPanel);
        
        // 패널에 컴포넌트 추가
        add(controlPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void updateStatistics() {
        String userId = userManager.getCurrentUser().getUserId();
        String period = (String) periodComboBox.getSelectedItem();
        String viewType = (String) viewTypeComboBox.getSelectedItem();
        
        // 기간 설정
        LocalDateTime start = getStartDate(period);
        LocalDateTime end = LocalDateTime.now();
        
        // 개요 업데이트
        updateOverview(userId, start, end, viewType);
        
        // 일일 요약 업데이트
        updateDailySummary(userId, viewType);
        
        // 주간 리뷰 업데이트
        updateWeeklyReview(userId, viewType);
        
        // 월간 리포트 업데이트
        updateMonthlyReport(userId, viewType);
        
        // 카테고리 통계 업데이트
        updateCategoryStatistics(userId, start, end, viewType);
        
        // 우선순위 통계 업데이트
        updatePriorityStatistics(userId, start, end, viewType);
        
        // 완료율 통계 업데이트
        updateCompletionStatistics(userId, start, end, viewType);
        
        // 타임라인 통계 업데이트
        updateTimelineStatistics(userId, start, end, viewType);
        
        // 생산성 분석 업데이트
        updateProductivityAnalysis(userId);
    }

    private LocalDateTime getStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "오늘":
                return now.withHour(0).withMinute(0).withSecond(0);
            case "이번 주":
                return now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
            case "이번 달":
                return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            case "올해":
                return now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            default:
                return LocalDateTime.of(2020, 1, 1, 0, 0);
        }
    }

    private void updateOverview(String userId, LocalDateTime start, LocalDateTime end, String viewType) {
        overviewPanel.removeAll();
        
        // 총 일정 수
        List<Schedule> schedules = scheduleManager.getSchedulesByDateRange(start, end);
        int totalSchedules = schedules.size();
        
        // 완료된 일정 수
        long completedSchedules = schedules.stream()
            .filter(Schedule::isCompleted)
            .count();
        
        // 카테고리 수
        long categoryCount = schedules.stream()
            .map(Schedule::getCategory)
            .distinct()
            .count();
        
        // 평균 우선순위
        double avgPriority = schedules.stream()
            .mapToInt(Schedule::getPriority)
            .average()
            .orElse(0.0);
        
        // 통계 카드 생성
        overviewPanel.add(createStatCard("총 일정", String.valueOf(totalSchedules), Color.BLUE));
        overviewPanel.add(createStatCard("완료된 일정", String.valueOf(completedSchedules), Color.GREEN));
        overviewPanel.add(createStatCard("카테고리 수", String.valueOf(categoryCount), Color.ORANGE));
        overviewPanel.add(createStatCard("평균 우선순위", String.format("%.1f", avgPriority), Color.RED));
        
        overviewPanel.revalidate();
        overviewPanel.repaint();
    }
    
    private void updateDailySummary(String userId, String viewType) {
        dailySummaryPanel.removeAll();
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        List<Schedule> todaySchedules = scheduleManager.getSchedulesByDateRange(startOfDay, endOfDay);
        
        // 일일 요약 정보
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("오늘의 일정 요약"));
        
        // 오늘의 일정 수
        int totalToday = todaySchedules.size();
        int completedToday = (int) todaySchedules.stream().filter(Schedule::isCompleted).count();
        int remainingToday = totalToday - completedToday;
        
        summaryPanel.add(createInfoPanel("총 일정", String.valueOf(totalToday)));
        summaryPanel.add(createInfoPanel("완료된 일정", String.valueOf(completedToday)));
        summaryPanel.add(createInfoPanel("남은 일정", String.valueOf(remainingToday)));
        
        // 오늘의 일정 목록
        JPanel scheduleListPanel = new JPanel(new BorderLayout());
        scheduleListPanel.setBorder(BorderFactory.createTitledBorder("오늘의 일정 목록"));
        
        String[] columnNames = {"시간", "제목", "카테고리", "상태"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        for (Schedule schedule : todaySchedules) {
            String time = schedule.getStartTime() != null ? 
                schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "시간 미정";
            String status = schedule.isCompleted() ? "완료" : "대기중";
            
            model.addRow(new Object[]{time, schedule.getTitle(), schedule.getCategory(), status});
        }
        
        JTable table = new JTable(model);
        scheduleListPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 레이아웃 구성
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(scheduleListPanel, BorderLayout.CENTER);
        
        dailySummaryPanel.add(mainPanel, BorderLayout.CENTER);
        dailySummaryPanel.revalidate();
        dailySummaryPanel.repaint();
    }
    
    private void updateWeeklyReview(String userId, String viewType) {
        weeklyReviewPanel.removeAll();
        
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        
        // 주간 통계
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("이번 주 통계"));
        
        int totalWeekly = 0;
        int completedWeekly = 0;
        
        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            List<Schedule> daySchedules = scheduleManager.getSchedulesByDateRange(startOfDay, endOfDay);
            totalWeekly += daySchedules.size();
            completedWeekly += daySchedules.stream().filter(Schedule::isCompleted).count();
        }
        
        statsPanel.add(createInfoPanel("총 일정", String.valueOf(totalWeekly)));
        statsPanel.add(createInfoPanel("완료된 일정", String.valueOf(completedWeekly)));
        statsPanel.add(createInfoPanel("완료율", String.format("%.1f%%", 
            totalWeekly > 0 ? (double) completedWeekly / totalWeekly * 100 : 0)));
        statsPanel.add(createInfoPanel("평균 일일 일정", String.format("%.1f", 
            (double) totalWeekly / 7)));
        
        // 요일별 일정
        JPanel dailyPanel = new JPanel(new BorderLayout());
        dailyPanel.setBorder(BorderFactory.createTitledBorder("요일별 일정"));
        
        String[] columnNames = {"요일", "일정 수", "완료 수", "완료율"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        String[] dayNames = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            List<Schedule> daySchedules = scheduleManager.getSchedulesByDateRange(startOfDay, endOfDay);
            int dayTotal = daySchedules.size();
            int dayCompleted = (int) daySchedules.stream().filter(Schedule::isCompleted).count();
            double dayCompletionRate = dayTotal > 0 ? (double) dayCompleted / dayTotal * 100 : 0;
            
            model.addRow(new Object[]{dayNames[i], dayTotal, dayCompleted, 
                String.format("%.1f%%", dayCompletionRate)});
        }
        
        JTable table = new JTable(model);
        dailyPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 레이아웃 구성
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(dailyPanel, BorderLayout.CENTER);
        
        weeklyReviewPanel.add(mainPanel, BorderLayout.CENTER);
        weeklyReviewPanel.revalidate();
        weeklyReviewPanel.repaint();
    }
    
    private void updateMonthlyReport(String userId, String viewType) {
        monthlyReportPanel.removeAll();
        
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        // 월간 통계
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("이번 달 통계"));
        
        int totalMonthly = 0;
        int completedMonthly = 0;
        Map<String, Integer> categoryCount = new HashMap<>();
        
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            List<Schedule> daySchedules = scheduleManager.getSchedulesByDateRange(startOfDay, endOfDay);
            totalMonthly += daySchedules.size();
            completedMonthly += daySchedules.stream().filter(Schedule::isCompleted).count();
            
            // 카테고리별 카운트
            for (Schedule schedule : daySchedules) {
                String category = schedule.getCategory();
                categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            }
        }
        
        statsPanel.add(createInfoPanel("총 일정", String.valueOf(totalMonthly)));
        statsPanel.add(createInfoPanel("완료된 일정", String.valueOf(completedMonthly)));
        statsPanel.add(createInfoPanel("완료율", String.format("%.1f%%", 
            totalMonthly > 0 ? (double) completedMonthly / totalMonthly * 100 : 0)));
        statsPanel.add(createInfoPanel("평균 일일 일정", String.format("%.1f", 
            (double) totalMonthly / today.lengthOfMonth())));
        
        // 카테고리별 통계
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBorder(BorderFactory.createTitledBorder("카테고리별 통계"));
        
        String[] columnNames = {"카테고리", "일정 수", "비율"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            String category = entry.getKey();
            int count = entry.getValue();
            double ratio = totalMonthly > 0 ? (double) count / totalMonthly * 100 : 0;
            
            model.addRow(new Object[]{category, count, String.format("%.1f%%", ratio)});
        }
        
        JTable table = new JTable(model);
        categoryPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 레이아웃 구성
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(categoryPanel, BorderLayout.CENTER);
        
        monthlyReportPanel.add(mainPanel, BorderLayout.CENTER);
        monthlyReportPanel.revalidate();
        monthlyReportPanel.repaint();
    }

    private void updateCategoryStatistics(String userId, LocalDateTime start, LocalDateTime end, String viewType) {
        categoryPanel.removeAll();
        
        Map<String, Integer> categoryStats = scheduleManager.getCategoryStatistics(userId);
        
        // 테이블 생성
        String[] columnNames = {"카테고리", "일정 수"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        categoryStats.forEach((category, count) -> {
            model.addRow(new Object[]{category, count});
        });
        
        JTable table = new JTable(model);
        categoryPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    private void updatePriorityStatistics(String userId, LocalDateTime start, LocalDateTime end, String viewType) {
        priorityPanel.removeAll();
        
        Map<Integer, Integer> priorityStats = scheduleManager.getPriorityStatistics(userId);
        
        // 테이블 생성
        String[] columnNames = {"우선순위", "일정 수"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        priorityStats.forEach((priority, count) -> {
            model.addRow(new Object[]{priority, count});
        });
        
        JTable table = new JTable(model);
        priorityPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        priorityPanel.revalidate();
        priorityPanel.repaint();
    }

    private void updateCompletionStatistics(String userId, LocalDateTime start, LocalDateTime end, String viewType) {
        completionPanel.removeAll();
        
        double completionRate = scheduleManager.getCompletionRate(userId);
        
        // 테이블 생성
        String[] columnNames = {"완료율"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        model.addRow(new Object[]{String.format("%.1f%%", completionRate)});
        
        JTable table = new JTable(model);
        completionPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        completionPanel.revalidate();
        completionPanel.repaint();
    }

    private void updateTimelineStatistics(String userId, LocalDateTime start, LocalDateTime end, String viewType) {
        timelinePanel.removeAll();
        
        List<Schedule> schedules = scheduleManager.getSchedulesByDateRange(start, end);
        
        // 테이블 생성
        String[] columnNames = {"날짜", "일정 수"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        Map<LocalDateTime, Long> dailyCounts = schedules.stream()
            .collect(Collectors.groupingBy(
                schedule -> schedule.getStartTime().withHour(0).withMinute(0).withSecond(0),
                Collectors.counting()
            ));
        
        dailyCounts.forEach((date, count) -> {
            model.addRow(new Object[]{
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                count
            });
        });
        
        JTable table = new JTable(model);
        timelinePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        timelinePanel.revalidate();
        timelinePanel.repaint();
    }

    private void updateProductivityAnalysis(String userId) {
        productivityPanel.removeAll();
        // 시간대별/요일별/카테고리별 완료율 분석
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createTitledBorder("생산성 분석"));
        
        // 시간대별 완료율
        String[] hourLabels = new String[24];
        int[] hourTotal = new int[24];
        int[] hourCompleted = new int[24];
        for (int i = 0; i < 24; i++) hourLabels[i] = i + "시";
        List<Schedule> all = scheduleManager.getSchedules();
        for (Schedule s : all) {
            if (s.getStartTime() != null) {
                int hour = s.getStartTime().getHour();
                hourTotal[hour]++;
                if (s.isCompleted()) hourCompleted[hour]++;
            }
        }
        JPanel hourPanel = new JPanel(new BorderLayout());
        hourPanel.setBorder(BorderFactory.createTitledBorder("시간대별 완료율"));
        String[] hourCol = {"시간대", "완료율"};
        DefaultTableModel hourModel = new DefaultTableModel(hourCol, 0);
        for (int i = 0; i < 24; i++) {
            double rate = hourTotal[i] > 0 ? (double) hourCompleted[i] / hourTotal[i] * 100 : 0;
            hourModel.addRow(new Object[]{hourLabels[i], String.format("%.1f%%", rate)});
        }
        hourPanel.add(new JScrollPane(new JTable(hourModel)), BorderLayout.CENTER);
        mainPanel.add(hourPanel);
        
        // 요일별 완료율
        String[] dayLabels = {"월", "화", "수", "목", "금", "토", "일"};
        int[] dayTotal = new int[7];
        int[] dayCompleted = new int[7];
        for (Schedule s : all) {
            if (s.getStartTime() != null) {
                int day = s.getStartTime().getDayOfWeek().getValue() - 1;
                dayTotal[day]++;
                if (s.isCompleted()) dayCompleted[day]++;
            }
        }
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setBorder(BorderFactory.createTitledBorder("요일별 완료율"));
        String[] dayCol = {"요일", "완료율"};
        DefaultTableModel dayModel = new DefaultTableModel(dayCol, 0);
        for (int i = 0; i < 7; i++) {
            double rate = dayTotal[i] > 0 ? (double) dayCompleted[i] / dayTotal[i] * 100 : 0;
            dayModel.addRow(new Object[]{dayLabels[i], String.format("%.1f%%", rate)});
        }
        dayPanel.add(new JScrollPane(new JTable(dayModel)), BorderLayout.CENTER);
        mainPanel.add(dayPanel);
        
        // 카테고리별 완료율
        Map<String, int[]> catMap = new HashMap<>();
        for (Schedule s : all) {
            String cat = s.getCategory();
            if (!catMap.containsKey(cat)) catMap.put(cat, new int[2]);
            catMap.get(cat)[0]++;
            if (s.isCompleted()) catMap.get(cat)[1]++;
        }
        JPanel catPanel = new JPanel(new BorderLayout());
        catPanel.setBorder(BorderFactory.createTitledBorder("카테고리별 완료율"));
        String[] catCol = {"카테고리", "완료율"};
        DefaultTableModel catModel = new DefaultTableModel(catCol, 0);
        for (String cat : catMap.keySet()) {
            int total = catMap.get(cat)[0];
            int comp = catMap.get(cat)[1];
            double rate = total > 0 ? (double) comp / total * 100 : 0;
            catModel.addRow(new Object[]{cat, String.format("%.1f%%", rate)});
        }
        catPanel.add(new JScrollPane(new JTable(catModel)), BorderLayout.CENTER);
        mainPanel.add(catPanel);
        
        productivityPanel.add(mainPanel, BorderLayout.CENTER);
        productivityPanel.revalidate();
        productivityPanel.repaint();
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        titleLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createInfoPanel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        
        return panel;
    }
} 