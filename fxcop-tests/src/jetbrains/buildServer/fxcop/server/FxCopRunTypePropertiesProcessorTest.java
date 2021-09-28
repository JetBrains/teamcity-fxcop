package jetbrains.buildServer.fxcop.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FxCopRunTypePropertiesProcessorTest {

  @Test
  public void testNullFxCopRoot() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    final Collection<InvalidProperty> invalidProperties = propertiesProcessor.process(properties);
    assertTrue(invalidProperties.stream().anyMatch(invalidProperty -> invalidProperty.getPropertyName().equals(FxCopConstants.SETTINGS_FXCOP_ROOT)));
  }

  @Test
  public void testEmptyFxCopRoot() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
      put(FxCopConstants.SETTINGS_FXCOP_ROOT, "");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    final Collection<InvalidProperty> invalidProperties = propertiesProcessor.process(properties);
    assertTrue(invalidProperties.stream().anyMatch(invalidProperty -> invalidProperty.getPropertyName().equals(FxCopConstants.SETTINGS_FXCOP_ROOT)));
  }

  @Test
  public void testNullFiles() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    final Collection<InvalidProperty> invalidProperties = propertiesProcessor.process(properties);
    assertTrue(invalidProperties.stream().anyMatch(invalidProperty -> invalidProperty.getPropertyName().equals(FxCopConstants.SETTINGS_FILES)));
  }

  @Test
  public void testEmptyFiles() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
      put(FxCopConstants.SETTINGS_FILES, "");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    final Collection<InvalidProperty> invalidProperties = propertiesProcessor.process(properties);
    assertTrue(invalidProperties.stream().anyMatch(invalidProperty -> invalidProperty.getPropertyName().equals(FxCopConstants.SETTINGS_FILES)));
  }

  @Test
  public void testNullProject() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    final Collection<InvalidProperty> invalidProperties = propertiesProcessor.process(properties);
    assertTrue(invalidProperties.stream().anyMatch(invalidProperty -> invalidProperty.getPropertyName().equals(FxCopConstants.SETTINGS_FILES)));
  }

  @Test
  public void testEmptyProject() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
      put(FxCopConstants.SETTINGS_PROJECT, "");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    final Collection<InvalidProperty> invalidProperties = propertiesProcessor.process(properties);
    assertTrue(invalidProperties.stream().anyMatch(invalidProperty -> invalidProperty.getPropertyName().equals(FxCopConstants.SETTINGS_FILES)));
  }

  @Test
  public void testResetRoot() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_AUTO);
      put(FxCopConstants.SETTINGS_FXCOP_VERSION, "some version");
      put(FxCopConstants.SETTINGS_FXCOP_ROOT, "some root");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    propertiesProcessor.process(properties);
    assertEquals(properties.get(FxCopConstants.SETTINGS_FXCOP_VERSION), "some version");
    assertFalse(properties.containsKey(FxCopConstants.SETTINGS_FXCOP_ROOT));
  }

  @Test
  public void testResetVersion() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
      put(FxCopConstants.SETTINGS_FXCOP_VERSION, "some version");
      put(FxCopConstants.SETTINGS_FXCOP_ROOT, "some root");
    }};
    final Map<String, String> defaults = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_FXCOP_VERSION, "def version");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(defaults);
    propertiesProcessor.process(properties);
    assertEquals(properties.get(FxCopConstants.SETTINGS_FXCOP_VERSION), "def version");
    assertEquals(properties.get(FxCopConstants.SETTINGS_FXCOP_ROOT), "some root");
  }

  @Test
  public void testResetFiles() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
      put(FxCopConstants.SETTINGS_PROJECT, "some project");
      put(FxCopConstants.SETTINGS_FILES, "some files");
      put(FxCopConstants.SETTINGS_FILES_EXCLUDE, "some exclude files");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    propertiesProcessor.process(properties);
    assertEquals(properties.get(FxCopConstants.SETTINGS_PROJECT), "some project");
    assertFalse(properties.containsKey(FxCopConstants.SETTINGS_FILES));
    assertFalse(properties.containsKey(FxCopConstants.SETTINGS_FILES_EXCLUDE));
  }

  @Test
  public void testResetProject() {
    final Map<String, String> properties = new HashMap<String, String>() {{
      put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
      put(FxCopConstants.SETTINGS_PROJECT, "some project");
      put(FxCopConstants.SETTINGS_FILES, "some files");
      put(FxCopConstants.SETTINGS_FILES_EXCLUDE, "some exclude files");
    }};
    final PropertiesProcessor propertiesProcessor = new FxCopRunTypePropertiesProcessor(Collections.emptyMap());
    propertiesProcessor.process(properties);
    assertEquals(properties.get(FxCopConstants.SETTINGS_FILES), "some files");
    assertEquals(properties.get(FxCopConstants.SETTINGS_FILES_EXCLUDE), "some exclude files");
    assertFalse(properties.containsKey(FxCopConstants.SETTINGS_PROJECT));
  }

}