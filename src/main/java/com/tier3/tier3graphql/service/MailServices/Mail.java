package com.tier3.tier3graphql.service.MailServices;

import lombok.Data;

@Data
public class Mail {

	private String from;
	private String fromName;
	private String body;
	private String to;
	private String cc;
	private String bcc;
	private String subject;

}
