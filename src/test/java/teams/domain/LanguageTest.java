package teams.domain;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Locale;
import java.util.Optional;

import org.junit.Test;

public class LanguageTest {

  @Test
  public void find_language_by_locale() {
    Optional<Language> lang = Language.find(Locale.ENGLISH);

    assertThat(lang.get(), is(Language.English));
  }

  @Test
  public void german_is_not_a_supported_language() {
    Optional<Language> lang = Language.find(Locale.GERMAN);

    assertFalse(lang.isPresent());
  }
}
