import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.List;

public class ScheduleCalendarPanel extends JPanel {
    private ScheduleManager scheduleManager;
    private JPanel calendarPanel;
    private JPanel headerPanel;
    private JLabel monthLabel;
    private LocalDate currentDate;
    private Font koreanFont;
    private Map<LocalDate, JPanel> datePanels;

    public ScheduleCalendarPanel(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
        this.currentDate = LocalDate.now();
        this.koreanFont = new Font("Malgun Gothic", Font.PLAIN, 14);
        this.datePanels = new HashMap<>();
        
        setLayout(new BorderLayout());
        setupUI();
    }

    private void setupUI() {
        // 헤더 패널 (월 이동 버튼, 현재 월 표시)
        headerPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");
        monthLabel = new JLabel();
        
        prevButton.setFont(koreanFont);
        nextButton.setFont(koreanFont);
        monthLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        
        prevButton.addActionListener(e -> changeMonth(-1));
        nextButton.addActionListener(e -> changeMonth(1));
        
        buttonPanel.add(prevButton);
        buttonPanel.add(monthLabel);
        buttonPanel.add(nextButton);
        
        headerPanel.add(buttonPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // 캘린더 패널
        calendarPanel = new JPanel(new GridLayout(0, 7, 1, 1));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(calendarPanel, BorderLayout.CENTER);
        
        // 요일 헤더 추가
        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : weekDays) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(koreanFont);
            if (day.equals("일")) dayLabel.setForeground(Color.RED);
            if (day.equals("토")) dayLabel.setForeground(Color.BLUE);
            calendarPanel.add(dayLabel);
        }
        
        updateCalendar();
    }

    private void updateCalendar() {
        // 월 표시 업데이트
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월")));
        
        // 기존 날짜 패널 제거
        calendarPanel.removeAll();
        datePanels.clear();
        
        // 요일 헤더 다시 추가
        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : weekDays) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(koreanFont);
            if (day.equals("일")) dayLabel.setForeground(Color.RED);
            if (day.equals("토")) dayLabel.setForeground(Color.BLUE);
            calendarPanel.add(dayLabel);
        }
        
        // 이전 달의 날짜들
        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate prevMonth = firstOfMonth.minusDays(dayOfWeek);
        
        // 다음 달의 날짜들
        LocalDate lastOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        int remainingDays = 7 - (lastOfMonth.getDayOfWeek().getValue() % 7) - 1;
        LocalDate nextMonth = lastOfMonth.plusDays(remainingDays);
        
        // 날짜 패널 생성
        LocalDate current = prevMonth;
        while (!current.isAfter(nextMonth)) {
            JPanel datePanel = createDatePanel(current);
            datePanels.put(current, datePanel);
            calendarPanel.add(datePanel);
            current = current.plusDays(1);
        }
        
        // 일정 표시
        displaySchedules();
        
        // 레이아웃 업데이트
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createDatePanel(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // 날짜 레이블
        JLabel dateLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dateLabel.setFont(koreanFont);
        
        // 이전/다음 달의 날짜는 회색으로 표시
        if (date.getMonth() != currentDate.getMonth()) {
            dateLabel.setForeground(Color.GRAY);
        }
        
        // 주말은 색상 표시
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dateLabel.setForeground(Color.RED);
        } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            dateLabel.setForeground(Color.BLUE);
        }
        
        // 오늘 날짜는 배경색 표시
        if (date.equals(LocalDate.now())) {
            panel.setBackground(new Color(255, 255, 200));
        }
        
        panel.add(dateLabel, BorderLayout.NORTH);
        
        // 일정 목록을 표시할 패널
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        panel.add(schedulePanel, BorderLayout.CENTER);
        
        // 클릭 이벤트 처리
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showAddScheduleDialog(date);
                }
            }
        });
        
        return panel;
    }

    private void displaySchedules() {
        // 해당 월의 일정 가져오기
        LocalDateTime startOfMonth = currentDate.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth())
            .atTime(23, 59, 59);
        
        List<Schedule> schedules = scheduleManager.getSchedulesByDateRange(startOfMonth, endOfMonth);
        
        // 각 날짜 패널에 일정 표시
        for (Schedule schedule : schedules) {
            LocalDate scheduleDate = schedule.getStartTime().toLocalDate();
            JPanel datePanel = datePanels.get(scheduleDate);
            if (datePanel != null) {
                JPanel schedulePanel = (JPanel) datePanel.getComponent(1);
                JLabel scheduleLabel = new JLabel(schedule.getTitle());
                scheduleLabel.setFont(koreanFont);
                scheduleLabel.setForeground(schedule.isImportant() ? Color.RED : Color.BLACK);
                schedulePanel.add(scheduleLabel);
                
                // 클릭 이벤트 처리
                scheduleLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            showScheduleDetail(schedule);
                        }
                    }
                });
            }
        }
    }

    private void changeMonth(int months) {
        currentDate = currentDate.plusMonths(months);
        updateCalendar();
    }

    private void showAddScheduleDialog(LocalDate date) {
        ScheduleDialog dialog = new ScheduleDialog(
            (ScheduleGUI) SwingUtilities.getWindowAncestor(this),
            date);
        dialog.setVisible(true);
        
        Schedule schedule = dialog.getSchedule();
        if (schedule != null) {
            try {
                scheduleManager.addSchedule(schedule);
                updateCalendar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "오류",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showScheduleDetail(Schedule schedule) {
        ScheduleDetailDialog dialog = new ScheduleDetailDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            schedule);
        dialog.setVisible(true);
        
        // 일정이 수정되거나 삭제된 경우 캘린더 업데이트
        updateCalendar();
    }
} 