package teams.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum Language {

  Dutch("nl"), English("en"), Nederlands("nl"), Engels("en");

  private final String languageCode;

  private Language(String languageCode) {
    this.languageCode = languageCode;
  }

  public Locale locale() {
    return Locale.forLanguageTag(languageCode);
  }

  public static Optional<Language> find(Locale locale) {
    return Arrays.stream(values())
      .filter(l -> l.languageCode.equals(locale.getLanguage()))
      .findFirst();
  }

  public static Language[] nlLanguages() {
    return new Language[]{Nederlands, Engels};
  }

  public static Language[] enLanguages() {
    return new Language[]{Dutch, English};
  }
}
