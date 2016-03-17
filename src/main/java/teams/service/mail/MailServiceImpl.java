package teams.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;

//we don't provide feedback, so why have bad UX for waiting on mails
public class MailServiceImpl implements MailService {

  @Autowired
  private JavaMailSender mailSender;

  @Async
  public void sendAsync(MimeMessagePreparator preparator) throws MailException {
    new Thread(() -> mailSender.send(preparator)).start();
  }

  @Async
  public void sendAsync(SimpleMailMessage msg) throws MailException {
    new Thread(() -> mailSender.send(msg)).start();
  }

  public void setMailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }
}
