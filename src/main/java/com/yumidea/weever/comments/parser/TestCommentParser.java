package com.yumidea.weever.comments.parser;

import com.yumidea.weever.comments.systemvariable.SystemVariable;
import com.yumidea.weever.comments.config.NewsConfigBean;
import com.yumidea.weever.comments.utils.DomainNameUtil;

public class TestCommentParser {

	public static NewsConfigBean getConfigBean(String url) {
		String topLevelDomainName = DomainNameUtil
				.extractTopLevelDomainName(url);

		NewsConfigBean config = SystemVariable
				.getConfigBean(topLevelDomainName);

		return config;
	}

	public static void test(String url) {
		CommentParser test = new CommentParser();
		NewsConfigBean config = getConfigBean(url);
		test.handleParse(config, url);
	}

	public static void main(String[] args) {
		test("http://news.163.com/14/0706/07/A0F275100001124J.html");
	}

}
