package teams.domain;

public class ExternalGroupProvider {

  private final String identifier;
  private final String name;

  public ExternalGroupProvider(String identifier, String name) {
    this.identifier = identifier;
    this.name = name;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ExternalGroupProvider that = (ExternalGroupProvider) o;

    if (!identifier.equals(that.identifier)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return identifier.hashCode();
  }
}
