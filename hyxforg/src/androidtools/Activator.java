package androidtools;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ILogger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin
  implements ILogger
{
  public static final String PLUGIN_ID = "HyxfTools";
  private static Activator plugin;

  public void start(BundleContext context)
    throws Exception
  {
    super.start(context);
    plugin = this;
  }

  public void stop(BundleContext context) throws Exception
  {
    plugin = null;
    super.stop(context);
  }

  public static Activator getDefault()
  {
    return plugin;
  }

  public static ImageDescriptor getImageDescriptor(String path)
  {
    return imageDescriptorFromPlugin("HyxfTools", path);
  }

  public void log(IStatus arg0)
  {
  }
}