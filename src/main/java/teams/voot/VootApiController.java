package teams.voot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teams.interceptor.LoginInterceptor;
import teams.migration.Membership;
import teams.migration.Team;
import teams.repository.MembershipRepository;
import teams.repository.TeamRepository;
import teams.service.TeamExternalGroupDao;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@RestController
public class VootApiController {

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  @Autowired
  private TeamExternalGroupDao teamExternalGroupDao;

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL+ "/group/{localGroupId}")
  public Group findByLocalGroupId(@PathVariable("localGroupId") String localGroupId) {
    Team team = resolveOptionalOrThrow(teamRepository.findByUrn(localGroupId), localGroupId);
    return convertTeamToGroup(team);
  }

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL+ "/linked-locals")
  public List<Group> linkedLocalTeamsGroup(@RequestParam("externalGroupIds") String fullyQualifiedExternalGroupIds) {
    String[] ids = fullyQualifiedExternalGroupIds.split(",");
    List<String> urns = teamExternalGroupDao.getByExternalGroupIdentifiers(Arrays.asList(ids)).stream()
      .map(teamExternalGroup -> teamExternalGroup.getGrouperTeamId())
      .collect(toList());
    return teamRepository.findByUrnIn(urns).stream().map(this::convertTeamToGroup).collect(toList());
  }

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL+ "/linked-externals")
  public List<String> linkedExternalGroupIds(@RequestParam("teamId") String localGroupUrn) {
    return teamExternalGroupDao.getByTeamIdentifier(localGroupUrn).stream()
      .map(teamExternalGroup -> teamExternalGroup.getExternalGroup().getIdentifier())
      .collect(toList());
  }

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL+ "/members/{localGroupId}")
  public List<Member> getMembers(@PathVariable("localGroupId") String localGroupId) {
    Team team = resolveOptionalOrThrow(teamRepository.findByUrn(localGroupId), localGroupId);
    return team.getMemberships().stream().map(this::convertMembershipToMember).collect(toList());
  }

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL + "/groups")
  public List<Group> getAllGroups() {
    return StreamSupport.stream(teamRepository.findAll().spliterator(), false).map(this::convertTeamToGroup).collect(toList());
  }

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL + "/user/{uid}/groups")
  public List<Group> getGroupsForMember(@PathVariable("uid") String uid) {
    return teamRepository.findByMembershipsUrnPersonOrderByNameAsc(uid, new PageRequest(0, Integer.MAX_VALUE))
      .getContent()
      .stream()
      .map(this::convertTeamToGroup)
      .collect(toList());
  }

  @GetMapping("/" + LoginInterceptor.API_VOOT_URL + "/user/{uid}/groups/{groupId}")
  public Group getGroupsForMemberAndTeamUrn(@PathVariable("uid") String uid, @PathVariable("groupId") String groupId) {
    Optional<Membership> membershipOptional = membershipRepository.findByTeamUrnAndPersonUrn(groupId, uid);
    Membership membership = membershipOptional.orElseThrow(
      () -> new ResourceNotFoundException(String.format("Membership for team %s and Person %s not found", groupId, uid)));
    return this.convertTeamToGroup(membership.getTeam());
  }

  private Member convertMembershipToMember(Membership membership) {
    return new Member(membership.getUrnPerson(), membership.getPerson().getName(), membership.getPerson().getEmail());
  }

  private <T> T resolveOptionalOrThrow(Optional<T> optional, String urn) {
    return optional.orElseThrow(() -> new ResourceNotFoundException(String.format("Non existent Team with urn %s", urn)));
  }

  private Group convertTeamToGroup(Team team) {
    return new Group(team.getUrn(),team.getName(), team.getDescription(), "member" );
  }

}
