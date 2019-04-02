package spring.mine.dataexchange.order.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import spring.mine.common.validator.ValidationHelper;
import spring.mine.dataexchange.order.form.ElectronicOrderViewForm;

@Component
public class ElectronicOrderViewFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ElectronicOrderViewForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ElectronicOrderViewForm form = (ElectronicOrderViewForm) target;

		ValidationHelper.validateOptionFieldIgnoreCase(form.getSortOrder(), "sortOrder", errors,
				new String[] { "lastupdated", "externalId", "statusId" });

		ValidationHelper.validateFieldMin(form.getPage(), "page", errors, 0);

		// eOrders doesn't need to be validated as it is purely used for display
	}

}
