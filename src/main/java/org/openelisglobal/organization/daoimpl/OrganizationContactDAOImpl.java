/**
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
*
* The Original Code is OpenELIS code.
*
* Copyright (C) CIRG, University of Washington, Seattle WA.  All Rights Reserved.
*
*/
package org.openelisglobal.organization.daoimpl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.organization.dao.OrganizationContactDAO;
import org.openelisglobal.organization.valueholder.OrganizationContact;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class OrganizationContactDAOImpl extends BaseDAOImpl<OrganizationContact, String>
        implements OrganizationContactDAO {

    public OrganizationContactDAOImpl() {
        super(OrganizationContact.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<OrganizationContact> getListForOrganizationId(String orgId) throws LIMSRuntimeException {
        String sql = "From OrganizationContact oc where oc.organizationId = :orgId";
        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("orgId", Integer.parseInt(orgId));
            List<OrganizationContact> contactList = query.list();
            // closeSession(); // CSL remove old
            return contactList;
        } catch (HibernateException e) {
            handleException(e, "getListForOrganizationId");
        }

        return null;
    }

}
