/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fu.swt301.sms.service;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.entity.Role;
import java.util.List;

/**
 *
 * @author admin
 */
public class RoleService {
    private final RoleDAO roleDAO = new RoleDAO();

    public List<Role> getAllRoles() {
        return roleDAO.getAllRoles();
    }
}
