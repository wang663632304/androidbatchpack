package com.marsor.qxc.android.popup.actions;

import androidtools.beans.BomConfig;
import androidtools.beans.Dependency;
import androidtools.beans.Repository;
import androidtools.context.CommonUtils;
import androidtools.context.HttpUtils;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class UpdateDependencyThread extends Thread {
	private static Map<IProject, Boolean> mapSwitch = new HashMap();
	private IProject project = null;
	private BomConfig bomConfig = null;
	private UpdateCompleteListener listener = null;

	public void setUpdateCompleteListener(UpdateCompleteListener listener) {
		this.listener = listener;
	}

	public void removeUpdateCompleteListener() {
		this.listener = null;
	}

	public UpdateDependencyThread(IProject project, BomConfig bomConfig) {
		this.project = project;
		this.bomConfig = bomConfig;

		setName("更新线程");
		CommonUtils.consolePrint("更新线程初始化......");
	}

	/* (non-Javadoc)  可能会存在问题
	 * @see java.lang.Thread#run()
	 */
	public void run() {
//		CommonUtils.consolePrint("更新线程开始运行......");
//		if ((mapSwitch.get(this.project) != null)
//				&& (((Boolean) mapSwitch.get(this.project)).booleanValue())) {
//			CommonUtils.consolePrint("有一个线程正在运行，好，我退出......");
//			return;
//		}
//		mapSwitch.put(this.project, Boolean.valueOf(true));
//		try {
//			CommonUtils.consolePrint("现在开始更新库文件......");
//			int dependencyIndex = 1;
//
//			for (Iterator<Dependency> localIterator1 = this.bomConfig
//					.getLstDependencies().iterator(); localIterator1.hasNext();) {
//				Dependency dependency = (Dependency) localIterator1.next();
//				boolean updated = false;
//				Iterator<Repository> localIterator2 = this.bomConfig
//						.getLstRepositories().iterator();
//				Repository repository = (Repository) localIterator2.next();
//
//				String dependencyPath = fixUrl(repository.getUrl(),
//						dependency.getGroupId(), dependency.getArtifactId(),
//						dependency.getVersion());
//
//				if (HttpUtils.detectUrl(dependencyPath + ".lastUpdated"))
//					;
//				try {
//					String snapShot = HttpUtils.readContent(dependencyPath
//							+ ".lastUpdated", null);
//					String lastUpdate = snapShot.replaceAll(
//							".*?lastUpdated=(\\d+?).*", "$1");
//
//					if ((!(needUpdate(dependency)))
//							&& (dependency.getLastUpdate() >= Long
//									.parseLong(lastUpdate)))
//						continue;
//					dependency.setLastUpdate(Long.parseLong(lastUpdate));
//
//					downAndUpdate(dependency, dependencyPath);
//					updated = true;
//				} catch (Exception e) {
//					e.printStackTrace();
//					CommonUtils.consolePrint("从远程库：" + repository.getName()
//							+ " 下载快照资源文件：" + dependency.getArtifactId()
//							+ "时出错，尝试下一个远程库。");
//
//					if (!(needUpdate(dependency)))
//						continue;
//
//					try {
//						dependency.setLastUpdate(System.currentTimeMillis());
//
//						downAndUpdate(dependency, dependencyPath);
//						updated = true;
//					} catch (Exception localException1) {
//						do
//							CommonUtils.consolePrint("从远程库："
//									+ repository.getName() + " 下载非快照资源文件："
//									+ dependency.getArtifactId()
//									+ "时出错，尝试下一个远程库。");
//						while (localIterator2.hasNext());
//					}
//
//				}
//
//				if (updated)
//					CommonUtils.consolePrint("已更新库文件：" + (dependencyIndex++)
//							+ "/" + this.bomConfig.getLstDependencies().size());
//				else
//					CommonUtils.consolePrint("不需要更新：" + (dependencyIndex++)
//							+ "/" + this.bomConfig.getLstDependencies().size());
//
//				if (dependencyIndex <= this.bomConfig.getLstDependencies()
//						.size())
//					;
//				CommonUtils.consolePrint("更新完毕");
//			}
//
//		} catch (Exception localException2) {
//			if (this.listener != null) {
//				this.listener.complete();
//			}
//
//			mapSwitch.put(this.project, Boolean.valueOf(false));
//		} finally {
//			if (this.listener != null) {
//				this.listener.complete();
//			}
//
//			mapSwitch.put(this.project, Boolean.valueOf(false));
//		}
		CommonUtils.consolePrint("更新完毕");
		if (this.listener != null) {
			this.listener.complete();
		}
	}

	private void downAndUpdate(Dependency dependency, String dependencyPath) {
		String localFile = downToLib(dependencyPath);
		dependency.setLocalFile(localFile);
		updateClassPath(localFile);
	}

	private static String fixUrl(String url, String groupId, String artifactId,
			String version) {
		groupId = groupId.replaceAll("\\.", "/");
		return String.format("%1$s/%2$s/%3$s/%4$s/%3$s-%4$s.jar", new Object[] {
				url, groupId, artifactId, version });
	}

	private void updateClassPath(String localFileName) {
		IPath localPath = new Path(localFileName);

		IJavaProject javaProject = JavaCore.create(this.project);
		try {
			IClasspathEntry[] arrayOfIClasspathEntry;
			IClasspathEntry classPath = JavaCore.newLibraryEntry(this.project
					.findMember(localPath).getFullPath(), null, null);

			int j = (arrayOfIClasspathEntry = javaProject.getRawClasspath()).length;
			for (int i = 0; i < j; ++i) {
				IClasspathEntry originPath = arrayOfIClasspathEntry[i];
				if (originPath.getPath().toString()
						.equalsIgnoreCase(classPath.getPath().toString()))
					return;
			}

			ArrayList total = new ArrayList();
			total.addAll(Arrays.asList(javaProject.getRawClasspath()));
			total.add(classPath);
			javaProject.setRawClasspath((IClasspathEntry[]) total
					.toArray(new IClasspathEntry[total.size()]), null);
			this.project.refreshLocal(2, null);
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtils.consolePrint("无法为工程更新ClassPath，依赖包：" + localFileName
					+ "未导入工程。");
		}
	}

	private String downToLib(String urlFileName) {
		String fileName = urlFileName.replaceAll("^.*/(.*)$", "$1");
		IFolder library = this.project.getFolder("libs/");
		try {
			if (!(library.exists())) {
				library.create(true, true, null);
			}

			URL url = new URL(urlFileName);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			connection.setConnectTimeout(2000);

			connection.setReadTimeout(1800000);
			connection.setRequestMethod("GET");

			connection.setRequestProperty("User-Agent",
					"Mozilla/4.0(compatible;MSIE7.0;windows NT 5)");

			connection.setRequestProperty("Content-Type",
					"application/octet-stream");
			connection.connect();
			InputStream netStream = connection.getInputStream();

			IFile file = library.getFile(fileName);
			if (file.exists()) {
				file.delete(true, null);
			}

			file.create(netStream, true, null);
			netStream.close();
			connection.disconnect();
			CommonUtils.consolePrint("已下载文件" + fileName + ",下载位置：" + "libs/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "libs/" + fileName;
	}

	private boolean needUpdate(Dependency dependency) {
		if ((dependency.getLocalFile() == null)
				|| (dependency.getLocalFile().trim().length() == 0))
			return true;

		IFile dependencyFile = this.project.getFile(new Path(dependency
				.getLocalFile()));
		if (!(dependencyFile.exists()))
			return true;

		IJavaProject javaProject = JavaCore.create(this.project);
		IClasspathEntry classPath = JavaCore.newLibraryEntry(
				dependencyFile.getFullPath(), null, null);
		boolean contains = false;
		try {
			IClasspathEntry[] arrayOfIClasspathEntry;
			int j = (arrayOfIClasspathEntry = javaProject.getRawClasspath()).length;
			for (int i = 0; i < j; ++i) {
				IClasspathEntry originPath = arrayOfIClasspathEntry[i];
				if (originPath.getPath().toString()
						.equalsIgnoreCase(classPath.getPath().toString())) {
					contains = true;
					break;
				}
			}
		} catch (Exception localException) {
		}
		return !(contains);
	}

	public static abstract interface UpdateCompleteListener {
		public abstract void complete();
	}
}