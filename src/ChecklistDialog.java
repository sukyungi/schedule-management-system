import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChecklistDialog extends JDialog {
    private JList<String> checklistList;
    private DefaultListModel<String> listModel;
    private JTextField newItemField;
    private JButton addButton;
    private JButton removeButton;
    private JButton toggleButton;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    
    public ChecklistDialog(Frame owner, Schedule task) {
        super(owner, "체크리스트 관리 - " + task.getTitle(), true);
        
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(owner);
        
        initComponents();
        loadChecklist();
        updateProgress();
    }
    
    private void initComponents() {
        // 상단 패널 - 새 아이템 추가
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("새 체크리스트 아이템 추가"));
        
        newItemField = new JTextField();
        addButton = new JButton("추가");
        addButton.addActionListener(_ -> addChecklistItem());
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(newItemField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        
        // 중앙 패널 - 체크리스트 목록
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("체크리스트"));
        
        listModel = new DefaultListModel<>();
        checklistList = new JList<>(listModel);
        checklistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 더블클릭으로 체크/언체크
        checklistList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleSelectedItem();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(checklistList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toggleButton = new JButton("체크/해제");
        removeButton = new JButton("삭제");
        
        toggleButton.addActionListener(_ -> toggleSelectedItem());
        removeButton.addActionListener(_ -> removeSelectedItem());
        
        buttonPanel.add(toggleButton);
        buttonPanel.add(removeButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 하단 패널 - 진행률
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("진행률"));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressLabel = new JLabel("0/0 완료");
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(progressLabel, BorderLayout.SOUTH);
        bottomPanel.add(progressPanel, BorderLayout.CENTER);
        
        // 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(_ -> dispose());
        bottomPanel.add(closeButton, BorderLayout.EAST);
        
        // 레이아웃 구성
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Enter 키로 새 아이템 추가
        newItemField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addChecklistItem();
                }
            }
        });
    }
    
    private void loadChecklist() {
        listModel.clear();
        // 실제로는 Task 객체의 체크리스트를 로드해야 함
        // 현재는 임시 데이터로 표시
        listModel.addElement("☐ 프로젝트 계획 수립");
        listModel.addElement("☐ 요구사항 분석");
        listModel.addElement("☐ 설계 문서 작성");
        listModel.addElement("☐ 코딩");
        listModel.addElement("☐ 테스트");
    }
    
    private void addChecklistItem() {
        String text = newItemField.getText().trim();
        if (!text.isEmpty()) {
            listModel.addElement("☐ " + text);
            newItemField.setText("");
            updateProgress();
        }
    }
    
    private void toggleSelectedItem() {
        int selectedIndex = checklistList.getSelectedIndex();
        if (selectedIndex != -1) {
            String item = listModel.getElementAt(selectedIndex);
            if (item.startsWith("☐ ")) {
                listModel.set(selectedIndex, "☑ " + item.substring(2));
            } else if (item.startsWith("☑ ")) {
                listModel.set(selectedIndex, "☐ " + item.substring(2));
            }
            updateProgress();
        }
    }
    
    private void removeSelectedItem() {
        int selectedIndex = checklistList.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
            updateProgress();
        }
    }
    
    private void updateProgress() {
        int total = listModel.size();
        int completed = 0;
        
        for (int i = 0; i < total; i++) {
            String item = listModel.getElementAt(i);
            if (item.startsWith("☑ ")) {
                completed++;
            }
        }
        
        double percentage = total > 0 ? (double) completed / total * 100 : 0;
        progressBar.setValue((int) percentage);
        progressLabel.setText(String.format("%d/%d 완료 (%.1f%%)", completed, total, percentage));
    }
} 