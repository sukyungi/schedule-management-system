import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TaskDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner startDateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endDateSpinner;
    private JSpinner endTimeSpinner;
    private JTextField locationField;
    private JComboBox<String> categoryCombo;
    private JCheckBox importantCheckBox;
    private boolean confirmed;
    private Schedule schedule;
    private Font koreanFont;

    public TaskDialog(Frame parent, String title, Schedule schedule) {
        super(parent, title, true);
        this.schedule = schedule;
        this.koreanFont = new Font("Malgun Gothic", Font.PLAIN, 14);
        
        setLayout(new BorderLayout());
        initComponents();
        if (schedule != null) {
            setSchedule(schedule);
        }
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // 입력 필드 패널
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 제목
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("제목:"), gbc);
        titleField = new JTextField(20);
        titleField.setFont(koreanFont);
        gbc.gridx = 1;
        inputPanel.add(titleField, gbc);
        
        // 설명
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("설명:"), gbc);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(koreanFont);
        descriptionArea.setLineWrap(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        inputPanel.add(descriptionScroll, gbc);
        
        // 시작 시간
        gbc.gridx = 0; gbc.gridy = 2;
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
        gbc.gridx = 0; gbc.gridy = 3;
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
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("장소:"), gbc);
        locationField = new JTextField(20);
        locationField.setFont(koreanFont);
        gbc.gridx = 1;
        inputPanel.add(locationField, gbc);
        
        // 카테고리
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("카테고리:"), gbc);
        categoryCombo = new JComboBox<>(new String[]{"업무", "개인", "학습", "기타"});
        categoryCombo.setFont(koreanFont);
        gbc.gridx = 1;
        inputPanel.add(categoryCombo, gbc);
        
        // 중요 여부
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        importantCheckBox = new JCheckBox("중요 일정");
        importantCheckBox.setFont(koreanFont);
        inputPanel.add(importantCheckBox, gbc);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("확인");
        JButton cancelButton = new JButton("취소");
        confirmButton.setFont(koreanFont);
        cancelButton.setFont(koreanFont);
        
        confirmButton.addActionListener(_ -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(_ -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        add(inputPanel, BorderLayout.CENTER);
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
        
        LocalDateTime startTime = LocalDateTime.of(
            ((java.util.Date) startDateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            ((java.util.Date) startTimeSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        );
        
        LocalDateTime endTime = LocalDateTime.of(
            ((java.util.Date) endDateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            ((java.util.Date) endTimeSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        );
        
        if (endTime.isBefore(startTime)) {
            JOptionPane.showMessageDialog(this,
                "종료 시간은 시작 시간보다 이후여야 합니다.",
                "입력 오류",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        titleField.setText(schedule.getTitle());
        descriptionArea.setText(schedule.getDescription());
        startDateSpinner.setValue(java.util.Date.from(schedule.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        startTimeSpinner.setValue(java.util.Date.from(schedule.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        endDateSpinner.setValue(java.util.Date.from(schedule.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        endTimeSpinner.setValue(java.util.Date.from(schedule.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        locationField.setText(schedule.getLocation());
        categoryCombo.setSelectedItem(schedule.getCategory());
        importantCheckBox.setSelected(schedule.isImportant());
    }

    public void setStartTime(LocalDateTime startTime) {
        startDateSpinner.setValue(java.util.Date.from(startTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        startTimeSpinner.setValue(java.util.Date.from(startTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    public void setEndTime(LocalDateTime endTime) {
        endDateSpinner.setValue(java.util.Date.from(endTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        endTimeSpinner.setValue(java.util.Date.from(endTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    public void setCategory(String category) {
        categoryCombo.setSelectedItem(category);
    }

    public Schedule getTask() {
        if (!confirmed) {
            return null;
        }
        
        LocalDateTime startTime = LocalDateTime.of(
            ((java.util.Date) startDateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            ((java.util.Date) startTimeSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        );
        
        LocalDateTime endTime = LocalDateTime.of(
            ((java.util.Date) endDateSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            ((java.util.Date) endTimeSpinner.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        );
        
        if (schedule == null) {
            String userId = UserManager.getInstance().getCurrentUser().getUserId();
            schedule = new Schedule(
                UUID.randomUUID().toString(),
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                startTime,
                endTime,
                locationField.getText().trim(),
                (String) categoryCombo.getSelectedItem(),
                importantCheckBox.isSelected(),
                userId
            );
        } else {
            schedule.setTitle(titleField.getText().trim());
            schedule.setDescription(descriptionArea.getText().trim());
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setLocation(locationField.getText().trim());
            schedule.setCategory((String) categoryCombo.getSelectedItem());
            schedule.setImportant(importantCheckBox.isSelected());
        }
        
        return schedule;
    }
} 