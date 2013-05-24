package androidtools.beans;

import java.io.Serializable;

public class Repository
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String id = null;
  private String name = null;
  private String url = null;

  public String getId()
  {
    return this.id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getUrl()
  {
    return this.url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }
}