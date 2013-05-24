package com.marsor.qxc.android.popup.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import androidtools.beans.BomConfig;
import androidtools.beans.PackageInfo;
import androidtools.context.CommonUtils;

import com.android.ide.eclipse.adt.AdtPlugin;
import com.android.ide.eclipse.adt.internal.project.ExportHelper;

public class PackageAndPublish extends MarsorBaseAction {
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void marsorRun(IAction action) {
		Key privateKey = null;
		Certificate certification = null;
		String outPath = null;
		String channelKeyName = null;
		String projectName = null;
		int i;
		BomConfig config = BomConfig.getInstance();

		if ((config == null) || (config.isEmpty())) {
			CommonUtils.messageBox("配置信息为空，不能进行打包、发布，请检查配置文件！");
			return;
		}
		PackageInfo packageInfo = config.getPackageInfo();
		if (packageInfo == null) {
			CommonUtils.messageBox("配置信息中没有关于打包发布的信息，不能进行打包、发布，请检查配置文件！");
			return;
		}
		try {
			if (this.activeProject
					.hasNature("com.android.ide.eclipse.adt.AndroidNature")) {
				privateKey = packageInfo.getKey();
				certification = packageInfo.getCertification();
				outPath = packageInfo.getOutPath();
				channelKeyName = packageInfo.getChannelKeyName();
				projectName = this.activeProject.getName();
			} else {
				CommonUtils.messageBox("只有安卓项目才能够进行打包发布！");
				return;
			}
		} catch (Exception localException1) {
			if ((privateKey == null) || (certification == null)) {
				CommonUtils.messageBox("无法进行打包，Key和证书无法获取,请检查bom.xml文件！");
				return;
			}
		}
		String[] channelKeys = (String[]) null;
		String[] strOriginChannels = (String[]) null;
		if (channelKeyName != null) {
			channelKeys = channelKeyName.split(",");
			strOriginChannels = new String[channelKeys.length];
			for (i = 0; i < channelKeys.length; ++i) {
				strOriginChannels[i] = readManifestAppMetaValue(channelKeys[i]);
				CommonUtils.consolePrint("原始的发布渠道  " + channelKeys[i] + " : "
						+ strOriginChannels[i]);
			}
		} else {
			CommonUtils.consolePrint("找不到可用的渠道名称，请确认");
			return;
		}
		CommonUtils.consolePrint("开始渠道打包，渠道包数量："+packageInfo.getChannels().size());
		for (Iterator<String> localIterator1 = packageInfo.getChannels()
				.iterator(); localIterator1.hasNext();) {
			String channel = (String) localIterator1.next();

			String outFileName = CommonUtils.fixFile(new String[] { outPath,
					projectName, projectName + "_" + channel + ".apk" });

			writeManifestAppMetaValue(channelKeyName, channel);

			exportSignedApk(outFileName, projectName, channel, privateKey,
					certification);

			if (channelKeyName == null)
				continue;
			if (channelKeyName.trim().length() == 0)
				break;
		}
		
		if (channelKeys != null) {
			CommonUtils.consolePrint("恢复初始状态的Manifest文件中的渠道信息。"+channelKeys.length);
			for (i = 0; i < channelKeys.length; ++i)
				writeManifestAppMetaValue(channelKeys[i], strOriginChannels[i]);
		}

		try {
			String strTmpdir = System.getProperty("java.io.tmpdir",
					"C:\\Windows\\Temp\\");
			File tmpFileDir = new File(strTmpdir);
			ArrayList<File> aryList = getAllFiles(tmpFileDir);

			Iterator<File> it = aryList.iterator();
			while (it.hasNext()) {
				File tmpFile = (File) it.next();
				String fileName = tmpFile.getName();
				if ((fileName.startsWith("android_"))
						&& (((fileName.endsWith("dex")) || (fileName
								.endsWith("ap_"))))) {
					tmpFile.delete();
					it.remove();
				}
				if ((fileName.startsWith("androidExport_"))
						&& (fileName.endsWith(".apk"))) {
					tmpFile.delete();
					it.remove();
				}
			}
			CommonUtils.consolePrint("清理临时文件夹完毕，打包结束！打包文件存放在："
					+ CommonUtils
							.fixFile(new String[] { outPath, projectName }));
		} catch (Exception localException2) {
		}
		CommonUtils.messageBox("所有渠道包生成完毕！");
	}

	private void exportSignedApk(final String outFileName,
			final String projectName, final String channel,
			final Key privateKey, final Certificate certification) {
		File file;
		try {
			file = new File(outFileName);
			File parent = file.getParentFile();
			if (parent.exists()) {
			} else {
				parent.mkdirs();
			}
		} catch (Exception localException1) {
			CommonUtils.messageBox("输出路径不存在，且无法生成，请检查bom.xml文件！");
			return;
		}
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(

			new IRunnableWithProgress() {

				@SuppressWarnings("restriction")
				@Override
				public void run(IProgressMonitor arg0)
						throws InvocationTargetException, InterruptedException {
					boolean runZipAlign;
					try {
						runZipAlign = false;
						String path = AdtPlugin.getOsAbsoluteZipAlign();
						File zipalign = new File(path);
						runZipAlign = zipalign.isFile();
						File apkExportFile = new File(outFileName);
						if (runZipAlign)
							apkExportFile = File.createTempFile(
									"androidExport_", ".apk");

						arg0.setTaskName("正在生成" + outFileName);
						ExportHelper.exportReleaseApk(activeProject,
								apkExportFile, (PrivateKey) privateKey,
								(X509Certificate) certification, arg0);
						if (runZipAlign) {
							zipAlign(path, apkExportFile, new File(outFileName));
						}

						CommonUtils.consolePrint("已生成文件：" + projectName + "_"
								+ channel + ".apk");
					} catch (Exception e) {
						e.printStackTrace();
						CommonUtils.messageBox("打包过程中出现错误！" + e.getMessage());
					}
					arg0.done();

				}
			});
		} catch (Exception localException2) {
		}
	}

	public static ArrayList<File> getAllFiles(File folder) {
		ArrayList result = new ArrayList();

		if ((!(folder.exists())) || (!(folder.isDirectory())))
			return result;

		File[] files = folder.listFiles();
		if (files != null) {
			File[] arrayOfFile1;
			int j = (arrayOfFile1 = files).length;
			for (int i = 0; i < j; ++i) {
				File subFile = arrayOfFile1[i];
				if (subFile.isDirectory())
					continue;

				result.add(subFile);
			}
		}

		return result;
	}

	private void zipAlign(String zipAlignPath, File source, File destination) {
		String[] command;
		try {
			command = new String[5];
			command[0] = zipAlignPath;
			command[1] = "-f";
			command[2] = "4";
			command[3] = source.getAbsolutePath();
			command[4] = destination.getAbsolutePath();
			Process process = Runtime.getRuntime().exec(command);
			if (process.waitFor() == 0)
				return;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			String message = reader.readLine();
			while (message != null)
				CommonUtils.consolePrint(message);
		} catch (Exception e) {
			CommonUtils.consolePrint(e.getMessage());
		}
	}

	public boolean needReloadConf() {
		return true;
	}

	private String readManifestAppMetaValue(String metaName) {
		if (this.activeProject == null)
			return null;

		IFile file = this.activeProject.getFile("AndroidManifest.xml");
		if (file == null) {
			return null;
		}

		Element element = getManifestAppMeta(metaName);
		if (element != null) {
			return element.attr("android:value");
		}

		return null;
	}

	private void writeManifestAppMetaValue(String metaName, String metaValue) {
		if (this.activeProject == null)
			return;

		if ((metaName == null) || (metaName.trim().length() == 0))
			return;

		String[] strMetaNames = null;
		if (metaName.indexOf(",") > 0)
			strMetaNames = metaName.split(",");
		else {
			strMetaNames = new String[]{metaName};
		}

		IFile file = this.activeProject.getFile("AndroidManifest.xml");
		if (file == null)
			return;
		try {
			String charset = file.getCharset();
			InputStream stream = file.getContents();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream, charset));
			StringBuffer totalContent = new StringBuffer(stream.available());
			String tmpLine = reader.readLine();
			boolean appSectionStarted = false;
			while (tmpLine != null) {
				if ((!(appSectionStarted))
						&& (tmpLine.matches("\\s*?<application.*")))
					appSectionStarted = true;

				if ((appSectionStarted)
						&& (tmpLine.matches("\\s*?</application.*"))) {
					appSectionStarted = false;
				}

				boolean isMeta = tmpLine
						.matches("(?i)\\s*<meta-data.*?(/>|</meta-data>)\\s*");

				if ((appSectionStarted) && (isMeta)) {
					String name = tmpLine.replaceAll(
							".*?android:name=\"(.*?)\".*", "$1");
					if (contains(strMetaNames, name)) {
						String part1 = tmpLine.replaceAll(
								"^(.*?android:value=\").*?(\".*)$", "$1");
						String part2 = tmpLine.replaceAll(
								"^(.*?android:value=\").*?(\".*)$", "$2");
						tmpLine = part1 + metaValue + part2;
					}
				}
				if ((appSectionStarted) && (!(isMeta))
						&& (tmpLine.matches("\\s*<meta-data.*"))) {
					tmpLine = tmpLine + reader.readLine();
				} else {
					totalContent.append(tmpLine).append("\r\n");
					tmpLine = reader.readLine();
				}
			}
			reader.close();
			stream.close();

			FileOutputStream outstream = new FileOutputStream(file
					.getRawLocation().toFile());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					outstream, charset));
			writer.write(totalContent.toString());
			writer.flush();
			writer.close();
			outstream.close();

			this.activeProject.refreshLocal(2, null);
		} catch (Exception e) {
			CommonUtils.consolePrint("写入Meta-Data：" + metaName + "时出错，应该写入值为："
					+ metaValue + "，错误消息：" + e.getMessage());
			try {
				this.activeProject.refreshLocal(2, null);
			} catch (Exception localException1) {
			}
		} finally {
			try {
				this.activeProject.refreshLocal(2, null);
			} catch (Exception localException2) {
			}
		}
	}

	private boolean contains(String[] array, String strOne) {
		String[] arrayOfString;
		if ((array == null) || (array.length == 0))
			return false;

		if ((strOne == null) || (strOne.trim().length() == 0))
			return false;

		int j = (arrayOfString = array).length;
		for (int i = 0; i < j; ++i) {
			String str = arrayOfString[i];
			if (str.equalsIgnoreCase(strOne))
				return true;
		}

		return false;
	}

	private Element getManifestAppMeta(String metaName) {
		if (this.activeProject == null)
			return null;

		IFile file = this.activeProject.getFile("AndroidManifest.xml");
		if (file == null)
			return null;
		try {
			String charset = file.getCharset();
			InputStream stream = file.getContents();
			Document doc = Jsoup.parse(stream, charset, "");
			if (doc == null)
				return null;
			Iterator<Element> localIterator1 = doc.getElementsByTag(
					"application").iterator();
			while (localIterator1.hasNext()) {
				Element ele = (Element) localIterator1.next();
				for (Iterator<Element> localIterator2 = ele.getElementsByTag(
						"meta-data").iterator(); localIterator2.hasNext();) {
					Element element = (Element) localIterator2.next();
					String name = element.attr("android:name");
					if ((name == null) || (name.trim().length() == 0)
							|| (!(name.equalsIgnoreCase(metaName)))) {
						return null;
					} else {
						return element;
					}
				}
			}
		} catch (Exception localException) {
			return null;
		}
		return null;
	}
}