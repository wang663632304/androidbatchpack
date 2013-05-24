package androidtools.beans;

import androidtools.context.CommonUtils;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackageInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<String> lstChannels = new ArrayList();
	private String outPath = null;
	private String channelKey = null;
	private KeyStore keyStore = null;
	private String strKeyStorePath = null;
	private String strKeyStorePasswd = null;
	private Map<String, String> mpKeyAliasPasswdPairs = new HashMap();

	public void setKeyStorePath(String strKeyStorePath) {
		if ((strKeyStorePath != null) && (strKeyStorePath.trim().length() != 0)) {
			this.strKeyStorePath = strKeyStorePath;
		}

		if (this.strKeyStorePasswd != null)
			loadKeyStore(strKeyStorePath, this.strKeyStorePasswd);
	}

	public void setKeyStorePasswd(String storePasswd) {
		if ((storePasswd != null) && (storePasswd.trim().length() != 0)) {
			this.strKeyStorePasswd = storePasswd;
		}

		if (this.strKeyStorePath != null)
			loadKeyStore(this.strKeyStorePath, this.strKeyStorePasswd);
	}

	/**
	 * 可能存在问题
	 * @param path
	 * @param passwd
	 * @return
	 */
	private boolean loadKeyStore(String path, String passwd) {
		if ((path != null) && (passwd != null) && (path.trim().length() != 0)
				&& (passwd.trim().length() != 0)) {
			try {
				FileInputStream fins = new FileInputStream(path);
				if (this.keyStore == null)
					this.keyStore = KeyStore.getInstance(KeyStore
							.getDefaultType());
				this.keyStore.load(fins, passwd.toCharArray());

				if ((this.mpKeyAliasPasswdPairs == null)
						|| (this.mpKeyAliasPasswdPairs.size() == 0))
					return false;
				for (Iterator<String> localIterator = this.mpKeyAliasPasswdPairs
						.keySet().iterator(); localIterator.hasNext();) {
					String strAlias = (String) localIterator.next();
					if (this.keyStore.containsAlias(strAlias)){
						this.mpKeyAliasPasswdPairs.remove(strAlias);
						break;
					}
				}

			} catch (Exception e) {
				CommonUtils.consolePrint("无法读取密钥库，检查路径！\r\n路径：" + path
						+ "\r\n错误消息：" + e.getMessage());
				return false;
			}
		} else {
			CommonUtils.consolePrint("无法读取密钥库，路径或者密钥库密码不能为空！\r\n路径：" + path
					+ "\r\n密码：" + passwd);
			return false;
		}
		return true;
	}

	public void setAliasPasswd(String alias, String passwd) {
		try {
			if ((this.keyStore != null)
					&& (!(this.keyStore.containsAlias(alias)))) {
				CommonUtils.consolePrint("无法保存密钥库Key的密码，指定的别名对应的Key不存在！\r\n别名："
						+ alias);
				return;
			}

			this.mpKeyAliasPasswdPairs.put(alias, passwd);
		} catch (Exception localException) {
		}
	}

	private String getAlias() {
		if ((this.mpKeyAliasPasswdPairs == null)
				|| (this.mpKeyAliasPasswdPairs.isEmpty()))
			return null;

		if (this.mpKeyAliasPasswdPairs.keySet() != null) {
			Iterator<String> localIterator = this.mpKeyAliasPasswdPairs
					.keySet().iterator();
			if (localIterator.hasNext()) {
				String strAlias = (String) localIterator.next();
				return strAlias;
			}
		}
		return null;
	}

	private String getAliasPasswd() {
		if ((this.mpKeyAliasPasswdPairs == null)
				|| (this.mpKeyAliasPasswdPairs.isEmpty()))
			return null;

		String alias = getAlias();
		if ((alias != null) && (alias.trim().length() != 0))
			return ((String) this.mpKeyAliasPasswdPairs.get(alias));

		return null;
	}

	public Certificate getCertification() {
		String alias = getAlias();
		if (this.keyStore != null) {
			try {
				return this.keyStore.getCertificate(alias);
			} catch (Exception localException) {
				CommonUtils.consolePrint("无法获取密钥库证书，请确认密钥库！" + alias);
				return null;
			}
		}
		CommonUtils.consolePrint("密钥库不存在，请先确认密钥库路径以及密钥库密码！" + alias);

		return null;
	}

	public boolean keyStoreLoaded() {
		return (this.keyStore != null);
	}

	public Key getKey() {
		String alias = getAlias();
		String passwd = getAliasPasswd();
		if (this.keyStore != null) {
			try {
				return this.keyStore.getKey(alias, passwd.toCharArray());
			} catch (Exception localException) {
				CommonUtils.consolePrint("获取对应的密钥失败，请确认密钥对应的密码是否正确！" + alias);
				CommonUtils.consolePrint("密钥库不存在，请先确认密钥库路径以及密钥库密码！" + alias);
				return null;
			}
		} else {
			return null;
		}
	}

	public List<String> getChannels() {
		return this.lstChannels;
	}

	public void addChannel(String channel) {
		if (this.lstChannels == null)
			this.lstChannels = new ArrayList<String>();

		this.lstChannels.add(channel);
	}

	public String getOutPath() {
		return this.outPath;
	}

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}

	public String getChannelKeyName() {
		return this.channelKey;
	}

	public void setChannelKeyName(String channelKeyName) {
		this.channelKey = channelKeyName;
	}
}