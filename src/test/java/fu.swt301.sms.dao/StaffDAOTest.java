package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class StaffDAOTest {

    private final StaffDAO dao = new StaffDAO();

    // ==========================================
    // 1. TEST KIỂM TRA TRÙNG EMAIL
    // ==========================================
    @Test
    public void testEmailExists_True() {
        String emailDaTonTai = "admin@example.com"; 
        boolean result = dao.isEmailExists(emailDaTonTai, null); 
        assertTrue(result, "Email này bắt buộc phải tồn tại!");
    }

    @Test
    public void testEmailExists_False() {
        String emailMoi = "lamnguyen_test_" + System.currentTimeMillis() + "@gmail.com"; 
        boolean result = dao.isEmailExists(emailMoi, null); 
        assertFalse(result, "Email ngẫu nhiên này không được trùng!");
    }

    // ==========================================
    // 2. TEST TÍNH NĂNG THÊM MỚI (ADD STAFF)
    // ==========================================
    @Test
    public void testAddStaff_Success() {
        Staff newStaff = new Staff();
        String randomEmail = "staff_add_" + System.currentTimeMillis() + "@example.com";
        
        newStaff.setEmail(randomEmail);
        newStaff.setPassword("$2a$10$Kqpt7TMLc8A/K16IEVb73eIdfN8kG2yV7WnBvLNZNnS0iWd6W7beK"); 
        newStaff.setFullName("Nguyễn Văn Add");
        newStaff.setGender(true); 
        newStaff.setPhone("0999888777");
        newStaff.setRoleId(2); 
        
        boolean result = dao.addStaff(newStaff); 
        assertTrue(result, "Thêm mới nhân viên phải thành công!");
    }

    // ==========================================
    // 3. TEST TÍNH NĂNG CẬP NHẬT (UPDATE STAFF)
    // ==========================================
    @Test
    public void testUpdateStaff_Success() {
        Staff updateStaff = new Staff();
        updateStaff.setStaffId(1); // ID 1 là tài khoản Admin có sẵn
        updateStaff.setEmail("admin@example.com");
        updateStaff.setFullName("Admin Đã Cập Nhật");
        updateStaff.setGender(true); 
        updateStaff.setPhone("0123456789");
        updateStaff.setRoleId(1); 
        
        boolean result = dao.updateStaff(updateStaff);
        assertTrue(result, "Cập nhật tài khoản Admin phải thành công!");
    }

    // ==========================================
    // 4. TEST CÁC HÀM TRUY VẤN VÀ TÌM KIẾM (READ)
    // ==========================================
    @Test
    public void testGetAllStaff() {
        List<Staff> list = dao.getAllStaff();
        assertNotNull(list, "Danh sách trả về không được null!");
        assertFalse(list.isEmpty(), "Danh sách nhân viên trong DB không được rỗng!");
    }

    @Test
    public void testGetStaffByEmail_Success() {
        Staff staff = dao.getStaffByEmail("admin@example.com");
        assertNotNull(staff, "Phải tìm thấy tài khoản admin@example.com!");
        assertEquals("admin@example.com", staff.getEmail());
    }

    @Test
    public void testGetStaffById_Success() {
        Staff staff = dao.getStaffById(1);
        assertNotNull(staff, "Phải tìm thấy nhân viên có ID = 1!");
        assertEquals(1, staff.getStaffId());
    }

    @Test
    public void testGetTotalStaffCount() {
        int count = dao.getTotalStaffCount("", null);
        assertTrue(count >= 0, "Tổng số lượng nhân viên phải lớn hơn hoặc bằng 0!");
    }

    @Test
    public void testGetStaffWithPagingAndSort() {
        // Test phân trang kiểm tra cụm từ trống, không lọc role, sắp xếp theo StaffID trang 1
        List<Staff> list = dao.getStaffWithPagingAndSort("", null, "StaffID", "asc", 1, 5);
        assertNotNull(list, "Kết quả phân trang không được null!");
    }

    // ==========================================
    // 5. TEST TÍNH NĂNG XÓA (DELETE STAFF)
    // ==========================================
    @Test
    public void testDeleteStaff_Success() {
        // Để tránh xóa nhầm data thật, tạo một staff tạm thời
        Staff tempStaff = new Staff();
        String tempEmail = "staff_del_" + System.currentTimeMillis() + "@example.com";
        tempStaff.setEmail(tempEmail);
        tempStaff.setPassword("123");
        tempStaff.setFullName("Staff Thử Nghiệm Xóa");
        tempStaff.setGender(false);
        tempStaff.setPhone("0000000000");
        tempStaff.setRoleId(2);
        
        // Thực hiện chuỗi: Thêm -> Lấy ID tự sinh -> Xóa
        dao.addStaff(tempStaff);
        Staff inserted = dao.getStaffByEmail(tempEmail);
        assertNotNull(inserted);
        
        boolean result = dao.deleteStaff(inserted.getStaffId());
        assertTrue(result, "Hàm xóa nhân viên hoạt động chưa chính xác!");
    }
}