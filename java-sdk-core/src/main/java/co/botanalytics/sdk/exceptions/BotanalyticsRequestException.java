package co.botanalytics.sdk.exceptions;

import java.io.IOException;

public class BotanalyticsRequestException extends Exception {

    public BotanalyticsRequestException(IOException e) {

        super("Failed to complete request.", e);
    }
}
