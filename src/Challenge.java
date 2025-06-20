import java.time.LocalDateTime;

public class Challenge {
    private String challengeId;
    private String title;
    private String description;
    private String target;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int progress;
    private int targetProgress;
    private String userId;
    private boolean completed;
    private int rewardPoints;

    public Challenge(String title, String description, String target, LocalDateTime startDate, 
                    LocalDateTime endDate, int targetProgress, String userId) {
        this.challengeId = generateChallengeId();
        this.title = title;
        this.description = description;
        this.target = target;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = 0;
        this.targetProgress = targetProgress;
        this.userId = userId;
        this.completed = false;
        this.rewardPoints = calculateRewardPoints();
    }

    private String generateChallengeId() {
        return "CHALLENGE" + System.currentTimeMillis();
    }

    private int calculateRewardPoints() {
        return targetProgress * 10; // 목표 진행도당 10포인트
    }

    public void updateProgress(int newProgress) {
        if (newProgress < 0) {
            throw new IllegalArgumentException("진행도는 음수가 될 수 없습니다.");
        }
        this.progress = newProgress;
        if (this.progress >= targetProgress && !completed) {
            this.completed = true;
            notifyCompletion();
        }
    }

    private void notifyCompletion() {
        System.out.println("축하합니다! 챌린지 '" + title + "'를 완료했습니다!");
        System.out.println("보상 포인트: " + rewardPoints);
    }

    public String getChallengeId() {
        return challengeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public int getProgress() {
        return progress;
    }

    public int getTargetProgress() {
        return targetProgress;
    }

    public void setTargetProgress(int targetProgress) {
        if (targetProgress > 0) {
            this.targetProgress = targetProgress;
            this.rewardPoints = calculateRewardPoints();
        } else {
            throw new IllegalArgumentException("목표 진행도는 0보다 커야 합니다.");
        }
    }

    public String getUserId() {
        return userId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %d%% 완료 (목표: %d%%, 보상: %d 포인트)",
            challengeId, title, (progress * 100 / targetProgress), targetProgress, rewardPoints);
    }
} 