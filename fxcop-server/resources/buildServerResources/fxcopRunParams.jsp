<%--
  ~ Copyright 2000-2009 JetBrains s.r.o.
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
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="What to inspect">
  <tr>
    <c:set var="onclick">
      $('fxcop.project').disabled = this.checked;
      $('fxcop.files').disabled = !this.checked;
      $('fxcop.files_exclude').disabled = !this.checked;
      $('fxcop.files').focus();
      BS.VisibilityHandlers.updateVisibility($('fxcop.files'));
      BS.VisibilityHandlers.updateVisibility($('fxcop.project'));
    </c:set>

    <th>
      <props:radioButtonProperty name="fxcop.what"
                                 value="files"
                                 id="mod-files"
                                 onclick="${onclick}"
                                 checked="${propertiesBean.properties['fxcop.what'] == 'files'}"/>
      <label for="mod-files">Assemblies:</label></th>
    <td><span>
      <props:multilineProperty name="fxcop.files"
                               linkTitle="Type assembly files or wildcards"
                               cols="40" rows="5"
                               expanded="true"
                               disabled="${propertiesBean.properties['fxcop.what'] != 'files'}"/>
      <props:multilineProperty name="fxcop.files_exclude"
                               linkTitle="Exclude assembly files by wildcard"
                               cols="40" rows="3"
                               expanded="true"
                               disabled="${propertiesBean.properties['fxcop.what'] != 'files'}"/>
      </span>
      <span class="smallNote">Assembly file names relative to checkout root separated by spaces.<br/>
        Ant-like wildcards are allowed.<br/>
        Example: bin\*.dll</span>
      <span class="error" id="error_fxcop.files"></span>
      <span class="error" id="error_fxcop.files_exclude"></span>
    </td>
  </tr>

  <tr>
    <c:set var="onclick">
      $('fxcop.files').disabled = this.checked;
      $('fxcop.files_exclude').disabled = this.checked;
      $('fxcop.project').disabled = !this.checked;
      $('fxcop.project').focus();
      BS.VisibilityHandlers.updateVisibility($('fxcop.files'));
      BS.VisibilityHandlers.updateVisibility($('fxcop.files_exclude'));
      BS.VisibilityHandlers.updateVisibility($('fxcop.project'));
    </c:set>
    <th>
      <props:radioButtonProperty name="fxcop.what"
                                 value="project"
                                 id="mod-project"
                                 onclick="${onclick}"
                                 checked="${propertiesBean.properties['fxcop.what'] == 'project'}"/>
      <label for="mod-project">FxCop project file:</label></th>
    <td>
      <span>
        <props:textProperty name="fxcop.project" className="longField"
                            disabled="${propertiesBean.properties['fxcop.what'] != 'project'}"/>
        </span>
      <span class="smallNote">FxCop project file name relative to checkout root</span>
      <span class="error" id="error_fxcop.project"></span></td>
    </td>
  </tr>

</l:settingsGroup>

<l:settingsGroup title="FxCop options">
  <tr>
    <th><label for="fxcop.search_in_gac">Search referenced assemblies in GAC</label></th>
    <td>
      <props:checkboxProperty name="fxcop.search_in_gac"/>
      <span class="error" id="error_fxcop.search_in_gac"></span>
    </td>
  </tr>
  <tr>
    <th><label for="fxcop.search_in_dirs">Search referenced assemblies in directories</label></th>
    <td>
      <props:textProperty name="fxcop.search_in_dirs" className="longField"/>
      <span class="error" id="error_fxcop.search_in_dirs"></span>
      <span
          class="smallNote">List of directories (relative to checkout root and separated by spaces) to search referenced assemblies in.<br/>
      Sets /d: options for FxCopCmd</span>
    </td>
  </tr>
  <tr>
    <th><label for="fxcop.ignore_generated_code">Ignore generated code</label></th>
    <td>
      <props:checkboxProperty name="fxcop.ignore_generated_code"/>
      <span class="error" id="error_fxcop.ignore_generated_code"></span>
      <span class="smallNote">Sets /ignoregeneratedcode for FxCopCmd (note: it's supported since FxCop 1.36)</span>
    </td>
  </tr>
  <tr>
    <th><label for="fxcop.report_xslt">Report XSLT file:</label></th>
    <td><props:textProperty name="fxcop.report_xslt" className="longField"/>
      <span class="error" id="error_fxcop.report_xslt"></span>
      <span class="smallNote">XSLT file used to generate HTML report.<br/>
        Leave it empty to skip generation,<br/>
        set to '%system.FxCopRoot%/Xml/FxCopReport.xsl' to get standard FxCop report,<br/>
        or specify any custom file relative to checkout directory</span>
    </td>
  </tr>
  <tr>
    <th><label for="fxcop.addon_options">Additional FxCopCmd options: </label></th>
    <td><props:textProperty name="fxcop.addon_options" className="longField"/>
      <span class="error" id="error_fxcop.addon_options"></span>
      <span class="smallNote">Additional options to be added to FxCopCmd.exe command line</span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="Build success criteria">

  <tr>
    <th><label for="fxcop.fail.error.limit">Errors limit:</label></th>
    <td><props:textProperty name="fxcop.fail.error.limit" style="width:6em;" maxlength="12"/>
      <span class="error" id="error_fxcop.fail.error.limit"></span>
      <span class="smallNote">Fail the build if the specified number of errors is exceeded.</span>
    </td>
  </tr>

  <tr>
    <th class="noBorder"><label for="fxcop.fail.warning.limit">Warnings limit:</label></th>
    <td class="noBorder"><props:textProperty name="fxcop.fail.warning.limit" style="width:6em;" maxlength="12"/>
      <span class="error" id="error_fxcop.fail.warning.limit"></span>
      <span class="smallNote">Fail the build if the specified number of warnings is exceeded. Leave blank if there is no limit.</span></td>
  </tr>

  <tr>
    <th><label for="fxcop.fail_on_analysis_error">Fail on analysis errors</label></th>
    <td>
      <props:checkboxProperty name="fxcop.fail_on_analysis_error"/>
      <span class="error" id="error_fxcop.fail_on_analysis_error"></span>
      <span class="smallNote">Fails build on analysis errors from FxCop such as:<br/>
        ANALYSIS_ERROR ASSEMBLY_LOAD_ERROR ASSEMBLY_REFERENCES_ERROR PROJECT_LOAD_ERROR RULE_LIBRARY_LOAD_ERROR UNKNOWN_ERROR OUTPUT_ERROR
      </span>
    </td>
  </tr>

</l:settingsGroup>

<l:settingsGroup title="FxCop location">

  <tr>
    <th><label for="fxcop.root">FxCop installation root: <l:star/></label></th>
    <td><props:textProperty name="fxcop.root" className="longField"/>
      <span class="error" id="error_fxcop.root"></span>
      <span class="smallNote">Place to call FxCopCmd.exe from.<br/>
                              Defaults to '%system.FxCopRoot%' (autodetection on agent side)</span>
    </td>
  </tr>

</l:settingsGroup>
