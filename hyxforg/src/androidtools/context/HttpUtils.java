package androidtools.context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils
{
  public static boolean detectUrl(String strUrl)
  {
    try
    {
      HttpURLConnection.setFollowRedirects(true);
      URL url = new URL(strUrl);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();

      connection.setReadTimeout(2000);
      connection.setRequestMethod("GET");

      connection.setRequestProperty("User-Agent", "Mozilla/4.0(compatible;MSIE7.0;windows NT 5)");

      connection.setRequestProperty("Content-Type", "text/html");

      return (connection.getResponseCode() == 200);
    }
    catch (Exception localException)
    {
    }

    return false;
  }

  public static String readContent(String strUrl, String strEncoding)
  {
    if ((strUrl == null) || (strUrl.trim().length() == 0))
      return null;

    if ((strEncoding == null) || (strEncoding.trim().length() == 0))
      strEncoding = "UTF8";
    try
    {
      HttpURLConnection.setFollowRedirects(true);
      URL url = new URL(strUrl);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();

      connection.setReadTimeout(2000);
      connection.setRequestMethod("GET");

      connection.setRequestProperty("User-Agent", "Mozilla/4.0(compatible;MSIE7.0;windows NT 5)");

      connection.setRequestProperty("Content-Type", "text/html");

      StringBuilder sb = new StringBuilder();

      InputStream netStream = connection.getInputStream();
      BufferedReader bf = new BufferedReader(new InputStreamReader(netStream));
      String tmpLine = bf.readLine();
      while (tmpLine != null) {
        sb.append(tmpLine);
        tmpLine = bf.readLine();
      }
      return sb.toString();
    } catch (Exception localException) {
    }
    return null;
  }
}