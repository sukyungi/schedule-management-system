import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class SettingsPanel extends JPanel {
    private JCheckBox notificationCheckBox;
    private JSpinner reminderTimeSpinner;
    private static final String SETTINGS_FILE = "data/settings.properties";

    public SettingsPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadSettings();
    }

    private void initComponents() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 알림 설정
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("알림:"), gbc);
        
        gbc.gridx = 1;
        notificationCheckBox = new JCheckBox("알림 활성화");
        settingsPanel.add(notificationCheckBox, gbc);

        // 알림 시간 설정
        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(new JLabel("알림 시간:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel model = new SpinnerNumberModel(30, 5, 120, 5);
        reminderTimeSpinner = new JSpinner(model);
        settingsPanel.add(reminderTimeSpinner, gbc);
        settingsPanel.add(new JLabel("분 전"), gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");

        saveButton.addActionListener(_ -> saveSettings());
        cancelButton.addActionListener(_ -> loadSettings());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(settingsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSettings() {
        try {
            Properties props = new Properties();
            File settingsFile = new File(SETTINGS_FILE);
            
            if (settingsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(settingsFile)) {
                    props.load(fis);
                    
                    // 설정값 로드
                    boolean notifications = Boolean.parseBoolean(props.getProperty("notifications", "true"));
                    int reminderTime = Integer.parseInt(props.getProperty("reminderTime", "30"));
                    
                    // UI에 적용
                    notificationCheckBox.setSelected(notifications);
                    reminderTimeSpinner.setValue(reminderTime);
                    System.out.println("설정을 로드했습니다: " + SETTINGS_FILE);
                }
            } else {
                // 기본값 설정
                notificationCheckBox.setSelected(true);
                reminderTimeSpinner.setValue(30);
                System.out.println("설정 파일이 없어 기본값을 사용합니다.");
            }
        } catch (Exception e) {
            System.err.println("설정 로드 중 오류 발생: " + e.getMessage());
            // 오류 발생 시 기본값 설정
            notificationCheckBox.setSelected(true);
            reminderTimeSpinner.setValue(30);
        }
    }

    private void saveSettings() {
        try {
            Properties props = new Properties();
            
            // 현재 설정값 저장
            props.setProperty("notifications", String.valueOf(notificationCheckBox.isSelected()));
            props.setProperty("reminderTime", String.valueOf(reminderTimeSpinner.getValue()));
            
            // data 디렉토리 생성
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            // 설정 파일에 저장
            File settingsFile = new File(SETTINGS_FILE);
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                props.store(fos, "일정 관리 시스템 설정");
                System.out.println("설정을 저장했습니다: " + SETTINGS_FILE);
            }
            
            JOptionPane.showMessageDialog(this,
                "설정이 저장되었습니다.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("설정 저장 중 오류 발생: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                "설정 저장 중 오류가 발생했습니다: " + e.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 