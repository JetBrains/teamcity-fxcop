<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
  Assemblies to inspect: <strong><props:displayValue name="fxcop.files" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Additional FxCopCmd options: <strong><props:displayValue name="fxcop.addon_options" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  FxCop installation root: <strong><props:displayValue name="fxcop.root" emptyValue="not specified"/></strong>
</div>
