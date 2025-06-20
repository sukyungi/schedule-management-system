import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

public class CalendarPanel extends JPanel {
    private final ScheduleManager scheduleManager;
    private final UserManager userManager;
    private final ScheduleGUI parent;
    private JTable calendarTable;
    private DefaultTableModel tableModel;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<ViewType> viewComboBox;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> priorityFilter;
    private JTextField tagFilter;
    private LocalDate currentDate;
    private ViewType currentView;
    private Map<LocalDate, List<Schedule>> scheduleMap;

    public enum ViewType {
        MONTH("월간"),
        WEEK("주간"),
        DAY("일간");

        private final String displayName;

        ViewType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public CalendarPanel(ScheduleGUI parent, ScheduleManager scheduleManager, UserManager userManager) {
        this.parent = parent;
        this.scheduleManager = scheduleManager;
        this.userManager = userManager;
        this.currentDate = LocalDate.now();
        this.currentView = ViewType.MONTH;
        this.scheduleMap = new HashMap<>();

        setLayout(new BorderLayout());
        initializeComponents();
        loadSchedules();
    }

    private void initializeComponents() {
        // 상단 컨트롤 패널
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 연도 선택
        yearComboBox = new JComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentDate.getYear());
        yearComboBox.addActionListener(_ -> updateCalendar());
        
        // 월 선택
        monthComboBox = new JComboBox<>(new String[]{"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"});
        monthComboBox.setSelectedIndex(currentDate.getMonthValue() - 1);
        monthComboBox.addActionListener(_ -> updateCalendar());
        
        // 뷰 타입 선택
        viewComboBox = new JComboBox<>(ViewType.values());
        viewComboBox.setSelectedItem(currentView);
        viewComboBox.addActionListener(_ -> {
            currentView = (ViewType) viewComboBox.getSelectedItem();
            updateCalendar();
        });
        
        // 검색 필드
        searchField = new JTextField(15);
        searchField.addActionListener(_ -> updateCalendar());
        
        // 카테고리 필터
        categoryFilter = new JComboBox<>(new String[]{"전체", "업무", "개인", "가족", "기타"});
        categoryFilter.addActionListener(_ -> updateCalendar());
        
        // 우선순위 필터
        priorityFilter = new JComboBox<>(new String[]{"전체", "높음", "중간", "낮음"});
        priorityFilter.addActionListener(_ -> updateCalendar());
        
        // 태그 필터
        tagFilter = new JTextField(10);
        tagFilter.addActionListener(_ -> updateCalendar());
        
        // 컨트롤 패널에 컴포넌트 추가
        controlPanel.add(new JLabel("연도:"));
        controlPanel.add(yearComboBox);
        controlPanel.add(new JLabel("월:"));
        controlPanel.add(monthComboBox);
        controlPanel.add(new JLabel("보기:"));
        controlPanel.add(viewComboBox);
        controlPanel.add(new JLabel("검색:"));
        controlPanel.add(searchField);
        controlPanel.add(new JLabel("카테고리:"));
        controlPanel.add(categoryFilter);
        controlPanel.add(new JLabel("우선순위:"));
        controlPanel.add(priorityFilter);
        controlPanel.add(new JLabel("태그:"));
        controlPanel.add(tagFilter);
        
        // 캘린더 테이블
        String[] columnNames = {"일", "월", "화", "수", "목", "금", "토"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        calendarTable = new JTable(tableModel);
        calendarTable.setRowHeight(100);
        calendarTable.setShowGrid(true);
        calendarTable.setGridColor(Color.LIGHT_GRAY);
        
        // 테이블 셀 렌더러
        calendarTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                if (value != null) {
                    LocalDate date = (LocalDate) value;
                    JLabel dateLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
                    dateLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    panel.add(dateLabel, BorderLayout.NORTH);
                    
                    List<Schedule> schedules = scheduleMap.getOrDefault(date, Collections.emptyList());
                    JPanel schedulePanel = new JPanel();
                    schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
                    schedulePanel.setBackground(panel.getBackground());
                    
                    for (Schedule schedule : schedules) {
                        JLabel scheduleLabel = new JLabel(schedule.getTitle());
                        scheduleLabel.setForeground(Color.decode(schedule.getColor()));
                        schedulePanel.add(scheduleLabel);
                    }
                    
                    panel.add(schedulePanel, BorderLayout.CENTER);
                }
                
                return panel;
            }
        });
        
        // 테이블 클릭 이벤트
        calendarTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = calendarTable.getSelectedRow();
                int col = calendarTable.getSelectedColumn();
                if (row >= 0 && col >= 0) {
                    LocalDate date = (LocalDate) tableModel.getValueAt(row, col);
                    if (date != null) {
                        showScheduleDialog(date);
                    }
                }
            }
        });
        
        // 스크롤 패널
        JScrollPane scrollPane = new JScrollPane(calendarTable);
        
        // 패널에 컴포넌트 추가
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSchedules() {
        scheduleMap.clear();
        String userId = userManager.getCurrentUser().getUserId();
        List<Schedule> schedules = scheduleManager.getSchedules();
        
        for (Schedule schedule : schedules) {
            if (schedule.getUserId().equals(userId)) {
            LocalDateTime start = schedule.getStartTime();
            LocalDateTime end = schedule.getEndTime();
            
            // 반복 일정 처리
            if (schedule.getRecurrenceType() != Schedule.RecurrenceType.NONE) {
                List<Schedule> instances = schedule.getRecurringInstances(
                    start,
                    end.plusYears(1) // 1년치 반복 일정 생성
                );
                
                for (Schedule instance : instances) {
                    addScheduleToMap(instance);
                }
            } else {
                addScheduleToMap(schedule);
                }
            }
        }
        
        updateCalendar();
    }

    private void addScheduleToMap(Schedule schedule) {
        LocalDate date = schedule.getStartTime().toLocalDate();
        scheduleMap.computeIfAbsent(date, _ -> new ArrayList<>()).add(schedule);
    }

    private void updateCalendar() {
        tableModel.setRowCount(0);
        
        int year = (Integer) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        currentDate = LocalDate.of(year, month, 1);
        
        switch (currentView) {
            case MONTH:
                updateMonthView();
                break;
            case WEEK:
                updateWeekView();
                break;
            case DAY:
                updateDayView();
                break;
        }
    }

    private void updateMonthView() {
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        LocalDate lastDay = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int totalDays = lastDay.getDayOfMonth();
        
        for (int i = 0; i < 6; i++) {
            Object[] row = new Object[7];
            for (int j = 0; j < 7; j++) {
                int day = i * 7 + j - firstDayOfWeek + 1;
                if (day > 0 && day <= totalDays) {
                    row[j] = currentDate.withDayOfMonth(day);
                } else {
                    row[j] = null;
                }
            }
            tableModel.addRow(row);
        }
    }

    private void updateWeekView() {
        LocalDate weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        
        Object[] row = new Object[7];
        for (int i = 0; i < 7; i++) {
            row[i] = weekStart.plusDays(i);
        }
        tableModel.addRow(row);
    }

    private void updateDayView() {
        Object[] row = new Object[7];
        row[0] = currentDate;
        tableModel.addRow(row);
    }

    private void showScheduleDialog(LocalDate date) {
        List<Schedule> schedulesOnDate = scheduleMap.getOrDefault(date, Collections.emptyList());

        if (schedulesOnDate.isEmpty()) {
            // 해당 날짜에 일정이 없으면 새 일정 추가 다이얼로그를 엽니다.
            ScheduleDialog dialog = new ScheduleDialog(parent, scheduleManager, userManager, null, this::loadSchedules);
            dialog.setStartTime(date.atStartOfDay());
            dialog.setVisible(true);
        } else {
            // 일정이 하나만 있으면 바로 수정 다이얼로그를 엽니다.
            if (schedulesOnDate.size() == 1) {
                Schedule scheduleToEdit = schedulesOnDate.get(0);
                ScheduleDialog dialog = new ScheduleDialog(parent, scheduleManager, userManager, scheduleToEdit, this::loadSchedules);
                dialog.setVisible(true);
            } else {
                // 여러 일정이 있으면 선택할 수 있는 목록을 보여줍니다.
                Schedule selected = (Schedule) JOptionPane.showInputDialog(
                        parent,
                        "수정할 일정을 선택하세요:",
                        "일정 선택",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        schedulesOnDate.toArray(),
                        schedulesOnDate.get(0)
                );
                if (selected != null) {
                    ScheduleDialog dialog = new ScheduleDialog(parent, scheduleManager, userManager, selected, this::loadSchedules);
        dialog.setVisible(true);
                }
            }
        }
    }

    public void showAddScheduleDialog() {
        ScheduleDialog dialog = new ScheduleDialog(parent, scheduleManager, userManager, null, this::loadSchedules);
        dialog.setVisible(true);
    }
} 