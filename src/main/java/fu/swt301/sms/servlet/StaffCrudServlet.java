package fu.swt301.sms.servlet;

import java.io.IOException;
import java.util.List;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.RoleService;
import fu.swt301.sms.service.StaffService;
import fu.swt301.sms.service.ValidationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet acts as a controller for all CRUD (Create, Read, Update, Delete)
 * operations related to Staff. It handles both the display of forms (for
 * creating/editing) and the processing of submitted form data.
 */
@WebServlet("/staff-crud")
public class StaffCrudServlet extends HttpServlet {

    private final StaffService staffService;
    private final RoleService roleService;
    private final ValidationService validationService;

    public StaffCrudServlet() {

        this.staffService = new StaffService();
        this.roleService = new RoleService();
        this.validationService
                = new ValidationService(staffService);

    }

    StaffCrudServlet(StaffService staffService,
            RoleService roleService,
            ValidationService validationService) {

        this.staffService = staffService;
        this.roleService = roleService;
        this.validationService = validationService;
    }

    /**
     * Handles POST requests, which are used to submit data for creating or
     * updating a staff member. This method contains the core logic for data
     * validation and persistence.
     *
     * @param request The HttpServletRequest object containing the form data.
     * @param response The HttpServletResponse object for sending the response.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // --- Step 1: Populate a Staff object from the request parameters ---
        Staff staff = populateStaff(request, action);

        // --- Step 2: Perform server-side validation for uniqueness ---
        String errorMessage;
        if ("create".equals(action)) {
            errorMessage = validationService.validateCreateStaff(staff);
        } else {
            errorMessage = validationService.validateUpdateStaff(staff);
        }

        // --- Step 3: Handle validation failure ---
        // If an error message was set, it means validation failed.
        if (errorMessage != null) {
            // Add the error message and the user-submitted data back into the request.
            request.setAttribute("errorMessage", errorMessage);
            request.setAttribute("staff", staff); // This preserves the user's input in the form fields.

            // Also, reload the list of roles for the dropdown.
            loadRoleList(request);

            // Forward the request back to the form page to display the error and the preserved data.
            // Using forward is crucial here instead of redirect to maintain the request attributes.
            request.getRequestDispatcher("staff-form.jsp").forward(request, response);
            return; // Stop further processing to prevent the invalid data from being saved.
        }

        // --- Step 4: Handle validation success ---
        // If there were no errors, proceed with the database operation.
        if ("create".equals(action)) {
            staffService.createStaff(staff);
        } else if ("update".equals(action)) {
            staffService.updateStaff(staff);
        }

        // After a successful operation, redirect the user to the staff list page.
        // A redirect is used to prevent form resubmission issues if the user refreshes the page.
        response.sendRedirect("staff-list");
    }

    /**
     * Handles GET requests, which are used to display pages or perform simple
     * actions like deletion.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            // Handle deletion action.
            int staffId = Integer.parseInt(request.getParameter("id"));
            staffService.deleteStaff(staffId);
            response.sendRedirect("staff-list");
        } else {
            // Handles both "create" and "edit" actions, as both need to display the form.
            // First, always fetch the list of roles for the dropdown.
            loadRoleList(request);

            if ("edit".equals(action)) {
                // If editing, fetch the existing staff member's data to pre-populate the form.
                int staffId = Integer.parseInt(request.getParameter("id"));
                Staff staff = staffService.getStaffById(staffId);
                request.setAttribute("staff", staff);
            }
            // If creating, we just need the empty form with the role list.

            // Forward to the JSP form for display.
            request.getRequestDispatcher("staff-form.jsp").forward(request, response);
        }
    }

    private Staff populateStaff(HttpServletRequest request, String action) {
        Staff staff = new Staff();
        String staffIdParam = request.getParameter("staffID");
        int staffId = (staffIdParam != null && !staffIdParam.isEmpty()) ? Integer.parseInt(staffIdParam) : 0;
        staff.setStaffID(staffId);
        staff.setFullName(request.getParameter("fullName").trim());
        staff.setGender(Boolean.parseBoolean(request.getParameter("gender")));
        staff.setPhoneNumber(request.getParameter("phoneNumber").trim());
        staff.setEmail(request.getParameter("email").trim());
        staff.setIsActive(Boolean.parseBoolean(request.getParameter("isActive")));
        if ("create".equals(action)) {
            staff.setPassword(request.getParameter("password"));
        }
        Role role = new Role();
        role.setRoleID(Integer.parseInt(request.getParameter("roleID")));
        staff.setRole(role);
        return staff;
    }

    private void loadRoleList(HttpServletRequest request) {
        List<Role> roleList = roleService.getAllRoles();
        request.setAttribute("roleList", roleList);

    }
}
