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
package org.openelisglobal.resultlimits.daoimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.Session;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.util.SystemConfiguration;
import org.openelisglobal.resultlimits.dao.ResultLimitDAO;
import org.openelisglobal.resultlimits.valueholder.ResultLimit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ResultLimitDAOImpl extends BaseDAOImpl<ResultLimit, String> implements ResultLimitDAO {

    public ResultLimitDAOImpl() {
        super(ResultLimit.class);
    }

//	@Override
//	public void deleteData(List resultLimits) throws LIMSRuntimeException {
//		// add to audit trail
//		try {
//
//			for (Object limitObj : resultLimits) {
//				ResultLimit data = (ResultLimit) limitObj;
//
//				ResultLimit oldData = readResultLimit(data.getId());
//
//				String sysUserId = data.getSysUserId();
//				String event = IActionConstants.AUDIT_TRAIL_DELETE;
//				String tableName = "RESULT_LIMITS";
//				auditDAO.saveHistory(new ResultLimit(), oldData, sysUserId, event, tableName);
//			}
//		} catch (Exception e) {
//			LogEvent.logError("ResultLimitsDAOImpl", "AuditTrail deleteData()", e.toString());
//			throw new LIMSRuntimeException("Error in ResultLimit AuditTrail deleteData()", e);
//		}
//
//		try {
//			for (Object resultLimit : resultLimits) {
//				ResultLimit data = (ResultLimit) resultLimit;
//				data = readResultLimit(data.getId());
//				entityManager.unwrap(Session.class).delete(data);
//				// entityManager.unwrap(Session.class).flush(); // CSL remove old
//				// entityManager.unwrap(Session.class).clear(); // CSL remove old
//			}
//		} catch (Exception e) {
//			LogEvent.logError("ResultLimitsDAOImpl", "deleteData()", e.toString());
//			throw new LIMSRuntimeException("Error in ResultLimit deleteData()", e);
//		}
//	}
//
//	@Override
//	public boolean insertData(ResultLimit resultLimit) throws LIMSRuntimeException {
//
//		try {
//			String id = (String) entityManager.unwrap(Session.class).save(resultLimit);
//			resultLimit.setId(id);
//
//			String sysUserId = resultLimit.getSysUserId();
//			String tableName = "RESULT_LIMITS";
//			auditDAO.saveNewHistory(resultLimit, sysUserId, tableName);
//
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//
//		} catch (Exception e) {
//			LogEvent.logError("ResultLimitsDAOImpl", "insertData()", e.toString());
//			throw new LIMSRuntimeException("Error in ResultLimit insertData()", e);
//		}
//
//		return true;
//	}

//	@Override
//	public void updateData(ResultLimit resultLimit) throws LIMSRuntimeException {
//
//		ResultLimit oldData = readResultLimit(resultLimit.getId());
//
//		try {
//
//			String sysUserId = resultLimit.getSysUserId();
//			String event = IActionConstants.AUDIT_TRAIL_UPDATE;
//			String tableName = "RESULT_LIMITS";
//			auditDAO.saveHistory(resultLimit, oldData, sysUserId, event, tableName);
//		} catch (Exception e) {
//			LogEvent.logError("ResultLimitsDAOImpl", "AuditTrail updateData()", e.toString());
//			throw new LIMSRuntimeException("Error in ResultLimit AuditTrail updateData()", e);
//		}
//
//		try {
//			entityManager.unwrap(Session.class).merge(resultLimit);
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//			// entityManager.unwrap(Session.class).evict // CSL remove old(resultLimit);
//			// entityManager.unwrap(Session.class).refresh // CSL remove old(resultLimit);
//		} catch (Exception e) {
//			LogEvent.logError("ResultLimitsDAOImpl", "updateData()", e.toString());
//			throw new LIMSRuntimeException("Error in ResultLimit updateData()", e);
//		}
//	}

    @Override
    @Transactional(readOnly = true)
    public void getData(ResultLimit resultLimit) throws LIMSRuntimeException {
        try {
            ResultLimit tmpLimit = entityManager.unwrap(Session.class).get(ResultLimit.class, resultLimit.getId());
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
            if (tmpLimit != null) {
                PropertyUtils.copyProperties(resultLimit, tmpLimit);
            } else {
                resultLimit.setId(null);
            }
        } catch (Exception e) {
            LogEvent.logError("ResultLimitsDAOImpl", "getData()", e.toString());
            throw new LIMSRuntimeException("Error in ResultLimit getData()", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllResultLimits() throws LIMSRuntimeException {
        List list;
        try {
            String sql = "from ResultLimit";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("ResultLimitsDAOImpl", "getAllResultLimits()", e.toString());
            throw new LIMSRuntimeException("Error in ResultLimit getAllResultLimits()", e);
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfResultLimits(int startingRecNo) throws LIMSRuntimeException {
        List list;
        try {
            // calculate maxRow to be one more than the page size
            int endingRecNo = startingRecNo + (SystemConfiguration.getInstance().getDefaultPageSize() + 1);

            String sql = "from ResultLimit t order by t.id";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setFirstResult(startingRecNo - 1);
            query.setMaxResults(endingRecNo - 1);

            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("ResultLimitsDAOImpl", "getPageOfResultLimits()", e.toString());
            throw new LIMSRuntimeException("Error in ResultLimit getPageOfResultLimits()", e);
        }

        return list;
    }

    public ResultLimit readResultLimit(String idString) {
        ResultLimit recoveredLimit;
        try {
            recoveredLimit = entityManager.unwrap(Session.class).get(ResultLimit.class, idString);
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("ResultLimitDAOImpl", "readResultLimit()", e.toString());
            throw new LIMSRuntimeException("Error in ResultLimit readResultLimit()", e);
        }

        return recoveredLimit;
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextResultLimitRecord(String id) throws LIMSRuntimeException {
        return getNextRecord(id, "ResultLimit", ResultLimit.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousResultLimitRecord(String id) throws LIMSRuntimeException {
        return getPreviousRecord(id, "ResultLimit", ResultLimit.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<ResultLimit> getAllResultLimitsForTest(String testId) throws LIMSRuntimeException {

        if (GenericValidator.isBlankOrNull(testId)) {
            return new ArrayList<>();
        }

        try {
            String sql = "from ResultLimit rl where rl.testId = :test_id";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("test_id", Integer.parseInt(testId));

            List<ResultLimit> list = query.list();
            // closeSession(); // CSL remove old
            return list;
        } catch (Exception e) {
            handleException(e, "getAllResultLimitsForTest");
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultLimit getResultLimitById(String resultLimitId) throws LIMSRuntimeException {
        try {
            ResultLimit resultLimit = entityManager.unwrap(Session.class).get(ResultLimit.class, resultLimitId);
            // closeSession(); // CSL remove old
            return resultLimit;
        } catch (Exception e) {
            handleException(e, "getResultLimitById");
        }

        return null;
    }
}