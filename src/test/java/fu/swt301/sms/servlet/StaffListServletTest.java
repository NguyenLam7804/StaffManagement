/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 *
 * @author admin
 */
public class StaffListServletTest {

    private StaffListServlet servlet;
    private StaffService staffService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        staffService = mock(StaffService.class);
        servlet = new StaffListServlet(staffService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    void testDoGetWithoutFilter() throws Exception {
        when(request.getParameter("searchName"))
                .thenReturn("");
        when(request.getParameter("searchStatus"))
                .thenReturn("");
        List<Staff> list = new ArrayList<>();
        list.add(new Staff());
        when(staffService.getStaffList("", ""))
                .thenReturn(list);
        when(request.getRequestDispatcher("staff-list.jsp"))
                .thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(staffService).getStaffList("", "");
        verify(request).setAttribute("staffList", list);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetWithFilter() throws Exception {
        when(request.getParameter("searchName"))
                .thenReturn("Nguyen");
        when(request.getParameter("searchStatus"))
                .thenReturn("true");
        List<Staff> list = new ArrayList<>();
        when(staffService.getStaffList("Nguyen", "true"))
                .thenReturn(list);
        when(request.getRequestDispatcher("staff-list.jsp"))
                .thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(staffService).getStaffList("Nguyen", "true");
        verify(request).setAttribute("staffList", list);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetEmptyList() throws Exception {
        when(request.getParameter("searchName"))
                .thenReturn("");
        when(request.getParameter("searchStatus"))
                .thenReturn("");
        when(staffService.getStaffList("", ""))
                .thenReturn(new ArrayList<>());
        when(request.getRequestDispatcher("staff-list.jsp"))
                .thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute(eq("staffList"), anyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetWithNullParameters() throws Exception {
        when(request.getParameter("searchName"))
                .thenReturn(null);
        when(request.getParameter("searchStatus"))
                .thenReturn(null);
        when(staffService.getStaffList("", ""))
                .thenReturn(new ArrayList<>());
        when(request.getRequestDispatcher("staff-list.jsp"))
                .thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(staffService).getStaffList("", "");
        verify(request).setAttribute(eq("staffList"), anyList());
        verify(dispatcher).forward(request, response);
    }
}
