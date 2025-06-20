import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;

public class ScheduleRecurrenceDialog extends JDialog {
    private Schedule schedule;
    private boolean confirmed = false;
    
    private JComboBox<String> recurrenceTypeCombo;
    private JSpinner intervalSpinner;
    private JSpinner endDateSpinner;
    private JList<LocalDateTime> exceptionDatesList;
    private DefaultListModel<LocalDateTime> exceptionDatesModel;
    
    public ScheduleRecurrenceDialog(Frame owner, Schedule schedule) {
        super(owner, "반복 일정 설정", true);
        this.schedule = schedule;
        
        setLayout(new BorderLayout());
        initComponents();
        loadScheduleData();
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        // 반복 설정 패널
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 반복 유형
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("반복 유형:"), gbc);
        
        gbc.gridx = 1;
        recurrenceTypeCombo = new JComboBox<>(new String[]{"일간", "주간", "월간", "연간"});
        settingsPanel.add(recurrenceTypeCombo, gbc);
        
        // 반복 간격
        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(new JLabel("반복 간격:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel intervalModel = new SpinnerNumberModel(1, 1, 99, 1);
        intervalSpinner = new JSpinner(intervalModel);
        settingsPanel.add(intervalSpinner, gbc);
        
        // 반복 종료일
        gbc.gridx = 0;
        gbc.gridy = 2;
        settingsPanel.add(new JLabel("반복 종료일:"), gbc);
        
        gbc.gridx = 1;
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        settingsPanel.add(endDateSpinner, gbc);
        
        // 예외 날짜 패널
        JPanel exceptionPanel = new JPanel(new BorderLayout());
        exceptionPanel.setBorder(BorderFactory.createTitledBorder("예외 날짜"));
        
        exceptionDatesModel = new DefaultListModel<>();
        exceptionDatesList = new JList<>(exceptionDatesModel);
        exceptionDatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane exceptionScrollPane = new JScrollPane(exceptionDatesList);
        exceptionPanel.add(exceptionScrollPane, BorderLayout.CENTER);
        
        JPanel exceptionButtonPanel = new JPanel();
        JButton addExceptionButton = new JButton("예외 추가");
        JButton removeExceptionButton = new JButton("예외 제거");
        
        addExceptionButton.addActionListener(_ -> addExceptionDate());
        removeExceptionButton.addActionListener(_ -> removeExceptionDate());
        
        exceptionButtonPanel.add(addExceptionButton);
        exceptionButtonPanel.add(removeExceptionButton);
        exceptionPanel.add(exceptionButtonPanel, BorderLayout.SOUTH);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("확인");
        JButton cancelButton = new JButton("취소");
        
        okButton.addActionListener(_ -> {
            saveScheduleData();
            confirmed = true;
            dispose();
        });
        
        cancelButton.addActionListener(_ -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // 패널 추가
        add(settingsPanel, BorderLayout.NORTH);
        add(exceptionPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadScheduleData() {
        if (schedule.isRecurring()) {
            Schedule.RecurrenceType type = schedule.getRecurrenceType();
            switch (type) {
                case DAILY: recurrenceTypeCombo.setSelectedIndex(0); break;
                case WEEKLY: recurrenceTypeCombo.setSelectedIndex(1); break;
                case MONTHLY: recurrenceTypeCombo.setSelectedIndex(2); break;
                case YEARLY: recurrenceTypeCombo.setSelectedIndex(3); break;
                default: break;
            }
            
            intervalSpinner.setValue(schedule.getRecurrenceInterval());
            
            if (schedule.getRecurrenceEndDate() != null) {
                endDateSpinner.setValue(java.sql.Timestamp.valueOf(schedule.getRecurrenceEndDate()));
            }
            
            for (LocalDateTime date : schedule.getExceptionDates()) {
                exceptionDatesModel.addElement(date);
            }
        }
    }
    
    private void saveScheduleData() {
        schedule.setRecurring(true);
        
        Schedule.RecurrenceType type = Schedule.RecurrenceType.NONE;
        switch (recurrenceTypeCombo.getSelectedIndex()) {
            case 0: type = Schedule.RecurrenceType.DAILY; break;
            case 1: type = Schedule.RecurrenceType.WEEKLY; break;
            case 2: type = Schedule.RecurrenceType.MONTHLY; break;
            case 3: type = Schedule.RecurrenceType.YEARLY; break;
        }
        schedule.setRecurrence(type, null);
        
        schedule.setRecurrenceInterval((Integer) intervalSpinner.getValue());
        
        java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
        schedule.setRecurrenceEndDate(endDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        
        schedule.getExceptionDates().clear();
        for (int i = 0; i < exceptionDatesModel.getSize(); i++) {
            schedule.addExceptionDate(exceptionDatesModel.getElementAt(i));
        }
    }
    
    private void addExceptionDate() {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        int result = JOptionPane.showConfirmDialog(this, dateSpinner, "예외 날짜 선택",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
        if (result == JOptionPane.OK_OPTION) {
            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            LocalDateTime dateTime = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            exceptionDatesModel.addElement(dateTime);
        }
    }
    
    private void removeExceptionDate() {
        int selectedIndex = exceptionDatesList.getSelectedIndex();
        if (selectedIndex != -1) {
            exceptionDatesModel.remove(selectedIndex);
        }
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
} 