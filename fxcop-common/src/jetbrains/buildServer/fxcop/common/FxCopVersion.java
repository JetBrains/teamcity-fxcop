package jetbrains.buildServer.fxcop.common;

import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Evgeniy.Koshkin
 * Date: 25.10.11
 * Time: 13:28
 */
public enum FxCopVersion {

  not_specified("not_specified", "Not Specified") {
    @Override
    public Requirement createRequirement() {
      return null;
    }
  },
  v1_35("1.35", "1.35"),
  v1_36("1.36", "1.36"),
  v10_0("10.0", "10.0");

  private final String myVersionName;
  private final String myDisplayName;

  FxCopVersion(final String versionName, final String displayName) {
    myVersionName = versionName;
    myDisplayName = displayName;
  }

  @NotNull
  public String getVersionName() {
    return myVersionName;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @Nullable
  public Requirement createRequirement(){
    return new Requirement(FxCopConstants.FXCOPCMD_FILE_VERSION_PROPERTY, getVersionName(), RequirementType.STARTS_WITH);
  }
}
