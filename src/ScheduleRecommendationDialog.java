import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ScheduleRecommendationDialog extends JDialog {
    private final ScheduleRecommender recommender;
    private final String userId;
    private JTable recommendationTable;
    private DefaultTableModel tableModel;
    private JButton applyButton;
    private JButton closeButton;
    private List<ScheduleRecommender.ScheduleRecommendation> recommendations;
    private ScheduleRecommender.ScheduleRecommendation selectedRecommendation;
    private boolean confirmed;

    public ScheduleRecommendationDialog(ScheduleGUI parent, ScheduleRecommender recommender, String userId) {
        super(parent, "일정 추천", true);
        this.recommender = recommender;
        this.userId = userId;
        setSize(600, 400);
        setLocationRelativeTo(parent);
        initComponents();
        loadRecommendations();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 테이블 모델 설정
        String[] columnNames = {"시작 시간", "종료 시간", "카테고리", "설명", "점수"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recommendationTable = new JTable(tableModel);
        recommendationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(recommendationTable);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectButton = new JButton("선택");
        JButton cancelButton = new JButton("취소");

        selectButton.addActionListener(_ -> {
            int selectedRow = recommendationTable.getSelectedRow();
            if (selectedRow != -1) {
                selectedRecommendation = (ScheduleRecommender.ScheduleRecommendation) tableModel.getValueAt(selectedRow, 0);
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "추천 일정을 선택해주세요.",
                    "알림",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(_ -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadRecommendations() {
        tableModel.setRowCount(0);
        List<ScheduleRecommender.ScheduleRecommendation> recommendations = recommender.getRecommendations(userId);
        
        for (ScheduleRecommender.ScheduleRecommendation recommendation : recommendations) {
            Object[] row = {
                recommendation.getTimeSlot().start,
                recommendation.getTimeSlot().end,
                recommendation.getCategory(),
                "추천 일정",
                String.format("%.2f", recommendation.getScore())
            };
            tableModel.addRow(row);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ScheduleRecommender.ScheduleRecommendation getSelectedRecommendation() {
        return selectedRecommendation;
    }
} 