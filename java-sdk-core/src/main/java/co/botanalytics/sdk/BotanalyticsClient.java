package co.botanalytics.sdk;

import co.botanalytics.sdk.exceptions.BotanalyticsAuthorizationException;
import co.botanalytics.sdk.exceptions.BotanalyticsConfigurationException;
import co.botanalytics.sdk.exceptions.BotanalyticsJSONException;
import co.botanalytics.sdk.exceptions.BotanalyticsRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class BotanalyticsClient {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(BotanalyticsClient.class);
    private static final String DEFAULT_DOMAIN = "api.botanalytics.co";
    private static final int DEFAULT_VERSION = 1;
    private static final String DEFAULT_ERROR_MESSAGE = "An unknown error returned from endpoint.";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String token;
    private final URI baseUri;

    public BotanalyticsClient(String token, String domain, int version) throws BotanalyticsConfigurationException {

        if (token == null || token.trim().length() == 0) {

            LOGGER.error("Failed to configure Botanalytics client due to missing 'token' field.");

            throw new BotanalyticsConfigurationException("token");
        }

        if (domain == null || domain.trim().length() == 0) {

            LOGGER.error("Failed to configure Botanalytics client due to missing 'domain' field.");

            throw new BotanalyticsConfigurationException("domain");
        }

        this.token = token;
        this.baseUri = constructBaseURI(domain, version);
    }

    public BotanalyticsClient(String token, String domain) throws BotanalyticsConfigurationException {

        this(token, domain, DEFAULT_VERSION);
    }

    public BotanalyticsClient(String token) throws BotanalyticsConfigurationException {

        this(token, DEFAULT_DOMAIN);
    }

    private URI constructBaseURI(String domain, int version) throws BotanalyticsConfigurationException {

        try {

            return new URI("https", domain, "/v".concat(Integer.toString(version)).concat("/"), null);

        } catch (URISyntaxException e) {

            LOGGER.error("Failed to construct base URL with given configuration.", e);

            throw new BotanalyticsConfigurationException(e);
        }
    }

    protected Logger getLogger() {

        return LOGGER;
    }

    protected URI getBaseUri() {
        return baseUri;
    }

    protected void sendRequest(URI uri, String payload) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        HttpPost httpPost = new HttpPost(uri.toString());

        httpPost.setHeader("Authorization", "Token ".concat(token));

        httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        CloseableHttpResponse response;

        try {

            response = httpClient.execute(httpPost);

        } catch (IOException e) {

            LOGGER.error("Failed to send request.", e);

            throw new BotanalyticsRequestException(e);
        }

        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {

            try {

                String errorMessage = extractErrorMessage(response.getEntity().getContent());

                LOGGER.error(errorMessage);

                throw new BotanalyticsAuthorizationException(errorMessage);

            } catch (IOException e) {

                LOGGER.error("Failed to parse response body.", e);

                throw new BotanalyticsJSONException("Failed to parse response body.", e);
            }

        } else if (statusCode == HttpStatus.SC_FORBIDDEN) {

            try {

                String errorMessage = extractErrorMessage(response.getEntity().getContent());

                LOGGER.error(errorMessage);

                throw new BotanalyticsAuthorizationException(errorMessage);

            } catch (IOException e) {

                LOGGER.error("Failed to parse response body.", e);

                throw new BotanalyticsJSONException("Failed to parse response body.", e);
            }

        } else {

            try {

                printWarningsIfPresent(response.getEntity().getContent());

            } catch (IOException e) {

                LOGGER.error("Failed to parse response body.", e);
                e.printStackTrace();

                throw new BotanalyticsJSONException("Failed to parse response body.", e);
            }
        }
    }

    private String extractErrorMessage(InputStream content) throws IOException {

        JsonNode rootNode = OBJECT_MAPPER.readTree(content);

        if (!rootNode.has("errors")) {

            return DEFAULT_ERROR_MESSAGE;
        }

        JsonNode errorsNode = rootNode.get("errors");

        if (!errorsNode.isArray()) {

            return DEFAULT_ERROR_MESSAGE;
        }

        JsonNode errorMessageNode = errorsNode.iterator().next();

        return errorMessageNode.asText();
    }

    private void printWarningsIfPresent(InputStream content) throws IOException {

        JsonNode rootNode = OBJECT_MAPPER.readTree(content);

        if (rootNode == null)
            return;

        if (!rootNode.has("warnings")) {

            return;
        }

        JsonNode warningsNode = rootNode.get("warnings");

        if (!warningsNode.isArray()) {

            return;
        }

        for (JsonNode warningMessageNode : warningsNode) {

            LOGGER.warn(warningMessageNode.asText());
        }
    }
}
