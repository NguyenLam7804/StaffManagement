/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author admin
 */
public class StaffServiceTest {

    private StaffDAO staffDAO;
    private StaffService staffService;

    @BeforeEach
    void setUp() {
        staffDAO = mock(StaffDAO.class);
        staffService = new StaffService(staffDAO);
    }

    private Staff createStaff() {
        Staff staff = new Staff();
        staff.setStaffID(1);
        staff.setFullName("Nguyen Van A");
        staff.setEmail("a@gmail.com");
        staff.setPhoneNumber("0123456789");
        return staff;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    void testGetStaffListSuccess() {
        List<Staff> expected = new ArrayList<>();
        expected.add(createStaff());
        when(staffDAO.getStaffByFilter("", ""))
                .thenReturn(expected);
        List<Staff> result = staffService.getStaffList("", "");

        assertEquals(1, result.size());
        verify(staffDAO).getStaffByFilter("", "");
    }

    @Test
    void testGetStaffListException() {
        when(staffDAO.getStaffByFilter(anyString(), anyString()))
                .thenThrow(new RuntimeException());
        List<Staff> result = staffService.getStaffList("", "");
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateStaffSuccess() {
        Staff staff = createStaff();
        doNothing().when(staffDAO).createStaff(staff);
        assertTrue(staffService.createStaff(staff));
        verify(staffDAO).createStaff(staff);
    }

    @Test
    void testCreateStaffFail() {
        Staff staff = createStaff();
        doThrow(new RuntimeException())
                .when(staffDAO)
                .createStaff(staff);
        assertFalse(staffService.createStaff(staff));
    }

    @Test
    void testUpdateStaffSuccess() {
        Staff staff = createStaff();
        doNothing().when(staffDAO).updateStaff(staff);
        assertTrue(staffService.updateStaff(staff));
    }

    @Test
    void testDeleteStaffSuccess() {
        doNothing().when(staffDAO).deleteStaff(1);
        assertTrue(staffService.deleteStaff(1));
    }

    @Test
    void testEmailExists() throws Exception {
        when(staffDAO.isEmailExists("a@gmail.com", 1))
                .thenReturn(true);

        assertTrue(staffService.isEmailExists("a@gmail.com", 1)
        );
    }

    @Test
    void testEmailExistsSQLException() throws Exception {
        when(staffDAO.isEmailExists(anyString(), anyInt()))
                .thenThrow(new SQLException());

        assertTrue(staffService.isEmailExists("a@gmail.com", 1)
        );
    }

    @Test
    void testPhoneExists() throws Exception {
        when(staffDAO.isPhoneNumberExists("0123456789", 1))
                .thenReturn(true);
        assertTrue(staffService.isPhoneExists("0123456789", 1)
        );
    }

    @Test
    void testFullNameExists() throws Exception {
        when(staffDAO.isFullNameExists("Nguyen Van A", 1))
                .thenReturn(true);

        assertTrue(staffService.isFullNameExists("Nguyen Van A", 1)
        );
    }

    @Test
    void testGetStaffById() {
        Staff expected = createStaff();
        when(staffDAO.getStaffById(1))
                .thenReturn(expected);
        Staff result = staffService.getStaffById(1);
        assertNotNull(result);
        assertEquals(1, result.getStaffID());
        verify(staffDAO).getStaffById(1);
    }

    @Test
    void testUpdateStaffException() {
        Staff staff = createStaff();
        doThrow(new RuntimeException())
                .when(staffDAO)
                .updateStaff(staff);

        assertFalse(staffService.updateStaff(staff));

    }

    @Test
    void testDeleteStaffException() {
        doThrow(new RuntimeException())
                .when(staffDAO)
                .deleteStaff(1);

        assertFalse(staffService.deleteStaff(1));
    }

    @Test
    void testPhoneExistsSQLException()
            throws Exception {
        when(staffDAO.isPhoneNumberExists(anyString(), anyInt()))
                .thenThrow(new SQLException());

        assertTrue(staffService.isPhoneExists("0123456789", 1)
        );

    }

    @Test
    void testFullNameExistsSQLException()
            throws Exception {
        when(staffDAO.isFullNameExists(anyString(), anyInt()))
                .thenThrow(new SQLException());

        assertTrue(staffService.isFullNameExists("Nguyen Van A", 1)
        );

    }

    @Test
    void testGetAllStaff() {
        List<Staff> list = new ArrayList<>();
        when(staffDAO.getStaffByFilter("", ""))
                .thenReturn(list);
        List<Staff> result = staffService.getAllStaff();
        assertNotNull(result);

    }

    @Test
    void testSearchStaff() {
        List<Staff> list = new ArrayList<>();
        when(staffDAO.getStaffByFilter("A", "true"))
                .thenReturn(list);
        List<Staff> result = staffService.searchStaff("A", "true");
        assertNotNull(result);
        verify(staffDAO).getStaffByFilter("A", "true");
    }
    
    
}
