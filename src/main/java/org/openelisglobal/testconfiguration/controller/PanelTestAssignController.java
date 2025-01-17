package org.openelisglobal.testconfiguration.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.panel.service.PanelService;
import org.openelisglobal.panel.valueholder.Panel;
import org.openelisglobal.panelitem.service.PanelItemService;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.test.service.TestService;
import org.openelisglobal.test.service.TestServiceImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.test.valueholder.TestComparator;
import org.openelisglobal.testconfiguration.action.PanelTests;
import org.openelisglobal.testconfiguration.form.PanelTestAssignForm;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PanelTestAssignController extends BaseController {

    @Autowired
    PanelService panelService;
    @Autowired
    PanelItemService panelItemService;
    @Autowired
    TestService testService;

    @RequestMapping(value = "/PanelTestAssign", method = RequestMethod.GET)
    public ModelAndView showPanelTestAssign(HttpServletRequest request) {
        PanelTestAssignForm form = new PanelTestAssignForm();

        String panelId = request.getParameter("panelId");
        if (panelId == null) {
            panelId = "";
        }
        form.setPanelId(panelId);

        setupDisplayItems(form);

        addFlashMsgsToRequest(request);

        return findForward(FWD_SUCCESS, form);
    }

    private void setupDisplayItems(PanelTestAssignForm form) {
        List<IdValuePair> panels = DisplayListService.getInstance().getList(DisplayListService.ListType.PANELS);

        try {
            PropertyUtils.setProperty(form, "panelList", panels);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!GenericValidator.isBlankOrNull(form.getPanelId())) {
            Panel panel = panelService.getPanelById(form.getPanelId());
            IdValuePair panelPair = new IdValuePair(panel.getId(), panel.getLocalizedName());

            List<IdValuePair> tests = new ArrayList<>();

            List<Test> testList = getAllTestsByPanelId(panel.getId());

            PanelTests panelTests = new PanelTests(panelPair);
            HashSet<String> testIdSet = new HashSet<>();

            for (Test test : testList) {
                if (test.isActive()) {
                    tests.add(new IdValuePair(test.getId(), TestServiceImpl.getUserLocalizedTestName(test)));
                    testIdSet.add(test.getId());
                }
            }
            panelTests.setTests(tests, testIdSet);

            try {

                PropertyUtils.setProperty(form, "selectedPanel", panelTests);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public List<Test> getAllTestsByPanelId(String panelId) {
        List<Test> testList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<PanelItem> testLinks = panelItemService.getPanelItemsForPanel(panelId);

        for (PanelItem link : testLinks) {
            testList.add(link.getTest());
        }

        Collections.sort(testList, TestComparator.NAME_COMPARATOR);
        return testList;
    }

    @RequestMapping(value = "/PanelTestAssign", method = RequestMethod.POST)
    public ModelAndView postPanelTestAssign(HttpServletRequest request,
            @ModelAttribute("form") @Valid PanelTestAssignForm form, BindingResult result,
            RedirectAttributes redirectAttributes) throws Exception {
        if (result.hasErrors()) {
            saveErrors(result);
            setupDisplayItems(form);
            return findForward(FWD_FAIL_INSERT, form);
        }

        String panelId = form.getString("panelId");
        String currentUser = getSysUserId(request);
        boolean updatePanel = false;

        Panel panel = panelService.getPanelById(panelId);

        if (!GenericValidator.isBlankOrNull(panelId)) {
            List<PanelItem> panelItems = panelItemService.getPanelItemsForPanel(panelId);

            List<String> newTestIds = (List<String>) form.get("currentTests");
            List<Test> newTests = new ArrayList<>();
            for (String testId : newTestIds) {
                newTests.add(testService.get(testId));
            }

            try {
                panelItemService.updatePanelItems(panelItems, panel, updatePanel, currentUser, newTests);
            } catch (LIMSRuntimeException lre) {
                lre.printStackTrace();
            }
        }

        DisplayListService.getInstance().refreshList(DisplayListService.ListType.PANELS);
        DisplayListService.getInstance().refreshList(DisplayListService.ListType.PANELS_INACTIVE);

        redirectAttributes.addFlashAttribute(FWD_SUCCESS, true);
        return findForward(FWD_SUCCESS_INSERT, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "panelAssignDefinition";
        } else if (FWD_SUCCESS_INSERT.equals(forward)) {
            String url = "/PanelTestAssign.do?panelId=" + Encode.forUriComponent(request.getParameter("panelId"));
            return "redirect:" + url;
        } else if (FWD_FAIL_INSERT.equals(forward)) {
            return "panelAssignDefinition";
        } else {
            return "PageNotFound";
        }
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
