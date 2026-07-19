/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.integration;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author admin
 */
public class StaffDAOIntegrationTest {

    private StaffDAO staffDAO;
    private Staff testStaff;
    private int createdStaffId;

    @BeforeEach
    void setUp() {
        staffDAO = new StaffDAO();
        testStaff = new Staff();
        testStaff.setFullName("JUnit Test");
        testStaff.setGender(true);
        testStaff.setPhoneNumber(
                "09" + System.currentTimeMillis() % 100000000);
        testStaff.setEmail(
                "junit"
                + System.currentTimeMillis()
                + "@gmail.com");
        testStaff.setPassword("123");
        testStaff.setIsActive(true);
        Role role = new Role();
        role.setRoleID(1);
        testStaff.setRole(role);
        staffDAO.createStaff(testStaff);
        List<Staff> list = staffDAO.getStaffByFilter(testStaff.getFullName(), "");
        assertFalse(list.isEmpty());
        createdStaffId = list.get(list.size() - 1).getStaffID();

    }

    @AfterEach
    void tearDown() {
        if (createdStaffId > 0) {
            staffDAO.deleteStaff(createdStaffId);
        }
    }

    private Staff createNewStaff() {
        Staff staff = new Staff();
        staff.setFullName("JUnit Create");
        staff.setGender(true);
        staff.setPhoneNumber(
                "09" + System.nanoTime() % 100000000);
        staff.setEmail(
                "create"
                + System.nanoTime()
                + "@gmail.com");
        staff.setPassword("123");
        staff.setIsActive(true);
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
    void testGetStaffByFilter() {
        List<Staff> list = staffDAO.getStaffByFilter("JUnit Test", "");
        assertEquals(1, list.size());
        assertEquals("JUnit Test", list.get(0).getFullName());
    }

    @Test
    void testGetStaffById() {
        Staff staff = staffDAO.getStaffById(createdStaffId);
        assertNotNull(staff);
        assertEquals(createdStaffId, staff.getStaffID());

    }

    @Test
    void testCreateStaff() {
        Staff staff = createNewStaff();
        staffDAO.createStaff(staff);
        List<Staff> result = staffDAO.getStaffByFilter("JUnit Test", "");
        assertFalse(result.isEmpty());
    }

    @Test
    void testUpdateStaff() {
        Staff staff = staffDAO.getStaffById(createdStaffId);
        staff.setFullName("JUnit Updated");
        staffDAO.updateStaff(staff);
        Staff updated = staffDAO.getStaffById(createdStaffId);
        assertEquals("JUnit Updated", updated.getFullName());
        assertEquals(createdStaffId,updated.getStaffID());
    }

    @Test
    void testDeleteStaff() {
        staffDAO.deleteStaff(createdStaffId);
        Staff deleted = staffDAO.getStaffById(createdStaffId);
        assertNull(deleted);
        createdStaffId = 0;
    }
}
