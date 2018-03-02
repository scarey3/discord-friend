package xyz.scarey.discordfriend;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.scarey.discordfriend.model.User;

import java.io.IOException;

public class DiscordFriendSpeechlet implements SpeechletV2 {

    private static final Logger log = LoggerFactory.getLogger(DiscordFriendSpeechlet.class);

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
        if(requestEnvelope.getSession().getUser().getAccessToken() == null) {
            LinkAccountCard linkAccountCard = new LinkAccountCard();
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("You must link your Discord account");
            SpeechletResponse.newTellResponse(speech, linkAccountCard);
        } else {
            // Object mapping
            Unirest.setObjectMapper(new ObjectMapper() {
                private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                        = new com.fasterxml.jackson.databind.ObjectMapper();

                public <T> T readValue(String value, Class<T> valueType) {
                    try {
                        return jacksonObjectMapper.readValue(value, valueType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                public String writeValue(Object value) {
                    try {
                        return jacksonObjectMapper.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
        return null;
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), requestEnvelope.getSession().getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        Session session = requestEnvelope.getSession();
        String token = session.getUser().getAccessToken();

        // Intents defined in the interaction model
        if("GetUsernameIntent".equals(intentName)) {
            return getUsername(intent, session);
        } else if("AMAZON.HelpIntent".equals(intentName)) {
            return null;
        } else if("AMAZON.StopIntent".equals(intentName)) {
            return null;
        } else if("AMAZON.CancelIntent".equals(intentName)) {
            return null;
        } else {
            String errorSpeech = "This is unsupported.  Please try something else.";
            return newAskResponse(errorSpeech, errorSpeech);
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
    }

    private SpeechletResponse getUsername(final Intent intent, final Session session) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        User user;

        // GET /users/@me returns a User object from the Discord API
        try {
            HttpResponse<User> response = Unirest.get("https://discordapp.com/api/users/@me")
                    .header("Authorization", "Bearer " + session.getUser().getAccessToken())
                    .asObject(User.class);

            user = response.getBody();
            Unirest.shutdown();

            speech.setText("Your Discord username is " + user.getUsername());
        } catch(UnirestException | IOException e) {
            log.error(e.toString());
        }

        return SpeechletResponse.newTellResponse(speech);
    }

    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);

        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
}
