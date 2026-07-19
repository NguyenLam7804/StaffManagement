package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final StaffDAO staffDAO = new StaffDAO();

    public Staff login(String email, String password) throws AuthException {
        // 1. Kiểm tra tài khoản tồn tại trong DB không
        Staff staff = staffDAO.getStaffByEmail(email);
        if (staff == null) {
            throw new AuthException("Invalid email or password");
        }

        // 2. Kiểm tra khớp mã hóa BCrypt giữa mật khẩu nhập vào và chuỗi hash trong DB
        // (Hỗ trợ cả mật khẩu dạng text thuần "123" nếu bạn test bằng tài khoản plain)
        if (staff.getPassword().startsWith("$2a$") || staff.getPassword().startsWith("$2b$")) {
            if (!BCrypt.checkpw(password, staff.getPassword())) {
                throw new AuthException("Invalid email or password");
            }
        } else {
            // Trường hợp mật khẩu trong DB chưa kịp mã hóa (dạng thô)
            if (!password.equals(staff.getPassword())) {
                throw new AuthException("Invalid email or password");
            }
        }

        return staff;
    }
}