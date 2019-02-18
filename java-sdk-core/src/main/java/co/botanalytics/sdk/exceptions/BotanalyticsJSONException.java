package co.botanalytics.sdk.exceptions;

public class BotanalyticsJSONException extends Throwable {

    public BotanalyticsJSONException(String message) {

        super(message);
    }

    public BotanalyticsJSONException(String message, Exception e) {

        super(message, e);
    }
}
