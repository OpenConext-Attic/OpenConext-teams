package nl.surfnet.coin.teams.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;

public class MailServiceImpl implements MailService {

  @Autowired
  private JavaMailSender mailSender;

  @Async
  public void sendAsync(MimeMessagePreparator preparator) throws MailException {
    mailSender.send(preparator);
  }

  @Async
  public void sendAsync(SimpleMailMessage msg) throws MailException {
    mailSender.send(msg);
  }

  public void setMailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }
}
