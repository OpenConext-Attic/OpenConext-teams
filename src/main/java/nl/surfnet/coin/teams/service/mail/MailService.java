package nl.surfnet.coin.teams.service.mail;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

public interface MailService {
  void sendAsync(MimeMessagePreparator preparator) throws MailException;
  void sendAsync(SimpleMailMessage msg) throws MailException;
}
