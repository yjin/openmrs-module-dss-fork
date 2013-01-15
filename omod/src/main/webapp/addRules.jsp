<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<link href="${pageContext.request.contextPath}/moduleResources/dss/dss.css" type="text/css" rel="stylesheet" />

<c:choose>

    <c:when test="${patientId == ''}">         
        <p><b>Run rules for the following patient:</b></p>
        <form name="input" action="addRules.form" method="post">
            <table style="padding: 5px">
                <tr>
                    <td>Paste your MLM rule here</td>
                </tr>
                <tr>
                    <td><textarea type="text" name="mlmRule" value="${mlmRuleSource}" size="20"></textarea></td>
                </tr>
                <tr>
                    <td colspan="2" style="text-align:right;"><input type="submit" value="OK"></td>
                </tr>
            </table>
        </form>
    </c:when>   

    <c:otherwise>
        Running all rules in dss_rule table...<br/><br />
        <c:forEach items="${rules}" var="rule">
            Results for ${rule.tokenName} rule: ${rule.result} <br/>
        </c:forEach>
    </c:otherwise>

</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>