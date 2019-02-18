package co.botanalytics.sdk;

import static org.testng.Assert.*;

import co.botanalytics.sdk.exceptions.BotanalyticsConfigurationException;
import co.botanalytics.sdk.exceptions.BotanalyticsJSONException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.Test;

import java.io.IOException;

public class BotanalyticsClientTest {

    @Test
    public void testCustomDomainConfiguration() throws BotanalyticsConfigurationException, IOException {

        String token = RandomStringUtils.random(10, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
        String customDomain = RandomStringUtils.random(10, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789").concat(".com");

        BotanalyticsClient instance = new TestBotanalyticsClient(token, customDomain);

        assertEquals(instance.getBaseUri().toString(), "https://".concat(customDomain).concat("/v1/"));

        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        JsonNode rootNode = OBJECT_MAPPER.readTree("{\"contentMessage\": {}}");

        JsonNode contentMessageNode = rootNode.get("contentMessage");

        ((ObjectNode) contentMessageNode).put("type", "image");
        ((ObjectNode) contentMessageNode).put("fileUrl", "blabla url");

        String payload = OBJECT_MAPPER.writeValueAsString(rootNode);

        System.out.println(payload);
        System.out.println(instance.getBaseUri().resolve("messages/rbm/"));
    }

    private static class TestBotanalyticsClient extends BotanalyticsClient {

        protected TestBotanalyticsClient(String token, String domain, int version) throws BotanalyticsConfigurationException {
            super(token, domain, version);
        }

        protected TestBotanalyticsClient(String token, String domain) throws BotanalyticsConfigurationException {
            super(token, domain);
        }

        protected TestBotanalyticsClient(String token) throws BotanalyticsConfigurationException {
            super(token);
        }
    }
}
