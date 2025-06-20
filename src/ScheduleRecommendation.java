import java.time.LocalDateTime;

public class ScheduleRecommendation {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String category;
    private String description;
    private double score;

    public ScheduleRecommendation(LocalDateTime startTime, LocalDateTime endTime, 
                                String category, String description, double score) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.description = description;
        this.score = score;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return String.format("%s ~ %s [%s] - %s (점수: %.2f)",
            startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            endTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            category,
            description,
            score);
    }
} 