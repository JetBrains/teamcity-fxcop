

package jetbrains.buildServer.fxcop.agent;

import java.util.EnumSet;

public enum FxCopReturnCode {
  ANALYSIS_ERROR(0x1),
  RULE_EXCEPTIONS(0x2),
  PROJECT_LOAD_ERROR(0x4),
  ASSEMBLY_LOAD_ERROR(0x8),
  RULE_LIBRARY_LOAD_ERROR(0x10),
  IMPORT_REPORT_LOAD_ERROR(0x20),
  OUTPUT_ERROR(0x40),
  COMMAND_LINE_SWITCH_ERROR(0x80),
  INITIALIZATION_ERROR(0x100),
  ASSEMBLY_REFERENCES_ERROR(0x200),
  BUILD_BREAKING_MESSAGE(0x400),
  UNKNOWN_ERROR(0x1000000);

  private final int myCode;

  public int getCode() {
    return myCode;
  }

  FxCopReturnCode(final int code) {
    myCode = code;
  }

  public static EnumSet<FxCopReturnCode> decodeReturnCode(int code) {
    EnumSet<FxCopReturnCode> result = EnumSet.noneOf(FxCopReturnCode.class);
    
    for (FxCopReturnCode value : FxCopReturnCode.values()) {
      if ((code & value.getCode()) != 0) {
        result.add(value);
      }
    }

    return result;    
  }
}