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
*/
package org.openelisglobal.panel.dao;

import java.util.List;

import org.openelisglobal.common.dao.BaseDAO;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.panel.valueholder.Panel;

/**
 * @author diane benz
 *
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public interface PanelDAO extends BaseDAO<Panel, String> {

//	public boolean insertData(Panel panel) throws LIMSRuntimeException;

//	public void deleteData(List panels) throws LIMSRuntimeException;

    public List<Panel> getAllActivePanels() throws LIMSRuntimeException;

    public List getPageOfPanels(int startingRecNo) throws LIMSRuntimeException;

    public void getData(Panel panel) throws LIMSRuntimeException;

//	public void updateData(Panel panel) throws LIMSRuntimeException;

    public List getActivePanels(String filter) throws LIMSRuntimeException;

    public List<Panel> getAllPanels() throws LIMSRuntimeException;

    public List getNextPanelRecord(String id) throws LIMSRuntimeException;

    public List getPreviousPanelRecord(String id) throws LIMSRuntimeException;

    public Panel getPanelByName(Panel panel) throws LIMSRuntimeException;

    public Integer getTotalPanelCount() throws LIMSRuntimeException;

    public String getNameForPanelId(String panelId);

    public String getDescriptionForPanelId(String id);

    public String getIdForPanelName(String name);

    public Panel getPanelByName(String panelName) throws LIMSRuntimeException;

    public Panel getPanelById(String id) throws LIMSRuntimeException;

    public boolean duplicatePanelExists(Panel panel);

    public boolean duplicatePanelDescriptionExists(Panel panel);

    public void clearIDMaps();

}
