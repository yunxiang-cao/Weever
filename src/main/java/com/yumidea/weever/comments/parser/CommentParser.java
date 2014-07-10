package com.yumidea.weever.comments.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.yumidea.weever.comments.config.NewsConfigBean;
import com.yumidea.weever.comments.bean.NewsComment;
import com.yumidea.weever.comments.bean.SingleComment;
import com.yumidea.weever.comments.utils.DomainNameUtil;

public class CommentParser {

	private static Logger log = Logger.getLogger(CommentParser.class);

	private boolean timeSet = false;

	public void handleParse(NewsConfigBean config, String contentPageUrl) {
		if (config == null) {
			log.info("config is null.");
			return;
		}

		Date lastCommentTime = new Date();
		String dataSrc = null;

		NewsConfigBean newsConfig = config;

		String commentPageUrl = getCommentPageUrl(contentPageUrl,
				newsConfig.getCommentPageUrl());

		dataSrc = this.getDataSrc(commentPageUrl, newsConfig.getDataSrc());
		int commentNum = getCommentNum(dataSrc);
		if (commentNum == 0) {
			log.info("num of comments is 0.");
			return;
		}

		parseComment(dataSrc, commentNum, newsConfig.getNumPerPage(),
				newsConfig.getListName(), newsConfig.getAttribute(),
				newsConfig.getCharSet(), commentPageUrl, lastCommentTime);
	}

	/**
	 * get web page content from data source
	 * 
	 * @param url
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static String read(String url, String charSet) throws IOException {
		StringBuffer html = new StringBuffer();

		URL addrUrl = null;
		URLConnection urlConn = null;
		BufferedReader br = null;

		try {
			addrUrl = new URL(url);
			urlConn = addrUrl.openConnection();

			br = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream(), charSet));

			String buf = null;

			if (br != null)
				while ((buf = br.readLine()) != null) {
					html.append(buf + "\r\n");
				}
		} catch (Exception e) {
			log.error("Error when read comments from data source!");
			log.error(e.getMessage());
		} finally {
			if (br != null) {
				br.close();
			}
		}

		return html.toString();
	}

	/**
	 * parse json data
	 * 
	 * @param commentDataurl
	 * @param listName
	 * @param attributes
	 * @param charSet
	 * @param domainName
	 * @param thisBeans
	 * @param pageNum
	 * @return
	 */
	private void parseJsonData(String commentDataurl, String listName,
			String attributes, String charSet, String domainName,
			List<SingleComment> thisBeans, int pageNum, Date lastCommentTime) {
		log.info("commentDataurl = : " + commentDataurl);
		String comment = null;

		try {
			comment = read(commentDataurl, charSet);

			if (null == comment || "".equals(comment)) {
				log.info("comment : " + comment);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		if (comment != null)
			comment = comment.substring(comment.indexOf("{"), comment.length());
		else
			return;

		JSONArray array = null;

		String fileName = "";

		if (domainName.contains("163")) {
			fileName = "163";
			JSONObject jsonObject = JSONObject.fromObject(comment);

			try {
				array = jsonObject.getJSONArray(listName);
			} catch (Exception e) {
				e.printStackTrace();
				log.info("JSONObject[\"" + listName + "\"] is not a JSONArray");
			}

		}

		String[] attribute = attributes.split("-");

		File f = new File("C:\\" + fileName + ".csv");

		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = JSONObject.fromObject(array.get(i).toString());
			if (null == obj)
				break;
			if (domainName.contains("163")) {
				// get floors of this comment
				int j = obj.size();
				if (j > 1)
					j--; // if this comment have more than one floors, get the
							// bottom one

				String floor = String.valueOf(j);

				// get the bottom one of this comment
				JSONObject obj2 = JSONObject.fromObject(obj.get(floor));

				if (null == obj2)
					continue;
				try {
					obj2.get("f");
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				if (null == obj2.get("f"))
					continue;
				String area = obj2.get("f").toString();
				if (area.contains("网友"))
					area = area.substring(0, area.indexOf("网友") + 2);
				if (null == obj2.get("n"))
					continue;
				String nickName = obj2.get("n").toString();
				if (area.contains("网友"))
					area = area.substring(0, area.indexOf("网友"));

				try {
					FileWriter output = new FileWriter(f, true);

					StringBuffer thisJson = new StringBuffer();
					thisJson.append("{");
					thisJson.append("\"nickName\":\"" + nickName + "\",");
					thisJson.append("\"area\":\"" + area + "\",");
					output.write("\n" + nickName + ",");
					output.write(area + ",");

					for (int k = 0; k < attribute.length; k++) {
						if (k == attribute.length - 1)
							output.write(obj2.get(attribute[k].split(",")[1])
									+ "");
						else
							output.write(obj2.get(attribute[k].split(",")[1])
									+ ",");
						thisJson.append("\"" + attribute[k].split(",")[0]
								+ "\":\""
								+ obj2.get(attribute[k].split(",")[1]) + "\",");
					}
					thisJson.deleteCharAt(thisJson.lastIndexOf(","));
					thisJson.append("}");
					String jsonString = thisJson.toString()
							.replace("\r", "\\r").replace("\n", "\\n");

					SingleComment thisComment = (SingleComment) JSONObject
							.toBean(JSONObject.fromObject(jsonString),
									SingleComment.class);

					if (thisComment != null) {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						Date thisDate;
						try {
							thisDate = sdf.parse(thisComment.getTime());

							if (this.timeSet == false) {
								this.timeSet = true;
							}
							if (thisDate.before(lastCommentTime)) {
								log.info("this date " + thisDate);
								log.info("last comment date " + lastCommentTime);
							}
						} catch (ParseException e) {
							log.error(e.getMessage());
						}

					}

					thisComment.setNickName(thisComment.getNickName());
					thisComment.setContent(thisComment.getContent());

					thisBeans.add(thisComment);
					output.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * parse comment
	 */
	private String parseComment(String dataSrc, int commentNum, int numPerPage,
			String listName, String attribute, String charSet,
			String commentPageUrl, Date lastCommentTime) {
		String comment = null;
		String currentPageurl = null;

		log.info("dataSrc =" + dataSrc);
		String domainName = DomainNameUtil.extractDomainName(commentPageUrl);

		DecimalFormat df = new DecimalFormat("000000000");

		NewsComment thisBean = new NewsComment();
		thisBean.setCharSet(charSet);
		thisBean.setCommentDataurl(dataSrc);
		thisBean.setDomainName(domainName);

		LinkedList<SingleComment> thisBeans = new LinkedList<SingleComment>();

		log.info(" numPfPage : " + Math.ceil(commentNum / (double) numPerPage));

		String fileName = "163";
		File f = new File("C:\\" + fileName + ".csv");
		FileWriter output;
		try {
			output = new FileWriter(f, true);
			output.write("nickName,area,time,ip,content");
			output.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		for (int i = 1; i <= Math.ceil(commentNum / (double) numPerPage); i++) {
			currentPageurl = dataSrc.replace("#page#", df.format(i));
			parseJsonData(currentPageurl, listName, attribute, charSet,
					domainName, thisBeans, i, lastCommentTime);
			log.info(currentPageurl);
		}

		return comment;
	}

	/**
	 * this method get data source link by template
	 * 
	 * @param commentPageUrl
	 * @param dataSrcFormat
	 * @return
	 */
	private String getDataSrc(String commentPageUrl, String dataSrcFormat) {
		String dataSrc = dataSrcFormat;
		String boardId = null;
		String threadId = null;

		log.info("commentPageUrl: " + commentPageUrl);

		String domainName = DomainNameUtil.extractDomainName(commentPageUrl);
		if (domainName.contains("163")) {
			String newsInfoUrl = commentPageUrl.substring(
					commentPageUrl.indexOf(domainName) + domainName.length()
							+ 1, commentPageUrl.lastIndexOf("."));
			log.info("domainName: " + domainName);
			log.info("newsInfoUrl: " + newsInfoUrl);
			String[] infos = newsInfoUrl.split("/");
			boardId = infos[0];
			threadId = infos[1];
			dataSrc = dataSrc.replace("#boardId#", boardId);
			dataSrc = dataSrc.replace("#threadId#", threadId);
		}

		log.info("data source link: " + dataSrc);

		return dataSrc;
	}

	private int getCommentNum(String datasrc) {
		datasrc = datasrc.replace("#page#", "000");
		String commentNum = "";
		try {
			String newPostList = read(datasrc, "gb2312");
			Pattern pattern = Pattern.compile("\"tcount\":\\w+");
			Matcher matcher = pattern.matcher(newPostList);

			if (matcher.find()) {
				String commentNumEquation = matcher.group();
				int begin = commentNumEquation.indexOf(":");
				if (begin != -1)
					commentNum = commentNumEquation.substring(begin + 1);

				System.out.println("commentNum = : " + commentNum);
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return Integer.parseInt(commentNum);
	}

	private String getCommentPageUrl(String contentPageUrl,
			String commentPageUrl) {
		try {
			String htmlContent = read(contentPageUrl, "gb2312");

			Pattern pattern = Pattern.compile("boardId = \"\\w+\",");
			Matcher matcher = pattern.matcher(htmlContent);

			String boardId = "";
			String threadId = "";

			threadId = contentPageUrl.substring(
					contentPageUrl.lastIndexOf("/") + 1,
					contentPageUrl.lastIndexOf("."));

			if (matcher.find()) {
				String boardIdEquation = matcher.group();
				int begin = boardIdEquation.indexOf("\"");
				int end = boardIdEquation.lastIndexOf("\"");
				if (begin != -1 && end != -1)
					boardId = boardIdEquation.substring(begin + 1, end);
				System.out.println("boardId = : " + boardId);
			}

			commentPageUrl = commentPageUrl.replace("#boardId#", boardId);
			commentPageUrl = commentPageUrl.replace("#threadId#", threadId);

			File dest = new File("C:/new.txt");
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
				writer.write(htmlContent);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			log.error(e1.getMessage());
			e1.printStackTrace();
		}

		return commentPageUrl;
	}
}
