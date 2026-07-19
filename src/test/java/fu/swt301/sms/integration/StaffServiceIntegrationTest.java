/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.integration;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;

import java.util.List;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author admin
 */
public class StaffServiceIntegrationTest {

    private StaffService staffService;
    private Staff testStaff;
    private int createdStaffId;

    @BeforeEach
    void setUp() {
        staffService = new StaffService();
        testStaff = new Staff();
        testStaff.setFullName("Service Integration");
        testStaff.setGender(true);
        testStaff.setPhoneNumber("09" + System.nanoTime() % 100000000);
        testStaff.setEmail(
                "service"
                + System.nanoTime()
                + "@gmail.com");
        testStaff.setPassword("123456");
        testStaff.setIsActive(true);
        Role role = new Role();
        role.setRoleID(1);
        testStaff.setRole(role);
        assertTrue(staffService.createStaff(testStaff));
        List<Staff> list = staffService.getStaffList(
                "Service Integration",
                "");
        assertFalse(list.isEmpty());

        createdStaffId = list.stream()
                .filter(s -> s.getEmail().equals(testStaff.getEmail()))
                .findFirst()
                .get()
                .getStaffID();
    }

    @AfterEach
    void tearDown() {
        if (createdStaffId > 0) {
            staffService.deleteStaff(createdStaffId);
        }
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    void testGetStaffList() {
        List<Staff> list = staffService.getStaffList("Service Integration", "");
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    void testGetStaffById() {
        Staff staff = staffService.getStaffById(createdStaffId);
        assertNotNull(staff);
        assertEquals(createdStaffId, staff.getStaffID());
    }

    @Test
    void testUpdateStaff() {
        Staff staff = staffService.getStaffById(createdStaffId);
        staff.setFullName("Updated By Service");
        boolean result = staffService.updateStaff(staff);
        assertTrue(result);
        Staff updated = staffService.getStaffById(createdStaffId);
        assertEquals("Updated By Service", updated.getFullName());
    }

    @Test
    void testDeleteStaff() {
        boolean result = staffService.deleteStaff(createdStaffId);
        assertTrue(result);
        Staff deleted = staffService.getStaffById(createdStaffId);
        assertNull(deleted);
        createdStaffId = 0;
    }

    @Test
    void testEmailExists() {
        assertTrue(staffService.isEmailExists(testStaff.getEmail(), 0));
    }

    @Test
    void testPhoneExists() {
        assertTrue(staffService.isPhoneExists(testStaff.getPhoneNumber(), 0));
    }

    @Test
    void testFullNameExists() {
        assertTrue(staffService.isFullNameExists(testStaff.getFullName(), 0));

    }
}
