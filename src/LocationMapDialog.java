import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LocationMapDialog extends JDialog {
    private JTextField locationField;
    private JButton searchButton;
    private JEditorPane mapPane;

    public LocationMapDialog(Frame owner, String location) {
        super(owner, "위치 정보", true);

        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(owner);

        // 상단 패널 (위치 검색)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (지도 표시)
        mapPane = new JEditorPane();
        mapPane.setEditable(false);
        mapPane.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane(mapPane);
        add(scrollPane, BorderLayout.CENTER);

        // 초기 위치 표시
        if (location != null && !location.isEmpty()) {
            locationField.setText(location);
            showLocation(location);
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));

        locationField = new JTextField(30);
        locationField.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        locationField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchLocation();
                }
            }
        });

        searchButton = new JButton("검색");
        searchButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        searchButton.addActionListener(_ -> searchLocation());

        panel.add(new JLabel("위치:"));
        panel.add(locationField);
        panel.add(searchButton);

        return panel;
    }

    private void searchLocation() {
        String searchLocation = locationField.getText().trim();
        if (!searchLocation.isEmpty()) {
            showLocation(searchLocation);
        }
    }

    private void showLocation(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.toString());
            String mapUrl = String.format(
                "https://www.google.com/maps/embed/v1/place?key=YOUR_API_KEY&q=%s",
                encodedLocation
            );

            String html = String.format(
                "<html><body style='margin:0;padding:0;'>" +
                "<iframe width='100%%' height='100%%' frameborder='0' style='border:0' " +
                "src='%s' allowfullscreen></iframe>" +
                "</body></html>",
                mapUrl
            );

            mapPane.setText(html);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "지도를 불러오는 중 오류가 발생했습니다.",
                "오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getLocationText() {
        return locationField.getText().trim();
    }
} 