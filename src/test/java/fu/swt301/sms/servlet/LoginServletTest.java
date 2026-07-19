package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.AuthService;
import fu.swt301.sms.service.LoginResult;
import fu.swt301.sms.service.LoginStatus;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test tầng Servlet cho LoginServlet, mock toàn bộ Servlet API bằng Mockito
 * (không cần Tomcat thật -> vẫn là Unit Test, không phải Integration Test).
 *
 * Bao phủ:
 *  - FR-01: đăng nhập thành công -> set session attribute "user", redirect "staff-list"
 *  - FR-02: thông báo lỗi PHẢI GIỐNG HỆT NHAU dù sai email hay sai password (không lộ trường sai)
 *  - FR-03 (gián tiếp): thông báo khi tài khoản bị khoá có kèm số phút còn lại
 *
 * LƯU Ý CHO NHÓM: LoginServlet hiện tự "new AuthService()" thay vì nhận qua constructor,
 * nên test này phải dùng reflection để thay field private "authService" bằng mock.
 * Đây là một vấn đề nên nêu trong Code Review: nên sửa thành constructor injection, ví dụ:
 *
 *   private final AuthService authService;
 *   public LoginServlet() { this(new AuthService()); }
 *   public LoginServlet(AuthService authService) { this.authService = authService; }
 *
 * để test không phải dùng reflection nữa.
 */
@ExtendWith(MockitoExtension.class)
class LoginServletTest {

    @Mock private AuthService authService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private LoginServlet loginServlet;

    @BeforeEach
    void setUp() throws Exception {
        loginServlet = new LoginServlet();
        injectMockAuthService(loginServlet, authService);
    }

    /** Thay field private final "authService" bằng mock thông qua reflection. */
    private void injectMockAuthService(LoginServlet servlet, AuthService mockService) throws Exception {
        Field field = LoginServlet.class.getDeclaredField("authService");
        field.setAccessible(true);
        field.set(servlet, mockService);
    }

    private Staff sampleStaff() {
        Staff staff = new Staff();
        staff.setStaffID(1);
        staff.setEmail("khanh@fpt.edu.vn");
        staff.setFullName("Nguyen Van Khanh");
        Role role = new Role();
        role.setRoleName("USER");
        staff.setRole(role);
        return staff;
    }

    @Test
    @DisplayName("FR-01: Đăng nhập đúng -> lưu user vào session, redirect sang staff-list")
    void doPost_success_redirectsToStaffList() throws Exception {
        when(request.getParameter("email")).thenReturn("khanh@fpt.edu.vn");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(request.getSession()).thenReturn(session);

        Staff staff = sampleStaff();
        when(authService.login("khanh@fpt.edu.vn", "Password@123"))
                .thenReturn(new LoginResult(LoginStatus.SUCCESS, staff, 0));

        loginServlet.doPost(request, response);

        verify(session).setAttribute("user", staff);
        verify(response).sendRedirect("staff-list");
        // Không được forward về login.jsp khi thành công
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("FR-02: Sai password -> forward login.jsp với thông báo lỗi CHUNG CHUNG")
    void doPost_wrongPassword_showsGenericError() throws Exception {
        when(request.getParameter("email")).thenReturn("khanh@fpt.edu.vn");
        when(request.getParameter("password")).thenReturn("sai-mat-khau");
        when(authService.login("khanh@fpt.edu.vn", "sai-mat-khau"))
                .thenReturn(new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, 0));
        when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

        loginServlet.doPost(request, response);

        verify(request).setAttribute("error", "Invalid email or password");
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("FR-02: Email không tồn tại -> thông báo lỗi GIỐNG HỆT trường hợp sai password")
    void doPost_emailNotFound_showsSameGenericErrorAsWrongPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("khong-ton-tai@fpt.edu.vn");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(authService.login("khong-ton-tai@fpt.edu.vn", "Password@123"))
                .thenReturn(new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, 0));
        when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

        loginServlet.doPost(request, response);

        // Đây chính là test case chứng minh FR-02: message phải trùng khớp 100% với
        // test "sai password" ở trên -> không được để lộ email hay sai ở đâu.
        verify(request).setAttribute("error", "Invalid email or password");
    }

    @Test
    @DisplayName("FR-03: Tài khoản bị khoá -> thông báo có kèm số phút còn lại, không redirect")
    void doPost_accountLocked_showsLockMessageWithMinutes() throws Exception {
        when(request.getParameter("email")).thenReturn("khanh@fpt.edu.vn");
        when(request.getParameter("password")).thenReturn("Password@123");
        when(authService.login("khanh@fpt.edu.vn", "Password@123"))
                .thenReturn(new LoginResult(LoginStatus.ACCOUNT_LOCKED, null, 5));
        when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

        loginServlet.doPost(request, response);

        verify(request).setAttribute("error",
                "Account is temporarily locked due to too many failed login attempts. "
                        + "Please try again in 5 minute(s).");
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("GET /login -> chỉ forward tới login.jsp, không xử lý logic đăng nhập")
    void doGet_forwardsToLoginPage() throws Exception {
        when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

        loginServlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
        verifyNoInteractions(authService);
    }
}
