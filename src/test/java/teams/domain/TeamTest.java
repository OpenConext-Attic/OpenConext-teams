package teams.domain;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

public class TeamTest {

  @Test
  public void shouldRemoveAllMembers() {
    Member member1 = new Member(ImmutableSet.of(Role.Member), "name", "id1", "email");
    Member member2 = new Member(ImmutableSet.of(Role.Member), "name", "id2", "email");
    Member member3 = new Member(ImmutableSet.of(Role.Member), "name", "id3", "email");

    Team team = new Team("id", "name", "description", Arrays.asList(member1, member2, member3));

    assertThat(team.getMembers().size(), is(3));

    team.removeMembers(member1, member2);

    assertThat(team.getMembers(), contains(member3));
  }
}
