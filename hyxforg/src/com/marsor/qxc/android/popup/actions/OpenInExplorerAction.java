package com.marsor.qxc.android.popup.actions;

import java.io.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

public class OpenInExplorerAction extends MarsorBaseAction {
	private IPath resourcePath = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void marsorRun(IAction action) {
		String file;
		try {
			file = this.resourcePath.toFile().getAbsolutePath();
			String params = "/select,";
			if (this.resourcePath.toFile().isDirectory()) {
				params = "";
			}

			Process p = Runtime.getRuntime().exec("explorer " + params + file);
			p.waitFor();
		} catch (Exception localException) {
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (!(selection instanceof StructuredSelection)) {
			return;
		}

		StructuredSelection sel = (StructuredSelection) selection;
		if (sel.size() <= 0) {
			return;
		}

		Object object = sel.getFirstElement();

		IResource resource = null;
		if (object instanceof IResource) {
			resource = (IResource) object;
		} else if (object instanceof IJavaElement) {
			resource = ((IJavaElement) object).getResource();
		} else if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			resource = (IResource) adaptable.getAdapter(IResource.class);
		}

		if (resource == null)
			return;

		this.resourcePath = resource.getLocation();
	}

	public boolean needReloadConf() {
		return false;
	}
}