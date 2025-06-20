import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskListPanel extends JPanel {
    private final ScheduleGUI parent;
    private final ScheduleManager scheduleManager;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    private JComboBox<String> tagFilterCombo;
    private JCheckBox importantFilterCheckBox;
    private Font koreanFont;

    public TaskListPanel(ScheduleGUI parent, ScheduleManager scheduleManager) {
        this.parent = parent;
        this.scheduleManager = scheduleManager;
        this.koreanFont = new Font("Malgun Gothic", Font.PLAIN, 14);
        
        setLayout(new BorderLayout());
        
        // 필터 패널
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // 테이블 패널
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 초기 데이터 로드
        loadTasks();
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 검색 필드
        searchField = new JTextField(15);
        searchField.setFont(koreanFont);
        panel.add(new JLabel("검색:"));
        panel.add(searchField);

        // 필터 콤보박스
        filterCombo = new JComboBox<>(new String[]{
            "전체", "대기중", "진행중", "완료", "중요", "지연"
        });
        filterCombo.setFont(koreanFont);
        panel.add(new JLabel("필터:"));
        panel.add(filterCombo);

        // 태그 필터
        tagFilterCombo = new JComboBox<>();
        tagFilterCombo.setFont(koreanFont);
        panel.add(new JLabel("태그:"));
        panel.add(tagFilterCombo);

        // 중요 태스크 필터
        importantFilterCheckBox = new JCheckBox("중요 태스크만");
        importantFilterCheckBox.setFont(koreanFont);
        panel.add(importantFilterCheckBox);

        // 필터 적용 버튼
        JButton applyFilterButton = new JButton("필터 적용");
        applyFilterButton.setFont(koreanFont);
        applyFilterButton.addActionListener(_ -> applyFilters());
        panel.add(applyFilterButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 테이블 모델 설정 - 진행률 컬럼 추가
        String[] columnNames = {"제목", "마감일", "상태", "우선순위", "진행률", "체크리스트"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        taskTable = new JTable(tableModel);
        taskTable.setFont(koreanFont);
        taskTable.getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        taskTable.setRowHeight(30); // 높이 증가
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 컬럼 너비 설정
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 제목
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 마감일
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 상태
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 우선순위
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 진행률
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(120); // 체크리스트

        // 더블클릭 이벤트 처리
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = taskTable.getSelectedRow();
                    if (row != -1) {
                        String taskId = (String) taskTable.getValueAt(row, 0);
                        Schedule task = scheduleManager.getSchedule(taskId);
                        if (task != null) {
                            showTaskDetail(task);
                        }
                    }
                }
            }
        });

        // 스크롤 패널에 테이블 추가
        JScrollPane scrollPane = new JScrollPane(taskTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("할 일 추가");
        JButton editButton = new JButton("수정");
        JButton deleteButton = new JButton("삭제");
        JButton completeButton = new JButton("완료");
        JButton checklistButton = new JButton("체크리스트"); // 체크리스트 버튼 추가

        addButton.setFont(koreanFont);
        editButton.setFont(koreanFont);
        deleteButton.setFont(koreanFont);
        completeButton.setFont(koreanFont);
        checklistButton.setFont(koreanFont);

        addButton.addActionListener(_ -> addTask());
        editButton.addActionListener(_ -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                    "수정할 태스크를 선택해주세요.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String taskId = (String) taskTable.getValueAt(selectedRow, 0);
            Schedule task = scheduleManager.getSchedule(taskId);
            if (task != null) {
                editTask(task);
            }
        });
        deleteButton.addActionListener(_ -> deleteTask());
        completeButton.addActionListener(_ -> completeTask());
        checklistButton.addActionListener(_ -> showChecklistDialog()); // 체크리스트 다이얼로그

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(completeButton);
        panel.add(checklistButton);

        return panel;
    }

    private void showTaskDetail(Schedule task) {
        TaskDialog dialog = new TaskDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "태스크 상세",
            task);
        dialog.setVisible(true);
        
        Schedule updatedTask = dialog.getTask();
        if (updatedTask != null) {
            try {
                scheduleManager.updateSchedule(task.getScheduleId(), updatedTask);
                loadTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "오류",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addTask() {
        TaskDialog dialog = new TaskDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "새 태스크 추가",
            null);
        dialog.setVisible(true);

        Schedule task = dialog.getTask();
        if (task != null) {
            try {
                scheduleManager.addSchedule(task);
                loadTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "오류",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editTask(Schedule task) {
        TaskDialog dialog = new TaskDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "태스크 수정",
            task);
        dialog.setVisible(true);

        Schedule updatedTask = dialog.getTask();
        if (updatedTask != null) {
            try {
                scheduleManager.updateSchedule(task.getScheduleId(), updatedTask);
                loadTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "오류",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "삭제할 태스크를 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String taskId = (String) taskTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "선택한 태스크를 삭제하시겠습니까?",
            "확인",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                scheduleManager.deleteSchedule(taskId);
                loadTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "오류",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void completeTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "완료할 태스크를 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String taskId = (String) taskTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "선택한 태스크를 완료하시겠습니까?",
            "확인",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                scheduleManager.completeSchedule(taskId);
                loadTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "오류",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChecklistDialog() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "체크리스트를 관리할 태스크를 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String taskId = (String) taskTable.getValueAt(selectedRow, 0);
        Schedule task = scheduleManager.getSchedule(taskId);
        if (task != null) {
            ChecklistDialog dialog = new ChecklistDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                task);
            dialog.setVisible(true);
            loadTasks(); // 테이블 새로고침
        }
    }

    private void loadTasks() {
        tableModel.setRowCount(0);
        List<Schedule> tasks = scheduleManager.getSchedules();
        
        for (Schedule task : tasks) {
            // 진행률 계산
            double progress = calculateProgress(task);
            String progressText = String.format("%.1f%%", progress);
            
            // 체크리스트 정보
            String checklistInfo = getChecklistInfo(task);
            
            Object[] row = {
                task.getTitle(),
                task.getStartTime() != null ? task.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "없음",
                getStatusText(task),
                getPriorityText(task),
                progressText,
                checklistInfo
            };
            tableModel.addRow(row);
        }
        
        updateTagFilter();
    }

    private void updateTagFilter() {
        tagFilterCombo.removeAllItems();
        tagFilterCombo.addItem("전체");
        for (String tag : scheduleManager.getAllTags()) {
            tagFilterCombo.addItem(tag);
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedFilter = (String) filterCombo.getSelectedItem();
        String selectedTag = (String) tagFilterCombo.getSelectedItem();
        boolean importantOnly = importantFilterCheckBox.isSelected();

        tableModel.setRowCount(0);
        List<Schedule> tasks = scheduleManager.getSchedulesByCategory("할 일");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Schedule task : tasks) {
            // 검색어 필터링
            if (!searchText.isEmpty() &&
                !task.getTitle().toLowerCase().contains(searchText) &&
                !task.getDescription().toLowerCase().contains(searchText)) {
                continue;
            }

            // 상태 필터링
            if (!selectedFilter.equals("전체")) {
                switch (selectedFilter) {
                    case "대기중":
                        if (!"TODO".equals(task.getStatus())) continue;
                        break;
                    case "진행중":
                        if (!"IN_PROGRESS".equals(task.getStatus())) continue;
                        break;
                    case "완료":
                        if (!"COMPLETED".equals(task.getStatus())) continue;
                        break;
                    case "중요":
                        if (!task.isImportant()) continue;
                        break;
                    case "지연":
                        if (task.getEndTime() == null || 
                            !task.getEndTime().isBefore(LocalDateTime.now()) ||
                            "COMPLETED".equals(task.getStatus())) {
                            continue;
                        }
                        break;
                }
            }

            // 태그 필터링
            if (!selectedTag.equals("전체") && !task.getTags().contains(selectedTag)) {
                continue;
            }

            // 중요 태스크 필터링
            if (importantOnly && !task.isImportant()) {
                continue;
            }

            // 진행률 계산
            double progress = calculateProgress(task);
            String progressText = String.format("%.1f%%", progress);
            
            // 체크리스트 정보
            String checklistInfo = getChecklistInfo(task);
            
            Object[] row = {
                task.getTitle(),
                task.getEndTime() != null ? task.getEndTime().format(formatter) : "없음",
                task.getStatus(),
                task.isImportant() ? "높음" : "보통",
                progressText,
                checklistInfo
            };
            tableModel.addRow(row);
        }
    }

    // 진행률 계산
    private double calculateProgress(Schedule task) {
        // 간단한 진행률 계산 (실제로는 Task 객체의 체크리스트를 사용해야 함)
        if (task.getTitle().contains("완료")) {
            return 100.0;
        } else if (task.getTitle().contains("진행")) {
            return 50.0;
        } else {
            return 0.0;
        }
    }
    
    // 체크리스트 정보 가져오기
    private String getChecklistInfo(Schedule task) {
        // 실제로는 Task 객체의 체크리스트 정보를 사용해야 함
        return "0/0"; // 임시 값
    }
    
    // 상태 텍스트 변환
    private String getStatusText(Schedule task) {
        if (task.getTitle().contains("완료")) {
            return "완료";
        } else if (task.getTitle().contains("진행")) {
            return "진행중";
        } else {
            return "대기중";
        }
    }
    
    // 우선순위 텍스트 변환
    private String getPriorityText(Schedule task) {
        if (task.isImportant()) {
            return "높음";
        } else {
            return "보통";
        }
    }
} 