import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleListPanel extends JPanel {
    private final ScheduleGUI parent;
    private final ScheduleManager scheduleManager;
    private final UserManager userManager;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton recommendButton;
    
    // 검색 및 필터링 컴포넌트
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton clearFilterButton;
    private List<Schedule> allSchedules; // 전체 일정 데이터
    
    public ScheduleListPanel(ScheduleGUI parent, ScheduleManager scheduleManager, UserManager userManager) {
        this.parent = parent;
        this.scheduleManager = scheduleManager;
        this.userManager = userManager;
        
        setLayout(new BorderLayout());
        initComponents();
        loadSchedules();
    }
    
    private void initComponents() {
        // 검색 및 필터링 패널
        JPanel searchPanel = createSearchPanel();
        
        // 상단 버튼 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("일정 추가");
        editButton = new JButton("일정 수정");
        deleteButton = new JButton("일정 삭제");
        recommendButton = new JButton("일정 추천");
        
        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(recommendButton);
        
        // 상단 패널을 BorderLayout으로 구성
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(topPanel, BorderLayout.CENTER);
        
        // 테이블 모델 설정
        String[] columnNames = {"제목", "시작 시간", "종료 시간", "장소", "카테고리", "중요"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // 버튼 이벤트 처리
        addButton.addActionListener(_ -> showAddScheduleDialog());
        editButton.addActionListener(_ -> showEditScheduleDialog());
        deleteButton.addActionListener(_ -> deleteSelectedSchedule());
        recommendButton.addActionListener(_ -> showRecommendationDialog());
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("검색 및 필터"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 검색 필드
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("검색:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        searchField = new JTextField(20);
        searchField.setToolTipText("제목, 설명, 장소로 검색");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterSchedules(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterSchedules(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterSchedules(); }
        });
        panel.add(searchField, gbc);
        
        // 카테고리 필터
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("카테고리:"), gbc);
        
        gbc.gridx = 1;
        categoryFilter = new JComboBox<>(new String[]{"전체", "업무", "개인", "학습", "기타"});
        categoryFilter.addActionListener(_ -> filterSchedules());
        panel.add(categoryFilter, gbc);
        
        // 날짜 필터
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("시작일:"), gbc);
        
        gbc.gridx = 1;
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.addChangeListener(_ -> filterSchedules());
        panel.add(startDateSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("종료일:"), gbc);
        
        gbc.gridx = 1;
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.addChangeListener(_ -> filterSchedules());
        panel.add(endDateSpinner, gbc);
        
        // 필터 초기화 버튼
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 4;
        clearFilterButton = new JButton("필터 초기화");
        clearFilterButton.addActionListener(_ -> clearFilters());
        panel.add(clearFilterButton, gbc);
        
        return panel;
    }
    
    private void loadSchedules() {
        allSchedules = scheduleManager.getSchedules();
        filterSchedules();
    }
    
    private void filterSchedules() {
        tableModel.setRowCount(0);
        
        if (allSchedules == null) return;
        
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
        
        List<Schedule> filteredSchedules = allSchedules.stream()
            .filter(schedule -> {
                // 검색어 필터링
                if (!searchText.isEmpty()) {
                    boolean matchesSearch = schedule.getTitle().toLowerCase().contains(searchText) ||
                                          (schedule.getDescription() != null && schedule.getDescription().toLowerCase().contains(searchText)) ||
                                          (schedule.getLocation() != null && schedule.getLocation().toLowerCase().contains(searchText));
                    if (!matchesSearch) return false;
                }
                
                // 카테고리 필터링
                if (!"전체".equals(selectedCategory)) {
                    if (!selectedCategory.equals(schedule.getCategory())) return false;
                }
                
                // 날짜 필터링
                if (startDate != null && endDate != null) {
                    try {
                        LocalDate scheduleDate = schedule.getStartTime().toLocalDate();
                        LocalDate startLocalDate = startDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        LocalDate endLocalDate = endDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        
                        if (scheduleDate.isBefore(startLocalDate) || scheduleDate.isAfter(endLocalDate)) {
                            return false;
                        }
                    } catch (Exception e) {
                        // 날짜 변환 오류 시 해당 일정 제외
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // 필터링된 결과를 테이블에 표시
        for (Schedule schedule : filteredSchedules) {
            Object[] row = {
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getLocation(),
                schedule.getCategory(),
                schedule.isImportant() ? "★" : ""
            };
            tableModel.addRow(row);
        }
        
        // 검색 결과 개수 표시
        updateSearchResultCount(filteredSchedules.size());
    }
    
    private void clearFilters() {
        searchField.setText("");
        categoryFilter.setSelectedItem("전체");
        startDateSpinner.setValue(new java.util.Date());
        endDateSpinner.setValue(new java.util.Date());
        filterSchedules();
    }
    
    private void updateSearchResultCount(int count) {
        String resultText = String.format("검색 결과: %d개", count);
        clearFilterButton.setText(resultText);
    }
    
    private void showAddScheduleDialog() {
        ScheduleDialog dialog = new ScheduleDialog(parent, scheduleManager, userManager, null, () -> loadSchedules());
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Schedule schedule = dialog.getSchedule();
            scheduleManager.createSchedule(schedule);
            loadSchedules();
        }
    }
    
    private void showEditScheduleDialog() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "수정할 일정을 선택해주세요.",
                "알림",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String title = (String) tableModel.getValueAt(selectedRow, 0);
        Schedule schedule = scheduleManager.getScheduleByTitle(title);
        
        if (schedule != null) {
            ScheduleDialog dialog = new ScheduleDialog(parent, scheduleManager, userManager, schedule, () -> loadSchedules());
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                Schedule updatedSchedule = dialog.getSchedule();
                scheduleManager.updateSchedule(updatedSchedule);
                loadSchedules();
            }
        }
    }
    
    private void deleteSelectedSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "삭제할 일정을 선택해주세요.",
                "알림",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "선택한 일정을 삭제하시겠습니까?",
            "일정 삭제",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String title = (String) tableModel.getValueAt(selectedRow, 0);
            Schedule schedule = scheduleManager.getScheduleByTitle(title);
            if (schedule != null) {
                scheduleManager.deleteSchedule(schedule.getScheduleId());
                loadSchedules();
            }
        }
    }
    
    private void showRecommendationDialog() {
        ScheduleRecommender recommender = new ScheduleRecommender(scheduleManager);
        ScheduleRecommendationDialog dialog = new ScheduleRecommendationDialog(parent, recommender, userManager.getCurrentUserId());
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ScheduleRecommender.ScheduleRecommendation recommendation = dialog.getSelectedRecommendation();
            if (recommendation != null) {
                ScheduleDialog scheduleDialog = new ScheduleDialog(parent, scheduleManager, userManager, null, () -> loadSchedules());
                scheduleDialog.setStartTime(recommendation.getTimeSlot().start);
                scheduleDialog.setEndTime(recommendation.getTimeSlot().end);
                scheduleDialog.setCategory(recommendation.getCategory());
                scheduleDialog.setVisible(true);
                
                if (scheduleDialog.isConfirmed()) {
                    Schedule schedule = scheduleDialog.getSchedule();
                    scheduleManager.createSchedule(schedule);
                    loadSchedules();
                }
            }
        }
    }
} 