import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.time.ZoneId;
import java.util.UUID;

public class ScheduleDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner startDateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endDateSpinner;
    private JSpinner endTimeSpinner;
    private JTextField locationField;
    private JComboBox<String> categoryCombo;
    private JCheckBox importantCheckBox;
    private JComboBox<String> colorComboBox;
    private JTextField tagField;
    private JList<String> tagList;
    private DefaultListModel<String> tagListModel;
    private JComboBox<Schedule.RecurrenceType> recurrenceComboBox;
    private JSpinner recurrenceEndDateSpinner;
    private JSpinner prioritySpinner;
    private JList<Schedule.SubTask> subTaskList;
    private DefaultListModel<Schedule.SubTask> subTaskListModel;
    private boolean confirmed;
    private Schedule schedule;
    private ScheduleManager scheduleManager;
    private UserManager userManager;
    private Runnable onSave;

    public ScheduleDialog(Frame parent, ScheduleManager scheduleManager, UserManager userManager, Schedule schedule, Runnable onSave) {
        super(parent, (schedule == null ? "일정 추가" : "일정 수정"), true);
        this.scheduleManager = scheduleManager;
        this.userManager = userManager;
        this.schedule = schedule;
        this.onSave = onSave;
        this.confirmed = false;
        initComponents();

        if (schedule != null) {
            setSchedule(schedule);
        } else {
            // Set default start time to the next hour
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextHour = now.withMinute(0).withSecond(0).plusHours(1);
            setDateTimeToSpinners(startDateSpinner, startTimeSpinner, nextHour);
            setDateTimeToSpinners(endDateSpinner, endTimeSpinner, nextHour.plusHours(1));
        }

        pack();
        setLocationRelativeTo(parent);
    }

    public ScheduleDialog(ScheduleGUI parent, java.time.LocalDate date) {
        this(parent, null, null, null, null); // This constructor might need reassessment or removal.
        if (date != null) {
            java.util.Date utilDate = java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            startDateSpinner.setValue(utilDate);
            endDateSpinner.setValue(utilDate);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 입력 필드 패널
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 템플릿 불러오기 버튼
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JButton templateButton = new JButton("템플릿 불러오기");
        templateButton.addActionListener(_ -> showTemplateDialog());
        inputPanel.add(templateButton, gbc);
        gbc.gridwidth = 1;
        
        // 제목
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("제목:"), gbc);
        titleField = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(titleField, gbc);
        
        // 설명
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("설명:"), gbc);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        inputPanel.add(descriptionScroll, gbc);
        
        // 시작 시간
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("시작 시간:"), gbc);
        JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startDateSpinner.setEditor(dateEditor);
        startTimeSpinner.setEditor(timeEditor);
        startTimePanel.add(startDateSpinner);
        startTimePanel.add(startTimeSpinner);
        gbc.gridx = 1;
        inputPanel.add(startTimePanel, gbc);
        
        // 종료 시간
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("종료 시간:"), gbc);
        JPanel endTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        dateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        timeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endDateSpinner.setEditor(dateEditor);
        endTimeSpinner.setEditor(timeEditor);
        endTimePanel.add(endDateSpinner);
        endTimePanel.add(endTimeSpinner);
        gbc.gridx = 1;
        inputPanel.add(endTimePanel, gbc);
        
        // 장소
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("장소:"), gbc);
        locationField = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(locationField, gbc);
        
        // 카테고리
        gbc.gridx = 0; gbc.gridy = 6;
        inputPanel.add(new JLabel("카테고리:"), gbc);
        categoryCombo = new JComboBox<>(new String[]{"업무", "개인", "학습", "기타"});
        gbc.gridx = 1;
        inputPanel.add(categoryCombo, gbc);
        
        // 중요 여부
        gbc.gridx = 0; gbc.gridy = 7;
        inputPanel.add(new JLabel("중요:"), gbc);
        importantCheckBox = new JCheckBox();
        gbc.gridx = 1;
        inputPanel.add(importantCheckBox, gbc);
        
        // 색상
        gbc.gridx = 0; gbc.gridy = 8;
        inputPanel.add(new JLabel("색상:"), gbc);
        colorComboBox = new JComboBox<>(new String[]{"#4A90E2", "#50E3C2", "#F5A623", "#D0021B", "#9013FE"});
        gbc.gridx = 1;
        inputPanel.add(colorComboBox, gbc);
        
        // 태그
        gbc.gridx = 0; gbc.gridy = 9;
        inputPanel.add(new JLabel("태그:"), gbc);
        gbc.gridx = 1;
        JPanel tagPanel = new JPanel(new BorderLayout());
        tagField = new JTextField(15);
        JButton addTagButton = new JButton("추가");
        tagPanel.add(tagField, BorderLayout.CENTER);
        tagPanel.add(addTagButton, BorderLayout.EAST);
        inputPanel.add(tagPanel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 10;
        tagListModel = new DefaultListModel<>();
        tagList = new JList<>(tagListModel);
        inputPanel.add(new JScrollPane(tagList), gbc);
        
        // 반복 설정
        gbc.gridx = 0; gbc.gridy = 11;
        inputPanel.add(new JLabel("반복:"), gbc);
        gbc.gridx = 1;
        recurrenceComboBox = new JComboBox<>(Schedule.RecurrenceType.values());
        inputPanel.add(recurrenceComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 12;
        inputPanel.add(new JLabel("반복 종료:"), gbc);
        gbc.gridx = 1;
        recurrenceEndDateSpinner = new JSpinner(new SpinnerDateModel());
        dateEditor = new JSpinner.DateEditor(recurrenceEndDateSpinner, "yyyy-MM-dd");
        recurrenceEndDateSpinner.setEditor(dateEditor);
        inputPanel.add(recurrenceEndDateSpinner, gbc);
        
        // 우선순위
        gbc.gridx = 0; gbc.gridy = 13;
        inputPanel.add(new JLabel("우선순위:"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel priorityModel = new SpinnerNumberModel(0, 0, 5, 1);
        prioritySpinner = new JSpinner(priorityModel);
        inputPanel.add(prioritySpinner, gbc);
        
        // 하위 작업
        gbc.gridx = 0; gbc.gridy = 14;
        inputPanel.add(new JLabel("하위 작업:"), gbc);
        gbc.gridx = 1;
        JPanel subTaskPanel = new JPanel(new BorderLayout());
        JTextField subTaskField = new JTextField(15);
        JButton addSubTaskButton = new JButton("추가");
        subTaskPanel.add(subTaskField, BorderLayout.CENTER);
        subTaskPanel.add(addSubTaskButton, BorderLayout.EAST);
        inputPanel.add(subTaskPanel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 15;
        subTaskListModel = new DefaultListModel<>();
        subTaskList = new JList<>(subTaskListModel);
        inputPanel.add(new JScrollPane(subTaskList), gbc);
        
        add(inputPanel, BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("확인");
        JButton cancelButton = new JButton("취소");
        
        okButton.addActionListener(_ -> {
            if (validateInput()) {
                saveSchedule();
            }
        });
        
        cancelButton.addActionListener(_ -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "제목을 입력해주세요.",
                "입력 오류",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        LocalDateTime startTime = getDateTimeFromSpinners(startDateSpinner, startTimeSpinner);
        LocalDateTime endTime = getDateTimeFromSpinners(endDateSpinner, endTimeSpinner);
        
        if (endTime.isBefore(startTime)) {
            JOptionPane.showMessageDialog(this,
                "종료 시간은 시작 시간보다 이후여야 합니다.",
                "입력 오류",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private LocalDateTime getDateTimeFromSpinners(JSpinner dateSpinner, JSpinner timeSpinner) {
        Date date = (Date) dateSpinner.getValue();
        Date time = (Date) timeSpinner.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        return calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        titleField.setText(schedule.getTitle());
        descriptionArea.setText(schedule.getDescription());
        setDateTimeToSpinners(startDateSpinner, startTimeSpinner, schedule.getStartTime());
        setDateTimeToSpinners(endDateSpinner, endTimeSpinner, schedule.getEndTime());
        locationField.setText(schedule.getLocation());
        categoryCombo.setSelectedItem(schedule.getCategory());
        importantCheckBox.setSelected(schedule.isImportant());
        colorComboBox.setSelectedItem(schedule.getColor());
        prioritySpinner.setValue(schedule.getPriority());
        
        // 태그 로드
        for (String tag : schedule.getTags()) {
            tagListModel.addElement(tag);
        }
        
        // 하위 작업 로드
        for (Schedule.SubTask subTask : schedule.getSubTasks()) {
            subTaskListModel.addElement(subTask);
        }
        
        // 반복 설정 로드
        recurrenceComboBox.setSelectedItem(schedule.getRecurrenceType());
        if (schedule.getRecurrenceEnd() != null) {
            recurrenceEndDateSpinner.setValue(java.util.Date.from(schedule.getRecurrenceEnd().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
    }
    
    private void setDateTimeToSpinners(JSpinner dateSpinner, JSpinner timeSpinner, LocalDateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateTime.getYear(), dateTime.getMonthValue() - 1, dateTime.getDayOfMonth());
        dateSpinner.setValue(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHour());
        calendar.set(Calendar.MINUTE, dateTime.getMinute());
        timeSpinner.setValue(calendar.getTime());
    }
    
    public void setStartTime(LocalDateTime startTime) {
        setDateTimeToSpinners(startDateSpinner, startTimeSpinner, startTime);
    }
    
    public void setEndTime(LocalDateTime endTime) {
        setDateTimeToSpinners(endDateSpinner, endTimeSpinner, endTime);
    }
    
    public void setCategory(String category) {
        categoryCombo.setSelectedItem(category);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public Schedule getSchedule() {
        if (schedule == null) {
            schedule = new Schedule(
                null,
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                getDateTimeFromSpinners(startDateSpinner, startTimeSpinner),
                getDateTimeFromSpinners(endDateSpinner, endTimeSpinner),
                locationField.getText().trim(),
                (String) categoryCombo.getSelectedItem(),
                importantCheckBox.isSelected(),
                null
            );
        } else {
            schedule.setTitle(titleField.getText().trim());
            schedule.setDescription(descriptionArea.getText().trim());
            schedule.setStartTime(getDateTimeFromSpinners(startDateSpinner, startTimeSpinner));
            schedule.setEndTime(getDateTimeFromSpinners(endDateSpinner, endTimeSpinner));
            schedule.setLocation(locationField.getText().trim());
            schedule.setCategory((String) categoryCombo.getSelectedItem());
            schedule.setImportant(importantCheckBox.isSelected());
            schedule.setColor((String) colorComboBox.getSelectedItem());
            schedule.setPriority((Integer) prioritySpinner.getValue());
        }
        
        // 태그 설정
        for (int i = 0; i < tagListModel.getSize(); i++) {
            schedule.addTag(tagListModel.getElementAt(i));
        }
        
        // 하위 작업 설정
        for (int i = 0; i < subTaskListModel.getSize(); i++) {
            schedule.addSubTask(subTaskListModel.getElementAt(i));
        }
        
        // 반복 설정
        Schedule.RecurrenceType recurrenceType = (Schedule.RecurrenceType) recurrenceComboBox.getSelectedItem();
        if (recurrenceType != Schedule.RecurrenceType.NONE) {
            LocalDateTime recurrenceEnd = LocalDateTime.ofInstant(((java.util.Date) recurrenceEndDateSpinner.getValue()).toInstant(), java.time.ZoneId.systemDefault());
            schedule.setRecurrence(recurrenceType, recurrenceEnd);
        }
        
        return schedule;
    }

    private void saveSchedule() {
        System.out.println("saveSchedule() 호출됨");
        
        if (userManager.getCurrentUser() == null) {
            System.err.println("사용자가 로그인되지 않음");
            JOptionPane.showMessageDialog(this, "일정을 저장하려면 로그인이 필요합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText();
            LocalDateTime startTime = getDateTimeFromSpinners(startDateSpinner, startTimeSpinner);
            LocalDateTime endTime = getDateTimeFromSpinners(endDateSpinner, endTimeSpinner);
            String location = locationField.getText();
            String category = (String) categoryCombo.getSelectedItem();
            boolean isImportant = importantCheckBox.isSelected();
            String color = (String) colorComboBox.getSelectedItem();
            // Tags, Recurrence, etc. would be retrieved here

            if (schedule == null) { // 새 일정 추가
                System.out.println("새 일정 추가 시도");
                String userId = userManager.getCurrentUser().getUserId();
                Schedule newSchedule = new Schedule(
                    UUID.randomUUID().toString(), title, description, startTime, endTime,
                    location, category, isImportant, userId
                );
                newSchedule.setColor(color);
                // set other properties like tags, recurrence...
                scheduleManager.addSchedule(newSchedule);
                System.out.println("새 일정 추가 성공");
                JOptionPane.showMessageDialog(this, "일정이 성공적으로 추가되었습니다.");
            } else { // 기존 일정 수정
                System.out.println("기존 일정 수정 시도: " + schedule.getScheduleId());
                schedule.setTitle(title);
                schedule.setDescription(description);
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setLocation(location);
                schedule.setCategory(category);
                schedule.setImportant(isImportant);
                schedule.setColor(color);
                // set other properties...
                scheduleManager.updateSchedule(schedule.getScheduleId(), schedule);
                System.out.println("기존 일정 수정 성공");
                JOptionPane.showMessageDialog(this, "일정이 성공적으로 수정되었습니다.");
            }

            if (onSave != null) {
                System.out.println("onSave 콜백 실행");
                onSave.run();
            }
            dispose();

        } catch (Exception e) {
            System.err.println("일정 저장 중 오류: " + e.getMessage());
            e.printStackTrace(); // Log the full error for debugging
            JOptionPane.showMessageDialog(this, "일정 저장 중 오류 발생: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 템플릿 다이얼로그 및 적용 메서드 추가
    private void showTemplateDialog() {
        String[] templates = {"회의", "운동", "공부", "개인 일정"};
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "템플릿을 선택하세요:",
            "일정 템플릿",
            JOptionPane.PLAIN_MESSAGE,
            null,
            templates,
            templates[0]
        );
        if (selected != null) {
            applyTemplate(selected);
        }
    }

    private void applyTemplate(String template) {
        switch (template) {
            case "회의":
                titleField.setText("팀 회의");
                descriptionArea.setText("주간 업무 공유 및 논의");
                categoryCombo.setSelectedItem("업무");
                locationField.setText("회의실");
                break;
            case "운동":
                titleField.setText("운동");
                descriptionArea.setText("헬스장 또는 야외 운동");
                categoryCombo.setSelectedItem("개인");
                locationField.setText("헬스장");
                break;
            case "공부":
                titleField.setText("공부");
                descriptionArea.setText("자격증/학습/과제");
                categoryCombo.setSelectedItem("학습");
                locationField.setText("도서관");
                break;
            case "개인 일정":
                titleField.setText("개인 일정");
                descriptionArea.setText("개인적인 용무");
                categoryCombo.setSelectedItem("개인");
                locationField.setText("");
                break;
        }
    }
} 