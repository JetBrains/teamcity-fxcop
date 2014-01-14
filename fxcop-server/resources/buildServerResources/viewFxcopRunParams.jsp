<%--
  ~ Copyright 2000-2014 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
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
<jsp:useBean id="constants" class="jetbrains.buildServer.fxcop.server.FxCopConstantsBean"/>

<c:choose>
  <c:when test="${propertiesBean.properties[constants.detectionModeKey] == constants.detectionModeAuto}">
    <div class="parameter">
      FxCop installation root: <strong>autodetected</strong>
    </div>
    <div class="parameter">
      FxCop version: <strong><props:displayValue name="${constants.versionKey}" emptyValue="any detected"/></strong>
    </div>
  </c:when>
  <c:otherwise>
    <div class="parameter">
      FxCop installation root: <strong><props:displayValue name="${constants.rootKey}" emptyValue="not specified"/></strong>
    </div>
  </c:otherwise>
</c:choose>

<c:choose>
  <c:when test="${propertiesBean.properties[constants.whatToInspectKey] == constants.whatToInspectProjectValue}">
    <div class="parameter">
      FxCop project file: <strong><props:displayValue name="${constants.projectKey}" emptyValue="not specified"/></strong>
    </div>
  </c:when>
  <c:otherwise>
    <div class="parameter">
      Assemblies to inspect: <strong><props:displayValue name="${constants.filesKey}" emptyValue="not specified"/></strong>
    </div>
    <div class="parameter">
      Assemblies to exclude: <strong><props:displayValue name="${constants.filesExcludeKey}" emptyValue="not specified"/></strong>
    </div>
  </c:otherwise>
</c:choose>

<div class="parameter">
  Additional FxCopCmd options: <strong><props:displayValue name="${constants.addtionalOptionsKey}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Ignore generated code: <strong><props:displayCheckboxValue name="${constants.ignoreGeneratedCodeKey}"/></strong>
</div>

<div class="parameter">
  Search dependencies in GAC: <strong><props:displayCheckboxValue name="${constants.searchInGacKey}"/></strong>
</div>

<div class="parameter">
  Search dependencies in directories: <strong><props:displayValue name="${constants.searchDirsKey}" emptyValue="empty list" /></strong>
</div>

<div class="parameter">
  Fail on analysis errors: <strong><props:displayCheckboxValue name="${constants.failOnAnalysisErrorKey}"/></strong>
</div>

<div class="parameter">
  Report XSLT file: <strong><props:displayValue name="${constants.reportXsltKey}" emptyValue="not specified"/></strong>
</div>
