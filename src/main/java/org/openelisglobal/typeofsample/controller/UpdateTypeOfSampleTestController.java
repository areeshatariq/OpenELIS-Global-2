package org.openelisglobal.typeofsample.controller;

import javax.servlet.http.HttpServletRequest;

import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.typeofsample.form.TypeOfSampleTestForm;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

//seemingly unused controller
@Controller
public class UpdateTypeOfSampleTestController extends BaseController {
    @RequestMapping(value = "/UpdateTypeOfSampleTest", method = RequestMethod.GET)
    public ModelAndView showUpdateTypeOfSampleTest(HttpServletRequest request,
            @ModelAttribute("form") TypeOfSampleTestForm form) {
        String forward = FWD_SUCCESS;
        if (form == null) {
            form = new TypeOfSampleTestForm();
        }
        form.setFormAction("");
        Errors errors = new BaseErrors();

        return findForward(forward, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "/TypeOfSampleTest.do";
        } else if (FWD_FAIL.equals(forward)) {
            return "typeOfSampleTestDefinition";
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
