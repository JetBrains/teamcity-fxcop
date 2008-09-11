<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<l:settingsGroup title="FxCop options">
  <tr>
    <c:set var="onclick">
      $('fxcop.project').disabled = true;
      $('fxcop.files').disabled = false;
      $('fxcop.files').focus();
    </c:set>
    <th><strong>What to inspect:</strong></th>
    <td>
      <forms:radioButton name="whatInspectRadioGroup"
                         id="mod-files"
                         checked="true"
                         value="fxcop.files"
                         onclick="${onclick}"/>
      <label for="mod-files">Assemblies:</label>
      <props:textProperty name="fxcop.files" className="longField"/>
    </td>
  </tr>

  <tr>
    <c:set var="onclick">
      $('fxcop.files').disabled = true;
      $('fxcop.project').disabled = false;
      $('fxcop.project').focus();
    </c:set>
    <td>&nbsp;</td>
    <td>
      <forms:radioButton name="whatInspectRadioGroup"
                         value="SPECIFIED"
                         id="mod-project"
                         checked="fxcop.project"
                         onclick="${onclick}"/>
      <label for="mod-project">FxCop project file:</label>
      <props:textProperty name="fxcop.project" className="longField"/>
    </td>
  </tr>
  <script type="text/javascript">
    // Default state
    $('fxcop.project').disabled = true;
    $('fxcop.files').disabled = false;
  </script>
  <tr>
    <th><label for="fxcop.addon_options">Additional FxCopCmd options: </label></th>
    <td><props:textProperty name="fxcop.addon_options" className="longField"/>
      <span class="error" id="error_fxcop.addon_options"></span>
      <span class="smallNote">Additional options to be added to FxCopCmd.exe command line</span>
    </td>
  </tr>
</l:settingsGroup>

<l:settingsGroup title="FxCop location">

  <tr>
    <th><label for="fxcop.root">FxCop installation root: <l:star/></label></th>
    <td><props:textProperty name="fxcop.root" className="longField"/>
      <span class="error" id="error_fxcop.root"></span>
      <span class="smallNote">Place to call FxCopCmd.exe from.<br/>
                              Defaults to autodetection on agent side</span>
    </td>
  </tr>

</l:settingsGroup>
