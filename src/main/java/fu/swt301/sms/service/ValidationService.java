/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;

import java.util.regex.Pattern;

/**
 *
 * @author admin
 */
public class ValidationService {

    private final StaffService staffService;

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN
            = Pattern.compile("^0\\d{9}$");

    public ValidationService() {
        this.staffService = new StaffService();
    }

    public ValidationService(StaffService staffService) {
        this.staffService = staffService;
    }

    public String validateCreateStaff(Staff staff) {
        String error = validateCommon(staff);
        if (error != null) {
            return error;
        }
        return validateDuplicate(staff);
    }

    public String validateUpdateStaff(Staff staff) {
        String error = validateCommon(staff);
        if (error != null) {
            return error;
        }
        return validateDuplicate(staff);
    }

    private String validateCommon(Staff staff) {

        if (staff.getFullName() == null
                || staff.getFullName().trim().isEmpty()) {
            return "Full name is required.";
        }
        if (staff.getEmail() == null
                || !EMAIL_PATTERN.matcher(staff.getEmail()).matches()) {
            return "Invalid email format.";
        }
        if (staff.getPhoneNumber() == null
                || !PHONE_PATTERN.matcher(staff.getPhoneNumber()).matches()) {
            return "Phone number must contain exactly 10 digits.";
        }
        if (staff.getRole() == null) {
            return "Role is required.";
        }
        return null;
    }

    private String validateDuplicate(Staff staff) {
        int currentId = staff.getStaffID();
        if (staffService.isEmailExists(staff.getEmail(), currentId)) {
            return "Email already exists. Please choose another one.";
        }
        if (staffService.isPhoneExists(staff.getPhoneNumber(), currentId)) {
            return "Phone number already exists. Please choose another one.";
        }
        if (staffService.isFullNameExists(staff.getFullName(), currentId)) {
            return "Full name already exists. Please choose another one.";
        }
        return null;
    }
}
