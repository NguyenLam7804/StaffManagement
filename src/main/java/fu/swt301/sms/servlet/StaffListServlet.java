package fu.swt301.sms.servlet;

import java.io.IOException;
import java.util.List;

import fu.swt301.sms.service.StaffService;
import fu.swt301.sms.entity.Staff;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/staff-list")
public class StaffListServlet extends HttpServlet {

    private final StaffService staffService;

    public StaffListServlet() {
        this.staffService = new StaffService();

    }

    StaffListServlet(StaffService service) {

        this.staffService = service;

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchName = request.getParameter("searchName");
        if (searchName == null) {
            searchName = "";
        }

        String searchStatus = request.getParameter("searchStatus");
        if (searchStatus == null) {
            searchStatus = "";
        }
        List<Staff> staffList = staffService.getStaffList(searchName, searchStatus);
        request.setAttribute("staffList", staffList);
        request.getRequestDispatcher("staff-list.jsp").forward(request, response);
    }
}
