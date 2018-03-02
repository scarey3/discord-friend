package xyz.scarey.discordfriend;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class DiscordFriendSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds;

    static {
        supportedApplicationIds = new HashSet<>();

        /*

            Alexa skill ID must be added to the supportedApplicationIds

         */
        supportedApplicationIds.add("<replace with skill ID>");
    }

    public DiscordFriendSpeechletRequestStreamHandler() {
        super(new DiscordFriendSpeechlet(), supportedApplicationIds);
    }
}
