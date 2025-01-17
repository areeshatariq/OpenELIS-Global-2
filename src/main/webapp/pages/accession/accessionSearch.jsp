<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="org.openelisglobal.common.action.IActionConstants,
                 org.openelisglobal.common.util.ConfigurationProperties,
			     org.openelisglobal.common.util.ConfigurationProperties.Property,
			     org.openelisglobal.common.provider.validation.AccessionNumberValidatorFactory,
			     org.openelisglobal.common.provider.validation.IAccessionNumberValidator,
                 org.openelisglobal.common.util.Versioning,
			     org.openelisglobal.internationalization.MessageUtil" %>

<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %>

<%--
  ~ The contents of this file are subject to the Mozilla Public License
  ~ Version 1.1 (the "License"); you may not use this file except in
  ~ compliance with the License. You may obtain a copy of the License at
  ~ http://www.mozilla.org/MPL/
  ~
  ~ Software distributed under the License is distributed on an "AS IS"
  ~ basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing rights and limitations under
  ~ the License.
  ~
  ~ The Original Code is OpenELIS code.
  ~
  ~ Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
  --%>

	

<%!
	String basePath = "";
	IAccessionNumberValidator accessionNumberValidator;
 %>
<%
	String path = request.getContextPath();
	basePath = request.getScheme() + "://" + request.getServerName() + ":"
			+ request.getServerPort() + path + "/";

	accessionNumberValidator = new AccessionNumberValidatorFactory().getValidator();
	String accessionFormat = ConfigurationProperties.getInstance().getPropertyValue(Property.AccessionFormat);
%>

<script type="text/javascript" src="<%=basePath%>scripts/utilities.js?ver=<%= Versioning.getBuildNumber() %>" ></script>

<script type="text/javascript">

function validateEntrySize( elementValue ){
	$("retrieveTestsID").disabled = (elementValue.length == 0);
}


function doShowTests(){
	var form = document.getElementById("mainForm");
	window.location.href = '${form.formName}'.sub('Form','') + ".do?accessionNumber="  + $("searchAccessionID").value;
}

function /*void*/ handleEnterEvent(  ){
	if( $("searchAccessionID").value.length > 0){
		doShowTests();
	}
	return false;
}



</script>


<div id="PatientPage" class="colorFill" style="display:inline" >

	<h2><spring:message code="sample.entry.search"/></h2>
	<table width="40%">
	<tr >
		<td width="50%" align="right" >
			<%=MessageUtil.getContextualMessage("quick.entry.accession.number")%>
		</td>
		<td width="50%">
			<input name="accessionNumber"
			       size="20"
			       id="searchAccessionID"
			       maxlength="<%= Integer.toString(accessionNumberValidator.getMaxAccessionLength()) %>"
			       onkeyup="validateEntrySize( this.value );"
			       onblur="validateEntrySize( this.value );"
			       class="text"
			       type="text">
			<spring:message code="sample.search.scanner.instructions"/>
		</td>
	</tr>

	</table>
	<br/>
	<button type="button" name="retrieveTestsButton" id="retrieveTestsID"  onclick="doShowTests();" disabled="disabled" >
		<%= MessageUtil.getContextualMessage("resultsentry.accession.search") %>
	</button>

</div>



