package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.AuthException;
import fu.swt301.sms.service.AuthService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị trang giao diện login.jsp
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            // Gọi tầng Service xử lý logic kiểm tra đăng nhập (đã có BCrypt)
            Staff staff = authService.login(email, password);
            
            // Đăng nhập thành công -> Lưu vào Session và nhảy tới trang danh sách nhân viên
            HttpSession session = request.getSession();
            session.setAttribute("user", staff);
            response.sendRedirect("staff-list"); 
            
        } catch (AuthException e) {
            // Thất bại -> Gửi thông báo lỗi về lại trang login.jsp
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}