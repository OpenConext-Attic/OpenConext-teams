package nl.surfnet.coin.teams.service.impl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.surfnet.coin.api.client.OpenConextApi10aTwoLegged;
import nl.surfnet.coin.api.client.OpenConextJsonParser;
import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.api.client.domain.PersonEntry;
import nl.surfnet.coin.teams.service.ApiService;

public class ApiServiceImpl implements ApiService {

    public static final Token TOKEN = new Token("", "");
    private Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);

    private String apiLocation;
    private OAuthService service;
    private OpenConextJsonParser parser;

    public ApiServiceImpl(String oauthSecret, String oauthKey, String apiLocation) {
        this.apiLocation = apiLocation;
        service = new ServiceBuilder()
                .provider(new OpenConextApi10aTwoLegged())
                .apiKey(oauthKey)
                .apiSecret(oauthSecret)
                .signatureType(SignatureType.QueryString)
                .debug()
                .build();
        parser = new OpenConextJsonParser();
    }

    public Person getPerson(String personId) {
        final String url = new StringBuilder().append(apiLocation)
                .append("social/rest/people/").append(personId).toString();
        OAuthRequest req = new OAuthRequest(Verb.GET, url);
        service.signRequest(TOKEN, req);
        Response response = req.send();
        final String bodyText = response.getBody();
        log.debug("Response body: {}", bodyText);

        try {
            final PersonEntry entry = parser.parsePerson(new ByteArrayInputStream(bodyText.getBytes("UTF-8")));
            final Person person = entry.getEntry();
            return person;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
