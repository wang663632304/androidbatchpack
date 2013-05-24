package com.marsor.qxc.android.popup.actions;

import androidtools.beans.BomConfig;
import androidtools.beans.Dependency;
import androidtools.beans.PackageInfo;
import androidtools.beans.Repository;
import androidtools.context.CommonUtils;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class MarsorBaseAction implements IObjectActionDelegate {
	protected IProject activeProject = null;

	public final void run(IAction action) {
		if (this.activeProject == null) {
			return;
		}

		if (needReloadConf())
			reloadConfig();

		marsorRun(action);
	}

	private void reloadConfig() {
		CommonUtils.consolePrint("检查配置文件。");
		IFile file = this.activeProject.getFile("bom.xml");

		if (!(file.exists()))
			try {
				CommonUtils.consolePrint("没有配置文件，使用默认配置文件！");
				file.create(super.getClass().getResourceAsStream("bom.xml"),
						true, null);
			} catch (Exception localException1) {
				CommonUtils.consolePrint("没有配置文件，且无法创建新的配置文件，读取失败！");
				return;
			}

		CommonUtils.consolePrint("创建新的配置");
		BomConfig config = BomConfig.getInstance();
		try {
			Element ele;
			CommonUtils.consolePrint("开始创建Doc");
			String charset = file.getCharset();
			InputStream is = file.getContents();
			Document doc = Jsoup.parse(is, charset, "");
			// CommonUtils.consolePrint("开始读取依赖文件");
			// config.getLstDependencies().clear();
			// for (Iterator<Element> localIterator =
			// doc.getElementsByTag("dependency")
			// .iterator(); localIterator.hasNext();) {
			// ele = (Element) localIterator.next();
			// Dependency dependency = new Dependency();
			// Element groupEle = ele.getElementsByTag("groupId").first();
			// if (groupEle != null) {
			// dependency.setGroupId(groupEle.text());
			// } else {
			// CommonUtils.consolePrint("无法读取依赖的GroupId,Bom 项目："
			// + ele.toString());
			// return;
			// }
			// Element artifactEle = ele.getElementsByTag("artifactId")
			// .first();
			// if (artifactEle != null) {
			// dependency.setArtifactId(artifactEle.text());
			// } else {
			// CommonUtils.consolePrint("无法读取依赖的artifactId,Bom 项目："
			// + ele.toString());
			// return;
			// }
			// Element versionEle = ele.getElementsByTag("version").first();
			// if (versionEle != null) {
			// dependency.setVersion(versionEle.text());
			// } else {
			// CommonUtils.consolePrint("无法读取依赖的version,Bom 项目："
			// + ele.toString());
			// return;
			// }
			// config.getLstDependencies().add(dependency);
			// }
			// CommonUtils.consolePrint("开始读取库信息");
			// config.getLstRepositories().clear();
			// for (localIterator =
			// doc.getElementsByTag("repository").iterator(); localIterator
			// .hasNext();) {
			// ele = (Element) localIterator.next();
			// repository = new Repository();
			// Element idEle = ele.getElementsByTag("id").first();
			// if (idEle != null) {
			// repository.setId(idEle.text());
			// } else {
			// CommonUtils.consolePrint("无法读取repository的id,Bom 项目："
			// + ele.toString());
			// return;
			// }
			// Element nameEle = ele.getElementsByTag("name").first();
			// if (nameEle != null) {
			// repository.setName(nameEle.text());
			// } else {
			// CommonUtils.consolePrint("无法读取repository的name,Bom 项目："
			// + ele.toString());
			// return;
			// }
			// Element urlEle = ele.getElementsByTag("url").first();
			// if (urlEle != null) {
			// repository.setUrl(urlEle.text());
			// } else {
			// CommonUtils.consolePrint("无法读取repository的url,Bom 项目："
			// + ele.toString());
			// return;
			// }
			// config.getLstRepositories().add(repository);
			// }
			CommonUtils.consolePrint("开始读取打包配置信息");
			PackageInfo packageInfo = new PackageInfo();
			config.setPackageInfo(packageInfo);
			for (Iterator<Element> repository = doc.getElementsByTag("channel")
					.iterator(); repository.hasNext();) {
				ele = (Element) repository.next();
				String channel = ele.attr("value");
				if ((channel == null) || (channel.trim().length() == 0)) {
				} else {
					packageInfo.addChannel(channel);
				}
			}

			if ((packageInfo.getChannels() == null)
					|| (packageInfo.getChannels().size() == 0)) {
				CommonUtils.consolePrint("无法读取渠道配置信息，如果需要打包发布，请检查配置文件的渠道配置信息。");
			}
			for (Iterator<Element> repository = doc
					.getElementsByTag("channels").iterator(); repository
					.hasNext();) {
				ele = (Element) repository.next();
				String keyName = ele.attr("keyname");
				if ((keyName == null) || (keyName.trim().length() == 0)) {
				} else {
					packageInfo.setChannelKeyName(keyName);
				}
			}

			if ((packageInfo.getChannelKeyName() == null)
					|| (packageInfo.getChannelKeyName().trim().length() == 0)) {
				CommonUtils
						.consolePrint("无法读取渠道在Manifest文件中的配置KeyName，如果需要打包发布，请检查配置文件的渠道配置信息。");
			} else {
				CommonUtils.consolePrint("读取渠道信息完毕");
			}

			for (Iterator<Element> repository = doc
					.getElementsByTag("keystore").iterator(); repository
					.hasNext();) {
				ele = (Element) repository.next();
				String keystorePath = ele.attr("path");
				String keystorePasswd = ele.attr("passwd");
				if ((keystorePath != null)
						&& (keystorePath.trim().length() != 0))
					packageInfo.setKeyStorePath(keystorePath);

				if ((keystorePasswd == null)
						|| (keystorePasswd.trim().length() == 0)) {
				} else {
					packageInfo.setKeyStorePasswd(keystorePasswd);
				}
			}

			if (!(packageInfo.keyStoreLoaded())) {
				CommonUtils.consolePrint("无法获取密钥库，请确认密钥库路径以及密钥库密码。");
			} else {
				CommonUtils.consolePrint("密钥库信息读取完毕");
			}

			for (Iterator<Element> repository = doc.getElementsByTag("alias").iterator(); repository
					.hasNext();) {
				ele = (Element) repository.next();
				String aliasName = ele.attr("name");
				String aliasPasswd = ele.attr("passwd");
				if ((aliasPasswd == null) || (aliasPasswd.trim().length() == 0)
						|| (aliasPasswd == null)
						|| (aliasPasswd.trim().length() == 0)){
				}else{
					packageInfo.setAliasPasswd(aliasName, aliasPasswd);
				}
			}

			for (Iterator<Element> repository = doc.getElementsByTag("outPath").iterator(); repository
					.hasNext();) {
				ele = (Element) repository.next();
				String outPath = ele.attr("value");
				if ((outPath == null) || (outPath.trim().length() == 0)){
				}else{
					packageInfo.setOutPath(outPath);
				}
			}

			if ((packageInfo.getOutPath() != null)
					&& (packageInfo.getOutPath().trim().length() != 0)){
			}else{
				CommonUtils.consolePrint("无法获取打包信息的输出路径，请确认配置文件");
			}
		} catch (Exception e) {
			CommonUtils.consolePrint("读取配置文件时出错啦！");
			e.printStackTrace();
			return;
		}
		CommonUtils.consolePrint("读取配置文件完毕");
	}

	public void selectionChanged(IAction action, ISelection selection) {
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

		if (resource == null) {
			return;
		}

		if ((this.activeProject != null)
				&& (resource.getProject() == this.activeProject))
			return;

		this.activeProject = resource.getProject();
	}

	public abstract boolean needReloadConf();

	public abstract void marsorRun(IAction paramIAction);
}