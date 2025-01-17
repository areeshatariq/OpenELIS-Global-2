package org.openelisglobal.role.service;

import java.util.List;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.role.dao.RoleDAO;
import org.openelisglobal.role.valueholder.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl extends BaseObjectServiceImpl<Role, String> implements RoleService {
    @Autowired
    protected RoleDAO baseObjectDAO;

    RoleServiceImpl() {
        super(Role.class);
    }

    @Override
    protected RoleDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllActiveRoles() {
        return baseObjectDAO.getAllMatching("active", true);
    }

    @Override
    @Transactional(readOnly = true)
    public void getData(Role role) {
        getBaseObjectDAO().getData(role);

    }

    @Override
    @Transactional(readOnly = true)
    public List getNextRoleRecord(String id) {
        return getBaseObjectDAO().getNextRoleRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousRoleRecord(String id) {
        return getBaseObjectDAO().getPreviousRoleRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getReferencingRoles(Role role) {
        return getBaseObjectDAO().getReferencingRoles(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getPageOfRoles(int startingRecNo) {
        return getBaseObjectDAO().getPageOfRoles(startingRecNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return getBaseObjectDAO().getRoleByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return getBaseObjectDAO().getAllRoles();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(String roleId) {
        return getBaseObjectDAO().getRoleById(roleId);

    }
}
