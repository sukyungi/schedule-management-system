import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

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
    private List<Schedule> displayedSchedules;

    public TaskListPanel(ScheduleGUI parent, ScheduleManager scheduleManager) {
        this.parent = parent;
        this.scheduleManager = scheduleManager;
        this.displayedSchedules = new ArrayList<>();
        
        // 한글 폰트 설정
        koreanFont = new Font("Malgun Gothic", Font.PLAIN, 12);
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(createFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        loadSchedules();
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("필터 및 검색"));
        
        // 검색 필드
        searchField = new JTextField(20);
        searchField.setFont(koreanFont);
        searchField.addActionListener(e -> applyFilters());
        
        // 상태 필터
        filterCombo = new JComboBox<>(new String[]{"전체", "대기중", "진행중", "완료", "중요", "지연"});
        filterCombo.setFont(koreanFont);
        filterCombo.addActionListener(e -> applyFilters());
        
        // 태그 필터
        tagFilterCombo = new JComboBox<>();
        tagFilterCombo.setFont(koreanFont);
        tagFilterCombo.addActionListener(e -> applyFilters());
        
        // 중요 필터
        importantFilterCheckBox = new JCheckBox("중요 일정만");
        importantFilterCheckBox.setFont(koreanFont);
        importantFilterCheckBox.addActionListener(e -> applyFilters());
        
        panel.add(new JLabel("검색:"));
        panel.add(searchField);
        panel.add(new JLabel("상태:"));
        panel.add(filterCombo);
        panel.add(new JLabel("태그:"));
        panel.add(tagFilterCombo);
        panel.add(importantFilterCheckBox);
        
        return panel;
    }

    private JPanel createTablePanel() {
        String[] columnNames = {"제목", "마감일", "상태", "우선순위", "진행률", "체크리스트"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        taskTable = new JTable(tableModel);
        taskTable.setFont(koreanFont);
        taskTable.getTableHeader().setFont(koreanFont);
        taskTable.setRowHeight(25);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 더블클릭으로 일정 상세보기
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = taskTable.getSelectedRow();
                    if (selectedRow != -1) {
                        showScheduleDetail(displayedSchedules.get(selectedRow));
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("일정 목록"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton addButton = new JButton("일정 추가");
        JButton editButton = new JButton("일정 수정");
        JButton deleteButton = new JButton("일정 삭제");
        JButton completeButton = new JButton("완료 처리");
        JButton checklistButton = new JButton("체크리스트");
        
        // 버튼 폰트 설정
        Font buttonFont = new Font("Malgun Gothic", Font.BOLD, 12);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        completeButton.setFont(buttonFont);
        checklistButton.setFont(buttonFont);
        
        addButton.addActionListener(e -> addSchedule());
        editButton.addActionListener(e -> editSchedule());
        deleteButton.addActionListener(e -> deleteSchedule());
        completeButton.addActionListener(e -> completeSchedule());
        checklistButton.addActionListener(e -> showChecklistDialog());
        
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(completeButton);
        panel.add(checklistButton);
        
        return panel;
    }

    private void showScheduleDetail(Schedule schedule) {
        ScheduleDetailDialog dialog = new ScheduleDetailDialog(parent, schedule);
        dialog.setVisible(true);
        loadSchedules();
    }

    private void addSchedule() {
        ScheduleDialog dialog = new ScheduleDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            scheduleManager, 
            UserManager.getInstance(), 
            null, 
            this::loadSchedules
        );
        dialog.setVisible(true);
    }

    private void editSchedule() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "수정할 일정을 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Schedule scheduleToEdit = displayedSchedules.get(selectedRow);
        System.out.println("수정할 일정: " + scheduleToEdit.getTitle() + " (ID: " + scheduleToEdit.getScheduleId() + ")");
        
        ScheduleDialog dialog = new ScheduleDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            scheduleManager, 
            UserManager.getInstance(), 
            scheduleToEdit, 
            this::loadSchedules
        );
        dialog.setVisible(true);
        System.out.println("수정 다이얼로그가 닫힘");
    }

    private void deleteSchedule() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "삭제할 일정을 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Schedule scheduleToDelete = displayedSchedules.get(selectedRow);
        System.out.println("삭제할 일정: " + scheduleToDelete.getTitle() + " (ID: " + scheduleToDelete.getScheduleId() + ")");
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "선택한 일정 '"+ scheduleToDelete.getTitle() +"'를 삭제하시겠습니까?",
            "확인",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                System.out.println("일정 삭제 시도: " + scheduleToDelete.getScheduleId());
                scheduleManager.deleteSchedule(scheduleToDelete.getScheduleId());
                System.out.println("일정 삭제 성공");
                loadSchedules();
                JOptionPane.showMessageDialog(this, "일정이 삭제되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                System.err.println("삭제 오류: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "삭제 중 오류 발생: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void completeSchedule() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "완료할 일정을 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Schedule scheduleToComplete = displayedSchedules.get(selectedRow);
        try {
            scheduleToComplete.setStatus("COMPLETED");
            scheduleToComplete.setCompleted(true);
            scheduleManager.updateSchedule(scheduleToComplete.getScheduleId(), scheduleToComplete);
            loadSchedules();
            JOptionPane.showMessageDialog(this, "일정이 완료 처리되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "완료 처리 중 오류 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChecklistDialog() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "체크리스트를 관리할 일정을 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Schedule schedule = displayedSchedules.get(selectedRow);
        ChecklistDialog dialog = new ChecklistDialog(parent, schedule);
        dialog.setVisible(true);
        loadSchedules();
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        displayedSchedules.clear();
        
        List<Schedule> allSchedules = scheduleManager.getUserSchedules();
        displayedSchedules.addAll(allSchedules);

        for (Schedule schedule : displayedSchedules) {
            Object[] rowData = {
                schedule.getTitle(),
                schedule.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                getStatusText(schedule),
                getPriorityText(schedule),
                String.format("%.1f%%", schedule.getProgress()),
                getChecklistInfo(schedule)
            };
            tableModel.addRow(rowData);
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
        displayedSchedules.clear();
        
        List<Schedule> allSchedules = scheduleManager.getUserSchedules();

        for (Schedule schedule : allSchedules) {
            boolean matches = true;

            // 검색어 필터
            if (!searchText.isEmpty() && !schedule.getTitle().toLowerCase().contains(searchText)) {
                matches = false;
            }

            // 상태 필터
            if (matches && !"전체".equals(selectedFilter)) {
                switch (selectedFilter) {
                    case "대기중":
                        if (!"대기중".equals(getStatusText(schedule))) matches = false;
                        break;
                    case "진행중":
                        if (!"진행중".equals(getStatusText(schedule))) matches = false;
                        break;
                    case "완료":
                        if (!"완료".equals(getStatusText(schedule))) matches = false;
                        break;
                    case "중요":
                        if (!schedule.isImportant()) matches = false;
                        break;
                    case "지연":
                        if (!isOverdue(schedule)) matches = false;
                        break;
                }
            }
            
            // 태그 필터
            if (matches && selectedTag != null && !"전체".equals(selectedTag)) {
                if (!schedule.getTags().contains(selectedTag)) {
                    matches = false;
                }
            }

            // 중요 필터
            if (matches && importantOnly && !schedule.isImportant()) {
                matches = false;
            }

            if (matches) {
                displayedSchedules.add(schedule);
                Object[] rowData = {
                    schedule.getTitle(),
                    schedule.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    getStatusText(schedule),
                    getPriorityText(schedule),
                    String.format("%.1f%%", schedule.getProgress()),
                    getChecklistInfo(schedule)
                };
                tableModel.addRow(rowData);
            }
        }
    }

    private boolean isOverdue(Schedule schedule) {
        return !schedule.isCompleted() && java.time.LocalDateTime.now().isAfter(schedule.getEndTime());
    }
    
    private String getChecklistInfo(Schedule schedule) {
        List<Schedule.SubTask> subTasks = schedule.getSubTasks();
        if (subTasks.isEmpty()) {
            return "0/0";
        }
        long completedCount = subTasks.stream().filter(Schedule.SubTask::isCompleted).count();
        return completedCount + "/" + subTasks.size();
    }
    
    private String getStatusText(Schedule schedule) {
        if (schedule.isCompleted()) {
            return "완료";
        } else if ("IN_PROGRESS".equals(schedule.getStatus())) {
            return "진행중";
        } else {
            return "대기중";
        }
    }
    
    private String getPriorityText(Schedule schedule) {
        if (schedule.isImportant()) {
            return "높음";
        } else {
            return "보통";
        }
    }
} 