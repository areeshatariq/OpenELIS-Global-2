package org.openelisglobal.history.service;

import java.util.List;

import org.openelisglobal.audittrail.dao.HistoryDAO;
import org.openelisglobal.audittrail.valueholder.History;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoryServiceImpl extends BaseObjectServiceImpl<History, String> implements HistoryService {
    @Autowired
    protected HistoryDAO baseObjectDAO;

    HistoryServiceImpl() {
        super(History.class);
        disableLogging();
    }

    @Override
    protected HistoryDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List getHistoryByRefIdAndRefTableId(History history) throws LIMSRuntimeException {
        return baseObjectDAO.getHistoryByRefIdAndRefTableId(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List getHistoryByRefIdAndRefTableId(String id, String table) throws LIMSRuntimeException {
        return baseObjectDAO.getHistoryByRefIdAndRefTableId(id, table);
    }

    @Override
    public String insert(History history) {
        return baseObjectDAO.insert(history);
    }

    @Override
    public History update(History history) {
        return baseObjectDAO.update(history);
    }

    @Override
    public void delete(History history) {
        baseObjectDAO.delete(history);
    }
}
