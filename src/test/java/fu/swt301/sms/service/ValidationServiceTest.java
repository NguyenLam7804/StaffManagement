/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package fu.swt301.sms.service;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author admin
 */
public class ValidationServiceTest {

    private StaffService staffService;
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        staffService = mock(StaffService.class);
        validationService = new ValidationService(staffService);
    }

    private Staff createValidStaff() {
        Staff staff = new Staff();

        staff.setStaffID(1);
        staff.setFullName("Nguyen Van A");
        staff.setEmail("a@gmail.com");
        staff.setPhoneNumber("0123456789");

        Role role = new Role();
        role.setRoleID(1);

        staff.setRole(role);

        return staff;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    void testValidateCreateStaff_Success() {
        Staff staff = createValidStaff();
        when(staffService.isEmailExists(anyString(), anyInt()))
                .thenReturn(false);
        when(staffService.isPhoneExists(anyString(), anyInt()))
                .thenReturn(false);
        when(staffService.isFullNameExists(anyString(), anyInt()))
                .thenReturn(false);
        String result = validationService.validateCreateStaff(staff);
        assertNull(result);
    }

    @Test
    void testInvalidEmailFormat() {
        Staff staff = createValidStaff();
        staff.setEmail("abc");
        String result = validationService.validateCreateStaff(staff);
        assertEquals("Invalid email format.", result);

    }

    @Test
    void testEmailAlreadyExists() {
        Staff staff = createValidStaff();
        when(staffService.isEmailExists(anyString(), anyInt()))
                .thenReturn(true);
        String result = validationService.validateCreateStaff(staff);

        assertEquals(
                "Email already exists. Please choose another one.",
                result);
    }

    @Test
    void testInvalidPhone() {
        Staff staff = createValidStaff();
        staff.setPhoneNumber("123");
        String result = validationService.validateCreateStaff(staff);
        assertEquals(
                "Phone number must contain exactly 10 digits.",
                result);
    }

    @Test
    void testPhoneAlreadyExists() {
        Staff staff = createValidStaff();
        when(staffService.isEmailExists(anyString(), anyInt()))
                .thenReturn(false);
        when(staffService.isPhoneExists(anyString(), anyInt()))
                .thenReturn(true);
        String result = validationService.validateCreateStaff(staff);
        assertEquals(
                "Phone number already exists. Please choose another one.",
                result);
    }

    @Test
    void testEmptyFullName() {
        Staff staff = createValidStaff();
        staff.setFullName("");
        String result = validationService.validateCreateStaff(staff);
        assertEquals("Full name is required.", result);
    }

    @Test
    void testFullNameAlreadyExists() {
        Staff staff = createValidStaff();
        when(staffService.isEmailExists(anyString(), anyInt()))
                .thenReturn(false);
        when(staffService.isPhoneExists(anyString(), anyInt()))
                .thenReturn(false);
        when(staffService.isFullNameExists(anyString(), anyInt()))
                .thenReturn(true);
        String result = validationService.validateCreateStaff(staff);
        assertEquals(
                "Full name already exists. Please choose another one.",
                result);

    }

    @Test
    void testRoleIsNull() {
        Staff staff = createValidStaff();
        staff.setRole(null);
        String result = validationService.validateCreateStaff(staff);
        assertEquals("Role is required.", result);
    }
}
