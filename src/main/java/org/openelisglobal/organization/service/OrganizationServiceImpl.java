package org.openelisglobal.organization.service;

import java.util.List;

import org.openelisglobal.common.action.IActionConstants;
import org.openelisglobal.common.exception.LIMSDuplicateRecordException;
import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.organization.dao.OrganizationDAO;
import org.openelisglobal.organization.dao.OrganizationOrganizationTypeDAO;
import org.openelisglobal.organization.valueholder.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationServiceImpl extends BaseObjectServiceImpl<Organization, String>
        implements OrganizationService {
    @Autowired
    protected OrganizationDAO baseObjectDAO;
    @Autowired
    private OrganizationOrganizationTypeDAO organizationOrganizationTypeDAO;

    OrganizationServiceImpl() {
        super(Organization.class);
    }

    @Override
    protected OrganizationDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getOrganizationByName(Organization organization, boolean ignoreCase) {
        return baseObjectDAO.getOrganizationByName(organization, ignoreCase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTypeIdsForOrganizationId(String id) {
        return organizationOrganizationTypeDAO.getTypeIdsForOrganizationId(id);
    }

    @Override
    @Transactional
    public void deleteAllLinksForOrganization(String id) {
        organizationOrganizationTypeDAO.deleteAllLinksForOrganization(id);
    }

    @Override
    @Transactional
    public void linkOrganizationAndType(Organization organization, String typeId) {
        organizationOrganizationTypeDAO.linkOrganizationAndType(organization, typeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getPagesOfSearchedOrganizations(int startingRecNo, String searchString) {
        return baseObjectDAO.getLikePage("organizationName", searchString, startingRecNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalSearchedOrganizationCount(String searchString) {
        return getCountLike("organizationName", searchString);
    }

    @Override
    @Transactional
    public void delete(Organization organization) {
        Organization oldObject = get(organization.getId());
        oldObject.setIsActive(IActionConstants.NO);
        oldObject.setSysUserId(organization.getSysUserId());
        updateDelete(oldObject);
    }

    @Override
    @Transactional(readOnly = true)
    public void getData(Organization organization) {
        getBaseObjectDAO().getData(organization);

    }

    @Override
    public String insert(Organization organization) {
        if (organization.getIsActive().equals(IActionConstants.YES)
                && getBaseObjectDAO().duplicateOrganizationExists(organization)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + organization.getOrganizationName());
        }
        return super.insert(organization);
    }

    @Override
    public Organization update(Organization organization) {
        if (organization.getIsActive().equals(IActionConstants.YES)
                && getBaseObjectDAO().duplicateOrganizationExists(organization)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + organization.getOrganizationName());
        }
        return super.update(organization);
    }

    @Override
    public Organization save(Organization organization) {
        if (organization.getIsActive().equals(IActionConstants.YES)
                && getBaseObjectDAO().duplicateOrganizationExists(organization)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + organization.getOrganizationName());
        }
        return super.save(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextOrganizationRecord(String id) {
        return getBaseObjectDAO().getNextOrganizationRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getOrganizationsByParentId(String parentId) {
        return getBaseObjectDAO().getOrganizationsByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getOrganizationsByTypeName(String orderByProperty, String[] typeName) {
        return getBaseObjectDAO().getOrganizationsByTypeName(orderByProperty, typeName);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalOrganizationCount() {
        return getBaseObjectDAO().getTotalOrganizationCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllOrganizations() {
        return getBaseObjectDAO().getAllOrganizations();
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousOrganizationRecord(String id) {
        return getBaseObjectDAO().getPreviousOrganizationRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getOrganizationById(String organizationId) {
        return getBaseObjectDAO().getOrganizationById(organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfOrganizations(int startingRecNo) {
        return getBaseObjectDAO().getPageOfOrganizations(startingRecNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List getOrganizations(String filter) {
        return getBaseObjectDAO().getOrganizations(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getOrganizationsByTypeNameAndLeadingChars(String partialName, String typeName) {
        return getBaseObjectDAO().getOrganizationsByTypeNameAndLeadingChars(partialName, typeName);
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getOrganizationByLocalAbbreviation(Organization organization, boolean ignoreCase) {
        return getBaseObjectDAO().getOrganizationByLocalAbbreviation(organization, ignoreCase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getOrganizationsByTypeName(String orderByProperty, String referralOrgType) {
        return getBaseObjectDAO().getOrganizationsByTypeName(orderByProperty, referralOrgType);
    }

}
