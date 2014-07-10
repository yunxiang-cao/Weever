package com.yumidea.weever.comments.systemvariable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.yumidea.weever.comments.config.NewsConfigBean;
import com.yumidea.weever.comments.utils.DomainNameUtil;
import com.yumidea.weever.comments.utils.XmlBeanUtil;

public class SystemVariable {

	private static Logger log = Logger.getLogger(SystemVariable.class);

	public static String APP_PATH = System.getProperty("user.dir");
	public static String CLASS_PATH = System.getProperty("java.class.path")
			.split(";")[0];

	public static ConcurrentHashMap<String, NewsConfigBean> configBeans = null;

	static {
		log.info("APP_PATH-----------" + APP_PATH);
		log.info("CLASS_PATH-----------" + CLASS_PATH);

		String log_Path = CLASS_PATH + "\\Logs";
		System.setProperty("LOG_PATH", log_Path);

		// load all configuration files in configFiles
		List<String> fileNames = XmlBeanUtil.getFileNames();

		configBeans = new ConcurrentHashMap<String, NewsConfigBean>();
		for (String fileName : fileNames) {
			try {
				NewsConfigBean configBean = XmlBeanUtil.read(fileName);
				log.info("fileName : " + fileName);
				String domainName = configBean.getDomainName();

				String topLevelDomainName = DomainNameUtil
						.extractTopLevelDomainName(domainName);

				NewsConfigBean existConfig = configBeans.get(topLevelDomainName);

				if (existConfig != null) {
					log.info("duplicated configuration:"
							+ existConfig.getFileName() + " : "
							+ configBean.getFileName());
				} else {
					configBeans.put(topLevelDomainName, configBean);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				continue;
			}

		}

	}

	/**
	 * 
	 * @param siteName
	 * @return
	 */
	public static NewsConfigBean getConfigBean(String siteName) {
		NewsConfigBean configBean = null;
		if (configBeans.containsKey(siteName)) {
			configBean = configBeans.get(siteName);
		}
		if (configBean == null) {
			log.error("Can not find" + siteName + "'s configuration");
		}
		return configBean;
	}

}
