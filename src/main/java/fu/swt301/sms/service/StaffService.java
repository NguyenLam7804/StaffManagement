package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import java.util.List;

public class StaffService {
    // Gọi tầng DAO để xử lý dữ liệu dưới Database
    private final StaffDAO staffDAO = new StaffDAO();

    // 1. Hàm cầu nối phục vụ thống kê số lượng (Dashboard) và đếm tổng số dòng
    public int getTotalStaffCount(String search, Integer roleId) {
        return staffDAO.getTotalStaffCount(search, roleId);
    }

    // 2. Hàm cầu nối lấy danh sách nhân viên có Tìm kiếm, Lọc quyền, Sắp xếp và Phân trang
    public List<Staff> getStaffWithPagingAndSort(String search, Integer roleId, String sortBy, String order, int page, int pageSize) {
        return staffDAO.getStaffWithPagingAndSort(search, roleId, sortBy, order, page, pageSize);
    }

    // 3. Hàm cầu nối lấy chi tiết một nhân viên theo ID
    public Staff getStaffById(int id) {
        return staffDAO.getStaffById(id);
    }

    // 4. Hàm cầu nối thêm mới nhân viên
    public boolean addStaff(Staff staff) {
        return staffDAO.addStaff(staff);
    }

    // 5. Hàm cầu nối cập nhật thông tin nhân viên
    public boolean updateStaff(Staff staff) {
        return staffDAO.updateStaff(staff);
    }

    // 6. Hàm cầu nối xóa nhân viên
    public boolean deleteStaff(int id) {
        return staffDAO.deleteStaff(id);
    }

    public List<Staff> getAllStaff() {
        return staffDAO.getAllStaff();
    }

    // 7. Hàm cầu nối kiểm tra trùng lặp Email (Gọi xuống tầng DAO phục vụ cả Add và Update)
    public boolean isEmailExists(String email, Integer staffId) {
        return staffDAO.isEmailExists(email, staffId);
    }
}