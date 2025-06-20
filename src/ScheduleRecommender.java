import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleRecommender {
    private ScheduleManager scheduleManager;
    
    public ScheduleRecommender(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }
    
    // 시간대별 생산성 분석
    private Map<Integer, Double> analyzeProductivityByHour(String userId) {
        Map<Integer, Double> productivityByHour = new HashMap<>();
        List<Schedule> completedSchedules = scheduleManager.getAllSchedules().stream()
            .filter(s -> s.getStatus().equals("완료"))
            .collect(Collectors.toList());
            
        // 각 시간대별 완료된 일정 수 계산
        for (Schedule schedule : completedSchedules) {
            int hour = schedule.getStartTime().getHour();
            productivityByHour.merge(hour, 1.0, Double::sum);
        }
        
        // 정규화
        double max = productivityByHour.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        productivityByHour.replaceAll((_, v) -> v / max);
        
        return productivityByHour;
    }
    
    // 요일별 생산성 분석
    private Map<Integer, Double> analyzeProductivityByDay(String userId) {
        Map<Integer, Double> productivityByDay = new HashMap<>();
        List<Schedule> completedSchedules = scheduleManager.getAllSchedules().stream()
            .filter(s -> s.getStatus().equals("완료"))
            .collect(Collectors.toList());
            
        // 각 요일별 완료된 일정 수 계산
        for (Schedule schedule : completedSchedules) {
            int dayOfWeek = schedule.getStartTime().getDayOfWeek().getValue();
            productivityByDay.merge(dayOfWeek, 1.0, Double::sum);
        }
        
        // 정규화
        double max = productivityByDay.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        productivityByDay.replaceAll((_, v) -> v / max);
        
        return productivityByDay;
    }
    
    // 카테고리별 선호도 분석
    private Map<String, Double> analyzeCategoryPreference(String userId) {
        Map<String, Double> categoryPreference = new HashMap<>();
        List<Schedule> completedSchedules = scheduleManager.getAllSchedules().stream()
            .filter(s -> s.getStatus().equals("완료"))
            .collect(Collectors.toList());
            
        // 각 카테고리별 완료된 일정 수 계산
        for (Schedule schedule : completedSchedules) {
            String category = schedule.getCategory();
            categoryPreference.merge(category, 1.0, Double::sum);
        }
        
        // 정규화
        double max = categoryPreference.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        categoryPreference.replaceAll((_, v) -> v / max);
        
        return categoryPreference;
    }
    
    // 사용 가능한 시간대 찾기
    private List<TimeSlot> findAvailableTimeSlots(LocalDateTime start, LocalDateTime end, Duration duration) {
        List<TimeSlot> availableSlots = new ArrayList<>();
        List<Schedule> existingSchedules = scheduleManager.getSchedulesByDate(start.toLocalDate());
        
        // 기존 일정을 시간순으로 정렬
        existingSchedules.sort(Comparator.comparing(Schedule::getStartTime));
        
        LocalDateTime current = start;
        for (Schedule schedule : existingSchedules) {
            if (current.isBefore(schedule.getStartTime())) {
                Duration gap = Duration.between(current, schedule.getStartTime());
                if (gap.compareTo(duration) >= 0) {
                    availableSlots.add(new TimeSlot(current, schedule.getStartTime()));
                }
            }
            current = schedule.getEndTime();
        }
        
        // 마지막 일정 이후의 시간도 확인
        if (current.isBefore(end)) {
            Duration gap = Duration.between(current, end);
            if (gap.compareTo(duration) >= 0) {
                availableSlots.add(new TimeSlot(current, end));
            }
        }
        
        return availableSlots;
    }
    
    // 일정 추천 (간단한 버전)
    public List<ScheduleRecommendation> getRecommendations(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusDays(7); // 1주일 이내 추천
        
        return recommendSchedules(userId, now, end);
    }
    
    // 일정 추천
    public List<ScheduleRecommendation> recommendSchedules(String userId, LocalDateTime start, LocalDateTime end) {
        Map<Integer, Double> productivityByHour = analyzeProductivityByHour(userId);
        Map<Integer, Double> productivityByDay = analyzeProductivityByDay(userId);
        Map<String, Double> categoryPreference = analyzeCategoryPreference(userId);
        
        List<ScheduleRecommendation> recommendations = new ArrayList<>();
        List<TimeSlot> availableSlots = findAvailableTimeSlots(start, end, Duration.ofHours(1));
        
        for (TimeSlot slot : availableSlots) {
            double score = calculateTimeSlotScore(slot, productivityByHour, productivityByDay);
            String recommendedCategory = recommendCategory(categoryPreference);
            
            recommendations.add(new ScheduleRecommendation(
                slot,
                recommendedCategory,
                score
            ));
        }
        
        // 점수 기준으로 정렬
        recommendations.sort(Comparator.comparing(ScheduleRecommendation::getScore).reversed());
        
        return recommendations;
    }
    
    // 시간대 점수 계산
    private double calculateTimeSlotScore(TimeSlot slot, 
                                        Map<Integer, Double> productivityByHour,
                                        Map<Integer, Double> productivityByDay) {
        double hourScore = productivityByHour.getOrDefault(slot.start.getHour(), 0.5);
        double dayScore = productivityByDay.getOrDefault(slot.start.getDayOfWeek().getValue(), 0.5);
        
        // 시간대와 요일 점수의 가중 평균
        return (hourScore * 0.7 + dayScore * 0.3);
    }
    
    // 카테고리 추천
    private String recommendCategory(Map<String, Double> categoryPreference) {
        return categoryPreference.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("기타");
    }
    
    // 시간대 클래스
    public static class TimeSlot {
        public final LocalDateTime start;
        public final LocalDateTime end;
        
        public TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
    
    // 일정 추천 클래스
    public static class ScheduleRecommendation {
        private final TimeSlot timeSlot;
        private final String category;
        private final double score;
        
        public ScheduleRecommendation(TimeSlot timeSlot, String category, double score) {
            this.timeSlot = timeSlot;
            this.category = category;
            this.score = score;
        }
        
        public TimeSlot getTimeSlot() { return timeSlot; }
        public String getCategory() { return category; }
        public double getScore() { return score; }
    }
} 