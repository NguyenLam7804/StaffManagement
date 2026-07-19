package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 5;

    private final StaffDAO staffDAO;

    public AuthService() {
        this(new StaffDAO());
    }

    public AuthService(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
    }

    public LoginResult login(String email, String rawPassword) {
        if (isBlank(email) || isBlank(rawPassword)) {
            return new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, 0);
        }

        Staff staff = staffDAO.findByEmail(email.trim());
        if (staff == null) {
            return new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, 0);
        }

        if (isCurrentlyLocked(staff)) {
            return new LoginResult(LoginStatus.ACCOUNT_LOCKED, null, minutesRemaining(staff));
        }

        // Lock (neu co) da het han: reset bo dem ve 0 truoc khi kiem tra mat khau,
        // de dam bao phai sai du 5 LAN LIEN TIEP MOI sau khi mo khoa thi moi bi khoa lai
        // (thay vi chi 1 lan sai la khoa lai ngay do bo dem cu con 4).
        if (staff.getLockedUntil() != null && staff.getLoginAttempts() > 0) {
            staffDAO.resetLoginAttempts(staff.getStaffID());
            staff.setLoginAttempts(0);
        }

        boolean passwordMatches = BCrypt.checkpw(rawPassword, staff.getPassword());
        if (!passwordMatches) {
            int attempts = staff.getLoginAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                staffDAO.lockAccount(staff.getStaffID(), LOCK_MINUTES);
                return new LoginResult(LoginStatus.ACCOUNT_LOCKED, null, LOCK_MINUTES);
            }
            staffDAO.updateLoginAttempts(staff.getStaffID(), attempts);
            return new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, 0);
        }

        staffDAO.resetLoginAttempts(staff.getStaffID());
        return new LoginResult(LoginStatus.SUCCESS, staff, 0);
    }

    public boolean changePassword(int staffId, String oldPassword, String newPassword) {
        Staff staff = staffDAO.getStaffById(staffId);
        if (staff == null || !BCrypt.checkpw(oldPassword, staff.getPassword())) {
            return false;
        }
        String newHash = hashPassword(newPassword);
        staffDAO.updatePassword(staffId, newHash);
        return true;
    }

    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    private boolean isCurrentlyLocked(Staff staff) {
        return staff.getLockedUntil() != null
                && staff.getLockedUntil().getTime() > System.currentTimeMillis();
    }

    private int minutesRemaining(Staff staff) {
        long millisLeft = staff.getLockedUntil().getTime() - System.currentTimeMillis();
        int minutes = (int) Math.ceil(millisLeft / 60000.0);
        return Math.max(minutes, 1);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}