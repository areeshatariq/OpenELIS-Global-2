package org.openelisglobal.testanalyte.service;

import java.util.List;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.testanalyte.dao.TestAnalyteDAO;
import org.openelisglobal.testanalyte.valueholder.TestAnalyte;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestAnalyteServiceImpl extends BaseObjectServiceImpl<TestAnalyte, String> implements TestAnalyteService {
    @Autowired
    protected TestAnalyteDAO baseObjectDAO;

    TestAnalyteServiceImpl() {
        super(TestAnalyte.class);
    }

    @Override
    protected TestAnalyteDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public TestAnalyte getData(TestAnalyte testAnalyte) {
        return getBaseObjectDAO().getData(testAnalyte);
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllTestAnalytes() {
        return getBaseObjectDAO().getAllTestAnalytes();
    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfTestAnalytes(int startingRecNo) {
        return getBaseObjectDAO().getPageOfTestAnalytes(startingRecNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextTestAnalyteRecord(String id) {
        return getBaseObjectDAO().getNextTestAnalyteRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousTestAnalyteRecord(String id) {
        return getBaseObjectDAO().getPreviousTestAnalyteRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TestAnalyte getTestAnalyteById(TestAnalyte testAnalyte) {
        return getBaseObjectDAO().getTestAnalyteById(testAnalyte);
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllTestAnalytesPerTest(Test test) {
        return getBaseObjectDAO().getAllTestAnalytesPerTest(test);
    }

    @Override
    @Transactional(readOnly = true)
    public List getTestAnalytes(String filter) {
        return getBaseObjectDAO().getTestAnalytes(filter);
    }
}
