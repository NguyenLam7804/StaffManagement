package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;

public class LoginResult {
    private final LoginStatus status;
    private final Staff staff;
    private final int lockMinutesRemaining;

    public LoginResult(LoginStatus status, Staff staff, int lockMinutesRemaining) {
        this.status = status;
        this.staff = staff;
        this.lockMinutesRemaining = lockMinutesRemaining;
    }

    public LoginStatus getStatus() { return status; }
    public Staff getStaff() { return staff; }
    public int getLockMinutesRemaining() { return lockMinutesRemaining; }
}