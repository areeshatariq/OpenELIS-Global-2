/*
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
 * Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
 */
                                                                                                                                                          
package org.openelisglobal.testconfiguration.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import org.openelisglobal.common.action.BaseAction;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.util.validator.GenericValidator;
import org.openelisglobal.hibernate.HibernateUtil;
import org.openelisglobal.panel.daoimpl.PanelDAOImpl;
import org.openelisglobal.panel.valueholder.Panel;
import org.openelisglobal.panelitem.dao.PanelItemDAO;
import org.openelisglobal.panelitem.daoimpl.PanelItemDAOImpl;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.test.dao.TestDAO;
import org.openelisglobal.test.daoimpl.TestDAOImpl;


public class PanelTestAssignUpdate extends BaseAction {
    @Override
    protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
    	DynaValidatorForm dynaForm = (DynaValidatorForm)form;
        String panelId = dynaForm.getString("panelId");
        String currentUser = getSysUserId(request);
        boolean updatepanel = false;
        
 
        
        Panel panel = new PanelDAOImpl().getPanelById(panelId);
        
                                
        if (!GenericValidator.isBlankOrNull(panelId)) {
        	PanelItemDAO panelItemDAO = new PanelItemDAOImpl();
        	@SuppressWarnings("unchecked")
			List<PanelItem> panelItems = panelItemDAO.getPanelItemsForPanel(panelId);
        	
        	String[] newTests = (String[]) dynaForm.get("currentTests");
            
        	Transaction tx = HibernateUtil.getSession().beginTransaction();
            try {
            	for (PanelItem oldPanelItem : panelItems) {
            		oldPanelItem.setSysUserId(currentUser);
            	}
        		panelItemDAO.deleteData(panelItems);
                
        		for (String testId : newTests) {
        			PanelItem panelItem = new PanelItem();
        			panelItem.setPanel(panel);
        			TestDAO testDAO = new TestDAOImpl();
        			panelItem.setTest(testDAO.getTestById(testId));
        			panelItem.setLastupdatedFields();
        			panelItem.setSysUserId(currentUserId);
        			new PanelItemDAOImpl().insertData(panelItem);
        		}
        		
//------------------------------------
        		if( "N".equals(panel.getIsActive())){
        			panel.setIsActive("Y");
        			panel.setSysUserId(currentUser);
        			updatepanel=true;
            	                	    
        					}
        		
        		 if(updatepanel==true){
                     new PanelDAOImpl().updateData(panel);
                 }    
        		
//-------------------------------------        		
      		
        		
        		tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
            } finally {
                HibernateUtil.closeSession();
            }
            
        }
        
//--------------------------        
        
 if(updatepanel==false){
	 panel.setIsActive("N");
    panel.setSysUserId(currentUser);
	 
    new PanelDAOImpl().updateData(panel);
 }        
//---------------------------        
        
        DisplayListService.getInstance().refreshList(DisplayListService.ListType.PANELS);
        DisplayListService.getInstance().refreshList(DisplayListService.ListType.PANELS_INACTIVE);

		setSuccessFlag(request);
        
        return mapping.findForward(FWD_SUCCESS);
    }


    @Override
    protected String getPageTitleKey() {
        return null;
    }

    @Override
    protected String getPageSubtitleKey() {
        return null;
    }
}
