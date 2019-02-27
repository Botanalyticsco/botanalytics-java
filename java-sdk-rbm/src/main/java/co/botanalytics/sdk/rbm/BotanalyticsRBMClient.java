package co.botanalytics.sdk.rbm;

import co.botanalytics.sdk.BotanalyticsClient;
import co.botanalytics.sdk.exceptions.BotanalyticsAuthorizationException;
import co.botanalytics.sdk.exceptions.BotanalyticsConfigurationException;
import co.botanalytics.sdk.exceptions.BotanalyticsJSONException;
import co.botanalytics.sdk.exceptions.BotanalyticsRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.services.rcsbusinessmessaging.v1.model.AgentEvent;
import com.google.api.services.rcsbusinessmessaging.v1.model.AgentMessage;
import com.google.api.services.rcsbusinessmessaging.v1.model.Capabilities;
import com.google.api.services.rcsbusinessmessaging.v1.model.Empty;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;

public class BotanalyticsRBMClient extends BotanalyticsClient {

    private static final String TYPE_IMAGE = "image";
    private static final String TYPE_AUDIO = "auido";
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_FILE = "file";

    private final URI MESSAGES_URI = getBaseUri().resolve("messages/rbm/");

    public BotanalyticsRBMClient(String token, String domain, int version) throws BotanalyticsConfigurationException {
        super(token, domain, version);
    }

    public BotanalyticsRBMClient(String token, String domain) throws BotanalyticsConfigurationException {
        super(token, domain);
    }

    public BotanalyticsRBMClient(String token) throws BotanalyticsConfigurationException {
        super(token);
    }

    public void logMessage(AgentEvent message) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        String payload = message.toString();

        getLogger().debug("Logging message: {}", payload);

        sendRequest(MESSAGES_URI, payload);
    }

    public void logMessage(AgentMessage message) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        String payload = message.toString();

        getLogger().debug("Logging message: {}", payload);

        sendRequest(MESSAGES_URI, payload);
    }

    public void logMessage(Empty empty) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        String payload = empty.toString();

        getLogger().debug("Logging message: {}", payload);

        sendRequest(MESSAGES_URI, payload);
    }

    public void logMessage(Capabilities capabilities) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        String payload = capabilities.toString();

        getLogger().debug("Logging message: {}", payload);

        sendRequest(MESSAGES_URI, payload);
    }

    public String logMessage(HttpServletRequest request) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        try {

            JsonNode rootNode = OBJECT_MAPPER.readTree(request.getInputStream());

            if (!rootNode.has("message"))
                return null;

            JsonNode messageNode = rootNode.get("message");

            if (!messageNode.has("data"))
                return null;

            JsonNode dataNode = messageNode.get("data");

            String encodedResponse = dataNode.asText();

            byte decodedResponse[] = Base64.getDecoder().decode(encodedResponse);

            String decodedDataPayload = new String(decodedResponse, "UTF-8");

            JsonNode dataDecodedNode = OBJECT_MAPPER.readTree(decodedDataPayload);

            ((ObjectNode) messageNode).put("data_decoded", dataDecodedNode);

            String payload = OBJECT_MAPPER.writeValueAsString(rootNode);

            getLogger().debug("Logging message: {}", payload);

            sendRequest(MESSAGES_URI, payload);

            return OBJECT_MAPPER.writeValueAsString(rootNode);

        } catch (IOException e) {

            getLogger().debug("Failed to extract encoded data from JSON payload.");

            throw new BotanalyticsJSONException("Failed to extract encoded data from JSON payload.", e);
        }
    }

    public void logImageMessage(AgentMessage message, String fileUrl) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        sendFilePayloadRequest(message, fileUrl, TYPE_IMAGE);
    }

    public void logAudioMessage(AgentMessage message, String fileUrl) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        sendFilePayloadRequest(message, fileUrl, TYPE_AUDIO);
    }

    public void logVideoMessage(AgentMessage message, String fileUrl) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        sendFilePayloadRequest(message, fileUrl, TYPE_VIDEO);
    }

    public void logFileMessage(AgentMessage message, String fileUrl) throws BotanalyticsRequestException, BotanalyticsAuthorizationException, BotanalyticsJSONException {

        sendFilePayloadRequest(message, fileUrl, TYPE_FILE);
    }

    private void sendFilePayloadRequest(AgentMessage message, String fileUrl, String fileType) throws BotanalyticsJSONException, BotanalyticsRequestException, BotanalyticsAuthorizationException {

        String payload = message.toString();

        getLogger().debug("Logging message: {}", payload);

        try {

            JsonNode rootNode = OBJECT_MAPPER.readTree(payload);

            JsonNode contentMessageNode = rootNode.get("contentMessage");

            if (contentMessageNode == null)
                throw new BotanalyticsJSONException("Payload is missing contentMessage field.");

            ((ObjectNode) contentMessageNode).put("type", fileType);
            ((ObjectNode) contentMessageNode).put("fileUrl", fileUrl);

            payload = OBJECT_MAPPER.writeValueAsString(rootNode);

        } catch (IOException e) {

            throw new BotanalyticsJSONException("Failed to modify JSON payload.", e);
        }

        sendRequest(MESSAGES_URI, payload);
    }
}
