package org.openelisglobal.panelitem.service;

import java.util.List;

import org.openelisglobal.common.exception.LIMSDuplicateRecordException;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.panel.service.PanelService;
import org.openelisglobal.panel.valueholder.Panel;
import org.openelisglobal.panelitem.dao.PanelItemDAO;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.test.valueholder.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PanelItemServiceImpl extends BaseObjectServiceImpl<PanelItem, String> implements PanelItemService {
    @Autowired
    protected PanelItemDAO baseObjectDAO;
    @Autowired
    private PanelService panelService;

    PanelItemServiceImpl() {
        super(PanelItem.class);
    }

    @Override
    protected PanelItemDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PanelItem> getPanelItemsForPanel(String panelId) {
        return baseObjectDAO.getPanelItemsForPanel(panelId);
    }

    @Override
    @Transactional(readOnly = true)
    public void getData(PanelItem panelItem) {
        getBaseObjectDAO().getData(panelItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalPanelItemCount() {
        return getBaseObjectDAO().getTotalPanelItemCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PanelItem> getPanelItemsForPanelAndItemList(String panelId, List<Integer> testList) {
        return getBaseObjectDAO().getPanelItemsForPanelAndItemList(panelId, testList);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousPanelItemRecord(String id) {
        return getBaseObjectDAO().getPreviousPanelItemRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfPanelItems(int startingRecNo) {
        return getBaseObjectDAO().getPageOfPanelItems(startingRecNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextPanelItemRecord(String id) {
        return getBaseObjectDAO().getNextPanelItemRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean getDuplicateSortOrderForPanel(PanelItem panelItem) {
        return getBaseObjectDAO().getDuplicateSortOrderForPanel(panelItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PanelItem> getPanelItemByTestId(String id) {
        return getBaseObjectDAO().getPanelItemByTestId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllPanelItems() {
        return getBaseObjectDAO().getAllPanelItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List getPanelItems(String filter) {
        return getBaseObjectDAO().getPanelItems(filter);
    }

    @Override
    public String insert(PanelItem panelItem) {
        if (getBaseObjectDAO().duplicatePanelItemExists(panelItem)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + panelItem.getPanelName());
        }
        return super.insert(panelItem);
    }

    @Override
    public PanelItem save(PanelItem panelItem) {
        if (getBaseObjectDAO().duplicatePanelItemExists(panelItem)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + panelItem.getPanelName());
        }
        return super.save(panelItem);
    }

    @Override
    public PanelItem update(PanelItem panelItem) {
        if (getBaseObjectDAO().duplicatePanelItemExists(panelItem)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + panelItem.getPanelName());
        }
        return super.update(panelItem);
    }

    public PanelItem readPanelItem(String idString) {
        PanelItem pi;
        try {
            pi = get(idString);
        } catch (Exception e) {
            LogEvent.logError("PanelItemDAOImpl", "readPanelItem()", e.toString());
            throw new LIMSRuntimeException("Error in PanelItem readPanelItem()", e);
        }

        return pi;
    }

    @Override
    @Transactional
    public void updatePanelItems(List<PanelItem> panelItems, Panel panel, boolean updatePanel, String currentUser,
            List<Test> newTests) {

        for (PanelItem oldPanelItem : panelItems) {
            oldPanelItem.setSysUserId(currentUser);
        }
        deleteAll(panelItems);

        for (Test test : newTests) {
            PanelItem panelItem = new PanelItem();
            panelItem.setPanel(panel);
            panelItem.setTest(test);
            panelItem.setLastupdatedFields();
            panelItem.setSysUserId(currentUser);
            insert(panelItem);
        }

        if ("N".equals(panel.getIsActive())) {
            panel.setIsActive("Y");
            panel.setSysUserId(currentUser);
            panelService.update(panel);
        } else {
            panel.setIsActive("N");
            panel.setSysUserId(currentUser);
            panelService.update(panel);
        }
    }
}
