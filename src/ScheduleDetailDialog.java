import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleDetailDialog extends JDialog {
    private Schedule schedule;
    private JLabel titleLabel;
    private JLabel timeLabel;
    private JLabel locationLabel;
    private JLabel descriptionLabel;
    private JLabel tagsLabel;
    private JLabel statusLabel;
    private JLabel importanceLabel;
    private JButton editButton;
    private JButton deleteButton;
    private JButton closeButton;
    private Font koreanFont;

    public ScheduleDetailDialog(Frame owner, Schedule schedule) {
        super(owner, "일정 상세 정보", true);
        this.schedule = schedule;
        this.koreanFont = new Font("Malgun Gothic", Font.PLAIN, 14);
        
        setupUI();
        fillScheduleData();
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        // 상세 정보 패널
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 제목
        gbc.gridx = 0; gbc.gridy = 0;
        detailPanel.add(new JLabel("제목:"), gbc);
        titleLabel = new JLabel();
        titleLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(titleLabel, gbc);
        
        // 시간
        gbc.gridx = 0; gbc.gridy = 1;
        detailPanel.add(new JLabel("시간:"), gbc);
        timeLabel = new JLabel();
        timeLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(timeLabel, gbc);
        
        // 위치
        gbc.gridx = 0; gbc.gridy = 2;
        detailPanel.add(new JLabel("위치:"), gbc);
        locationLabel = new JLabel();
        locationLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(locationLabel, gbc);
        
        // 설명
        gbc.gridx = 0; gbc.gridy = 3;
        detailPanel.add(new JLabel("설명:"), gbc);
        descriptionLabel = new JLabel();
        descriptionLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(descriptionLabel, gbc);
        
        // 태그
        gbc.gridx = 0; gbc.gridy = 4;
        detailPanel.add(new JLabel("태그:"), gbc);
        tagsLabel = new JLabel();
        tagsLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(tagsLabel, gbc);
        
        // 상태
        gbc.gridx = 0; gbc.gridy = 5;
        detailPanel.add(new JLabel("상태:"), gbc);
        statusLabel = new JLabel();
        statusLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(statusLabel, gbc);
        
        // 중요도
        gbc.gridx = 0; gbc.gridy = 6;
        detailPanel.add(new JLabel("중요도:"), gbc);
        importanceLabel = new JLabel();
        importanceLabel.setFont(koreanFont);
        gbc.gridx = 1;
        detailPanel.add(importanceLabel, gbc);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editButton = new JButton("수정");
        deleteButton = new JButton("삭제");
        closeButton = new JButton("닫기");
        
        editButton.setFont(koreanFont);
        deleteButton.setFont(koreanFont);
        closeButton.setFont(koreanFont);
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        
        // 이벤트 리스너
        closeButton.addActionListener(e -> dispose());
        
        // 패널 추가
        add(detailPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fillScheduleData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        titleLabel.setText(schedule.getTitle());
        timeLabel.setText(String.format("%s ~ %s", 
            schedule.getStartTime().format(formatter),
            schedule.getEndTime().format(formatter)));
        locationLabel.setText(schedule.getLocation());
        descriptionLabel.setText(schedule.getDescription());
        tagsLabel.setText(String.join(", ", schedule.getTags()));
        statusLabel.setText(schedule.getStatus());
        importanceLabel.setText(schedule.isImportant() ? "중요" : "일반");
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }
} 