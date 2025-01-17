package org.openelisglobal.sample.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.StaleObjectStateException;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.formfields.FormFields;
import org.openelisglobal.common.provider.validation.IAccessionNumberValidator.ValidationResults;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.services.DisplayListService.ListType;
import org.openelisglobal.common.services.SampleOrderService;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.services.StatusService.SampleStatus;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.patient.action.bean.PatientSearch;
import org.openelisglobal.patient.service.PatientService;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.person.service.PersonService;
import org.openelisglobal.result.service.ResultService;
import org.openelisglobal.sample.bean.SampleEditItem;
import org.openelisglobal.sample.form.SampleEditForm;
import org.openelisglobal.sample.service.SampleEditService;
import org.openelisglobal.sample.service.SampleService;
import org.openelisglobal.sample.util.AccessionNumberUtil;
import org.openelisglobal.sample.validator.SampleEditFormValidator;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.service.SampleHumanService;
import org.openelisglobal.sampleitem.service.SampleItemService;
import org.openelisglobal.sampleitem.valueholder.SampleItem;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestService;
import org.openelisglobal.test.service.TestServiceImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.typeofsample.service.TypeOfSampleService;
import org.openelisglobal.typeofsample.service.TypeOfSampleTestService;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.openelisglobal.userrole.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SampleEditController extends BaseController {

    @Autowired
    SampleEditFormValidator formValidator;

    // private ObservationHistory paymentObservation = null;
    private static final SampleEditItemComparator testComparator = new SampleEditItemComparator();
    private static final Set<Integer> excludedAnalysisStatusList;
    private static final Set<Integer> ENTERED_STATUS_SAMPLE_LIST = new HashSet<>();
    private static final Collection<String> ABLE_TO_CANCEL_ROLE_NAMES = new ArrayList<>();

    static {
        excludedAnalysisStatusList = new HashSet<>();
        excludedAnalysisStatusList
                .add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Canceled)));

        ENTERED_STATUS_SAMPLE_LIST.add(Integer.parseInt(StatusService.getInstance().getStatusID(SampleStatus.Entered)));
        ABLE_TO_CANCEL_ROLE_NAMES.add("Validator");
        ABLE_TO_CANCEL_ROLE_NAMES.add("Validation");
        ABLE_TO_CANCEL_ROLE_NAMES.add("Biologist");
    }

    @Autowired
    private SampleItemService sampleItemService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private TestService testService;
//	@Autowired
//	private OrganizationOrganizationTypeService orgOrgTypeService;
    @Autowired
    private TypeOfSampleService typeOfSampleService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    TypeOfSampleTestService typeOfSampleTestService;
    @Autowired
    ResultService resultService;
    @Autowired
    SampleHumanService sampleHumanService;
    @Autowired
    UserRoleService userRoleService;
    @Autowired
    private SampleEditService sampleEditService;

    @RequestMapping(value = "/SampleEdit", method = RequestMethod.GET)
    public ModelAndView showSampleEdit(HttpServletRequest request)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        SampleEditForm form = new SampleEditForm();
        form.setFormAction("SampleEdit.do");

        request.getSession().setAttribute(SAVE_DISABLED, TRUE);

        boolean allowedToCancelResults = userModuleService.isUserAdmin(request)
                || userRoleService.userInRole(getSysUserId(request), ABLE_TO_CANCEL_ROLE_NAMES);
        boolean isEditable = "readwrite".equals(request.getSession().getAttribute(SAMPLE_EDIT_WRITABLE))
                || "readwrite".equals(request.getParameter("type"));
        PropertyUtils.setProperty(form, "isEditable", isEditable);

        String accessionNumber = request.getParameter("accessionNumber");
        if (GenericValidator.isBlankOrNull(accessionNumber)) {
            accessionNumber = getMostRecentAccessionNumberForPaitient(request.getParameter("patientID"));
        }
        if (!GenericValidator.isBlankOrNull(accessionNumber)) {
            PropertyUtils.setProperty(form, "accessionNumber", accessionNumber);
            PropertyUtils.setProperty(form, "searchFinished", Boolean.TRUE);

            Sample sample = getSample(accessionNumber);

            if (sample != null && !GenericValidator.isBlankOrNull(sample.getId())) {

                List<SampleItem> sampleItemList = getSampleItems(sample);
                setPatientInfo(form, sample);
                List<SampleEditItem> currentTestList = getCurrentTestInfo(sampleItemList, accessionNumber,
                        allowedToCancelResults);
                PropertyUtils.setProperty(form, "existingTests", currentTestList);
                setAddableTestInfo(form, sampleItemList, accessionNumber);
                setAddableSampleTypes(form);
                setSampleOrderInfo(form, sample);
                PropertyUtils.setProperty(form, "ableToCancelResults",
                        hasResults(currentTestList, allowedToCancelResults));
                String maxAccessionNumber;
                if (sampleItemList.size() > 0) {
                    maxAccessionNumber = accessionNumber + "-"
                            + sampleItemList.get(sampleItemList.size() - 1).getSortOrder();
                } else {
                    maxAccessionNumber = accessionNumber + "-0";
                }
                PropertyUtils.setProperty(form, "maxAccessionNumber", maxAccessionNumber);
                PropertyUtils.setProperty(form, "isConfirmationSample", sampleService.isConfirmationSample(sample));
            } else {
                PropertyUtils.setProperty(form, "noSampleFound", Boolean.TRUE);
            }
        } else {
            PropertyUtils.setProperty(form, "searchFinished", Boolean.FALSE);
            request.getSession().setAttribute(SAMPLE_EDIT_WRITABLE, request.getParameter("type"));
        }

        if (FormFields.getInstance().useField(FormFields.Field.InitialSampleCondition)) {
            PropertyUtils.setProperty(form, "initialSampleConditionList",
                    DisplayListService.getInstance().getList(ListType.INITIAL_SAMPLE_CONDITION));
        }

        PropertyUtils.setProperty(form, "currentDate", DateUtil.getCurrentDateAsText());
        PatientSearch patientSearch = new PatientSearch();
        patientSearch.setLoadFromServerWithPatient(true);
        patientSearch.setSelectedPatientActionButtonText(MessageUtil.getMessage("label.patient.search.select"));
        PropertyUtils.setProperty(form, "patientSearch", patientSearch);
        PropertyUtils.setProperty(form, "warning", true);

        addFlashMsgsToRequest(request);
        return findForward(FWD_SUCCESS, form);
    }

    private Boolean hasResults(List<SampleEditItem> currentTestList, boolean allowedToCancelResults) {
        if (!allowedToCancelResults) {
            return false;
        }

        for (SampleEditItem editItem : currentTestList) {
            if (editItem.isHasResults()) {
                return true;
            }
        }

        return false;
    }

    private void setSampleOrderInfo(SampleEditForm form, Sample sample)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        SampleOrderService sampleOrderService = new SampleOrderService(sample);
        PropertyUtils.setProperty(form, "sampleOrderItems", sampleOrderService.getSampleOrderItem());
    }

    private String getMostRecentAccessionNumberForPaitient(String patientID) {
        String accessionNumber = null;
        if (!GenericValidator.isBlankOrNull(patientID)) {
            List<Sample> samples = sampleService.getSamplesForPatient(patientID);

            int maxId = 0;
            for (Sample sample : samples) {
                if (Integer.parseInt(sample.getId()) > maxId) {
                    maxId = Integer.parseInt(sample.getId());
                    accessionNumber = sample.getAccessionNumber();
                }
            }

        }
        return accessionNumber;
    }

    private Sample getSample(String accessionNumber) {
        return sampleService.getSampleByAccessionNumber(accessionNumber);
    }

    private List<SampleItem> getSampleItems(Sample sample) {

        return sampleItemService.getSampleItemsBySampleIdAndStatus(sample.getId(), ENTERED_STATUS_SAMPLE_LIST);
    }

    private void setPatientInfo(SampleEditForm form, Sample sample)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Patient patient = sampleHumanService.getPatientForSample(sample);
        PatientService patientPatientService = SpringContext.getBean(PatientService.class);
        PersonService personService = SpringContext.getBean(PersonService.class);
        personService.getData(patient.getPerson());

        PropertyUtils.setProperty(form, "patientName", patientPatientService.getLastFirstName(patient));
        PropertyUtils.setProperty(form, "dob", patientPatientService.getEnteredDOB(patient));
        PropertyUtils.setProperty(form, "gender", patientPatientService.getGender(patient));
        PropertyUtils.setProperty(form, "nationalId", patientPatientService.getNationalId(patient));
    }

    private List<SampleEditItem> getCurrentTestInfo(List<SampleItem> sampleItemList, String accessionNumber,
            boolean allowedToCancelAll) {
        List<SampleEditItem> currentTestList = new ArrayList<>();

        for (SampleItem sampleItem : sampleItemList) {
            addCurrentTestsToList(sampleItem, currentTestList, accessionNumber, allowedToCancelAll);
        }

        return currentTestList;
    }

    private void addCurrentTestsToList(SampleItem sampleItem, List<SampleEditItem> currentTestList,
            String accessionNumber, boolean allowedToCancelAll) {

        TypeOfSample typeOfSample = typeOfSampleService.get(sampleItem.getTypeOfSampleId());

        List<Analysis> analysisList = analysisService.getAnalysesBySampleItemsExcludingByStatusIds(sampleItem,
                excludedAnalysisStatusList);

        List<SampleEditItem> analysisSampleItemList = new ArrayList<>();

        String collectionDate = DateUtil.convertTimestampToStringDate(sampleItem.getCollectionDate());
        String collectionTime = DateUtil.convertTimestampToStringTime(sampleItem.getCollectionDate());
        boolean canRemove = true;
        for (Analysis analysis : analysisList) {
            SampleEditItem sampleEditItem = new SampleEditItem();

            sampleEditItem.setTestId(analysis.getTest().getId());
            sampleEditItem.setTestName(TestServiceImpl.getUserLocalizedTestName(analysis.getTest()));
            sampleEditItem.setSampleItemId(sampleItem.getId());

            boolean canCancel = allowedToCancelAll
                    || (!StatusService.getInstance().matches(analysis.getStatusId(), AnalysisStatus.Canceled)
                            && StatusService.getInstance().matches(analysis.getStatusId(), AnalysisStatus.NotStarted));

            if (!canCancel) {
                canRemove = false;
            }
            sampleEditItem.setCanCancel(canCancel);
            sampleEditItem.setAnalysisId(analysis.getId());
            sampleEditItem.setStatus(StatusService.getInstance().getStatusNameFromId(analysis.getStatusId()));
            sampleEditItem.setSortOrder(analysis.getTest().getSortOrder());
            sampleEditItem.setHasResults(
                    !StatusService.getInstance().matches(analysis.getStatusId(), AnalysisStatus.NotStarted));

            analysisSampleItemList.add(sampleEditItem);
        }

        if (!analysisSampleItemList.isEmpty()) {
            Collections.sort(analysisSampleItemList, testComparator);
            SampleEditItem firstItem = analysisSampleItemList.get(0);

            firstItem.setAccessionNumber(accessionNumber + "-" + sampleItem.getSortOrder());
            firstItem.setSampleType(typeOfSample.getLocalizedName());
            firstItem.setCanRemoveSample(canRemove);
            firstItem.setCollectionDate(collectionDate == null ? "" : collectionDate);
            firstItem.setCollectionTime(collectionTime);
            currentTestList.addAll(analysisSampleItemList);
        }
    }

    private void setAddableTestInfo(SampleEditForm form, List<SampleItem> sampleItemList, String accessionNumber)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<SampleEditItem> possibleTestList = new ArrayList<>();

        for (SampleItem sampleItem : sampleItemList) {
            addPossibleTestsToList(sampleItem, possibleTestList, accessionNumber);
        }

        PropertyUtils.setProperty(form, "possibleTests", possibleTestList);
        PropertyUtils.setProperty(form, "testSectionList",
                DisplayListService.getInstance().getList(ListType.TEST_SECTION));
    }

    private void setAddableSampleTypes(SampleEditForm form)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PropertyUtils.setProperty(form, "sampleTypes",
                DisplayListService.getInstance().getList(ListType.SAMPLE_TYPE_ACTIVE));
    }

    private void addPossibleTestsToList(SampleItem sampleItem, List<SampleEditItem> possibleTestList,
            String accessionNumber) {

        TypeOfSample typeOfSample = typeOfSampleService.get(sampleItem.getTypeOfSampleId());

        List<TypeOfSampleTest> typeOfSampleTestList = typeOfSampleTestService
                .getTypeOfSampleTestsForSampleType(typeOfSample.getId());
        List<SampleEditItem> typeOfTestSampleItemList = new ArrayList<>();

        for (TypeOfSampleTest typeOfSampleTest : typeOfSampleTestList) {
            SampleEditItem sampleEditItem = new SampleEditItem();

            sampleEditItem.setTestId(typeOfSampleTest.getTestId());
            Test test = testService.get(typeOfSampleTest.getTestId());
            if ("Y".equals(test.getIsActive()) && test.getOrderable()) {
                sampleEditItem.setTestName(TestServiceImpl.getUserLocalizedTestName(test));
                sampleEditItem.setSampleItemId(sampleItem.getId());
                sampleEditItem.setSortOrder(test.getSortOrder());
                typeOfTestSampleItemList.add(sampleEditItem);
            }
        }

        if (!typeOfTestSampleItemList.isEmpty()) {
            Collections.sort(typeOfTestSampleItemList, testComparator);

            typeOfTestSampleItemList.get(0).setAccessionNumber(accessionNumber + "-" + sampleItem.getSortOrder());
            typeOfTestSampleItemList.get(0).setSampleType(typeOfSample.getLocalizedName());

            possibleTestList.addAll(typeOfTestSampleItemList);
        }

    }

    @RequestMapping(value = "/SampleEdit", method = RequestMethod.POST)
    public ModelAndView showSampleEditUpdate(HttpServletRequest request,
            @ModelAttribute("form") @Validated(SampleEditForm.SampleEdit.class) SampleEditForm form,
            BindingResult result, RedirectAttributes redirectAttributes) {
        formValidator.validate(form, result);
        if (result.hasErrors()) {
            saveErrors(result);
            return findForward(FWD_FAIL_INSERT, form);
        }

        request.getSession().setAttribute(SAVE_DISABLED, TRUE);
        boolean sampleChanged = accessionNumberChanged(form);
        Sample updatedSample = null;

        if (sampleChanged) {
            validateNewAccessionNumber(form.getNewAccessionNumber(), result);
            if (result.hasErrors()) {
                saveErrors(result);
                return findForward(FWD_FAIL_INSERT, form);
            } else {
                updatedSample = updateAccessionNumberInSample(form);
            }
        }

        try {
            sampleEditService.editSample(form, request, updatedSample, sampleChanged, getSysUserId(request));

        } catch (LIMSRuntimeException lre) {
            if (lre.getException() instanceof StaleObjectStateException) {
                result.reject("errors.OptimisticLockException", "errors.OptimisticLockException");
            } else {
                lre.printStackTrace();
                result.reject("errors.UpdateException", "errors.UpdateException");
            }
            saveErrors(result);
            return findForward(FWD_FAIL_INSERT, form);

        }

        String sampleEditWritable = (String) request.getSession().getAttribute(SAMPLE_EDIT_WRITABLE);

        redirectAttributes.addFlashAttribute(FWD_SUCCESS, true);

        if (GenericValidator.isBlankOrNull(sampleEditWritable)) {
            return findForward(FWD_SUCCESS_INSERT, form);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put("type", sampleEditWritable);
            return getForwardWithParameters(findForward(FWD_SUCCESS_INSERT, form), params);
        }
    }

    private Errors validateNewAccessionNumber(String accessionNumber, Errors errors) {
        ValidationResults results = AccessionNumberUtil.correctFormat(accessionNumber, false);

        if (results != ValidationResults.SUCCESS) {
            errors.reject("sample.entry.invalid.accession.number.format",
                    "sample.entry.invalid.accession.number.format");
        } else if (AccessionNumberUtil.isUsed(accessionNumber)) {
            errors.reject("sample.entry.invalid.accession.number.used", "sample.entry.invalid.accession.number.used");
        }

        return errors;
    }

    private Sample updateAccessionNumberInSample(SampleEditForm form) {
        Sample sample = sampleService.getSampleByAccessionNumber(form.getAccessionNumber());

        if (sample != null) {
            sample.setAccessionNumber(form.getNewAccessionNumber());
            sample.setSysUserId(getSysUserId(request));
        }

        return sample;
    }

    private boolean accessionNumberChanged(SampleEditForm form) {
        String newAccessionNumber = form.getNewAccessionNumber();

        return !GenericValidator.isBlankOrNull(newAccessionNumber)
                && !newAccessionNumber.equals(form.getAccessionNumber());

    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "sampleEditDefinition";
        } else if (FWD_FAIL.equals(forward)) {
            return "homePageDefinition";
        } else if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "redirect:/SampleEdit.do";
        } else if (FWD_FAIL_INSERT.equals(forward)) {
            return "sampleEditDefinition";
        } else {
            return "PageNotFound";
        }
    }

    @Override
    protected String getPageTitleKey() {
        boolean isEditable = "readwrite".equals(request.getSession().getAttribute(SAMPLE_EDIT_WRITABLE))
                || "readwrite".equals(request.getParameter("type"));
        return isEditable ? MessageUtil.getContextualKey("sample.edit.title")
                : MessageUtil.getContextualKey("sample.view.title");
    }

    @Override
    protected String getPageSubtitleKey() {
        boolean isEditable = "readwrite".equals(request.getSession().getAttribute(SAMPLE_EDIT_WRITABLE))
                || "readwrite".equals(request.getParameter("type"));
        return isEditable ? MessageUtil.getContextualKey("sample.edit.subtitle")
                : MessageUtil.getContextualKey("sample.view.subtitle");
    }

    private static class SampleEditItemComparator implements Comparator<SampleEditItem> {

        @Override
        public int compare(SampleEditItem o1, SampleEditItem o2) {
            if (GenericValidator.isBlankOrNull(o1.getSortOrder())
                    || GenericValidator.isBlankOrNull(o2.getSortOrder())) {
                return o1.getTestName().compareTo(o2.getTestName());
            }

            try {
                return Integer.parseInt(o1.getSortOrder()) - Integer.parseInt(o2.getSortOrder());
            } catch (NumberFormatException e) {
                return o1.getTestName().compareTo(o2.getTestName());
            }
        }

    }
}
