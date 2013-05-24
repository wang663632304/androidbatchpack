package androidtools.beans;

import java.io.Serializable;

public class Dependency
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String groupId = null;
  private String artifactId = null;
  private String version = null;
  private long lastUpdate = -8506138027708383232L;
  private String localFile = null;

  public String getGroupId()
  {
    return this.groupId;
  }

  public void setGroupId(String groupId)
  {
    this.groupId = groupId;
  }

  public String getArtifactId()
  {
    return this.artifactId;
  }

  public void setArtifactId(String artifactId)
  {
    this.artifactId = artifactId;
  }

  public String getVersion()
  {
    return this.version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public long getLastUpdate()
  {
    return this.lastUpdate;
  }

  public void setLastUpdate(long lastUpdate)
  {
    this.lastUpdate = lastUpdate;
  }

  public String getLocalFile()
  {
    return this.localFile;
  }

  public void setLocalFile(String localFile)
  {
    this.localFile = localFile;
  }
}