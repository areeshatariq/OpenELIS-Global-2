package org.openelisglobal.systemuser.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.constants.Constants;
import org.openelisglobal.common.controller.BaseMenuController;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.form.MenuForm;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.common.util.SystemConfiguration;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.login.service.LoginService;
import org.openelisglobal.login.valueholder.Login;
import org.openelisglobal.systemuser.form.UnifiedSystemUserMenuForm;
import org.openelisglobal.systemuser.service.SystemUserService;
import org.openelisglobal.systemuser.service.UnifiedSystemUserService;
import org.openelisglobal.systemuser.valueholder.SystemUser;
import org.openelisglobal.systemuser.valueholder.UnifiedSystemUser;
import org.openelisglobal.userrole.service.UserRoleService;
import org.openelisglobal.userrole.valueholder.UserRole;
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
public class UnifiedSystemUserMenuController extends BaseMenuController {

    @Autowired
    SystemUserService systemUserService;
    @Autowired
    LoginService loginService;
    @Autowired
    UserRoleService userRoleService;
    @Autowired
    UnifiedSystemUserService unifiedSystemUserService;

    @RequestMapping(value = "/UnifiedSystemUserMenu", method = RequestMethod.GET)
    public ModelAndView showUnifiedSystemUserMenu(HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String forward = FWD_SUCCESS;
        UnifiedSystemUserMenuForm form = new UnifiedSystemUserMenuForm();

        form.setFormAction("UnifiedSystemUserMenu.do");
        forward = performMenuAction(form, request);
        if (FWD_FAIL.equals(forward)) {
            Errors errors = new BaseErrors();
            errors.reject("error.generic");
            redirectAttributes.addFlashAttribute(Constants.REQUEST_ERRORS, errors);
            return findForward(FWD_FAIL, form);
        } else {
            return findForward(forward, form);
        }
    }

    @Override
    protected List createMenuList(MenuForm form, HttpServletRequest request) throws Exception {
        List<SystemUser> systemUsers = new ArrayList<>();

        String stringStartingRecNo = (String) request.getAttribute("startingRecNo");
        int startingRecNo = Integer.parseInt(stringStartingRecNo);

        systemUsers = systemUserService.getPage(startingRecNo);

        List<UnifiedSystemUser> unifiedUsers = getUnifiedUsers(systemUsers);

        request.setAttribute("menuDefinition", "UnifiedSystemUserMenuDefinition");

        setDisplayPageBounds(request, systemUsers.size(), startingRecNo, systemUserService);

        return unifiedUsers;
    }

    private List<UnifiedSystemUser> getUnifiedUsers(List<SystemUser> systemUsers) {

        List<UnifiedSystemUser> unifiedUsers = new ArrayList<>();

        List<Login> loginUsers = loginService.getAll();

        HashMap<String, Login> loginMap = createLoginMap(loginUsers);

        for (SystemUser user : systemUsers) {
            UnifiedSystemUser unifiedUser = createUnifiedSystemUser(loginMap, user);
            unifiedUsers.add(unifiedUser);
        }

        return unifiedUsers;
    }

    private UnifiedSystemUser createUnifiedSystemUser(HashMap<String, Login> loginMap, SystemUser user) {

        UnifiedSystemUser unifiedUser = new UnifiedSystemUser();
        unifiedUser.setFirstName(user.getFirstName());
        unifiedUser.setLastName(user.getLastName());
        unifiedUser.setLoginName(user.getLoginName());
        unifiedUser.setSystemUserId(user.getId());
        unifiedUser.setActive(user.getIsActive());

        Login login = loginMap.get(user.getLoginName());

        if (login != null) {
            unifiedUser.setExpDate(DateUtil.formatDateAsText(login.getPasswordExpiredDate()));
            unifiedUser.setDisabled(login.getAccountDisabled());
            unifiedUser.setLocked(login.getAccountLocked());
            unifiedUser.setTimeout(login.getUserTimeOut());
            unifiedUser.setLoginUserId(login.getId());
        }
        return unifiedUser;
    }

    private HashMap<String, Login> createLoginMap(List<Login> loginUsers) {
        HashMap<String, Login> loginMap = new HashMap<>();

        for (Login login : loginUsers) {
            loginMap.put(login.getLoginName(), login);
        }

        return loginMap;
    }

    @Override
    protected String getDeactivateDisabled() {
        return "false";
    }

    @Override
    protected int getPageSize() {
        return SystemConfiguration.getInstance().getDefaultPageSize();
    }

    @RequestMapping(value = "/DeleteUnifiedSystemUser", method = RequestMethod.POST)
    public ModelAndView showDeleteUnifiedSystemUser(HttpServletRequest request,
            @ModelAttribute("form") UnifiedSystemUserMenuForm form, BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            saveErrors(result);
            return findForward(FWD_FAIL_DELETE, form);
        }
        List<String> selectedIDs = form.getSelectedIDs();
        List<Login> loginUsers = new ArrayList<>();
        List<SystemUser> systemUsers = new ArrayList<>();
        List<UserRole> userRoles = new ArrayList<>();

        String sysUserId = getSysUserId(request);

        for (int i = 0; i < selectedIDs.size(); i++) {
            String systemUserId = UnifiedSystemUser.getSystemUserIDFromCombinedID(selectedIDs.get(i));

            if (!GenericValidator.isBlankOrNull(systemUserId)) {
                SystemUser systemUser = new SystemUser();
                systemUser.setId(systemUserId);
                systemUser.setSysUserId(sysUserId);
                systemUsers.add(systemUser);
            }

            String loginUserId = UnifiedSystemUser.getLoginUserIDFromCombinedID(selectedIDs.get(i));

            if (!GenericValidator.isBlankOrNull(loginUserId)) {
                Login loginUser = new Login();
                loginUser.setId(loginUserId);
                loginUser.setSysUserId(sysUserId);
                loginUsers.add(loginUser);
            }
        }

        for (SystemUser systemUser : systemUsers) {
            List<String> roleIds = userRoleService.getRoleIdsForUser(systemUser.getId());

            for (String roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setSystemUserId(systemUser.getId());
                userRole.setRoleId(roleId);
                userRole.setSysUserId(sysUserId);
                userRoles.add(userRole);
            }
        }

        try {
            unifiedSystemUserService.deleteData(userRoles, systemUsers, loginUsers, getSysUserId(request));
        } catch (LIMSRuntimeException lre) {

            if (lre.getException() instanceof org.hibernate.StaleObjectStateException) {
                result.reject("errors.OptimisticLockException", "errors.OptimisticLockException");
            } else {
                result.reject("errors.DeleteException", "errors.DeleteException");
            }
            saveErrors(result);
            return findForward(FWD_FAIL_DELETE, form);

        }

        return findForward(FWD_SUCCESS_DELETE, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "haitiMasterListsPageDefinition";
        } else if (FWD_FAIL.equals(forward)) {
            return "redirect:/MasterListsPage.do";
        } else if (FWD_SUCCESS_DELETE.equals(forward)) {
            return "redirect:/UnifiedSystemUserMenu.do";
        } else if (FWD_FAIL_DELETE.equals(forward)) {
            return "redirect:/UnifiedSystemUserMenu.do";
        } else {
            return "PageNotFound";
        }
    }

    @Override
    protected String getPageTitleKey() {
        return "unifiedSystemUser.browser.title";
    }

    @Override
    protected String getPageSubtitleKey() {
        return "unifiedSystemUser.browser.title";
    }
}
