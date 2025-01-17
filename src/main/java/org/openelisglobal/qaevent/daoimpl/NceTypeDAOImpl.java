package org.openelisglobal.qaevent.daoimpl;

import org.hibernate.Session;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.qaevent.dao.NceTypeDAO;
import org.openelisglobal.qaevent.valueholder.NceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class NceTypeDAOImpl extends BaseDAOImpl<NceType, String> implements NceTypeDAO {

    public NceTypeDAOImpl() {
        super(NceType.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllNceType() throws LIMSRuntimeException {
        List list;
        try {
            String sql = "from NceType nt order by nt.id";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old

        } catch (Exception e) {
            LogEvent.logError("NceTypeDAOImpl", "getAllNceType()", e.toString());
            throw new LIMSRuntimeException("Error in NceCategory getAllNceType()", e);
        }
        return list;
    }
}
