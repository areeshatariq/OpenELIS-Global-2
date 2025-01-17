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
package org.openelisglobal.typeofsample.daoimpl;

import java.util.List;
import java.util.Vector;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.typeofsample.dao.TypeOfSamplePanelDAO;
import org.openelisglobal.typeofsample.valueholder.TypeOfSamplePanel;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TypeOfSamplePanelDAOImpl extends BaseDAOImpl<TypeOfSamplePanel, String> implements TypeOfSamplePanelDAO {

    public TypeOfSamplePanelDAOImpl() {
        super(TypeOfSamplePanel.class);
    }

//	@Override
//	public void deleteData(String[] typeOfSamplesPanelIDs, String currentUserId) throws LIMSRuntimeException {
//
//		try {
//
//			for (String id : typeOfSamplesPanelIDs) {
//				TypeOfSamplePanel data = readTypeOfSamplePanel(id);
//
//				auditDAO.saveHistory(new TypeOfSamplePanel(), data, currentUserId, IActionConstants.AUDIT_TRAIL_DELETE,
//						"SAMPLETYPE_PANEL");
//				entityManager.unwrap(Session.class).delete(data);
//				// entityManager.unwrap(Session.class).flush(); // CSL remove old
//				// entityManager.unwrap(Session.class).clear(); // CSL remove old
//			}
//
//		} catch (Exception e) {
//			LogEvent.logError("TypeOfSampleDAOImpl", "deleteData()", e.toString());
//			throw new LIMSRuntimeException("Error in TypeOfSampleTest deleteData()", e);
//		}
//	}

//	@Override
//	public boolean insertData(TypeOfSamplePanel typeOfSamplePanel) throws LIMSRuntimeException {
//
//		try {
//			String id = (String) entityManager.unwrap(Session.class).save(typeOfSamplePanel);
//
//			typeOfSamplePanel.setId(id);
//
//			auditDAO.saveNewHistory(typeOfSamplePanel, typeOfSamplePanel.getSysUserId(), "SAMPLETYPE_PANEL");
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//		} catch (Exception e) {
//			LogEvent.logError("TypeOfSamplePanelDAOImpl", "insertData()", e.toString());
//			throw new LIMSRuntimeException("Error in TypeOfSamplePanel insertData()", e);
//		}
//
//		return true;
//	}

    @Override
    @Transactional(readOnly = true)
    public void getData(TypeOfSamplePanel typeOfSamplePanel) throws LIMSRuntimeException {

        try {
            TypeOfSamplePanel tos = entityManager.unwrap(Session.class).get(TypeOfSamplePanel.class,
                    typeOfSamplePanel.getId());
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
            if (tos != null) {
                PropertyUtils.copyProperties(typeOfSamplePanel, tos);
            } else {
                typeOfSamplePanel.setId(null);
            }
        } catch (Exception e) {
            LogEvent.logError("TypeOfSamplePanelDAOImpl", "getData()", e.toString());
            throw new LIMSRuntimeException("Error in TypeOfSamplePanel getData()", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllTypeOfSamplePanels() throws LIMSRuntimeException {

        List list = new Vector();
        try {
            String sql = "from TypeOfSamplePanel";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            // query.setMaxResults(10);
            // query.setFirstResult(3);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            // bugzilla 2154
            LogEvent.logError("TypeOfSamplePanelDAOImpl", "getAllTypeOfSamples()", e.toString());
            throw new LIMSRuntimeException("Error in TypeOfSamplePanel getAllTypeOfSamplePanels()", e);
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfTypeOfSamplePanel(int startingRecNo) throws LIMSRuntimeException {

        List list = new Vector();
        try {
            // calculate maxRow to be one more than the page size
            int endingRecNo = startingRecNo + DEFAULT_PAGE_SIZE + 1;

            String sql = "from TypeOfSamplePanel t order by t.typeOfSampleId, t.panelId";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setFirstResult(startingRecNo - 1);
            query.setMaxResults(endingRecNo - 1);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("TypeOfSamplePanelDAOImpl", "getPageOfTypeOfSamplePanels()", e.toString());
            throw new LIMSRuntimeException("Error in TypeOfSamplePanel getPageOfTypeOfSamples()", e);
        }

        return list;
    }

    public TypeOfSamplePanel readTypeOfSamplePanel(String idString) {
        TypeOfSamplePanel tos = null;
        try {
            tos = entityManager.unwrap(Session.class).get(TypeOfSamplePanel.class, idString);
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            // bugzilla 2154
            LogEvent.logError("TypeOfSamplePanelDAOImpl", "readTypeOfSample()", e.toString());
            throw new LIMSRuntimeException("Error in TypeOfSamplePanel readTypeOfSample()", e);
        }

        return tos;
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextTypeOfSamplePanelRecord(String id) throws LIMSRuntimeException {

        return getNextRecord(id, "TypeOfSamplePanel", TypeOfSamplePanel.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousTypeOfSamplePanelRecord(String id) throws LIMSRuntimeException {

        return getPreviousRecord(id, "TypeOfSamplePanel", TypeOfSampleTest.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalTypeOfSamplePanelCount() throws LIMSRuntimeException {
        return getTotalCount("TypeOfSamplePanel", TypeOfSamplePanel.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextRecord(String id, String table, Class clazz) throws LIMSRuntimeException {
        int currentId = (Integer.valueOf(id)).intValue();
        String tablePrefix = getTablePrefix(table);

        List list = new Vector();
        int rrn = 0;
        try {
            // bugzilla 1908 cannot use named query for postgres because of
            // oracle ROWNUM
            // instead get the list in this sortorder and determine the index of
            // record with id = currentId
            String sql = "select tos.id from TypeOfSampleTest tos " + " order by tos.domain, tos.description";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
            rrn = list.indexOf(String.valueOf(currentId));

            list = entityManager.unwrap(Session.class).getNamedQuery(tablePrefix + "getNext").setFirstResult(rrn + 1)
                    .setMaxResults(2).list();

        } catch (Exception e) {
            LogEvent.logError("TypeOfSampleDAOImpl", "getNextRecord()", e.toString());
            throw new LIMSRuntimeException("Error in getNextRecord() for " + table, e);
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousRecord(String id, String table, Class clazz) throws LIMSRuntimeException {
        int currentId = (Integer.valueOf(id)).intValue();
        String tablePrefix = getTablePrefix(table);

        List list = new Vector();

        int rrn = 0;
        try {
            // bugzilla 1908 cannot use named query for postgres because of
            // oracle ROWNUM
            // instead get the list in this sortorder and determine the index of
            // record with id = currentId
            String sql = "select tos.id from TypeOfSampleTest tos " + " order by tos.domain desc, tos.description desc";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
            rrn = list.indexOf(String.valueOf(currentId));

            list = entityManager.unwrap(Session.class).getNamedQuery(tablePrefix + "getPrevious")
                    .setFirstResult(rrn + 1).setMaxResults(2).list();

        } catch (Exception e) {
            LogEvent.logError("TypeOfSampleDAOImpl", "getPreviousRecord()", e.toString());
            throw new LIMSRuntimeException("Error in getPreviousRecord() for " + table, e);
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<TypeOfSamplePanel> getTypeOfSamplePanelsForSampleType(String sampleType) {
        List<TypeOfSamplePanel> list;

        String sql = "from TypeOfSamplePanel tp where tp.typeOfSampleId = :sampleId order by tp.panelId";

        try {
            if (sampleType.equals("null")) {
                // so parseInt doesn't throw
                sampleType = "0";
            }
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleId", Integer.parseInt(sampleType));
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("TypeOfSamplePanelDAOImpl", "getTypeOfSamplePanelsForSampleType", e.toString());
            throw new LIMSRuntimeException("Error in TypeOfSamplePanelDAOImpl getTypeOfSamplePanelsForSampleType", e);
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<TypeOfSamplePanel> getTypeOfSamplePanelsForPanel(String panelId) throws LIMSRuntimeException {
        String sql = "from TypeOfSamplePanel tosp where tosp.panelId = :panelId";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("panelId", Integer.parseInt(panelId));
            List<TypeOfSamplePanel> typeOfSamplePanels = query.list();
            // closeSession(); // CSL remove old
            return typeOfSamplePanels;
        } catch (HibernateException e) {
            handleException(e, "getTypeOfSamplePanelsForPanel");
        }

        return null;
    }

}