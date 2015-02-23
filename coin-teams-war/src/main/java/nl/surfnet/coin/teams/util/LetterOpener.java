package nl.surfnet.coin.teams.util;

import nl.surfnet.coin.shared.service.MailService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class LetterOpener implements MailService {

  private static final Logger log = LoggerFactory.getLogger(LetterOpener.class);

  @Override
  public void sendAsync(MimeMessagePreparator preparator) throws MailException {

    try {
      MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
      preparator.prepare(mimeMessage);
      MimeMultipart m = (MimeMultipart) mimeMessage.getContent();
      for (int i = 0; i < m.getCount(); i++) {
        BodyPart bodyPart = m.getBodyPart(i);
        String text = getText(bodyPart);
        openInBrowser(text);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private void openInBrowser(String text) throws IOException {
    File tempFile = File.createTempFile("javamail", ".html");
    FileUtils.writeStringToFile(tempFile, text);
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("mac os x")) {
      Runtime runtime = Runtime.getRuntime();
      runtime.exec("open " + tempFile.getAbsolutePath());
    }
    log.info("*************************** CONTENTS EMAIL ***************************");
    log.info(text);
    log.info("**********************************************************************");
  }

  /**
   * Return the primary text content of the message.
   */
  private String getText(Part p) throws MessagingException, IOException {
    if (p.isMimeType("text/plain") || p.isMimeType("text/html")) {
      return (String) p.getContent();
    }

    if (p.isMimeType("multipart/alternative")) {
      // prefer html text over plain text
      Multipart mp = (Multipart) p.getContent();
      String text = null;
      for (int i = 0; i < mp.getCount(); i++) {
        Part bp = mp.getBodyPart(i);
        if (bp.isMimeType("text/plain")) {
          if (text == null)
            text = getText(bp);
          continue;
        } else if (bp.isMimeType("text/html")) {
          String s = getText(bp);
          if (s != null)
            return s;
        } else {
          return getText(bp);
        }
      }
      return text;
    } else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      for (int i = 0; i < mp.getCount(); i++) {
        String s = getText(mp.getBodyPart(i));
        if (s != null)
          return s;
      }
    }

    return null;
  }

  @Override
  public void sendAsync(SimpleMailMessage msg) throws MailException {
    try {
      openInBrowser(msg.getText());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
