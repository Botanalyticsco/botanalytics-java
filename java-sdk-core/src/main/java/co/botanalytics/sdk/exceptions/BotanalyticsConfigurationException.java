package co.botanalytics.sdk.exceptions;

import java.net.URISyntaxException;
import java.text.MessageFormat;

public class BotanalyticsConfigurationException extends Exception {

    public BotanalyticsConfigurationException(String missingField) {

        super(MessageFormat.format("Failed to configure Botanalytics client due to missing '{0}' field.", missingField));
    }

    public BotanalyticsConfigurationException(URISyntaxException e) {

        super("Failed to construct base URL with given configuration.", e);
    }
}
