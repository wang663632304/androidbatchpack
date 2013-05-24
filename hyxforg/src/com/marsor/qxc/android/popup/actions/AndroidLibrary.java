package com.marsor.qxc.android.popup.actions;

import androidtools.beans.BomConfig;
import androidtools.context.CommonUtils;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;

public class AndroidLibrary extends MarsorBaseAction {
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void marsorRun(IAction action) {
		CommonUtils.consolePrint("检测到更新动作。");

		if (this.activeProject == null) {
			CommonUtils.messageBox("工程为空，不能更新。");
			return;
		}

		UpdateDependencyThread.UpdateCompleteListener listener =new UpdateDependencyThread.UpdateCompleteListener() {
			
			@Override
			public void complete() {
				// TODO Auto-generated method stub
				try {
					CommonUtils.consolePrint("更新线程执行完毕，现在开始刷新工程。");
					activeProject.refreshLocal(2, null);
					CommonUtils.consolePrint("刷新工程完毕。更新依赖包完毕！");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		UpdateDependencyThread thread = new UpdateDependencyThread(
				this.activeProject, BomConfig.getInstance());
		thread.setUpdateCompleteListener(listener);
		thread.start();
	}

	public boolean needReloadConf() {
		return true;
	}
}