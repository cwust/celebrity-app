package br.com.cwust.celebrity;

import android.support.annotation.NonNull;
import android.util.Log;

import br.com.cwust.celebrity.dto.GameSetupDTO;
import br.com.cwust.celebrity.engine.CelebrityGameState;
import br.com.cwust.rtmp.GenericMessageHandler;
import br.com.cwust.rtmp.RTMPClient;
import br.com.cwust.rtmp.RTMPMessage;


public class CelebrityMessageHandler extends GenericMessageHandler implements CelebrityCodes{

    private static final String TAG = "cwust.celebrity.CelebrityMessageHandler";

    private CelebrityActivity activity;
    private CelebrityGameState gameState;

    public CelebrityMessageHandler(@NonNull CelebrityActivity activity, @NonNull RTMPClient client) {
        super(client);
        this.activity = activity;
        initMapHandlers();
    }

    private void initMapHandlers() {
        setHandler(MSG_GAME_SETUP_UPDATED, this::handleMsgGameSetupUpdated);
        setHandler(MSG_GAME_SETUP_COMPLETE, this::handleMsgGameSetupComplete);
        setHandler(MSG_PLAYER_READY, this::handleMsgPlayerReady);
    }

    public void setGameState(CelebrityGameState gameState) {
        this.gameState = gameState;
    }

    private void handleMsgPlayerReady(@NonNull RTMPMessage message) {
        Log.d(TAG, "handleMsgPlayerReady: " + message);
        this.gameState.setPlayerReady(message.getSenderId(), message.getValue());
    }

    public void broadcastMsgGameSetupUpdated(@NonNull GameSetupDTO gameSetupInfo) {
        this.broadcastMessage(MSG_GAME_SETUP_UPDATED, gameSetupInfo);
    }

    private void handleMsgGameSetupUpdated(@NonNull RTMPMessage message) {
        this.activity.gameSetupUpdated(message.getValue());
    }

    private void handleMsgGameSetupComplete(@NonNull RTMPMessage message) {
        this.activity.gameSetupComplete(message.getValue());
    }

    public void sendMsgImReady(boolean ready) {
        this.sendMsgToHost(MSG_PLAYER_READY, new Boolean(ready));
    }

}
