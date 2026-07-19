package fu.swt301.sms.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class LogoutServletTest extends LogoutServlet {

    @Test
    void testLogoutSuccess() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        doGet(request, response);

        verify(session).invalidate();

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testLogoutWithoutSession() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession(false)).thenReturn(null);

        doGet(request, response);

        verify(response).sendRedirect("login.jsp");
    }
}