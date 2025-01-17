package org.openelisglobal.testconfiguration.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.HibernateException;
import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.common.util.validator.GenericValidator;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestServiceImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.testconfiguration.form.SampleTypeTestAssignForm;
import org.openelisglobal.testconfiguration.service.SampleTypeTestAssignService;
import org.openelisglobal.typeofsample.service.TypeOfSampleService;
import org.openelisglobal.typeofsample.service.TypeOfSampleTestService;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SampleTypeTestAssignController extends BaseController {

    @Autowired
    private TypeOfSampleService typeOfSampleService;
    @Autowired
    private TypeOfSampleTestService typeOfSampleTestService;
    @Autowired
    private SampleTypeTestAssignService sampleTypeTestAssignService;

    @RequestMapping(value = "/SampleTypeTestAssign", method = RequestMethod.GET)
    public ModelAndView showSampleTypeTestAssign(HttpServletRequest request) {
        SampleTypeTestAssignForm form = new SampleTypeTestAssignForm();

        setupDisplayItems(form);

        return findForward(FWD_SUCCESS, form);
    }

    private void setupDisplayItems(SampleTypeTestAssignForm form) {
        List<IdValuePair> typeOfSamples = DisplayListService.getInstance()
                .getListWithLeadingBlank(DisplayListService.ListType.SAMPLE_TYPE);
        LinkedHashMap<IdValuePair, List<IdValuePair>> sampleTypesTestsMap = new LinkedHashMap<>(typeOfSamples.size());

        for (IdValuePair sampleTypePair : typeOfSamples) {
            List<IdValuePair> tests = new ArrayList<>();
            sampleTypesTestsMap.put(sampleTypePair, tests);
            List<Test> testList = typeOfSampleService.getAllTestsBySampleTypeId(sampleTypePair.getId());

            for (Test test : testList) {
                if (test.isActive()) {
                    tests.add(new IdValuePair(test.getId(), TestServiceImpl.getLocalizedTestNameWithType(test)));
                }
            }
        }

        // we can't just append the original list because that list is in the cache
        List<IdValuePair> joinedList = new ArrayList<>(typeOfSamples);
        joinedList.addAll(DisplayListService.getInstance().getList(DisplayListService.ListType.SAMPLE_TYPE_INACTIVE));
        try {
            PropertyUtils.setProperty(form, "sampleTypeList", joinedList);
            PropertyUtils.setProperty(form, "sampleTypeTestList", sampleTypesTestsMap);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "sampleTypeAssignDefinition";
        } else if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "redirect:/SampleTypeTestAssign.do";
        } else if (FWD_FAIL_INSERT.equals(forward)) {
            return "sampleTypeAssignDefinition";
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

    @RequestMapping(value = "/SampleTypeTestAssign", method = RequestMethod.POST)
    public ModelAndView postSampleTypeTestAssign(HttpServletRequest request,
            @ModelAttribute("form") @Valid SampleTypeTestAssignForm form, BindingResult result) {
        if (result.hasErrors()) {
            saveErrors(result);
            setupDisplayItems(form);
            return findForward(FWD_FAIL_INSERT, form);
        }
        String testId = form.getString("testId");
        String sampleTypeId = form.getString("sampleTypeId");
        String deactivateSampleTypeId = form.getString("deactivateSampleTypeId");
        boolean updateTypeOfSample = false;
        String systemUserId = getSysUserId(request);

        TypeOfSample typeOfSample = SpringContext.getBean(TypeOfSampleService.class)
                .getTransientTypeOfSampleById(sampleTypeId);
        TypeOfSample deActivateTypeOfSample = null;

        // Test test = new TestService(testId).getTest();

        // This covers the case that they are moving the test to the same sample type
        // they are moving it from
        if (sampleTypeId.equals(deactivateSampleTypeId)) {
            return findForward(FWD_SUCCESS_INSERT, form);
        }

        TypeOfSampleTest typeOfSampleTestOld = typeOfSampleTestService.getTypeOfSampleTestForTest(testId);
        boolean deleteExistingTypeOfSampleTest = false;
        String typeOfSamplesTestID = new String();

        if (typeOfSampleTestOld != null) {
            typeOfSamplesTestID = typeOfSampleTestOld.getId();
            deleteExistingTypeOfSampleTest = true;
        }
        // ---------------------------
        /*
         * if( "N".equals(typeOfSample.getIsActive())){ typeOfSample.setIsActive(true);
         * typeOfSample.setSysUserId(currentUser); updateTypeOfSample = true; }
         */

        // Boolean value = false;
        if (typeOfSample.getIsActive() == false) {
            typeOfSample.setIsActive(true);
            typeOfSample.setSysUserId(systemUserId);
            updateTypeOfSample = true;
        }

//------------------------------------------
        if (!GenericValidator.isBlankOrNull(deactivateSampleTypeId)) {
            deActivateTypeOfSample = SpringContext.getBean(TypeOfSampleService.class)
                    .getTransientTypeOfSampleById(deactivateSampleTypeId);
            deActivateTypeOfSample.setIsActive(false);
            deActivateTypeOfSample.setSysUserId(systemUserId);
        }

        try {
            sampleTypeTestAssignService.update(typeOfSample, testId, typeOfSamplesTestID, sampleTypeId,
                    deleteExistingTypeOfSampleTest, updateTypeOfSample, deActivateTypeOfSample, systemUserId);
        } catch (HibernateException lre) {
            lre.printStackTrace();
        }

        DisplayListService.getInstance().refreshList(DisplayListService.ListType.SAMPLE_TYPE);
        DisplayListService.getInstance().refreshList(DisplayListService.ListType.SAMPLE_TYPE_INACTIVE);

        return findForward(FWD_SUCCESS_INSERT, form);
    }
}
