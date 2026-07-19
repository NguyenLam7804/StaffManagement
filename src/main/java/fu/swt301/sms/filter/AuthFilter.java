package fu.swt301.sms.filter;

import fu.swt301.sms.entity.Staff;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = { "/login", "/login.jsp" };
    private static final String[] ADMIN_ONLY_PATHS = { "/staff-crud" };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (isStaticResource(path) || isPublicPath(path)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpSession session = request.getSession(false);
        Staff user = (session != null) ? (Staff) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (isAdminOnlyPath(path) && !isAdmin(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin role required");
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    private boolean isAdmin(Staff user) {
        return user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
    }

    private boolean isPublicPath(String path) {
        for (String p : PUBLIC_PATHS) if (path.equals(p)) return true;
        return false;
    }

    private boolean isAdminOnlyPath(String path) {
        for (String p : ADMIN_ONLY_PATHS) if (path.equals(p)) return true;
        return false;
    }

    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }
}