package fu.swt301.sms.integration;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.filter.AuthFilter;
import fu.swt301.sms.service.AuthService;
import fu.swt301.sms.service.LoginResult;
import fu.swt301.sms.service.LoginStatus;
import fu.swt301.sms.servlet.LoginServlet;
import fu.swt301.sms.servlet.LogoutServlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test cho MODULE XAC THUC (FR-01 -> FR-05) â€” day la pham vi
 * duoc phan cong, KHONG bao gom module Quan ly nhan vien (FR-06 -> FR-11).
 *
 *   FR-01: Dang nhap bang email/password, mat khau bam BCrypt.
 *   FR-02: Thong bao loi ro rang khi sai tai khoan/mat khau, KHONG tiet lo truong nao sai.
 *   FR-03: Khoa tai khoan tam thoi sau 5 lan dang nhap sai lien tiep.
 *   FR-04: Dang xuat - invalidate session, xoa cookie JSESSIONID, ve trang login.
 *   FR-05: Phan quyen ADMIN/USER qua Servlet Filter.
 *
 * Khac voi cac UNIT TEST hien co (AuthServiceTest, LoginServletTest, LogoutServletTest,
 * AuthFilterTest deu mock AuthService/StaffDAO), cac test trong lop nay bam sat dung
 * huong dan trong Test Plan muc 4.2.4: "Mockito mock doi tuong Servlet API; H2 in-memory
 * cho tang DAO". Cu the:
 *   - StaffDAO va AuthService la INSTANCE THAT, noi that vao H2 in-memory qua DBUtils.
 *   - Chi HttpServletRequest/HttpServletResponse/HttpSession/FilterChain la mock (vi
 *     khong dung embedded Tomcat/Jetty), dung de lai duoc luong Servlet -> Service -> DAO -> DB
 *     ma khong can mock AuthService nhu cac unit test cu.
 *   - LoginServlet tu "new AuthService()" ben trong (xem ghi chu trong LoginServletTest), va
 *     AuthService() mac dinh cung tao StaffDAO() moi -> DBUtils van tro ve cung mot H2 in-memory
 *     instance (jdbc:h2:mem:StaffManagementDB;DB_CLOSE_DELAY=-1) trong cung JVM, nen KHONG can
 *     reflection de inject mock: goi thang LoginServlet that la du de kiem tra tich hop.
 */
@DisplayName("Integration (FR-01..FR-05): Auth module - Servlet/Filter + AuthService + StaffDAO + H2 that")
class StaffManagementIntegrationTest {

    private static final int ROLE_ADMIN = 1;
    private static final int ROLE_STAFF = 2;

    private final StaffDAO staffDAO = new StaffDAO();
    private final AuthService authService = new AuthService(staffDAO);

    /** Theo doi cac StaffID duoc tao trong moi test de xoa sach sau khi test xong. */
    private final List<Integer> createdStaffIds = new ArrayList<>();

    @BeforeAll
    static void enableH2() {
        System.setProperty("useH2", "true");
    }

    @AfterAll
    static void disableH2() {
        System.clearProperty("useH2");
    }

    @AfterEach
    void cleanUpCreatedStaff() {
        for (Integer id : createdStaffIds) {
            staffDAO.deleteStaff(id);
        }
        createdStaffIds.clear();
    }

    /** Tao va luu mot Staff moi vao DB that, mat khau duoc hash bang BCrypt qua AuthService. */
    private Staff createPersistedStaff(String fullName, String email, String phone, String rawPassword, int roleId) {
        Staff staff = new Staff();
        staff.setFullName(fullName);
        staff.setGender(true);
        staff.setPhoneNumber(phone);
        staff.setEmail(email);
        staff.setPassword(authService.hashPassword(rawPassword));
        staff.setIsActive(true);
        Role role = new Role();
        role.setRoleID(roleId);
        staff.setRole(role);

        staffDAO.createStaff(staff);

        Staff persisted = staffDAO.findByEmail(email);
        assertNotNull(persisted, "Staff vua tao phai doc lai duoc tu DB");
        createdStaffIds.add(persisted.getStaffID());
        return persisted;
    }

    // =====================================================================
    // FR-01, FR-02, FR-03 â€” luong AuthService + StaffDAO qua H2 that
    // =====================================================================
    @Nested
    @DisplayName("FR-01/02/03: AuthService + StaffDAO + H2 that (khong qua Servlet)")
    class LoginServiceFlow {

        @Test
        @DisplayName("FR-01: Tao nhan vien roi dang nhap dung mat khau -> SUCCESS, tra ve dung thong tin tu DB")
        void createThenLogin_success() {
            Staff created = createPersistedStaff("Nguyen Van Test", "itest.success@fu.edu.vn", "0900000001", "Passw0rd!", ROLE_STAFF);

            LoginResult result = authService.login("itest.success@fu.edu.vn", "Passw0rd!");

            assertEquals(LoginStatus.SUCCESS, result.getStatus());
            assertNotNull(result.getStaff());
            assertEquals(created.getStaffID(), result.getStaff().getStaffID());
            assertEquals("Nguyen Van Test", result.getStaff().getFullName());
            assertEquals("USER", result.getStaff().getRole().getRoleName());
        }

        @Test
        @DisplayName("FR-02: Email khong ton tai trong DB -> INVALID_CREDENTIALS")
        void login_unknownEmail_returnsInvalidCredentials() {
            LoginResult result = authService.login("khong-ton-tai.itest@fu.edu.vn", "bat-ky-mat-khau");

            assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
            assertNull(result.getStaff());
        }

        @Test
        @DisplayName("FR-01/03: Sai mat khau -> so lan dang nhap sai duoc GHI THAT vao DB (doc lai qua DAO khac)")
        void login_wrongPassword_persistsAttemptCountInDatabase() {
            createPersistedStaff("Tran Thi Test", "itest.wrongpw@fu.edu.vn", "0900000002", "CorrectPass1!", ROLE_STAFF);

            LoginResult first = authService.login("itest.wrongpw@fu.edu.vn", "sai-mat-khau");
            assertEquals(LoginStatus.INVALID_CREDENTIALS, first.getStatus());

            Staff reloaded = staffDAO.findByEmail("itest.wrongpw@fu.edu.vn");
            assertEquals(1, reloaded.getLoginAttempts());

            LoginResult second = authService.login("itest.wrongpw@fu.edu.vn", "sai-mat-khau-nua");
            assertEquals(LoginStatus.INVALID_CREDENTIALS, second.getStatus());

            reloaded = staffDAO.findByEmail("itest.wrongpw@fu.edu.vn");
            assertEquals(2, reloaded.getLoginAttempts());
        }

        @Test
        @DisplayName("FR-03: Dang nhap dung sau khi tung sai -> AuthService reset bo dem ve 0 that trong DB")
        void login_successAfterFailures_resetsAttemptCounterInDatabase() {
            createPersistedStaff("Le Van Test", "itest.reset@fu.edu.vn", "0900000003", "RightPass1!", ROLE_STAFF);

            authService.login("itest.reset@fu.edu.vn", "sai-mat-khau");
            assertEquals(1, staffDAO.findByEmail("itest.reset@fu.edu.vn").getLoginAttempts());

            LoginResult result = authService.login("itest.reset@fu.edu.vn", "RightPass1!");

            assertEquals(LoginStatus.SUCCESS, result.getStatus());
            assertEquals(0, staffDAO.findByEmail("itest.reset@fu.edu.vn").getLoginAttempts());
        }

        @Test
        @DisplayName("FR-03: 5 lan sai lien tiep -> ACCOUNT_LOCKED, kiem tra qua toan bo pipeline that")
        void login_fiveConsecutiveFailures_returnsAccountLocked() {
            createPersistedStaff("Pham Thi Test", "itest.locked@fu.edu.vn", "0900000004", "RightPass1!", ROLE_STAFF);

            LoginResult last = null;
            for (int i = 0; i < 5; i++) {
                last = authService.login("itest.locked@fu.edu.vn", "sai-mat-khau");
            }

            assertEquals(LoginStatus.ACCOUNT_LOCKED, last.getStatus());
            assertEquals(AuthService.LOCK_MINUTES, last.getLockMinutesRemaining());
            // StaffDAO.lockAccount() nay tinh LockedUntil bang Java (System.currentTimeMillis())
            // thay vi ham DATEADD/GETDATE cua SQL Server, nen chay dung tren ca H2 (test) lan
            // SQL Server (production) -> kiem tra luon cot LockedUntil da duoc ghi that vao DB.
            Staff reloaded = staffDAO.findByEmail("itest.locked@fu.edu.vn");
            assertNotNull(reloaded.getLockedUntil(), "LockedUntil phai duoc ghi that vao DB");
            assertTrue(reloaded.getLockedUntil().getTime() > System.currentTimeMillis(),
                    "LockedUntil phai la mot thoi diem trong tuong lai");
        }
    }

    // =====================================================================
    // FR-01, FR-02, FR-03 qua LoginServlet that (mock Servlet API, DB that)
    // =====================================================================
    @Nested
    @DisplayName("FR-01/02/03: LoginServlet that (mock Servlet API) + AuthService/StaffDAO/H2 that")
    class LoginServletFlow {

        private HttpServletRequest request;
        private HttpServletResponse response;
        private HttpSession session;
        private RequestDispatcher dispatcher;
        private final LoginServlet loginServlet = new LoginServlet();

        @BeforeEach
        void setUpMocks() {
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);
            session = mock(HttpSession.class);
            dispatcher = mock(RequestDispatcher.class);
        }

        @Test
        @DisplayName("FR-01: POST /login dung email/password -> luu Staff that vao session, redirect staff-list")
        void doPost_correctCredentials_setsSessionAndRedirects() throws Exception {
            createPersistedStaff("Servlet Login OK", "itest.servlet.ok@fu.edu.vn", "0900000101", "Passw0rd!", ROLE_STAFF);
            when(request.getParameter("email")).thenReturn("itest.servlet.ok@fu.edu.vn");
            when(request.getParameter("password")).thenReturn("Passw0rd!");
            when(request.getSession()).thenReturn(session);
            when(request.getMethod()).thenReturn("POST");

            loginServlet.service(request, response);

            verifySessionUserCaptured(session, "itest.servlet.ok@fu.edu.vn");
            verify(response).sendRedirect("staff-list");
        }

        @Test
        @DisplayName("FR-02: Sai password -> forward login.jsp voi thong bao chung 'Invalid email or password'")
        void doPost_wrongPassword_showsGenericErrorMessage() throws Exception {
            createPersistedStaff("Servlet Login Wrong", "itest.servlet.wrong@fu.edu.vn", "0900000102", "RightPass1!", ROLE_STAFF);
            when(request.getParameter("email")).thenReturn("itest.servlet.wrong@fu.edu.vn");
            when(request.getParameter("password")).thenReturn("sai-mat-khau");
            when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);
            when(request.getMethod()).thenReturn("POST");

            loginServlet.service(request, response);

            verify(request).setAttribute("error", "Invalid email or password");
            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }

        @Test
        @DisplayName("FR-02: Email khong ton tai -> CUNG thong bao loi giong het truong hop sai password (khong lo truong sai)")
        void doPost_unknownEmail_showsSameGenericErrorMessageAsWrongPassword() throws Exception {
            when(request.getParameter("email")).thenReturn("khong-ton-tai.servlet@fu.edu.vn");
            when(request.getParameter("password")).thenReturn("bat-ky-mat-khau");
            when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);
            when(request.getMethod()).thenReturn("POST");

            loginServlet.service(request, response);

            // Phai giong het chuoi loi trong test "wrong password" o tren -> FR-02
            verify(request).setAttribute("error", "Invalid email or password");
            verify(dispatcher).forward(request, response);
        }

        @Test
        @DisplayName("FR-03: Tai khoan bi khoa sau 5 lan sai -> forward login.jsp voi thong bao co so phut con lai")
        void doPost_accountLocked_showsLockMessageWithMinutesRemaining() throws Exception {
            createPersistedStaff("Servlet Login Locked", "itest.servlet.locked@fu.edu.vn", "0900000103", "RightPass1!", ROLE_STAFF);
            when(request.getParameter("email")).thenReturn("itest.servlet.locked@fu.edu.vn");
            when(request.getParameter("password")).thenReturn("sai-mat-khau");
            when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);
            when(request.getMethod()).thenReturn("POST");

            // 4 lan sai truoc qua thang service that (cung mot H2 instance voi servlet)
            for (int i = 0; i < 4; i++) {
                authService.login("itest.servlet.locked@fu.edu.vn", "sai-mat-khau");
            }

            // Lan sai thu 5 di qua chinh LoginServlet
            loginServlet.service(request, response);

            verify(request).setAttribute(eq("error"), contains("temporarily locked"));
            verify(request).setAttribute(eq("error"), contains("5 minute"));
            verify(dispatcher).forward(request, response);
        }

        /** Helper: xac nhan session.setAttribute("user", staff) duoc goi voi dung email. */
        private void verifySessionUserCaptured(HttpSession session, String expectedEmail) {
            org.mockito.ArgumentCaptor<Staff> captor = org.mockito.ArgumentCaptor.forClass(Staff.class);
            verify(session).setAttribute(eq("user"), captor.capture());
            assertEquals(expectedEmail, captor.getValue().getEmail());
        }
    }

    // =====================================================================
    // FR-04 â€” LogoutServlet that (mock Servlet API)
    // =====================================================================
    @Nested
    @DisplayName("FR-04: LogoutServlet that (mock Servlet API)")
    class LogoutServletFlow {

        private HttpServletRequest request;
        private HttpServletResponse response;
        private HttpSession session;
        private final LogoutServlet logoutServlet = new LogoutServlet();

        @BeforeEach
        void setUpMocks() {
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);
            session = mock(HttpSession.class);
        }

        @Test
        @DisplayName("FR-04: Dang xuat khi dang co session -> invalidate session that, xoa cookie JSESSIONID, redirect login.jsp")
        void doGet_withActiveSession_invalidatesAndClearsCookieAndRedirects() throws Exception {
            when(request.getSession(false)).thenReturn(session);
            when(request.getContextPath()).thenReturn("");
            when(request.getMethod()).thenReturn("GET");

            logoutServlet.service(request, response);

            verify(session).invalidate();

            org.mockito.ArgumentCaptor<Cookie> cookieCaptor = org.mockito.ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(cookieCaptor.capture());
            Cookie cleared = cookieCaptor.getValue();
            assertEquals("JSESSIONID", cleared.getName());
            assertEquals(0, cleared.getMaxAge());

            verify(response).sendRedirect("login.jsp");
        }

        @Test
        @DisplayName("FR-04: Dang xuat khi khong co session (da het han/da logout truoc do) -> khong loi, van redirect ve login")
        void doGet_noActiveSession_stillRedirectsWithoutError() throws Exception {
            when(request.getSession(false)).thenReturn(null);
            when(request.getContextPath()).thenReturn("");
            when(request.getMethod()).thenReturn("GET");

            assertDoesNotThrow(() -> logoutServlet.service(request, response));

            verify(response).sendRedirect("login.jsp");
        }
    }

    // =====================================================================
    // FR-05 â€” AuthFilter that, dung Staff THAT lay tu AuthService/StaffDAO/H2
    // =====================================================================
    @Nested
    @DisplayName("FR-05: AuthFilter that, phan quyen tren Staff that tu DB")
    class AuthFilterFlow {

        private HttpServletRequest request;
        private HttpServletResponse response;
        private HttpSession session;
        private FilterChain chain;
        private final AuthFilter authFilter = new AuthFilter();

        @BeforeEach
        void setUpMocks() {
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);
            session = mock(HttpSession.class);
            chain = mock(FilterChain.class);
            when(request.getContextPath()).thenReturn("");
        }

        @Test
        @DisplayName("Chua dang nhap (session null) truy cap trang bao ve -> redirect login.jsp, KHONG cho di tiep")
        void doFilter_noSession_redirectsToLogin() throws Exception {
            when(request.getRequestURI()).thenReturn("/staff-list");
            when(request.getSession(false)).thenReturn(null);

            authFilter.doFilter(request, response, chain);

            verify(response).sendRedirect("/login.jsp");
            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Da dang nhap voi role Staff (tu DB that) truy cap /staff-crud (chi danh cho Admin) -> 403")
        void doFilter_loggedInStaffRole_accessingAdminOnlyPath_returnsForbidden() throws Exception {
            Staff staffUser = createPersistedStaff("Filter Staff Role", "itest.filter.staff@fu.edu.vn", "0900000201", "Passw0rd!", ROLE_STAFF);
            when(request.getRequestURI()).thenReturn("/staff-crud");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(staffUser);

            authFilter.doFilter(request, response, chain);

            verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Da dang nhap voi role Admin (tu DB that) truy cap /staff-crud -> cho di tiep binh thuong")
        void doFilter_loggedInAdminRole_accessingAdminOnlyPath_passesThrough() throws Exception {
            Staff adminUser = createPersistedStaff("Filter Admin Role", "itest.filter.admin@fu.edu.vn", "0900000202", "Passw0rd!", ROLE_ADMIN);
            when(request.getRequestURI()).thenReturn("/staff-crud");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(adminUser);

            authFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        @DisplayName("Da dang nhap voi role Staff truy cap trang khong gioi han Admin -> cho di tiep binh thuong")
        void doFilter_loggedInStaffRole_accessingNonAdminPath_passesThrough() throws Exception {
            Staff staffUser = createPersistedStaff("Filter Staff NonAdmin", "itest.filter.staff2@fu.edu.vn", "0900000203", "Passw0rd!", ROLE_STAFF);
            when(request.getRequestURI()).thenReturn("/staff-list");
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn(staffUser);

            authFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("Duong dan public (/login.jsp) -> luon cho di tiep du khong co session")
        void doFilter_publicPath_allowsWithoutSession() throws Exception {
            when(request.getRequestURI()).thenReturn("/login.jsp");
            when(request.getSession(false)).thenReturn(null);

            authFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(response, never()).sendRedirect(anyString());
        }
    }
}
