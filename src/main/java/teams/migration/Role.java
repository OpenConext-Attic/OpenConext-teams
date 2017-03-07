package teams.migration;

public enum Role {

  MEMBER(0), MANAGER(1), ADMIN(2);

  private int importance;

  Role(int importance) {
    this.importance = importance;
  }

  boolean isMoreImportant(Role role) {
    return this.importance > role.importance;
  }

}
