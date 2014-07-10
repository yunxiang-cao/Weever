package com.yumidea.weever.comments.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "info_news_comment")
public class NewsComment {
	@Id
	@Column(length = 35)
	private String infoId;
	private String domainName;
	private String commentDataurl;
	private String charSet;

	private Date lastCommentTime = null;

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getCommentDataurl() {
		return commentDataurl;
	}

	public void setCommentDataurl(String commentDataurl) {
		this.commentDataurl = commentDataurl;
	}

	public String getCharSet() {
		return charSet;
	}

	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	public String getInfoId() {
		return infoId;
	}

	public void setInfoId(String infoId) {
		this.infoId = infoId;
	}

	public Date getLastCommentTime() {
		return lastCommentTime;
	}

	public void setLastCommentTime(Date lastCommentTime) {
		this.lastCommentTime = lastCommentTime;
	}

}
