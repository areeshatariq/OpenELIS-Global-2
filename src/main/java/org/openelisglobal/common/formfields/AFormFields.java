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
*
* Contributor(s): CIRG, University of Washington, Seattle WA.
*/
package org.openelisglobal.common.formfields;

import java.util.HashMap;
import java.util.Map;

public abstract class AFormFields {

    protected abstract HashMap<FormFields.Field, Boolean> getSetAttributes();

    protected abstract HashMap<FormFields.Field, Boolean> getDefaultAttributes();

    public Map<FormFields.Field, Boolean> getFieldFormSet() throws IllegalStateException {

        boolean patientRequired;
        Map<FormFields.Field, Boolean> defaultAttributes = getDefaultAttributes();
        Map<FormFields.Field, Boolean> setAttributes = getSetAttributes();

        patientRequired = defaultAttributes.get(FormFields.Field.PatientRequired);
        if (defaultAttributes == null) {
            defaultAttributes = new HashMap<>();
        }

        if (setAttributes == null) {
            setAttributes = new HashMap<>();
        }

        defaultAttributes.putAll(setAttributes);
        patientRequired = defaultAttributes.get(FormFields.Field.PatientRequired);

        return defaultAttributes;
    }
}
