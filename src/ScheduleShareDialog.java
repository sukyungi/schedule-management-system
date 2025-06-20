import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ScheduleShareDialog extends JDialog {
    private JComboBox<String> userComboBox;
    private JComboBox<String> permissionComboBox;
    private JList<String> sharedUsersList;
    private DefaultListModel<String> sharedUsersModel;
    private Schedule schedule;
    private ScheduleManager scheduleManager;
    private UserManager userManager;
    private boolean confirmed = false;

    public ScheduleShareDialog(Frame owner, Schedule schedule, 
                             ScheduleManager scheduleManager, 
                             UserManager userManager) {
        super(owner, "일정 공유", true);
        this.schedule = schedule;
        this.scheduleManager = scheduleManager;
        this.userManager = userManager;

        setLayout(new BorderLayout());
        setSize(400, 500);
        setLocationRelativeTo(owner);

        // 상단 패널 (사용자 선택 및 권한 설정)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (공유된 사용자 목록)
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // 하단 패널 (버튼)
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // 공유된 사용자 목록 초기화
        updateSharedUsersList();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 사용자 선택 콤보박스
        List<String> users = userManager.getAllUsers();
        userComboBox = new JComboBox<>(users.toArray(new String[0]));
        userComboBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 권한 선택 콤보박스
        permissionComboBox = new JComboBox<>(new String[]{"읽기", "쓰기"});
        permissionComboBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        // 공유 버튼
        JButton shareButton = new JButton("공유");
        shareButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        shareButton.addActionListener(e -> shareSchedule());

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("사용자:"), gbc);
        gbc.gridx = 1;
        panel.add(userComboBox, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("권한:"), gbc);
        gbc.gridx = 3;
        panel.add(permissionComboBox, gbc);
        gbc.gridx = 4;
        panel.add(shareButton, gbc);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("공유된 사용자 목록"));

        sharedUsersModel = new DefaultListModel<>();
        sharedUsersList = new JList<>(sharedUsersModel);
        sharedUsersList.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(sharedUsersList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 공유 해제 버튼
        JButton removeButton = new JButton("공유 해제");
        removeButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        removeButton.addActionListener(e -> removeSharedUser());
        panel.add(removeButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton closeButton = new JButton("닫기");
        closeButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        closeButton.addActionListener(e -> dispose());
        
        panel.add(closeButton);
        return panel;
    }

    private void shareSchedule() {
        String selectedUser = (String) userComboBox.getSelectedItem();
        String permission = permissionComboBox.getSelectedItem().equals("읽기") ? "READ" : "WRITE";

        try {
            scheduleManager.shareSchedule(schedule.getScheduleId(), selectedUser, permission);
            updateSharedUsersList();
            JOptionPane.showMessageDialog(this,
                "일정이 공유되었습니다.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "일정 공유 중 오류가 발생했습니다: " + ex.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSharedUser() {
        int selectedIndex = sharedUsersList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this,
                "공유 해제할 사용자를 선택해주세요.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedUser = sharedUsersList.getSelectedValue();
        try {
            scheduleManager.removeSharedUser(schedule.getScheduleId(), selectedUser);
            updateSharedUsersList();
            JOptionPane.showMessageDialog(this,
                "공유가 해제되었습니다.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "공유 해제 중 오류가 발생했습니다: " + ex.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSharedUsersList() {
        sharedUsersModel.clear();
        List<String> sharedUsers = schedule.getSharedWithUsers();
        for (String userId : sharedUsers) {
            String permission = schedule.getUserPermission(userId);
            sharedUsersModel.addElement(userId + " (" + 
                (permission.equals("READ") ? "읽기" : "쓰기") + ")");
        }
    }
} 