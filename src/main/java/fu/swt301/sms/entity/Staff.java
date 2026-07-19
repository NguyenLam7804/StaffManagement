package fu.swt301.sms.entity;

public class Staff {
    private int staffId;
    private String email;
    private String password;
    private String fullName;
    private boolean gender;
    private String phone;
    private int roleId;

    public Staff() {}

    public Staff(int staffId, String email, String password, String fullName, boolean gender, String phone, int roleId) {
        this.staffId = staffId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.gender = gender;
        this.phone = phone;
        this.roleId = roleId;
    }

    // Getters and Setters
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isGender() { return gender; }
    public void setGender(boolean gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
}