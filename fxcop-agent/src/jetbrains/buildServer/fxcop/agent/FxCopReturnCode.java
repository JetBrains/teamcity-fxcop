/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  private int myCode;

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
