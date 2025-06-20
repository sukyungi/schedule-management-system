import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String password;
    private String name;
    private boolean loggedIn;
    private Set<String> sharedSchedules;
    
    public User(String userId, String password, String name) {
        if (userId == null || userId.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 올바르지 않습니다.");
        }
        
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.loggedIn = false;
        this.sharedSchedules = new HashSet<>();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        this.password = password;
    }
    
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름이 올바르지 않습니다.");
        }
        this.name = name;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    
    public boolean validatePassword(String password) {
        if (password == null) {
            System.out.println("입력된 비밀번호가 null입니다.");
            return false;
        }
        if (this.password == null) {
            System.out.println("저장된 비밀번호가 null입니다.");
            return false;
        }
        
        String trimmedInput = password.trim();
        String trimmedStored = this.password.trim();
        
        System.out.println("입력된 비밀번호 길이: " + trimmedInput.length());
        System.out.println("저장된 비밀번호 길이: " + trimmedStored.length());
        
        boolean matches = trimmedStored.equals(trimmedInput);
        if (!matches) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            System.out.println("입력된 비밀번호: [" + trimmedInput + "]");
            System.out.println("저장된 비밀번호: [" + trimmedStored + "]");
        }
        
        return matches;
    }
    
    public Set<String> getSharedSchedules() {
        return new HashSet<>(sharedSchedules);
    }
    
    public void addSharedSchedule(String scheduleId) {
        sharedSchedules.add(scheduleId);
    }
    
    public void removeSharedSchedule(String scheduleId) {
        sharedSchedules.remove(scheduleId);
    }
    
    public boolean hasSharedSchedule(String scheduleId) {
        return sharedSchedules.contains(scheduleId);
    }
} 