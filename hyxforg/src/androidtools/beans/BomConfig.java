package androidtools.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BomConfig
  implements Serializable
{
  private static BomConfig instance = null;
  private static final long serialVersionUID = 1L;
  private List<Dependency> lstDependencies = new ArrayList();
  private List<Repository> lstRepositories = new ArrayList();
  private PackageInfo packageInfo = null;

  public static BomConfig getInstance()
  {
    if (instance == null)
      instance = new BomConfig();

    return instance;
  }

  public boolean isEmpty()
  {
    return ((((this.lstDependencies == null) || (this.lstDependencies.size() == 0))) && 
      (((this.lstRepositories == null) || (this.lstRepositories.size() == 0))) && 
      (this.packageInfo == null));
  }

  public List<Dependency> getLstDependencies()
  {
    return this.lstDependencies;
  }

  public void setLstDependencies(List<Dependency> lstDependencies)
  {
    this.lstDependencies = lstDependencies;
  }

  public List<Repository> getLstRepositories()
  {
    return this.lstRepositories;
  }

  public void setLstRepositories(List<Repository> lstRepositories)
  {
    this.lstRepositories = lstRepositories;
  }

  public PackageInfo getPackageInfo()
  {
    return this.packageInfo;
  }

  public void setPackageInfo(PackageInfo packageInfo)
  {
    this.packageInfo = packageInfo;
  }
}