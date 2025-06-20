import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;

public class RecurrenceDialog extends JDialog {
    private JComboBox<String> recurrenceTypeCombo;
    private JSpinner intervalSpinner;
    private JSpinner endDateSpinner;
    private JList<LocalDateTime> exceptionDatesList;
    private DefaultListModel<LocalDateTime> exceptionDatesModel;
    private Schedule schedule;
    private boolean confirmed = false;

    public RecurrenceDialog(Frame owner, Schedule schedule) {
        super(owner, "반복 설정", true);
        this.schedule = schedule;

        setLayout(new BorderLayout());
        setSize(400, 500);
        setLocationRelativeTo(owner);

        // 상단 패널 (반복 유형 및 간격 설정)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (예외 날짜 목록)
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // 하단 패널 (버튼)
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // 초기값 설정
        initializeValues();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 반복 유형 선택
        recurrenceTypeCombo = new JComboBox<>(new String[]{
            "반복 안함", "매일", "매주", "매월", "매년"
        });
        recurrenceTypeCombo.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 반복 간격 설정
        SpinnerNumberModel intervalModel = new SpinnerNumberModel(1, 1, 99, 1);
        intervalSpinner = new JSpinner(intervalModel);
        intervalSpinner.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 반복 종료일 설정
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("반복 유형:"), gbc);
        gbc.gridx = 1;
        panel.add(recurrenceTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("반복 간격:"), gbc);
        gbc.gridx = 1;
        panel.add(intervalSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("반복 종료일:"), gbc);
        gbc.gridx = 1;
        panel.add(endDateSpinner, gbc);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("예외 날짜"));

        exceptionDatesModel = new DefaultListModel<>();
        exceptionDatesList = new JList<>(exceptionDatesModel);
        exceptionDatesList.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(exceptionDatesList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 예외 날짜 추가/제거 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("예외 날짜 추가");
        JButton removeButton = new JButton("예외 날짜 제거");
        addButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        removeButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        addButton.addActionListener(_ -> addExceptionDate());
        removeButton.addActionListener(_ -> removeExceptionDate());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("확인");
        JButton cancelButton = new JButton("취소");
        okButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        cancelButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        
        okButton.addActionListener(_ -> {
            saveChanges();
            confirmed = true;
            dispose();
        });
        cancelButton.addActionListener(_ -> dispose());
        
        panel.add(okButton);
        panel.add(cancelButton);
        return panel;
    }

    private void initializeValues() {
        if (schedule.isRecurring()) {
            Schedule.RecurrenceType type = schedule.getRecurrenceType();
            switch (type) {
                case DAILY:
                    recurrenceTypeCombo.setSelectedItem("매일");
                    break;
                case WEEKLY:
                    recurrenceTypeCombo.setSelectedItem("매주");
                    break;
                case MONTHLY:
                    recurrenceTypeCombo.setSelectedItem("매월");
                    break;
                case YEARLY:
                    recurrenceTypeCombo.setSelectedItem("매년");
                    break;
                default:
                    recurrenceTypeCombo.setSelectedItem("반복 안함");
                    break;
            }
            intervalSpinner.setValue(schedule.getRecurrenceInterval());
            if (schedule.getRecurrenceEndDate() != null) {
                endDateSpinner.setValue(java.util.Date.from(
                    schedule.getRecurrenceEndDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }
        } else {
            recurrenceTypeCombo.setSelectedItem("반복 안함");
        }

        // 예외 날짜 목록 초기화
        for (LocalDateTime date : schedule.getExceptionDates()) {
            exceptionDatesModel.addElement(date);
        }
    }

    private void addExceptionDate() {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        int result = JOptionPane.showConfirmDialog(this, dateSpinner,
            "예외 날짜 선택", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            LocalDateTime dateTime = date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
            exceptionDatesModel.addElement(dateTime);
        }
    }

    private void removeExceptionDate() {
        int selectedIndex = exceptionDatesList.getSelectedIndex();
        if (selectedIndex != -1) {
            exceptionDatesModel.remove(selectedIndex);
        }
    }

    private void saveChanges() {
        String selectedType = (String) recurrenceTypeCombo.getSelectedItem();
        boolean isRecurring = !selectedType.equals("반복 안함");
        schedule.setRecurring(isRecurring);

        if (isRecurring) {
            Schedule.RecurrenceType recurrenceType = Schedule.RecurrenceType.NONE;
            switch (selectedType) {
                case "매일":
                    recurrenceType = Schedule.RecurrenceType.DAILY;
                    break;
                case "매주":
                    recurrenceType = Schedule.RecurrenceType.WEEKLY;
                    break;
                case "매월":
                    recurrenceType = Schedule.RecurrenceType.MONTHLY;
                    break;
                case "매년":
                    recurrenceType = Schedule.RecurrenceType.YEARLY;
                    break;
            }
            schedule.setRecurrence(recurrenceType, null);
            schedule.setRecurrenceInterval((Integer) intervalSpinner.getValue());
            
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
            if (endDate != null) {
                LocalDateTime endDateTime = endDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
                schedule.setRecurrenceEndDate(endDateTime);
            }
        }

        // 예외 날짜 업데이트
        schedule.getExceptionDates().clear();
        for (int i = 0; i < exceptionDatesModel.size(); i++) {
            schedule.addExceptionDate(exceptionDatesModel.get(i));
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 