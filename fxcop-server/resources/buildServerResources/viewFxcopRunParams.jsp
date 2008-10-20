<%--
  ~ Copyright (c) 2008, JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

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
  Ignore generated code: <strong><props:displayCheckboxValue name="fxcop.ignore_generated_code"/></strong>
</div>

<div class="parameter">
  Search dependencies in GAC: <strong><props:displayCheckboxValue name="fxcop.search_in_gac"/></strong>
</div>

<div class="parameter">
  Search dependencies in directories: <strong><props:displayCheckboxValue name="fxcop.search_in_dirs"/></strong>
</div>

<div class="parameter">
  Report XSLT file: <strong><props:displayValue name="fxcop.report_xslt" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  FxCop installation root: <strong><props:displayValue name="fxcop.root" emptyValue="not specified"/></strong>
</div>
