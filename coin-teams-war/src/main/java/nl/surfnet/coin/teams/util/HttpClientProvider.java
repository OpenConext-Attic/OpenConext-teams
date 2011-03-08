/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.springframework.stereotype.Component;

/**
 * Responsible for configuring httpClient
 * 
 */
// TODO move the httpClientProvider to a commons package
@Component(value = "httpClientProvider")
public class HttpClientProvider {
  /*
   * http://hc.apache.org/httpcomponents-client/tutorial/html/connmgmt.html#d4e492
   */
  private DefaultHttpClient httpClient;

  /**
   * Constructor
   */
  public HttpClientProvider() {
    super();
    try {
      init();
    } catch (Exception e) {
      throw new RuntimeException("Exception in configuration for httpClient", e);
    }
  }

  /*
   * Initialise the schemaRegistry for connecting to Engine Block
   */
  private void init() throws Exception {
    /*
     * See for documentation
     * http://java.sun.com/javase/6/docs/technotes/guides/security
     * /jsse/JSSERefGuide.html
     */
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    /*
     * Only used for internal https communication (no need for certificate
     * validation)
     */
    schemeRegistry.register(new Scheme("https", 443, new SSLSocketFactory(
        new TrustStrategy() {
          @Override
          public boolean isTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
            return true;
          }
        })));
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
        .getSocketFactory()));
    /*
     * To re-use connections we use the ThreadSafeClientConnManager
     */
    ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(
        schemeRegistry);
    connManager.setMaxTotal(50);
    // There is only one route, so the maximum per route equals the maximum
    // total
    connManager.setDefaultMaxPerRoute(50);
    httpClient = new DefaultHttpClient(connManager);

  }

  /**
   * @return the httpClient
   */
  public DefaultHttpClient getHttpClient() {
    return httpClient;
  }

}
