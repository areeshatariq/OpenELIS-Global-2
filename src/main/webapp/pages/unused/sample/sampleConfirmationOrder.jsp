<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.openelisglobal.common.formfields.FormFields.Field,
                 org.openelisglobal.common.provider.validation.AccessionNumberValidatorFactory,
                 org.openelisglobal.common.provider.validation.IAccessionNumberValidator,
                 org.openelisglobal.common.util.ConfigurationProperties,
                 org.openelisglobal.common.util.ConfigurationProperties.Property,
                 org.openelisglobal.common.action.IActionConstants,
                 org.openelisglobal.common.formfields.FormFields,
                 org.openelisglobal.common.util.DateUtil,
                 org.openelisglobal.internationalization.MessageUtil,
                 org.openelisglobal.common.util.Versioning" %>

<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %>

 
<bean:define id="entryDate" name="${form.formName}" property="currentDate"/>
<bean:define id="sampleOrderItem" name='${form.formName}' property="sampleOrderItems" type="org.openelisglobal.sample.bean.SampleOrderItem" />

<%!
    String path = "";
    String basePath = "";
    boolean useCollectionDate = true;
    boolean useInitialSampleCondition = false;
    boolean useCollector = false;
    boolean autofillCollectionDate = true;
    boolean useReferralSiteList = false;
    boolean useReferralSiteCode = false;
    boolean useProviderInfo = false;
    boolean patientRequired = false;
    boolean trackPayment = false;
    boolean requesterLastNameRequired = false;
    boolean acceptExternalOrders = false;
    boolean restrictNewReferringSiteEntries = false;
    IAccessionNumberValidator accessionNumberValidator;
%>
<%
    path = request.getContextPath();
    basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
    useCollectionDate = FormFields.getInstance().useField( Field.CollectionDate );
    useInitialSampleCondition = FormFields.getInstance().useField( Field.InitialSampleCondition );
    useCollector = FormFields.getInstance().useField( Field.SampleEntrySampleCollector );
    autofillCollectionDate = ConfigurationProperties.getInstance().isPropertyValueEqual( Property.AUTOFILL_COLLECTION_DATE, "true" );
    useReferralSiteList = FormFields.getInstance().useField( FormFields.Field.RequesterSiteList );
    useReferralSiteCode = FormFields.getInstance().useField( FormFields.Field.SampleEntryReferralSiteCode );
    useProviderInfo = FormFields.getInstance().useField( FormFields.Field.ProviderInfo );
    patientRequired = FormFields.getInstance().useField( FormFields.Field.PatientRequired );
    trackPayment = ConfigurationProperties.getInstance().isPropertyValueEqual( Property.TRACK_PATIENT_PAYMENT, "true" );
    accessionNumberValidator = new AccessionNumberValidatorFactory().getValidator();
    requesterLastNameRequired = FormFields.getInstance().useField( Field.SampleEntryRequesterLastNameRequired );
    acceptExternalOrders = ConfigurationProperties.getInstance().isPropertyValueEqual( Property.ACCEPT_EXTERNAL_ORDERS, "true" );
    restrictNewReferringSiteEntries = ConfigurationProperties.getInstance().isPropertyValueEqual(Property.restrictFreeTextRefSiteEntry, "true");

%>

<script type="text/javascript" src="<%=basePath%>scripts/additional_utilities.js"></script>
<script type="text/javascript" src="<%=basePath%>scripts/utilities.js?ver=<%= Versioning.getBuildNumber() %>" ></script>
<script type="text/javascript" src="scripts/jquery.asmselect.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="scripts/ajaxCalls.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="scripts/laborder.js?ver=<%= Versioning.getBuildNumber() %>"></script>


<link rel="stylesheet" type="text/css" href="css/jquery.asmselect.css?ver=<%= Versioning.getBuildNumber() %>"/>


<script type="text/javascript">
    var requesterInfoHash = {};

    function checkAccessionNumber(accessionNumber) {
        if (!fieldIsEmptyById(accessionNumber.id)) {
            validateAccessionNumberOnServer(false, false, accessionNumber.id, accessionNumber.value, processAccessionSuccess);
        }
        else {
            fieldValidator.setFieldValidity(false, accessionNumber.id);
            setFieldErrorDisplay(accessionNumber);
            setSaveButton();
        }
    }

    function processAccessionSuccess(xhr) {
        //alert(xhr.responseText);
        var formField = xhr.responseXML.getElementsByTagName("formfield").item(0);
        var message = xhr.responseXML.getElementsByTagName("message").item(0);
        var messageValue = message.firstChild.nodeValue;
        var success = messageValue == "SAMPLE_NOT_FOUND" || messageValue == "valid";
        var labElement = $(formField.firstChild.nodeValue);

        selectFieldErrorDisplay(success, labElement);

        if (!success) {
            if (messageValue == "SAMPLE_FOUND") {
                alert("<%= MessageUtil.getMessage("sample.entry.invalid.accession.number.used") %>");
            } else {
                alert(message.firstChild.nodeValue);
            }
        }
        setSaveButton();
    }


    function getNextAccessionNumber() {
        generateNextScanNumber(processScanSuccess);
    }

    function processScanSuccess(xhr) {
        var formField = xhr.responseXML.getElementsByTagName("formfield").item(0);
        var returnedData = formField.firstChild.nodeValue;

        var message = xhr.responseXML.getElementsByTagName("message").item(0);
        var success = message.firstChild.nodeValue == "valid";
        var target = $("labNo");

        target.value = success ? returnedData : "";

        selectFieldErrorDisplay(success, target);
        fieldValidator.setFieldValidity(success, target.id);

        setSaveButton();
    }

    function getRequestersForOrg( textValue ) {
        var orgEnteredValue = $("siteId").value,
                orgSelectList = $("orgRequesterId").options,
                orgKey = 0,
                searchCount = 0,
                requesterList;

        for (searchCount; searchCount < orgSelectList.length; searchCount++) {
            if (orgSelectList[searchCount].text.toUpperCase() == orgEnteredValue) {
                orgKey = orgSelectList[searchCount].value;
                break;
            }
        }

        if (orgKey == 0) { //if no match with site list
            jQuery("#newRequesterName").val(textValue);
            requesterList = $("personRequesterId");
            requesterList.options.length = 0;
            addOptionToSelect(requesterList, '<%=MessageUtil.getMessage("sample.entry.requester.new")%>', "0");
        } else {
            getProvidersForOrg( orgKey, processRequestersSuccess);
        }
    }

    function processRequestersSuccess(xhr) {
        //alert(xhr.responseText);

        var requesters = xhr.responseXML.getElementsByTagName("requester");
        var requesterList = $("personRequesterId");
        requesterList.options.length = 0;

        if (requesters.length == 0) {
            addOptionToSelect(requesterList, '<%=MessageUtil.getMessage("sample.entry.requester.new")%>', "0");
        } else {
            requesterInfoHash = {};
            addOptionToSelect(requesterList, '', '');
            for (var i = 0; i < requesters.length; ++i) {
                addRequester(requesterList, requesters[i]);
            }
            addOptionToSelect(requesterList, '<%=MessageUtil.getMessage("sample.entry.requester.new")%>', "0");
        }
    }

    function addRequester(requesterList, requesterXml) {
        var display = requesterXml.getAttribute("lastName") + ', ' + requesterXml.getAttribute("firstName");
        if( display.length < 3){
           display = requesterXml.getAttribute("email");
            if( display.length < 3){
                display = requesterXml.getAttribute("phone");
                if( display.length < 3){
                    display = requesterXml.getAttribute("fax");
                    if(display < 3){
                        return;
                    }
                }
            }
        }


        addOptionToSelect(requesterList,
                display,
                requesterXml.getAttribute("id"));

        requesterInfoHash[requesterXml.getAttribute("id")] = {'firstName': requesterXml.getAttribute("firstName"),
            'lastName': requesterXml.getAttribute("lastName"),
            'phone': requesterXml.getAttribute("phone"),
            'fax': requesterXml.getAttribute("fax"),
            'email': requesterXml.getAttribute("email")};
    }

    function populateRequesterDetail(requesterSelector) {
        var requesterId = requesterSelector[requesterSelector.selectedIndex].value;

        var details = requesterInfoHash[requesterId];

        if (details) {
            setRequesterDetails(details);
        } else {
            setRequesterDetails({'firstName': '', 'lastName': '', 'phone': '', 'fax': '', 'email': ''});
        }
    }

    function setRequesterDetails(details) {
        $("requesterFirstName").value = details["firstName"];
        $("requesterLastName").value = details["lastName"];
        $("requesterPhone").value = details["phone"];
        $("requesterFax").value = details["fax"];
        $("requesterEMail").value = details["email"];
    }

    function sc_validatePhoneNumber( phoneElement){
        validatePhoneNumberOnServer( phoneElement, sc_processPhoneSuccess);
    }

    function  sc_processPhoneSuccess(xhr){
        //alert(xhr.responseText);

        var formField = xhr.responseXML.getElementsByTagName("formfield").item(0);
        var message = xhr.responseXML.getElementsByTagName("message").item(0);
        var success = false;

        if (message.firstChild.nodeValue == "valid"){
            success = true;
        }
        var labElement = formField.firstChild.nodeValue;

        selectFieldErrorDisplay( success, $(labElement));
        fieldValidator.setFieldValidity(success, labElement  );

        if( !success ){
            alert( message.firstChild.nodeValue );
        }

        setOrderModified();
    }

    function addOptionToSelect( selectElement, text, value ){
        var option = document.createElement("OPTION");
        option.text = text;
        option.value = value;
        selectElement.options.add(option);
    }

    function setOrderModified(){
        jQuery("#orderModified").val("true");
        orderChanged = true;
        if( window.makeDirty ){ makeDirty(); }
        if( window.setSave){
            setSave()
        }else if( window.setSaveButton){
            setSaveButton();
        }
    }

    function sampleConfirmationValid(){
        var enable = true;

        jQuery(".required").each( function(i, val){
            var elValue = jQuery(val).val();
            if( !elValue.trim().length || elValue == 0){
                enable = false;
                return;
            }
        });

        return enable;
    }
</script>

<form:hidden path="sampleOrderItems.modified" name='${form.formName}' id="orderModified"  />
<form:hidden path="sampleOrderItems.newRequesterName" name='${form.formName}' id="newRequesterName" />

<table style="width:70%" border="0">
    <logic:empty name="${form.formName}" property="sampleOrderItems.labNo">
        <tr>
            <td>
                <%=MessageUtil.getContextualMessage( "quick.entry.accession.number" )%>:
                <span class="requiredlabel">*</span>
            </td>
            <td style="width:15%">
                <app:text name="${form.formName}" property="sampleOrderItems.labNo"
                          maxlength='<%= Integer.toString(accessionNumberValidator.getMaxAccessionLength())%>'
                          onchange="checkAccessionNumber(this);makeDirty();setOrderModified();" styleClass="text"
                          id="labNo"/>
            </td>
            <td id="generate">
                <spring:message code="sample.entry.scanner.instructions"/>
                <input type="button"
                       value='<%=MessageUtil.getMessage("sample.entry.scanner.generate")%>'
                       onclick="getNextAccessionNumber(); makeDirty();" class="textButton">
            </td>
        </tr>
    </logic:empty>
    <logic:notEmpty name="${form.formName}" property="sampleOrderItems.labNo">
        <tr>
            <td></td>
            <td style="width:15%"></td>
            <td></td>
        </tr>
    </logic:notEmpty>
    <tr>
        <td>
            <spring:message code="quick.entry.received.date"/>:
            <span class="requiredlabel">*</span>
			<span style="font-size: xx-small; "><%=DateUtil.getDateUserPrompt()%>
			</span>
        </td>
        <td colspan="2">
            <app:text name="${form.formName}" property="sampleOrderItems.receivedDateForDisplay"
                      onchange="checkValidDate(this);setOrderModified();"
                      onkeyup="addDateSlashes(this,event);"
                      styleClass="text required"
                      maxlength="10"
                      id="receivedDate"/>

            <% if( FormFields.getInstance().useField( Field.SampleEntryUseReceptionHour ) ){ %>
            <spring:message code="sample.receptionTime"/>:
            <html:text name="${form.formName}"
                       onkeyup="filterTimeKeys(this, event);"
                       property="sampleOrderItems.receivedTime"
                       id="receivedTime"
                       maxlength="5"
                       onblur="makeDirty(); updateFieldValidity(checkValidTimeEntry(this, true), this.id );"/>

            <% } %>

        </td>
    </tr>
</table>
<hr/>
<h3>
    <spring:message code="sample.entry.requester"/>
</h3>
<table>
    <tr>
        <td>
            <spring:message code="organization.site"/>
        </td>
        <td colspan="5">
            <!-- N.B. this is replaced by auto complete -->
            <html:select id="orgRequesterId"
                         name="${form.formName}"
                         property="sampleOrderItems.referringSiteId"
                         onchange="getRequestersForOrg();setOrderModified();"
                    >
                <option value=""></option>
                <logic:iterate name="${form.formName}" property="sampleOrderItems.referringSiteList"
                               id="org" type="org.openelisglobal.common.util.IdValuePair">
                    <option value="<%=org.getId() %>" <%= org.getId().equals( sampleOrderItem.getReferringSiteId()) ? "selected = 'selected'" : "" %> ><%= org.getValue().toUpperCase() %>
                    </option>
                </logic:iterate>
            </html:select>
        </td>
    </tr>
    <tr>
        <td>
            <spring:message code="sample.entry.contact"/>
        </td>
        <td colspan="5">
            <html:select name='${form.formName}'
                         property="sampleOrderItems.providerId"
                         id="personRequesterId"
                         onchange="populateRequesterDetail(this); makeDirty();setOrderModified();">
                <option value="0"><spring:message code="sample.entry.requester.new"/></option>
            </html:select>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <spring:message code="person.firstName"/>
        </td>
        <td>
            <spring:message code="person.lastName"/>
        </td>
        <td>
            <spring:message code="person.phone"/>
        </td>
        <td>
            <spring:message code="person.fax"/>
        </td>
        <td>
            <spring:message code="person.email"/>
        </td>

    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <form:input path="sampleOrderItems.providerFirstName"
                       id="requesterFirstName" onchange="setOrderModified()"/>
        </td>
        <td>
            <form:input path="sampleOrderItems.providerLastName"
                       id="requesterLastName" onchange="setOrderModified()"/>
        </td>
        <td>
            <form:input path="sampleOrderItems.providerWorkPhone"
                       id="requesterPhone" onchange="sc_validatePhoneNumber(this);"/>
        </td>
        <td>
            <form:input path="sampleOrderItems.providerFax"
                       id="requesterFax" onchange="sc_validatePhoneNumber(this);"/>
        </td>
        <td>
            <form:input path="sampleOrderItems.providerEmail"
                       id="requesterEMail" onchange="setOrderModified()"/>
        </td>

    </tr>
</table>

<script type="text/javascript">
   jQuery(document).ready( function() {
        //dropdown defined in customAutocomplete.js
        autoCompId = 'siteId'; //needs to be set before the dropdown is created N.B. shouuld be passed in as arg
        var dropdown = jQuery( "select#orgRequesterId" );
        autoCompleteWidth = dropdown.width() + 66 + 'px';
        <% if(restrictNewReferringSiteEntries) { %>
			clearNonMatching = true;
		<% } else {%>
				clearNonMatching = false;
		<% } %>
        capitialize = true;
        dropdown.combobox();
        invalidLabID = '<spring:message code="error.site.invalid"/>'; // Alert if value is typed that's not on list. FIXME - add badmessage icon
        maxRepMsg = '<spring:message code="sample.entry.project.siteMaxMsg"/>';

        //resultCallBack defined in customAutocomplete.js
        resultCallBack = function(textValue) {
            getRequestersForOrg(textValue );
            makeDirty();
            setOrderModified();
        };
    } );
</script>