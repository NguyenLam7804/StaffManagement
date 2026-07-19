package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AuthService — bao phủ:
 *  - FR-01: Đăng nhập bằng email/password, mật khẩu băm BCrypt
 *  - FR-02: (gián tiếp) status trả về không phân biệt sai email hay sai password
 *  - FR-03: Khoá tài khoản 5 phút sau 5 lần đăng nhập sai liên tiếp
 *
 * StaffDAO được mock hoàn toàn -> test không cần kết nối CSDL thật.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "khanh@fpt.edu.vn";
    private static final String RAW_PASSWORD = "Password@123";

    @Mock
    private StaffDAO staffDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(staffDAO);
    }

    /** Tạo một Staff hợp lệ dùng chung cho nhiều test, password đã được hash thật bằng BCrypt. */
    private Staff buildActiveStaff(int loginAttempts, Timestamp lockedUntil) {
        Staff staff = new Staff();
        staff.setStaffID(1);
        staff.setEmail(EMAIL);
        staff.setPassword(BCrypt.hashpw(RAW_PASSWORD, BCrypt.gensalt()));
        staff.setLoginAttempts(loginAttempts);
        staff.setLockedUntil(lockedUntil);
        Role role = new Role();
        role.setRoleID(2);
        role.setRoleName("USER");
        staff.setRole(role);
        return staff;
    }

    @Nested
    @DisplayName("FR-01: Đăng nhập với email/password hợp lệ")
    class LoginSuccess {

        @Test
        @DisplayName("Email và password đúng -> SUCCESS, reset bộ đếm login attempts")
        void login_success_whenCredentialsCorrect() {
            Staff staff = buildActiveStaff(2, null);
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login(EMAIL, RAW_PASSWORD);

            assertEquals(LoginStatus.SUCCESS, result.getStatus());
            assertNotNull(result.getStaff());
            assertEquals(EMAIL, result.getStaff().getEmail());
            assertEquals(0, result.getLockMinutesRemaining());
            verify(staffDAO).resetLoginAttempts(1);
            // Đăng nhập thành công thì không được cập nhật số lần sai hay khoá tài khoản
            verify(staffDAO, never()).updateLoginAttempts(anyInt(), anyInt());
            verify(staffDAO, never()).lockAccount(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Email có khoảng trắng thừa vẫn đăng nhập được (do trim())")
        void login_success_trimsEmailWhitespace() {
            Staff staff = buildActiveStaff(0, null);
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login("  " + EMAIL + "  ", RAW_PASSWORD);

            assertEquals(LoginStatus.SUCCESS, result.getStatus());
            verify(staffDAO).findByEmail(EMAIL);
        }
    }

    @Nested
    @DisplayName("FR-01/FR-02: Đăng nhập sai -> INVALID_CREDENTIALS, không lộ trường sai")
    class LoginInvalid {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Email rỗng/null/blank -> INVALID_CREDENTIALS, không gọi DB")
        void login_invalid_whenEmailBlank(String blankEmail) {
            LoginResult result = authService.login(blankEmail, RAW_PASSWORD);

            assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
            assertNull(result.getStaff());
            verifyNoInteractions(staffDAO);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Password rỗng/null/blank -> INVALID_CREDENTIALS, không gọi DB")
        void login_invalid_whenPasswordBlank(String blankPassword) {
            LoginResult result = authService.login(EMAIL, blankPassword);

            assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
            verifyNoInteractions(staffDAO);
        }

        @Test
        @DisplayName("Email không tồn tại trong CSDL -> INVALID_CREDENTIALS (giống hệt trường hợp sai password)")
        void login_invalid_whenEmailNotFound() {
            when(staffDAO.findByEmail("khong-ton-tai@fpt.edu.vn")).thenReturn(null);

            LoginResult result = authService.login("khong-ton-tai@fpt.edu.vn", RAW_PASSWORD);

            assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
            assertNull(result.getStaff());
        }

        @Test
        @DisplayName("Email đúng nhưng password sai (chưa đủ 5 lần) -> INVALID_CREDENTIALS, tăng bộ đếm")
        void login_invalid_whenPasswordWrong_incrementsAttempts() {
            Staff staff = buildActiveStaff(1, null); // đã sai 1 lần trước đó
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login(EMAIL, "sai-mat-khau");

            assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
            assertNull(result.getStaff());
            // lần sai thứ 2, chưa đến ngưỡng khoá (5) nên chỉ update attempts
            verify(staffDAO).updateLoginAttempts(1, 2);
            verify(staffDAO, never()).lockAccount(anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("FR-03: Khoá tài khoản sau 5 lần đăng nhập sai liên tiếp")
    class AccountLocking {

        @Test
        @DisplayName("Lần sai thứ 5 liên tiếp -> khoá tài khoản 5 phút, trả ACCOUNT_LOCKED")
        void login_locksAccount_onFifthFailedAttempt() {
            Staff staff = buildActiveStaff(4, null); // đã sai 4 lần, lần này là lần thứ 5
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login(EMAIL, "sai-mat-khau");

            assertEquals(LoginStatus.ACCOUNT_LOCKED, result.getStatus());
            assertEquals(AuthService.LOCK_MINUTES, result.getLockMinutesRemaining());
            verify(staffDAO).lockAccount(1, AuthService.LOCK_MINUTES);
            verify(staffDAO, never()).updateLoginAttempts(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Tài khoản đang trong thời gian khoá -> ACCOUNT_LOCKED dù nhập đúng mật khẩu, KHÔNG kiểm tra password")
        void login_returnsLocked_whenAccountCurrentlyLocked() {
            Timestamp futureLock = new Timestamp(System.currentTimeMillis() + 3 * 60 * 1000); // còn khoá 3 phút
            Staff staff = buildActiveStaff(5, futureLock);
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login(EMAIL, RAW_PASSWORD); // password ĐÚNG

            assertEquals(LoginStatus.ACCOUNT_LOCKED, result.getStatus());
            assertTrue(result.getLockMinutesRemaining() >= 1);
            // Quan trọng: không được gọi DAO cập nhật/khoá lại, và không cho qua dù pass đúng
            verify(staffDAO, never()).updateLoginAttempts(anyInt(), anyInt());
            verify(staffDAO, never()).lockAccount(anyInt(), anyInt());
            verify(staffDAO, never()).resetLoginAttempts(anyInt());
        }

        @Test
        @DisplayName("LockedUntil đã ở quá khứ (hết hạn khoá) -> cho đăng nhập lại bình thường")
        void login_allowsLogin_whenLockExpired() {
            Timestamp pastLock = new Timestamp(System.currentTimeMillis() - 60 * 1000); // đã hết hạn khoá 1 phút trước
            Staff staff = buildActiveStaff(5, pastLock);
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login(EMAIL, RAW_PASSWORD);

            assertEquals(LoginStatus.SUCCESS, result.getStatus());
            // Goi 2 lan: 1 lan do lock da het han (reset bo dem truoc khi kiem tra password),
            // 1 lan do dang nhap thanh cong.
            verify(staffDAO, times(2)).resetLoginAttempts(1);
        }

        @Test
        @DisplayName("Lock đã hết hạn -> bộ đếm được reset về 0 TRƯỚC khi kiểm tra mật khẩu")
        void login_resetsAttemptCounter_whenLockExpired_beforePasswordCheck() {
            Timestamp pastLock = new Timestamp(System.currentTimeMillis() - 60 * 1000);
            Staff staff = buildActiveStaff(4, pastLock); // đã sai 4 lần trước khi bị khoá
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            LoginResult result = authService.login(EMAIL, "sai-mat-khau"); // sai đúng 1 lần sau khi hết khoá

            assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
            // Quan trọng: KHÔNG bị khoá lại ngay, vì bộ đếm đã được reset về 0 trước đó
            // -> lần sai này chỉ là lần thứ 1, không phải lần thứ 5.
            verify(staffDAO, never()).lockAccount(anyInt(), anyInt());
            verify(staffDAO).updateLoginAttempts(1, 1);
        }

        @Test
        @DisplayName("Lock chưa hết hạn thì KHÔNG reset bộ đếm (giữ nguyên hành vi khoá)")
        void login_doesNotResetAttemptCounter_whenStillLocked() {
            Timestamp futureLock = new Timestamp(System.currentTimeMillis() + 3 * 60 * 1000);
            Staff staff = buildActiveStaff(5, futureLock);
            when(staffDAO.findByEmail(EMAIL)).thenReturn(staff);

            authService.login(EMAIL, "sai-mat-khau");

            verify(staffDAO, never()).resetLoginAttempts(anyInt());
        }
    }
}