package nl.surfnet.coin.teams.control;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;

import java.util.List;

import nl.surfnet.coin.teams.domain.TeamServiceProvider;
import nl.surfnet.coin.teams.service.TeamsDao;
import nl.surfnet.coin.teams.util.Stoker;
import nl.surfnet.coin.teams.util.StokerEntry;
import nl.surfnet.coin.teams.util.ViewUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

@Controller
public class AddAllowedServiceProvidersToTeamController {

  private final static Log logger = LogFactory.getLog(AddAllowedServiceProvidersToTeamController.class);
  private final static Function<TeamServiceProvider, String> toSpEntityId = new Function<TeamServiceProvider, String>() {
    @Override
    public String apply(TeamServiceProvider input) {
      return input.getSpEntityId();
    }
  };
  private final static Predicate<String> emptyStrings = new Predicate<String>() {
    @Override
    public boolean apply(String input) {
      return StringUtils.hasText(input);
    }
  };

  private static class ServiceProviderOrderer {
    private final String language;
    private final Collection<StokerEntry> serviceProviders;

    private final static Ordering<StokerEntry> byDisplayNameNl = new Ordering<StokerEntry>() {
      @Override
      public int compare(StokerEntry left, StokerEntry right) {
        return left.getDisplayNameNl().compareTo(right.getDisplayNameNl());
      }
    };
    private final static Ordering<StokerEntry> byDisplayNameEn = new Ordering<StokerEntry>() {
      @Override
      public int compare(StokerEntry left, StokerEntry right) {
        return left.getDisplayNameEn().compareTo(right.getDisplayNameEn());
      }
    };

    private ServiceProviderOrderer(String language, Collection<StokerEntry> serviceProviders) {
      this.language = language;
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

  @RequestMapping(value = "/teams/{id}/service-providers.shtml", method = RequestMethod.GET)
  public ModelAndView get(@PathVariable("id") String teamId, @RequestParam(value = "view", required = false) String view) {
    ModelMap model = new ModelMap();
    model.put("serviceProviders", new ServiceProviderOrderer("en", stoker.getEduGainServiceProviders()).ordered());
    model.put("teamId", teamId);
    model.put("view", view);
    model.put("existingServiceProviders", new ServiceProviderOrderer("en", stoker.getEduGainServiceProviders(transform(teamsDao.forTeam(teamId), toSpEntityId))).ordered());
    return new ModelAndView("add-allowed-serviceproviders", model);
  }

  @RequestMapping(value = "/service-providers.json", method = RequestMethod.GET, produces = "json")
  @ResponseBody
  public Collection<StokerEntry> serviceProviders() {
    return new ServiceProviderOrderer("en", stoker.getEduGainServiceProviders()).ordered();
  }

  @RequestMapping(value = "/teams/{id}/service-providers.shtml", method = RequestMethod.POST)
  public String post(@PathVariable("id") String teamId, @RequestParam(value = "view", required = false) String view, @RequestParam("services[]") List<String> services) throws UnsupportedEncodingException {

    Collection<String> spEntityIds = filter(services, emptyStrings);
    if (logger.isDebugEnabled()) {
      logger.debug("Adding the following spEntityIds " + spEntityIds);
    }

    teamsDao.persist(teamId, spEntityIds);
    return "redirect:/detailteam.shtml?team="
      + URLEncoder.encode(teamId, "utf-8") + "&view="
      + ViewUtil.getView(view);
  }

  public void setStoker(Stoker stoker) {
    this.stoker = stoker;
  }
}
