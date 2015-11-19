package teams.util;

import static com.google.common.html.HtmlEscapers.htmlEscaper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.common.base.Throwables;

import org.apache.commons.io.FileUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

import teams.service.mail.MailService;

public class LetterOpener implements MailService {

  @Override
  public void sendAsync(MimeMessagePreparator preparator) throws MailException {
    try {
      MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
      preparator.prepare(mimeMessage);

      String page = "<!DOCTYPE html><html>" +
          "<head>" +
          "<style type=\"text/css\">" +
          "iframe { border: 0; width: 100%; height: 800px; }" +
          "pre { white-space: pre-wrap; }" +
          "</style>" +
          "</head>" +
          "<body><dl>" +
          "<dt>to:</dt><dd>" + htmlEscaper().escape(Arrays.toString(mimeMessage.getAllRecipients())) + "</dd>" +
          "<dt>from:</dt><dd>" + htmlEscaper().escape(Arrays.toString(mimeMessage.getFrom())) + "</dd>" +
          "<dt>subject:</dt><dd>" + htmlEscaper().escape(mimeMessage.getSubject()) + "</dd>" +
          "<dt>content:</dt><dd>";

      MimeMultipart m = (MimeMultipart) mimeMessage.getContent();
      for (int i = 0; i < m.getCount(); i++) {
        String text = getText(m.getBodyPart(i));

        if (text.matches("(?s)\\s<html>.*")) { // somehow content type is not text/html
            page += "<iframe srcdoc=\"" + text.replace("\"", "&quot;") + "\"></iframe>";
        } else {
            page += "<pre>" + text + "</pre>";
        }
      }

      page += "</dd></dl></body></html>";

      openInBrowser(page);
    } catch (Exception e) {
      throw Throwables.propagate(e);
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
