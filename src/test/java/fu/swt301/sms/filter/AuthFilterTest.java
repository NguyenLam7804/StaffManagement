package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit test cho AuthFilter — bao phủ FR-05 (phân quyền ADMIN/USER qua Servlet Filter)
 * và một phần FR-04 (chặn truy cập vào trang cần đăng nhập sau khi đã logout / session null).
 *
 * Servlet Filter API dễ test thuần bằng Mockito vì doFilter() nhận đủ 3 tham số
 * (request, response, chain) qua interface -> không cần Tomcat thật.
 */
@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private FilterChain chain;

    private final AuthFilter authFilter = new AuthFilter();

    @BeforeEach
    void setUp() {
        when(request.getContextPath()).thenReturn("");
    }

    private Staff staffWithRole(String roleName) {
        Staff staff = new Staff();
        staff.setStaffID(1);
        Role role = new Role();
        role.setRoleName(roleName);
        staff.setRole(role);
        return staff;
    }

    @Test
    @DisplayName("FR-04/05: Chưa đăng nhập (session null) truy cập trang bảo vệ -> redirect login.jsp, KHÔNG cho đi tiếp")
    void doFilter_redirectsToLogin_whenNoSession() throws Exception {
        when(request.getRequestURI()).thenReturn("/staff-list");
        when(request.getSession(false)).thenReturn(null);

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login.jsp");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("FR-04: Đã logout (session tồn tại nhưng không có attribute 'user') -> vẫn bị chặn")
    void doFilter_redirectsToLogin_whenSessionHasNoUser() throws Exception {
        when(request.getRequestURI()).thenReturn("/staff-list");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login.jsp");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("FR-05: USER truy cập /staff-crud (chức năng ADMIN) -> 403 Forbidden")
    void doFilter_forbidsUser_onAdminOnlyPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/staff-crud");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(staffWithRole("USER"));

        authFilter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Admin role required");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("FR-05: ADMIN truy cập /staff-crud -> cho đi tiếp bình thường")
    void doFilter_allowsAdmin_onAdminOnlyPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/staff-crud");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(staffWithRole("ADMIN"));

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("FR-05: USER truy cập /staff-list (chỉ xem, không phải admin-only) -> vẫn được phép")
    void doFilter_allowsUser_onNonAdminPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/staff-list");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(staffWithRole("USER"));

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Truy cập /login.jsp khi chưa đăng nhập -> luôn cho qua (public path)")
    void doFilter_allowsPublicPath_withoutSession() throws Exception {
        when(request.getRequestURI()).thenReturn("/login.jsp");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        // Không cần kiểm tra session vì path public phải return sớm, trước khi đọc session
        verify(request, never()).getSession(false);
    }

    @Test
    @DisplayName("Truy cập file tĩnh /css/style.css khi chưa đăng nhập -> luôn cho qua")
    void doFilter_allowsStaticResource_withoutSession() throws Exception {
        when(request.getRequestURI()).thenReturn("/css/style.css");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request, never()).getSession(false);
    }
}
