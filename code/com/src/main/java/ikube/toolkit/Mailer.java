package ikube.toolkit;

import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.net.InetAddress;
import java.security.Security;
import java.util.Properties;

/**
 * For sending mail to various accounts.
 * 
 * @author Michael Couck
 * @since 16.08.10
 * @version 01.00
 */
@SuppressWarnings("restriction")
public class Mailer implements IMailer {

	static {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}

	private Logger logger = Logger.getLogger(this.getClass());

	/** "smtp" */
	private transient String protocol;
	/** "true" */
	private transient String auth;
	/** "465" */
	private transient String port;
	/** "smtp.gmail.com" */
	private transient String mailHost;
	/** ikube.notifications */
	private transient String user;
	/** '*********' */
	private transient String password;
	/** ikube.notifications@gmail.com */
	private transient String sender;
	/** ikube.notifications@gmail.com */
	private transient String recipients;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean sendMail(final String subject, final String body, final String... attachmentFilePaths) throws Exception {
		Properties properties = System.getProperties();
		properties.put("mail.transport.protocol", protocol);
		properties.put("mail.host", mailHost);
		properties.put("mail.smtp.auth", auth);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.socketFactory.port", port);
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");
		properties.put("mail.smtp.quitwait", "false");

		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});
        if (logger.isDebugEnabled()) {
            session.setDebug(Boolean.TRUE);
        }

		MimeMessage message = new MimeMessage(session);
		message.setSender(new InternetAddress(sender));
		message.setSubject(subject + " from : " + InetAddress.getLocalHost().getHostAddress());
		message.setContent(body, "text/plain");
		if (recipients.indexOf(',') > 0) {
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
		} else {
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
		}

		if (attachmentFilePaths != null && attachmentFilePaths.length > 0) {
			Multipart multipart = new MimeMultipart();
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			for (final String attachmentFilePath : attachmentFilePaths) {
				File file = new File(attachmentFilePath);
				if (!file.exists() || !file.canRead() || !file.isFile() || file.isHidden()) {
					logger.warn("Couldn't attach file as attachment : " + file);
					continue;
				}
				MimeBodyPart bodyPart = getAttachment(file);
				multipart.addBodyPart(bodyPart);
			}
			message.setContent(multipart);
			multipart.addBodyPart(mimeBodyPart);
		}

		// Transport.send(message);
		Transport transport = null;
		try {
			logger.debug("Sending mail to : " + user + ", " + mailHost);
			transport = session.getTransport("smtps");
			transport.connect(mailHost, Integer.parseInt(port), user, password);
			transport.sendMessage(message, message.getAllRecipients());
		} catch (final Exception e) {
			logger.error("Exception sending mail to : " + recipients + ", " + subject + ", " + body, e);
			throw new RuntimeException(e);
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (final Exception e) {
					logger.error("Exception closing the mail transport : " + transport, e);
				}
			}
		}
		return Boolean.TRUE;
	}

	private MimeBodyPart getAttachment(final File file) throws MessagingException {
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setFileName(file.getName());
		mimeBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
		return mimeBodyPart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMailHost(final String mailHost) {
		this.mailHost = mailHost;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUser(final String user) {
		this.user = user;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSender(final String sender) {
		this.sender = sender;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRecipients(final String recipients) {
		this.recipients = recipients;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAuth(final String auth) {
		this.auth = auth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPort(final String port) {
		this.port = port;
	}

}