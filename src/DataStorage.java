import java.io.*;
import java.util.*;
import java.nio.file.*;

public class DataStorage {
    private static final String DATA_DIR;
    private static final String USERS_FILE = "users.dat";
    private static final String SCHEDULES_FILE = "schedules.dat";
    private static final String TASKS_FILE = "tasks.dat";

    static {
        // 현재 작업 디렉토리에 data 폴더 생성
        String currentDir = System.getProperty("user.dir");
        DATA_DIR = currentDir + File.separator + "data";
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
    }

    // 사용자 데이터 저장
    public static void saveUsers(Map<String, User> users) {
        if (users == null) {
            System.err.println("저장할 사용자 데이터가 null입니다.");
            return;
        }
        
        File file = new File(DATA_DIR + File.separator + USERS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(file)))) {
            oos.writeObject(new HashMap<>(users)); // 새로운 HashMap으로 복사하여 저장
            oos.flush();
            System.out.println("사용자 데이터 저장 완료: " + users.size() + "명");
        } catch (IOException e) {
            System.err.println("사용자 데이터 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 사용자 데이터 불러오기
    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUsers() {
        File file = new File(DATA_DIR + File.separator + USERS_FILE);
        if (!file.exists()) {
            System.out.println("사용자 데이터 파일이 없습니다. 새로 생성합니다.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<String, User> loadedUsers = (Map<String, User>) obj;
                System.out.println("사용자 데이터 로드 완료: " + loadedUsers.size() + "명");
                return new HashMap<>(loadedUsers); // 새로운 HashMap으로 복사하여 반환
            } else {
                System.err.println("잘못된 데이터 형식입니다.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("사용자 데이터 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    // 일정 데이터 저장
    public static void saveSchedules(Map<String, Schedule> schedules) {
        if (schedules == null) {
            System.err.println("저장할 일정 데이터가 null입니다.");
            return;
        }
        
        File file = new File(DATA_DIR + File.separator + SCHEDULES_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(file)))) {
            oos.writeObject(new HashMap<>(schedules)); // 새로운 HashMap으로 복사하여 저장
            oos.flush();
            System.out.println("일정 데이터 저장 완료: " + schedules.size() + "개");
        } catch (IOException e) {
            System.err.println("일정 데이터 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 일정 데이터 불러오기
    @SuppressWarnings("unchecked")
    public static Map<String, Schedule> loadSchedules() {
        File file = new File(DATA_DIR + File.separator + SCHEDULES_FILE);
        if (!file.exists()) {
            System.out.println("일정 데이터 파일이 없습니다. 새로 생성합니다.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<String, Schedule> loadedSchedules = (Map<String, Schedule>) obj;
                System.out.println("일정 데이터 로드 완료: " + loadedSchedules.size() + "개");
                return new HashMap<>(loadedSchedules); // 새로운 HashMap으로 복사하여 반환
            } else {
                System.err.println("잘못된 데이터 형식입니다.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("일정 데이터 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    // 할 일 데이터 저장
    public static void saveTasks(Map<String, Task> tasks) {
        if (tasks == null) {
            System.err.println("저장할 할 일 데이터가 null입니다.");
            return;
        }
        
        File file = new File(DATA_DIR + File.separator + TASKS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(file)))) {
            oos.writeObject(new HashMap<>(tasks));
            oos.flush();
            System.out.println("할 일 데이터 저장 완료: " + tasks.size() + "개");
        } catch (IOException e) {
            System.err.println("할 일 데이터 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 할 일 데이터 불러오기
    @SuppressWarnings("unchecked")
    public static Map<String, Task> loadTasks() {
        File file = new File(DATA_DIR + File.separator + TASKS_FILE);
        if (!file.exists()) {
            System.out.println("할 일 데이터 파일이 없습니다. 새로 생성합니다.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<String, Task> loadedTasks = (Map<String, Task>) obj;
                System.out.println("할 일 데이터 로드 완료: " + loadedTasks.size() + "개");
                return new HashMap<>(loadedTasks);
            } else {
                System.err.println("잘못된 데이터 형식입니다.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("할 일 데이터 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    // 데이터 백업
    public static void backupData() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupDir = DATA_DIR + File.separator + "backup_" + timestamp;
        new File(backupDir).mkdir();

        try {
            // 사용자 데이터 백업
            File usersFile = new File(DATA_DIR + File.separator + USERS_FILE);
            if (usersFile.exists()) {
                Files.copy(usersFile.toPath(), 
                    new File(backupDir + File.separator + USERS_FILE).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            // 일정 데이터 백업
            File schedulesFile = new File(DATA_DIR + File.separator + SCHEDULES_FILE);
            if (schedulesFile.exists()) {
                Files.copy(schedulesFile.toPath(), 
                    new File(backupDir + File.separator + SCHEDULES_FILE).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            // 할 일 데이터 백업
            File tasksFile = new File(DATA_DIR + File.separator + TASKS_FILE);
            if (tasksFile.exists()) {
                Files.copy(tasksFile.toPath(), 
                    new File(backupDir + File.separator + TASKS_FILE).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("데이터 백업 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 데이터 복원
    public static void restoreData(String backupTimestamp) {
        String backupDir = DATA_DIR + File.separator + "backup_" + backupTimestamp;

        try {
            // 사용자 데이터 복원
            File usersBackup = new File(backupDir + File.separator + USERS_FILE);
            if (usersBackup.exists()) {
                Files.copy(usersBackup.toPath(), 
                    new File(DATA_DIR + File.separator + USERS_FILE).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            // 일정 데이터 복원
            File schedulesBackup = new File(backupDir + File.separator + SCHEDULES_FILE);
            if (schedulesBackup.exists()) {
                Files.copy(schedulesBackup.toPath(), 
                    new File(DATA_DIR + File.separator + SCHEDULES_FILE).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }

            // 할 일 데이터 복원
            File tasksBackup = new File(backupDir + File.separator + TASKS_FILE);
            if (tasksBackup.exists()) {
                Files.copy(tasksBackup.toPath(), 
                    new File(DATA_DIR + File.separator + TASKS_FILE).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("데이터 복원 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 백업 목록 조회
    public static List<String> getBackupList() {
        File dataDir = new File(DATA_DIR);
        File[] backupDirs = dataDir.listFiles((dir, name) -> name.startsWith("backup_"));
        
        List<String> backupList = new ArrayList<>();
        if (backupDirs != null) {
            for (File backupDir : backupDirs) {
                backupList.add(backupDir.getName().substring(7)); // "backup_" 제거
            }
        }
        return backupList;
    }
} 