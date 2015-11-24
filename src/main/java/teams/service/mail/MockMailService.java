package teams.service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class MockMailService implements MailService {

  private static final Logger logger = LoggerFactory.getLogger(MockMailService.class);

  @Override
  public void sendAsync(final MimeMessagePreparator preparator) throws MailException {
    logger.info("Sending mail\n:" + preparator.toString());
  }

  @Override
  public void sendAsync(SimpleMailMessage msg) throws MailException {
    logger.info("Sending mail\n:" + msg.toString());
  }
}
