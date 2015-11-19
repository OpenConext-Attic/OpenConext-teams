package teams.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum Language {
  Dutch("nl"), English("en");

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
}
