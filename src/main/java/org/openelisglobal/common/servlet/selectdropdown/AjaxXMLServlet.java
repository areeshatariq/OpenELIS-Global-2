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
package org.openelisglobal.common.servlet.selectdropdown;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ajaxtags.helpers.AjaxXmlBuilder;
import org.ajaxtags.servlets.BaseAjaxServlet;
import org.openelisglobal.common.provider.selectdropdown.BaseSelectDropDownProvider;
import org.openelisglobal.common.provider.selectdropdown.SelectDropDownProviderFactory;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.login.dao.UserModuleService;
import org.openelisglobal.spring.util.SpringContext;

public class AjaxXMLServlet extends BaseAjaxServlet {

    @Override
    public String getXmlContent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // check for authentication
        UserModuleService userModuleService = SpringContext.getBean(UserModuleService.class);
        if (userModuleService.isSessionExpired(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().println(MessageUtil.getMessage("message.error.unauthorized"));
            return new AjaxXmlBuilderForSortableTests().toString();
        }

        String selectDropDownProvider = request.getParameter("provider");
        String selectDropDownFieldName = request.getParameter("fieldName");
        String selectDropDownId = request.getParameter("idName");

        // bugzilla 1844 adding sorting ability
        boolean sortable = false;
        String selectDropDownSortFieldA = null;
        String selectDropDownSortFieldB = null;
        String selectDropDownAlternateLabel = null;
        // this is sortable test target
        if (request.getParameter("sortFieldA") != null && request.getParameter("sortFieldB") != null
                && request.getParameter("alternateLabel") != null) {
            selectDropDownSortFieldA = request.getParameter("sortFieldA");
            selectDropDownSortFieldB = request.getParameter("sortFieldB");
            selectDropDownAlternateLabel = request.getParameter("alternateLabel");
            sortable = true;

        }

        BaseSelectDropDownProvider provider = SelectDropDownProviderFactory.getInstance()
                .getSelectDropDownProvider(selectDropDownProvider);

        provider.setServlet(this);
        List list = provider.processRequest(request, response);

        // bugzilla 1844 adding toggle sort and toggle label values
        if (sortable) {
            AjaxXmlBuilderForSortableTests axb = new AjaxXmlBuilderForSortableTests();
            // add blank option!!
            axb.addItemAsCData("", "", "", "", "");
            axb.addItems(list, selectDropDownFieldName, selectDropDownId, selectDropDownSortFieldA,
                    selectDropDownSortFieldB, selectDropDownAlternateLabel);
            return axb.toString();

        } else {
            AjaxXmlBuilder axb = new AjaxXmlBuilder();
            // add blank option!!
            axb.addItemAsCData("", "");
            axb.addItems(list, selectDropDownFieldName, selectDropDownId);
            return axb.toString();
        }

    }

}
