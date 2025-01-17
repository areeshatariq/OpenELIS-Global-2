package org.openelisglobal.organization.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.openelisglobal.common.constants.Constants;
import org.openelisglobal.common.controller.BaseMenuController;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.form.MenuForm;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.SystemConfiguration;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.organization.form.OrganizationMenuForm;
import org.openelisglobal.organization.service.OrganizationService;
import org.openelisglobal.organization.valueholder.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrganizationMenuController extends BaseMenuController {

    @Autowired
    OrganizationService organizationService;

    @RequestMapping(value = { "/OrganizationMenu", "/SearchOrganizationMenu" }, method = RequestMethod.GET)
    public ModelAndView showOrganizationMenu(HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String forward;
        OrganizationMenuForm form = new OrganizationMenuForm();

        forward = performMenuAction(form, request);
        if (FWD_FAIL.equals(forward)) {
            Errors errors = new BaseErrors();
            errors.reject("error.generic");
            redirectAttributes.addFlashAttribute(Constants.REQUEST_ERRORS, errors);
            return findForward(forward, form);
        } else {
            request.setAttribute("menuDefinition", "OrganizationMenuDefinition");
            addFlashMsgsToRequest(request);
            return findForward(forward, form);
        }
    }

    @Override
    protected List<Organization> createMenuList(MenuForm form, HttpServletRequest request) throws Exception {

        // System.out.println("I am in OrganizationMenuAction createMenuList()");

        List<Organization> organizations = new ArrayList<>();

        String stringStartingRecNo = (String) request.getAttribute("startingRecNo");
        int startingRecNo = Integer.parseInt(stringStartingRecNo);

        // bugzilla 2372
        String searchString = request.getParameter("searchString");

        String doingSearch = request.getParameter("search");

        if (!StringUtil.isNullorNill(doingSearch) && doingSearch.equals(YES)) {
            organizations = organizationService.getPagesOfSearchedOrganizations(startingRecNo, searchString);
        } else {
            organizations = organizationService.getOrderedPage("organizationName", false, startingRecNo);
        }

        request.setAttribute("menuDefinition", "OrganizationMenuDefinition");

        // bugzilla 1411 set pagination variables
        // bugzilla 2372 set pagination variables for searched results
        if (!StringUtil.isNullorNill(doingSearch) && doingSearch.equals(YES)) {
            request.setAttribute(MENU_TOTAL_RECORDS,
                    String.valueOf(organizationService.getTotalSearchedOrganizationCount(searchString)));
        } else {
            request.setAttribute(MENU_TOTAL_RECORDS, String.valueOf(organizationService.getCount()));
        }

        request.setAttribute(MENU_FROM_RECORD, String.valueOf(startingRecNo));
        int numOfRecs = 0;
        if (organizations != null) {
            if (organizations.size() > SystemConfiguration.getInstance().getDefaultPageSize()) {
                numOfRecs = SystemConfiguration.getInstance().getDefaultPageSize();
            } else {
                numOfRecs = organizations.size();
            }
            numOfRecs--;
        }
        int endingRecNo = startingRecNo + numOfRecs;
        request.setAttribute(MENU_TO_RECORD, String.valueOf(endingRecNo));
        // end bugzilla 1411

        // bugzilla 2372
        request.setAttribute(MENU_SEARCH_BY_TABLE_COLUMN, "organization.organizationName");
        // bugzilla 2372 set up a seraching mode so the next and previous action will
        // know
        // what to do

        if (!StringUtil.isNullorNill(doingSearch) && doingSearch.equals(YES)) {

            request.setAttribute(IN_MENU_SELECT_LIST_HEADER_SEARCH, "true");

            request.setAttribute(MENU_SELECT_LIST_HEADER_SEARCH_STRING, searchString);
        }

        return organizations;
    }

    @Override
    protected String getDeactivateDisabled() {
        return "false";
    }

    @Override
    protected int getPageSize() {
        return SystemConfiguration.getInstance().getDefaultPageSize();
    }

    @RequestMapping(value = "/DeleteOrganization", method = RequestMethod.POST)
    public ModelAndView showDeleteOrganization(HttpServletRequest request,
            @ModelAttribute("form") @Valid OrganizationMenuForm form, BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(Constants.REQUEST_ERRORS, result);
            findForward(FWD_FAIL_DELETE, form);
        }

        List<String> selectedIDs = (List<String>) form.get("selectedIDs");
        List<Organization> organizations = new ArrayList<>();
        for (int i = 0; i < selectedIDs.size(); i++) {
            Organization organization = new Organization();
            organization.setId(selectedIDs.get(i));
            organization.setSysUserId(getSysUserId(request));
            organizations.add(organization);
        }

        try {
            // System.out.println("Going to delete Organization");
            organizationService.deleteAll(organizations);
            // System.out.println("Just deleted Organization");
        } catch (LIMSRuntimeException lre) {
            // bugzilla 2154
            LogEvent.logError("OrganizationDeleteAction", "performAction()", lre.toString());

            String errorMsg;
            if (lre.getException() instanceof org.hibernate.StaleObjectStateException) {
                errorMsg = "errors.OptimisticLockException";
            } else {
                errorMsg = "errors.DeleteException";
            }
            result.reject(errorMsg);
            redirectAttributes.addFlashAttribute(Constants.REQUEST_ERRORS, result);
            return findForward(FWD_FAIL_DELETE, form);

        }
        redirectAttributes.addAttribute(FWD_SUCCESS, true);
        return findForward(FWD_SUCCESS_DELETE, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "masterListsPageDefinition";
        } else if (FWD_FAIL.equals(forward)) {
            return "redirect:/MasterListsPage.do";
        } else if (FWD_SUCCESS_DELETE.equals(forward)) {
            return "redirect:/OrganizationMenu.do";
        } else if (FWD_FAIL_DELETE.equals(forward)) {
            return "redirect:/OrganizationMenu.do";
        } else {
            return "PageNotFound";
        }
    }

    @Override
    protected String getPageTitleKey() {
        return "organization.browse.title";
    }

    @Override
    protected String getPageSubtitleKey() {
        return "organization.browse.title";
    }

}
