package com.yumidea.weever.comments.utils;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.yumidea.weever.comments.systemvariable.SystemVariable;
import com.yumidea.weever.comments.config.NewsConfigBean;

public class XmlBeanUtil {
	private static Logger log = Logger.getLogger(XmlBeanUtil.class);
	private static String configFilesDir;

	/**
	 * get all configuration files
	 */
	public static List<String> getFileNames() {
		List<String> fileNames = new ArrayList<String>();

		if (SystemVariable.APP_PATH.contains("webapps")) {
			configFilesDir = SystemVariable.APP_PATH + "WEB-INF\\"
					+ "configFiles";
		} else {
			configFilesDir = SystemVariable.CLASS_PATH + "\\" + "configFiles";
		}
		log.info("configFilesDir: " + configFilesDir);

		getFileNames(configFilesDir, fileNames);

		return fileNames;
	}

	/**
	 * files traversal
	 * 
	 * @param configFileDir
	 * @param fileNames
	 * @return
	 */
	public static List<String> getFileNames(String configFileDir,
			List<String> fileNames) {
		File dir = new File(configFileDir);
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					if (file.getName().lastIndexOf(".") != -1)
						fileNames.add(file.getName().substring(0,
								file.getName().lastIndexOf(".")));
					log.info("file: " + file.getName());
				}
			}
		}

		return fileNames;
	}

	/**
	 * read XML file and write value in bean
	 * 
	 * @param fileName
	 * @return ConfigBean
	 */
	public static NewsConfigBean read(String fileName) {
		String filePath = null;

		BeanReader beanReader = new BeanReader();

		String className = "com.yumidea.weever.comments.config.NewsConfigBean";

		try {
			beanReader.registerBeanClass("NewsConfigBean",
					Class.forName(className));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (IntrospectionException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		NewsConfigBean configBean = null;
		try {
			filePath = configFilesDir + "\\" + fileName + ".xml";
			log.info("Config filePath: " + filePath);

			FileInputStream file = new FileInputStream(filePath);
			InputStreamReader isr = new InputStreamReader(file, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			try {
				String temp = new String();
				while ((temp = br.readLine()) != null) {
					sb.append(temp);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				br.close();
			}

			StringReader xmlReader = new StringReader(sb.toString());
			configBean = (NewsConfigBean) beanReader.parse(xmlReader);

		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (SAXException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return configBean;
	}

}