/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.RoleService;
import fu.swt301.sms.service.StaffService;
import fu.swt301.sms.service.ValidationService;

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
public class StaffCrudServletTest {

    private StaffCrudServlet servlet;

    private StaffService staffService;
    private RoleService roleService;
    private ValidationService validationService;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        staffService = mock(StaffService.class);
        roleService = mock(RoleService.class);
        validationService = mock(ValidationService.class);
        servlet = new StaffCrudServlet(
                staffService,
                roleService,
                validationService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
    }

    private Staff createStaff() {
        Staff staff = new Staff();
        staff.setStaffID(1);
        staff.setFullName("Nguyen Van A");
        return staff;
    }

    private List<Role> createRoleList() {
        List<Role> list = new ArrayList<>();
        Role role = new Role();
        role.setRoleID(1);
        list.add(role);
        return list;
    }

    private void mockStaffForm() {
        when(request.getParameter("staffID")).thenReturn("1");
        when(request.getParameter("fullName")).thenReturn("Nguyen Van A");
        when(request.getParameter("gender")).thenReturn("true");
        when(request.getParameter("phoneNumber")).thenReturn("0123456789");
        when(request.getParameter("email")).thenReturn("a@gmail.com");
        when(request.getParameter("isActive")).thenReturn("true");
        when(request.getParameter("roleID")).thenReturn("1");
        when(request.getParameter("password")).thenReturn("123456");
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    void testDoGetCreate() throws Exception {
        when(request.getParameter("action"))
                .thenReturn("create");
        when(roleService.getAllRoles())
                .thenReturn(createRoleList());
        when(request.getRequestDispatcher("staff-form.jsp"))
                .thenReturn(dispatcher);
        servlet.doGet(request, response);

        verify(roleService).getAllRoles();
        verify(request).setAttribute(eq("roleList"), anyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetEdit() throws Exception {
        Staff staff = createStaff();
        when(request.getParameter("action"))
                .thenReturn("edit");
        when(request.getParameter("id"))
                .thenReturn("1");
        when(roleService.getAllRoles())
                .thenReturn(createRoleList());
        when(staffService.getStaffById(1))
                .thenReturn(staff);
        when(request.getRequestDispatcher("staff-form.jsp"))
                .thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(roleService).getAllRoles();
        verify(staffService).getStaffById(1);
        verify(request).setAttribute("staff", staff);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void testDoGetDelete() throws Exception {
        when(request.getParameter("action"))
                .thenReturn("delete");
        when(request.getParameter("id"))
                .thenReturn("1");
        when(staffService.deleteStaff(1))
                .thenReturn(true);
        servlet.doGet(request, response);
        verify(staffService).deleteStaff(1);
        verify(response).sendRedirect("staff-list");
    }

    @Test
    void testDoPostCreateSuccess() throws Exception {
        when(request.getParameter("action"))
                .thenReturn("create");
        mockStaffForm();
        when(validationService.validateCreateStaff(any(Staff.class)))
                .thenReturn(null);
        servlet.doPost(request, response);

        verify(validationService).validateCreateStaff(any(Staff.class));
        verify(staffService).createStaff(any(Staff.class));
        verify(response).sendRedirect("staff-list");
    }

    @Test
    void testDoPostCreateValidationFail() throws Exception {
        when(request.getParameter("action"))
                .thenReturn("create");
        mockStaffForm();
        when(validationService.validateCreateStaff(any(Staff.class)))
                .thenReturn("Email already exists. Please choose another one.");
        when(roleService.getAllRoles())
                .thenReturn(new ArrayList<>());
        when(request.getRequestDispatcher("staff-form.jsp"))
                .thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(
                "errorMessage",
                "Email already exists. Please choose another one.");
        verify(request).setAttribute(eq("staff"), any(Staff.class));
        verify(request).setAttribute(eq("roleList"), anyList());
        verify(dispatcher).forward(request, response);
        verify(staffService, never()).createStaff(any());
    }

    @Test
    void testDoPostUpdateSuccess() throws Exception {
        when(request.getParameter("action"))
                .thenReturn("update");
        mockStaffForm();
        when(validationService.validateUpdateStaff(any(Staff.class)))
                .thenReturn(null);
        servlet.doPost(request, response);
        verify(validationService).validateUpdateStaff(any(Staff.class));
        verify(staffService).updateStaff(any(Staff.class));
        verify(response).sendRedirect("staff-list");
    }

    @Test
    void testDoPostUpdateValidationFail() throws Exception {
        when(request.getParameter("action"))
                .thenReturn("update");
        mockStaffForm();
        when(validationService.validateUpdateStaff(any(Staff.class)))
                .thenReturn("Phone number already exists. Please choose another one.");
        when(roleService.getAllRoles())
                .thenReturn(new ArrayList<>());
        when(request.getRequestDispatcher("staff-form.jsp"))
                .thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(
                "errorMessage",
                "Phone number already exists. Please choose another one.");
        verify(request).setAttribute(eq("staff"), any(Staff.class));
        verify(request).setAttribute(eq("roleList"), anyList());
        verify(dispatcher).forward(request, response);
        verify(staffService, never()).updateStaff(any());
    }
}
