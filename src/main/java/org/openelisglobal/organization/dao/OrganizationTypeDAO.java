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
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*
* Contributor(s): CIRG, University of Washington, Seattle WA.
*/
package org.openelisglobal.organization.dao;

import java.util.List;

import org.openelisglobal.common.dao.BaseDAO;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.organization.valueholder.Organization;
import org.openelisglobal.organization.valueholder.OrganizationType;

public interface OrganizationTypeDAO extends BaseDAO<OrganizationType, String> {

    public List<OrganizationType> getAllOrganizationTypes() throws LIMSRuntimeException;

    /**
     * @param name the
     * @return
     * @throws LIMSRuntimeException
     */
    public OrganizationType getOrganizationTypeByName(String name) throws LIMSRuntimeException;

    /**
     * Find the organizations which belong to a certain organization type.
     *
     * @param names name of organization type
     * @return a list (possibly empty) of all the organizations associated with the
     *         type.
     * @throws LIMSRuntimeException
     */
    public List<Organization> getOrganizationsByTypeName(String orderByCol, String... names)
            throws LIMSRuntimeException;
}
