package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.entity.Role;       
import fu.swt301.sms.dao.RoleDAO;         
import fu.swt301.sms.service.StaffService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet(name = "StaffCrudServlet", urlPatterns = {"/staff-list"})
public class StaffCrudServlet extends HttpServlet {
    private final StaffService staffService = new StaffService();
    private final RoleDAO roleDAO = new RoleDAO(); 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new": 
                List<Role> rolesNew = roleDAO.getAllRoles(); 
                request.setAttribute("roles", rolesNew);     
                request.getRequestDispatcher("staff-form.jsp").forward(request, response);
                break;

            case "delete":
                int idDel = Integer.parseInt(request.getParameter("id"));
                staffService.deleteStaff(idDel);
                response.sendRedirect("staff-list");
                break;
                
            case "edit":
                int idEdit = Integer.parseInt(request.getParameter("id"));
                Staff staff = staffService.getStaffById(idEdit);
                List<Role> rolesEdit = roleDAO.getAllRoles(); 
                
                request.setAttribute("staff", staff);
                request.setAttribute("roles", rolesEdit);     
                request.getRequestDispatcher("staff-form.jsp").forward(request, response);
                break;
                
            case "list":
            default:
                // 1. Đọc và xử lý an toàn tham số Tìm kiếm & Lọc quyền
                String search = request.getParameter("search");
                if (search == null) search = "";
                
                String roleFilterStr = request.getParameter("roleFilter");
                Integer roleFilter = null;
                if (roleFilterStr != null && !roleFilterStr.trim().isEmpty()) {
                    try {
                        roleFilter = Integer.parseInt(roleFilterStr);
                    } catch (NumberFormatException e) {
                        roleFilter = null;
                    }
                }

                // 2. Đọc tham số Sắp xếp (Mặc định xếp theo StaffID tăng dần)
                String sort = request.getParameter("sort");
                String order = request.getParameter("order");
                if (sort == null || sort.isEmpty()) sort = "StaffID";
                if (order == null || order.isEmpty()) order = "asc";

                // 3. Đọc tham số Phân trang (Mặc định Page = 1, mỗi trang 5 bản ghi)
                int page = 1;
                int pageSize = 5;
                String pageStr = request.getParameter("page");
                if (pageStr != null && !pageStr.isEmpty()) {
                    try {
                        page = Integer.parseInt(pageStr);
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                }

                // 4. Lấy dữ liệu phân trang và tính toán tổng số trang dựa trên bộ lọc hoạt động
                List<Staff> list = staffService.getStaffWithPagingAndSort(search, roleFilter, sort, order, page, pageSize);
                int totalRecords = staffService.getTotalStaffCount(search, roleFilter);
                int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
                if (totalPages == 0) totalPages = 1;

                // 5. Tính toán dữ liệu Dashboard Stats (Luôn hiển thị tổng thể không bị ảnh hưởng bởi thanh tìm kiếm)
                int totalStaffCount = staffService.getTotalStaffCount("", null);
                int totalAdminCount = staffService.getTotalStaffCount("", 1);
                int totalUserCount = staffService.getTotalStaffCount("", 2);

                // 6. Đẩy toàn bộ dữ liệu ra Request Attribute
                request.setAttribute("staffList", list);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("totalStaffCount", totalStaffCount);
                request.setAttribute("totalAdminCount", totalAdminCount);
                request.setAttribute("totalUserCount", totalUserCount);
                
                request.getRequestDispatcher("staff-list.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String idStr = request.getParameter("staffId");
        
        // 1. Xác định ID hiện tại an toàn (Loại bỏ khoảng trắng, ép "0", null, trống về số 0 hoàn toàn)
        int currentId = 0;
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                currentId = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException e) {
                currentId = 0; // Nếu lỗi định dạng từ trình duyệt, ép về luồng Add mới an toàn
            }
        }
        
        // 2. Khởi tạo đối tượng Staff và nạp dữ liệu (Gán luôn cả StaffId vừa xử lý)
        Staff staff = new Staff();
        staff.setStaffId(currentId); 
        staff.setEmail(request.getParameter("email"));
        staff.setFullName(request.getParameter("fullName"));
        staff.setGender(Boolean.parseBoolean(request.getParameter("gender")));
        staff.setPhone(request.getParameter("phone"));
        staff.setRoleId(Integer.parseInt(request.getParameter("roleId")));
        
        // 3. Chặn trùng Email ở Server-side bằng cách gọi qua tầng Service
        if (staffService.isEmailExists(staff.getEmail(), currentId)) {
            // Đính kèm câu thông báo lỗi về trang JSP
            request.setAttribute("errorMessage", "Email này đã tồn tại trong hệ thống! Vui lòng nhập email khác.");
            
            // Đẩy ngược staff (đã có sẵn staffId = 0 hoặc > 0) để JSTL dưới JSP nhận diện chính xác
            request.setAttribute("staff", staff);
            
            // Nạp lại danh sách Roles để phục vụ thẻ dropdown select tránh bị trống form
            request.setAttribute("roles", roleDAO.getAllRoles());
            
            // Forward ngược về lại trang Form nhập liệu
            request.getRequestDispatcher("staff-form.jsp").forward(request, response);
            return; // Ngắt tiến trình xử lý tại đây, không cho hệ thống ghi nhận xuống DB
        }
        
        // 4. Nếu kiểm tra Email hợp lệ, tiến hành lưu trữ dữ liệu dựa trên currentId chuẩn hóa
        if (currentId == 0) {
            String hashedPassword = BCrypt.hashpw("123", BCrypt.gensalt());
            staff.setPassword(hashedPassword); 
            staffService.addStaff(staff);
        } else {
            staffService.updateStaff(staff);
        }
        response.sendRedirect("staff-list");
    }
}