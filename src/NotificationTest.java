import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class NotificationTest {
    public static void main(String[] args) {
        // Swing GUI를 위한 기본 설정
        SwingUtilities.invokeLater(() -> {
            // 테스트 프레임 생성
            JFrame frame = new JFrame("알림 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);

            // 테스트 패널 생성
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            // 테스트 버튼들
            JButton testImmediateButton = new JButton("즉시 알림 테스트");
            JButton testScheduledButton = new JButton("예약 알림 테스트 (1분 후)");
            JButton testMultipleButton = new JButton("여러 알림 테스트");

            // 즉시 알림 테스트
            testImmediateButton.addActionListener(e -> {
                NotificationManager.getInstance().scheduleNotification(
                    "test1",
                    "즉시 알림 테스트",
                    LocalDateTime.now().plus(5, ChronoUnit.SECONDS),
                    0
                );
            });

            // 예약 알림 테스트
            testScheduledButton.addActionListener(e -> {
                NotificationManager.getInstance().scheduleNotification(
                    "test2",
                    "예약 알림 테스트",
                    LocalDateTime.now().plus(1, ChronoUnit.MINUTES),
                    0
                );
            });

            // 여러 알림 테스트
            testMultipleButton.addActionListener(e -> {
                // 30초 후 알림
                NotificationManager.getInstance().scheduleNotification(
                    "test3",
                    "첫 번째 알림",
                    LocalDateTime.now().plus(30, ChronoUnit.SECONDS),
                    0
                );

                // 1분 후 알림
                NotificationManager.getInstance().scheduleNotification(
                    "test4",
                    "두 번째 알림",
                    LocalDateTime.now().plus(1, ChronoUnit.MINUTES),
                    0
                );

                // 1분 30초 후 알림
                NotificationManager.getInstance().scheduleNotification(
                    "test5",
                    "세 번째 알림",
                    LocalDateTime.now().plus(1, ChronoUnit.MINUTES).plus(30, ChronoUnit.SECONDS),
                    0
                );
            });

            // 버튼들을 패널에 추가
            panel.add(Box.createVerticalStrut(20));
            panel.add(testImmediateButton);
            panel.add(Box.createVerticalStrut(10));
            panel.add(testScheduledButton);
            panel.add(Box.createVerticalStrut(10));
            panel.add(testMultipleButton);

            // 프레임에 패널 추가
            frame.add(panel);
            frame.setVisible(true);
        });
    }
} 