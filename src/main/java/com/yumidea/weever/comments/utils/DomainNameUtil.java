package com.yumidea.weever.comments.utils;

import java.util.HashSet;
import java.util.Set;

public class DomainNameUtil 
{
	/** 顶级类别域名 */
	public static final String[] CATEGORY_TOP_LEVEL_DOMAINNAMES = {
		"ac",
		"aero",
		"asia",
		"biz",
		"cc",
		"com",
		"coop",
		"edu",
		"gov",
		"idv",
		"info",
		"int",
		"mil",
		"mobi",
		"museum",
		"name",
		"net",
		"org",
		"pro",
		"tel",
		"travel",
		"tv",
		"xxx"
	};
	
	/** 顶级国家域名，目前只需要cn和hk*/
	public static final String[] COUNTY_TOP_LEVEL_DOMAINAMES = 
	{
		"cn",
		"hk"
	};
	
	private static Set<String> topLevelDomainNameSet;
	static 
	{
		topLevelDomainNameSet = new HashSet<String>();
		for(String topLevelDomainName : CATEGORY_TOP_LEVEL_DOMAINNAMES)
		{
			topLevelDomainNameSet.add(topLevelDomainName);
		}
		for(String topLevelDomainName : COUNTY_TOP_LEVEL_DOMAINAMES)
		{
			topLevelDomainNameSet.add(topLevelDomainName);
		}
	}
	
	public static String extractDomainName(String url)
	{
		if(url == null)
			return null;
		
		String domainName = url.replace("https://", "");
		domainName = url.replace("http://", "");
		int endIndex = domainName.indexOf("/");
		if(endIndex >=0)
		{
			domainName = domainName.substring(0, endIndex);
		}
		return domainName;
	}
	
	
	public static String extractTopLevelDomainName(String url)
	{
		if(url == null)
			return null;
		
		String domainName = extractDomainName(url);
		
		if(domainName == null)
			return null;
		
		String[] tokens = domainName.split("\\.");
		
		if(tokens.length == 0)
			return null;
			
		if(tokens.length <= 3)
			return domainName;
		
		String token;
		int i;
		for( i = tokens.length - 1; i>=0; i--)
		{
			token = tokens[i];
			if(!topLevelDomainNameSet.contains(token))
				break;
		}
		
		
		StringBuilder topLevelDomainName = new StringBuilder();
		for( int j = i; j<tokens.length; j++)
		{
			topLevelDomainName.append(tokens[j] + ".");
		}
		int len = topLevelDomainName.length();
		if(len > 0)
		{
			topLevelDomainName.deleteCharAt(len-1);
		}
		
		return topLevelDomainName.toString();
		
	}
}
