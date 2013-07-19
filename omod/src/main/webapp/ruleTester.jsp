<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<link href="${pageContext.request.contextPath}/moduleResources/dss/dss.css" type="text/css" rel="stylesheet" />

<p>
    Please choose a rule to test:
</p>
<form name="input" action="ruleTester.form" method="get">
<select name="ruleName">
<c:forEach items="${rules}" var="rule">
<option value="${rule.tokenName}"
<c:if test="${rule.tokenName==lastRuleName}">
selected
</c:if>
>${rule.tokenName}</option>
</c:forEach>
</select>
<p>
Please enter the patient's mrn:
</p>
<input type="text" name="mrn" value="${lastMRN}"/>
<p>
Please enter the mode:
</p>
<input type="text" name="mode" value="${mode}"/>
<input type="submit" value="Test Rule">
</form>
<p>
    Result from running <b>${lastRuleName}</b> was:
</p><br/><br/>
<c:if test="${!empty runResult}">

    <b>${runResult}</b>

</c:if>

<br />
<span>DEBUG</span>
<br />
<span>PATIENT:</span>
<span>${patient}</span>
<span> | </span>
<br />
<span>MRN:</span>
<span>${mrn}</span>
<span> | </span>
<br />
<span>ruleName:</span>
<span>${ruleName}</span>
<span> | </span>
<br />
<span>NUMBER OF RULES</span>
<span>${numberOfRules}</span>
<span> | </span>
<br />
<span>RULE TO EVALUATE:</span>
<span>${ruleToEvaluate}</span>
<span> | </span>
<br />
<span>AGE RESTRICTION SATISFIED:</span>
<span>${ageRestriction}</span>
<span> | </span>
<br />
<span>RESULT SIZE:</span>
<span>${resultSize}</span>
<span> | </span>
<br />
<span>RESULT IS NULL:</span>
<span>${resultIsNull}</span>
<span> | </span>
<br />
<span>PROGRESS:</span>
<span>${progress}</span>
<span> | </span>





<%@ include file="/WEB-INF/template/footer.jsp" %>