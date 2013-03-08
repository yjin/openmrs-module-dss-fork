<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2>Map a rule to a list of concepts</h2>

<link href="${pageContext.request.contextPath}/moduleResources/dss/dss.css" type="text/css" rel="stylesheet" />

<br />

<fieldset>
    <legend>Add a new mapping</legend>
    <form name="input" action="ruleMapper.form" method="get">
        <p>
            <label>Please choose a rule to map:
                <select name="ruleId">
                    <c:forEach items="${rules}" var="rule">
                        <option value="${rule.ruleId}">${rule.tokenName}</option>
                    </c:forEach>
                </select>
            </label>
        </p>
        <p>
            <label>Please select one or more concepts:
                <input value="" name="conceptIds" placeholder="1,2,3,4,5">
            </label>
        </p>
        <input type="submit" value="Map">
    </form>    
</fieldset>

<br />

<fieldset>
    <legend>Current mappings</legend>
    <ul>
        <c:forEach items="${mappings}" var="entry">
            <li>${entry.key.name} (conceptId ${entry.key.conceptId}) 
                <ul>
                    <c:forEach items="${entry.value}" var="rule">
                        <li>${rule.tokenName} (ruleId ${rule.ruleId})</li>
                    </c:forEach>
                </ul>
            </li>
        </c:forEach>
    </ul>    
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp" %>