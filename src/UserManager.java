import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private User currentUser;

    private UserManager() {
        users = new HashMap<>();
        loadUsers();
        // 테스트용 관리자 계정 추가
        if (!users.containsKey("admin")) {
            User admin = new User("admin", "admin123", "관리자");
            users.put(admin.getUserId(), admin);
            saveUsers();
        }
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void loadUsers() {
        users = DataStorage.loadUsers();
        System.out.println("로드된 사용자 목록:");
        
        // 데이터 검증 및 복구
        List<String> corruptedUsers = new ArrayList<>();
        for (String userId : users.keySet()) {
            User user = users.get(userId);
            if (user.getUserId() == null || user.getPassword() == null || user.getName() == null) {
                System.out.println("손상된 사용자 데이터 발견: " + userId);
                corruptedUsers.add(userId);
            } else {
                System.out.println("- " + userId + " (" + user.getName() + ")");
            }
        }
        
        // 손상된 사용자 데이터 제거
        for (String userId : corruptedUsers) {
            users.remove(userId);
            System.out.println("손상된 사용자 데이터 제거: " + userId);
        }
        
        // 관리자 계정 확인 및 추가
        if (!users.containsKey("admin")) {
            User admin = new User("admin", "admin123", "관리자");
            users.put(admin.getUserId(), admin);
            System.out.println("관리자 계정 생성: admin");
        }
        
        // 변경사항 저장
        if (!corruptedUsers.isEmpty()) {
            saveUsers();
        }
    }

    private void saveUsers() {
        DataStorage.saveUsers(users);
    }

    public boolean registerUser(String userId, String password, String name) {
        if (userId == null || userId.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {
            System.out.println("사용자 정보가 올바르지 않습니다.");
            return false;
        }
        
        String trimmedUserId = userId.trim();
        if (users.containsKey(trimmedUserId)) {
            // 기존 사용자 데이터가 손상된 경우 재등록 허용
            User existingUser = users.get(trimmedUserId);
            if (existingUser.getUserId() == null || existingUser.getPassword() == null || existingUser.getName() == null) {
                System.out.println("손상된 사용자 데이터를 재등록합니다: " + trimmedUserId);
                users.remove(trimmedUserId);
            } else {
                System.out.println("이미 존재하는 사용자 ID입니다: " + trimmedUserId);
                return false;
            }
        }
        
        try {
            User user = new User(trimmedUserId, password, name);
            users.put(trimmedUserId, user);
            saveUsers();
            System.out.println("사용자 등록 성공: " + trimmedUserId);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("사용자 등록 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String userId, String password) {
        if (userId == null || userId.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.out.println("아이디 또는 비밀번호가 비어있습니다.");
            return false;
        }
        
        String trimmedUserId = userId.trim();
        User user = users.get(trimmedUserId);
        
        if (user == null) {
            System.out.println("사용자를 찾을 수 없습니다: " + trimmedUserId);
            return false;
        }
        
        // 사용자 데이터 검증
        if (user.getUserId() == null || user.getPassword() == null || user.getName() == null) {
            System.out.println("손상된 사용자 데이터입니다. 다시 등록해주세요.");
            users.remove(trimmedUserId);
            saveUsers();
            return false;
        }
        
        System.out.println("로그인 시도: " + trimmedUserId);
        System.out.println("사용자 정보:");
        System.out.println("- 이름: " + user.getName());
        System.out.println("- 비밀번호 존재: " + (user.getPassword() != null));
        
        boolean isValid = user.validatePassword(password);
        
        if (isValid) {
            user.setLoggedIn(true);
            currentUser = user;
            System.out.println("로그인 성공: " + trimmedUserId);
            return true;
        } else {
            System.out.println("로그인 실패: " + trimmedUserId);
            return false;
        }
    }

    public void logout() {
        if (currentUser != null) {
            currentUser.setLoggedIn(false);
            currentUser = null;
        }
    }

    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public List<String> getAllUsers() {
        return new ArrayList<>(users.keySet());
    }

    public void updateUser(User user) {
        users.put(user.getUserId(), user);
        saveUsers();
    }

    public void deleteUser(String userId) {
        users.remove(userId);
        saveUsers();
    }

    public boolean isLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }

    public Map<String, User> getUsers() {
        return users;
    }
} 