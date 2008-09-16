<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
  <c:choose>
    <c:when test="${propertiesBean.properties['fxcop.what'] == 'project'}">
      FxCop project file:
      <strong><props:displayValue name="fxcop.project" emptyValue="not specified"/></strong>
    </c:when>
    <c:otherwise>
      Assemblies to inspect:
      <strong><props:displayValue name="fxcop.files" emptyValue="not specified"/></strong>
    </c:otherwise>
  </c:choose>
</div>

<div class="parameter">
  Additional FxCopCmd options: <strong><props:displayValue name="fxcop.addon_options" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Report XSLT file: <strong><props:displayValue name="fxcop.report_xslt" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  FxCop installation root: <strong><props:displayValue name="fxcop.root" emptyValue="not specified"/></strong>
</div>
