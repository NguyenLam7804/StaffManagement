/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class StaffService {
    private static final Logger LOGGER =
            Logger.getLogger(StaffService.class.getName());

    private final StaffDAO staffDAO;

    public StaffService() {
        this.staffDAO = new StaffDAO();
    }

    public StaffService(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
    }

    public List<Staff> getStaffList(String name, String status) {
        try {   
            return staffDAO.getStaffByFilter(name, status);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot load staff list.", ex);
            return Collections.emptyList();
        }
    }

    public Staff getStaffById(int id) {
        return staffDAO.getStaffById(id);
    }

    public boolean createStaff(Staff staff) {
        try {
            staffDAO.createStaff(staff);
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot create staff.", ex);
            return false;
        }
    }

    public boolean updateStaff(Staff staff) {
        try {
            staffDAO.updateStaff(staff);
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot update staff.", ex);
            return false;
        }
    }

    public boolean deleteStaff(int id) {
        try {
            staffDAO.deleteStaff(id);
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot delete staff.", ex);
            return false;
        }
    }

    public boolean isEmailExists(String email, int currentId) {
        try {
            return staffDAO.isEmailExists(email, currentId);
        } catch (SQLException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Email validation failed.", ex);
            return true;
        }
    }

    public boolean isPhoneExists(String phone, int currentId) {
        try {
            return staffDAO.isPhoneNumberExists(phone, currentId);
        } catch (SQLException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Phone validation failed.", ex);
            return true;
        }
    }

    public boolean isFullNameExists(String fullName, int currentId) {
        try {
            return staffDAO.isFullNameExists(fullName, currentId);
        } catch (SQLException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Name validation failed.", ex);
            return true;
        }
    }
}
