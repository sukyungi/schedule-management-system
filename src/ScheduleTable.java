import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class ScheduleTable extends JTable {
    private ScheduleManager scheduleManager;
    private Font koreanFont;
    private DateTimeFormatter formatter;

    public ScheduleTable(ScheduleManager scheduleManager) {
        super();
        this.scheduleManager = scheduleManager;
        this.koreanFont = new Font("Malgun Gothic", Font.PLAIN, 14);
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        setupTable();
        setupDragAndDrop();
    }

    private void setupTable() {
        setFont(koreanFont);
        setRowHeight(25);
        getTableHeader().setFont(new Font("Malgun Gothic", Font.BOLD, 14));
    }

    private void setupDragAndDrop() {
        // 드래그 소스 설정
        setDragEnabled(true);
        setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                JTable table = (JTable) c;
                int row = table.getSelectedRow();
                if (row != -1) {
                    String scheduleId = (String) table.getValueAt(row, 0);
                    return new StringSelection(scheduleId);
                }
                return null;
            }

            @Override
            protected void exportDone(JComponent c, Transferable data, int action) {
                // 드래그 앤 드롭 완료 후 처리
            }
        });

        // 드롭 타겟 설정
        setDropMode(DropMode.ON_OR_INSERT_ROWS);
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    String scheduleId = (String) support.getTransferable()
                        .getTransferData(DataFlavor.stringFlavor);
                    
                    // 드롭 위치의 행 인덱스 가져오기
                    JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
                    int row = dl.getRow();
                    
                    if (row != -1) {
                        // 새로운 시간 가져오기
                        String newTimeStr = (String) getValueAt(row, 1);
                        LocalDateTime newTime = LocalDateTime.parse(newTimeStr, formatter);
                        
                        // 일정 업데이트
                        Schedule schedule = scheduleManager.getSchedule(scheduleId);
                        if (schedule != null) {
                            Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
                            schedule.setStartTime(newTime);
                            schedule.setEndTime(newTime.plus(duration));
                            
                            try {
                                scheduleManager.updateSchedule(scheduleId, schedule);
                                return true;
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(ScheduleTable.this,
                                    e.getMessage(), "오류",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // 마우스 이벤트 처리
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (getSelectedRow() != -1) {
                    getTransferHandler().exportAsDrag(ScheduleTable.this, e, TransferHandler.MOVE);
                }
            }
        });
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        if (dataModel instanceof DefaultTableModel) {
            DefaultTableModel model = (DefaultTableModel) dataModel;
            
            // 컬럼 너비 설정
            if (getColumnModel().getColumnCount() > 0) {
                getColumnModel().getColumn(0).setPreferredWidth(200);
                getColumnModel().getColumn(1).setPreferredWidth(150);
                getColumnModel().getColumn(2).setPreferredWidth(150);
                getColumnModel().getColumn(3).setPreferredWidth(150);
                getColumnModel().getColumn(4).setPreferredWidth(150);
                getColumnModel().getColumn(5).setPreferredWidth(100);
                getColumnModel().getColumn(6).setPreferredWidth(50);
            }
            
            model.addTableModelListener(e -> {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (column == 1 || column == 2) { // 시작 시간 또는 종료 시간이 변경된 경우
                        String scheduleId = (String) getValueAt(row, 0);
                        try {
                            String startTimeStr = (String) getValueAt(row, 1);
                            String endTimeStr = (String) getValueAt(row, 2);
                            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
                            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
                            
                            Schedule schedule = scheduleManager.getSchedule(scheduleId);
                            if (schedule != null) {
                                schedule.setStartTime(startTime);
                                schedule.setEndTime(endTime);
                                scheduleManager.updateSchedule(scheduleId, schedule);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                "시간 형식이 올바르지 않습니다.", "오류",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        }
    }
} 