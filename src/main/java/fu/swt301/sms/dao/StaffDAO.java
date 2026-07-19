package fu.swt301.sms.dao;

import fu.swt301.sms.utils.DBUtils; 
import fu.swt301.sms.entity.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // 1. Lấy toàn bộ danh sách nhân viên
    public List<Staff> getAllStaff() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM Staff";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Staff s = new Staff();
                s.setStaffId(rs.getInt("Staff_ID"));
                s.setFullName(rs.getString("FullName"));
                s.setGender(rs.getBoolean("Gender"));
                s.setPhone(rs.getString("Phone"));
                s.setEmail(rs.getString("Email"));
                s.setRoleId(rs.getInt("Role_ID"));
                s.setPassword(rs.getString("Password"));
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Tìm kiếm nhân viên bằng Email (dùng cho Login)
    public Staff getStaffByEmail(String email) {
        String sql = "SELECT * FROM Staff WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Staff s = new Staff();
                    s.setStaffId(rs.getInt("Staff_ID"));
                    s.setFullName(rs.getString("FullName"));
                    s.setGender(rs.getBoolean("Gender"));
                    s.setPhone(rs.getString("Phone"));
                    s.setEmail(rs.getString("Email"));
                    s.setRoleId(rs.getInt("Role_ID"));
                    s.setPassword(rs.getString("Password"));
                    return s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Đếm số lượng phục vụ phân trang và dashboard
    public int getTotalStaffCount(String search, Integer roleId) {
        int count = 0;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Staff WHERE (FullName LIKE ? OR Email LIKE ?)");
        if (roleId != null) sql.append(" AND Role_ID = ?");
        
        try (Connection conn = DBUtils.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            if (roleId != null) ps.setInt(3, roleId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // 4. Lấy danh sách nhân viên theo trang (Đã sửa lỗi đồng bộ tên cột mapping)
    public List<Staff> getStaffWithPagingAndSort(String search, Integer roleId, String sortBy, String order, int page, int pageSize) {
        List<Staff> list = new ArrayList<>();
        
        // Đồng bộ hóa chuỗi "StaffID" từ Servlet gửi xuống thành tên cột "Staff_ID" trong DB
        if (sortBy == null || sortBy.isEmpty() || sortBy.equalsIgnoreCase("idStr") || sortBy.equalsIgnoreCase("StaffID")) {
            sortBy = "Staff_ID";
        } else if (sortBy.equalsIgnoreCase("roleId") || sortBy.equalsIgnoreCase("roleFilter")) {
            sortBy = "Role_ID";
        } else if (sortBy.equalsIgnoreCase("FullName")) {
            sortBy = "FullName";
        }
        
        if (order == null || !order.equalsIgnoreCase("desc")) order = "asc";

        StringBuilder sql = new StringBuilder("SELECT * FROM Staff WHERE (FullName LIKE ? OR Email LIKE ?)");
        if (roleId != null) sql.append(" AND Role_ID = ?");
        sql.append(" ORDER BY ").append(sortBy).append(" ").append(order)
           .append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setString(index++, "%" + search + "%");
            ps.setString(index++, "%" + search + "%");
            if (roleId != null) ps.setInt(index++, roleId);
            ps.setInt(index++, (page - 1) * pageSize);
            ps.setInt(index, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Staff s = new Staff();
                    s.setStaffId(rs.getInt("Staff_ID"));
                    s.setFullName(rs.getString("FullName"));
                    s.setGender(rs.getBoolean("Gender"));
                    s.setPhone(rs.getString("Phone"));
                    s.setEmail(rs.getString("Email"));
                    s.setRoleId(rs.getInt("Role_ID"));
                    s.setPassword(rs.getString("Password"));
                    list.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 5. Lấy chi tiết 1 nhân viên theo ID
    public Staff getStaffById(int id) {
        String sql = "SELECT * FROM Staff WHERE Staff_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Staff s = new Staff();
                    s.setStaffId(rs.getInt("Staff_ID"));
                    s.setFullName(rs.getString("FullName"));
                    s.setGender(rs.getBoolean("Gender"));
                    s.setPhone(rs.getString("Phone"));
                    s.setEmail(rs.getString("Email"));
                    s.setRoleId(rs.getInt("Role_ID"));
                    s.setPassword(rs.getString("Password"));
                    return s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 6. Thêm mới nhân viên
    public boolean addStaff(Staff staff) {
        String sql = "INSERT INTO Staff (FullName, Gender, Phone, Email, Role_ID, Password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getFullName());
            ps.setBoolean(2, staff.isGender()); 
            ps.setString(3, staff.getPhone());
            ps.setString(4, staff.getEmail());
            ps.setInt(5, staff.getRoleId());
            ps.setString(6, staff.getPassword());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 7. Cập nhật nhân viên
    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE Staff SET FullName = ?, Gender = ?, Phone = ?, Email = ?, Role_ID = ? WHERE Staff_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getFullName());
            ps.setBoolean(2, staff.isGender());
            ps.setString(3, staff.getPhone());
            ps.setString(4, staff.getEmail());
            ps.setInt(5, staff.getRoleId());
            ps.setInt(6, staff.getStaffId());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 8. Xóa nhân viên
    public boolean deleteStaff(int id) {
        String sql = "DELETE FROM Staff WHERE Staff_ID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // 9. Kiểm tra trùng lặp Email (Dùng cho cả Add và Update)
// - Khi Add: truyền staffId là null hoặc 0
// - Khi Update: truyền staffId của nhân viên đang sửa
public boolean isEmailExists(String email, Integer staffId) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Staff WHERE Email = ?");
    if (staffId != null && staffId > 0) {
        sql.append(" AND Staff_ID != ?");
    }
    
    try (Connection conn = DBUtils.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {
        
        ps.setString(1, email);
        if (staffId != null && staffId > 0) {
            ps.setInt(2, staffId);
        }
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
    }
}