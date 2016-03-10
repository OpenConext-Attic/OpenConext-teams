package teams.control;

import com.google.common.collect.Ordering;
import nl.surfnet.coin.stoker.Stoker;
import nl.surfnet.coin.stoker.StokerEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import teams.domain.TeamServiceProvider;
import teams.service.TeamsDao;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static java.util.Comparator.comparing;
import static teams.util.ViewUtil.escapeViewParameters;

@Controller
@Profile("groupzy")
public class AddAllowedServiceProvidersToTeamController {

  private final static Log logger = LogFactory.getLog(AddAllowedServiceProvidersToTeamController.class);

  @Value("${group-name-context}")
  private String groupNameContext;

  private static class ServiceProviderOrderer {

    private final static Ordering<StokerEntry> byDisplayNameEn = Ordering.from(comparing(StokerEntry::getDisplayNameEn));

    private final Collection<StokerEntry> serviceProviders;

    private ServiceProviderOrderer(Collection<StokerEntry> serviceProviders) {
      this.serviceProviders = serviceProviders;
    }

    public Collection<StokerEntry> ordered() {
      return byDisplayNameEn.sortedCopy(serviceProviders);
    }
  }

  @Autowired
  private Stoker stoker;

  @Autowired
  private TeamsDao teamsDao;

  @RequestMapping(value = "/{id}/service-providers.shtml", method = RequestMethod.GET)
  public ModelAndView get(@PathVariable("id") String teamId) {
    ModelMap model = new ModelMap();
    model.put("serviceProviders", new ServiceProviderOrderer(stoker.getEduGainServiceProviders()).ordered());
    model.put("teamId", teamId);
    Collection<StokerEntry> eduGainServiceProviders = stoker.getEduGainServiceProviders(transform(teamsDao.forTeam(groupNameContext + teamId), TeamServiceProvider::getSpEntityId));
    model.put("existingServiceProviders", new ServiceProviderOrderer(eduGainServiceProviders).ordered());

    return new ModelAndView("add-allowed-serviceproviders", model);
  }

  @RequestMapping(value = "/service-providers.json", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public Collection<StokerEntry> serviceProviders() {
    return new ServiceProviderOrderer(stoker.getEduGainServiceProviders()).ordered();
  }

  @RequestMapping(value = "/{id}/service-providers.shtml", method = RequestMethod.POST)
  public String post(@PathVariable("id") String teamId, @RequestParam("services[]") List<String> services) throws UnsupportedEncodingException {
    Collection<String> spEntityIds = filter(services, StringUtils::hasText);
    if (logger.isDebugEnabled()) {
      logger.debug("Adding the following spEntityIds " + spEntityIds);
    }

    // We need the teamId pre-fixed with the group name context as this is what API uses when filtering
    String uniqueTeamId = groupNameContext + teamId;
    teamsDao.persist(uniqueTeamId, spEntityIds);

    return escapeViewParameters("redirect:/detailteam.shtml?team=%s", teamId);
  }

}
