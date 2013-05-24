package com.marsor.qxc.android.popup.actions;

import androidtools.context.CommonUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;

public class AboutUsAction extends MarsorBaseAction
{
  public void setActivePart(IAction action, IWorkbenchPart targetPart)
  {
  }

  public void marsorRun(IAction action)
  {
    CommonUtils.messageBox("鸿音晓枫^_^");
  }

  public boolean needReloadConf()
  {
    return false;
  }
}