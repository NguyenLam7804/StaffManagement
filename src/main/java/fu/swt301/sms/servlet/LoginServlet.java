package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.AuthService;
import fu.swt301.sms.service.LoginResult;
import fu.swt301.sms.service.LoginStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        LoginResult result = authService.login(email, password);

        if (result.getStatus() == LoginStatus.SUCCESS) {
            Staff staff = result.getStaff();
            HttpSession session = request.getSession();
            session.setAttribute("user", staff);
            response.sendRedirect("staff-list");
            return;
        }

        if (result.getStatus() == LoginStatus.ACCOUNT_LOCKED) {
            request.setAttribute("error",
                    "Account is temporarily locked due to too many failed login attempts. "
                            + "Please try again in " + result.getLockMinutesRemaining() + " minute(s).");
        } else {
            request.setAttribute("error", "Invalid email or password");
        }
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}