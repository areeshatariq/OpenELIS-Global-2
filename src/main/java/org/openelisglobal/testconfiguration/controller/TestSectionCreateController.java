package org.openelisglobal.testconfiguration.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.localization.service.LocalizationService;
import org.openelisglobal.localization.valueholder.Localization;
import org.openelisglobal.role.service.RoleService;
import org.openelisglobal.role.valueholder.Role;
import org.openelisglobal.systemmodule.valueholder.SystemModule;
import org.openelisglobal.systemusermodule.valueholder.RoleModule;
import org.openelisglobal.test.service.TestSectionService;
import org.openelisglobal.test.valueholder.TestSection;
import org.openelisglobal.testconfiguration.form.TestSectionCreateForm;
import org.openelisglobal.testconfiguration.service.TestSectionCreateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestSectionCreateController extends BaseController {

    public static final String NAME_SEPARATOR = "$";

    @Autowired
    private TestSectionService testSectionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private TestSectionCreateService testSectionCreateService;
    @Autowired
    private LocalizationService localizationService;

    @RequestMapping(value = "/TestSectionCreate", method = RequestMethod.GET)
    public ModelAndView showTestSectionCreate(HttpServletRequest request) {
        TestSectionCreateForm form = new TestSectionCreateForm();

        setupDisplayItems(form);

        return findForward(FWD_SUCCESS, form);
    }

    private void setupDisplayItems(TestSectionCreateForm form) {
        try {
            PropertyUtils.setProperty(form, "existingTestUnitList",
                    DisplayListService.getInstance().getList(DisplayListService.ListType.TEST_SECTION));
            PropertyUtils.setProperty(form, "inactiveTestUnitList",
                    DisplayListService.getInstance().getList(DisplayListService.ListType.TEST_SECTION_INACTIVE));
            List<TestSection> testSections = testSectionService.getAllTestSections();
            PropertyUtils.setProperty(form, "existingEnglishNames", getExistingTestNames(testSections, Locale.ENGLISH));

            PropertyUtils.setProperty(form, "existingFrenchNames", getExistingTestNames(testSections, Locale.FRENCH));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private String getExistingTestNames(List<TestSection> testSections, Locale locale) {
        StringBuilder builder = new StringBuilder(NAME_SEPARATOR);

        for (TestSection testSection : testSections) {
            builder.append(testSection.getLocalization().getLocalizedValue(locale));
            builder.append(NAME_SEPARATOR);
        }

        return builder.toString();
    }

    @RequestMapping(value = "/TestSectionCreate", method = RequestMethod.POST)
    public ModelAndView postTestSectionCreate(HttpServletRequest request,
            @ModelAttribute("form") @Valid TestSectionCreateForm form, BindingResult result) throws Exception {
        if (result.hasErrors()) {
            saveErrors(result);
            setupDisplayItems(form);
            return findForward(FWD_FAIL_INSERT, form);
        }

        String identifyingName = form.getString("testUnitEnglishName");
        String userId = getSysUserId(request);

        Localization localization = createLocalization(form.getString("testUnitFrenchName"), identifyingName, userId);

        TestSection testSection = createTestSection(identifyingName, userId);

        SystemModule workplanModule = createSystemModule("Workplan", identifyingName, userId);
        SystemModule resultModule = createSystemModule("LogbookResults", identifyingName, userId);
        SystemModule validationModule = createSystemModule("ResultValidation", identifyingName, userId);

        Role resultsEntryRole = roleService.getRoleByName("Results entry");
        Role validationRole = roleService.getRoleByName("Validator");

        RoleModule workplanResultModule = createRoleModule(userId, workplanModule, resultsEntryRole);
        RoleModule resultResultModule = createRoleModule(userId, resultModule, resultsEntryRole);
        RoleModule validationValidationModule = createRoleModule(userId, validationModule, validationRole);

        try {
            testSectionCreateService.insertTestSection(localization, testSection, workplanModule, resultModule,
                    validationModule, workplanResultModule, resultResultModule, validationValidationModule);
        } catch (LIMSRuntimeException lre) {
            lre.printStackTrace();
        }

        DisplayListService.getInstance().refreshList(DisplayListService.ListType.TEST_SECTION);
        DisplayListService.getInstance().refreshList(DisplayListService.ListType.TEST_SECTION_INACTIVE);

        return findForward(FWD_SUCCESS_INSERT, form);
    }

    private Localization createLocalization(String french, String english, String currentUserId) {
        Localization localization = new Localization();
        localization.setEnglish(english);
        localization.setFrench(french);
        localization.setDescription("test unit name");
        localization.setSysUserId(currentUserId);
        return localization;
    }

    private RoleModule createRoleModule(String userId, SystemModule workplanModule, Role role) {
        RoleModule roleModule = new RoleModule();
        roleModule.setRole(role);
        roleModule.setSystemModule(workplanModule);
        roleModule.setSysUserId(userId);
        roleModule.setHasAdd("Y");
        roleModule.setHasDelete("Y");
        roleModule.setHasSelect("Y");
        roleModule.setHasUpdate("Y");
        return roleModule;
    }

    private TestSection createTestSection(String identifyingName, String userId) {
        TestSection testSection = new TestSection();
        testSection.setDescription(identifyingName);
        testSection.setTestSectionName(identifyingName);
        testSection.setIsActive("N");
        String identifyingNameKey = identifyingName.replaceAll(" ", "_");
        testSection.setNameKey("testSection." + identifyingNameKey);

        testSection.setSortOrderInt(Integer.MAX_VALUE);
        testSection.setSysUserId(userId);
        return testSection;
    }

    private SystemModule createSystemModule(String menuItem, String identifyingName, String userId) {
        SystemModule module = new SystemModule();
        module.setSystemModuleName(menuItem + ":" + identifyingName);
        module.setDescription(menuItem + "=>" + identifyingName);
        module.setSysUserId(userId);
        module.setHasAddFlag("Y");
        module.setHasDeleteFlag("Y");
        module.setHasSelectFlag("Y");
        module.setHasUpdateFlag("Y");
        return module;
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "testSectionCreateDefinition";
        } else if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "redirect:/TestSectionCreate.do";
        } else if (FWD_FAIL_INSERT.equals(forward)) {
            return "testSectionCreateDefinition";
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
