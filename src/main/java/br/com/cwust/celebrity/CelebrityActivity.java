package br.com.cwust.celebrity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.stream.Collectors;

import br.com.cwust.celebrity.dto.GameSetupDTO;
import br.com.cwust.celebrity.engine.CelebrityGameState;
import br.com.cwust.rtmp.PlayerInfo;
import br.com.cwust.rtmp.RTMPClient;

public class CelebrityActivity extends AppCompatActivity {

    private static final String TAG = "cwust.celebrity.CelebrityActivity";

    private TextView tvMiddle;
    private Switch swImReady;

    private RTMPClient rtmpClient;
    private CelebrityMessageHandler messageHandler;

    private CelebrityGameState gameState;
    private boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screen);

        this.tvMiddle = findViewById(R.id.tv_middle);
        this.swImReady = findViewById(R.id.sw_im_ready);
        this.swImReady.setOnCheckedChangeListener((btnView, isChecked) -> changeReadyStatus(isChecked));
        this.initRTMPClient();

    }

    private void initRTMPClient() {
        this.rtmpClient = new RTMPClient(this);
        this.rtmpClient.setOnSelectPlayersClosed(() -> this.showWaitingForPlayers());
        this.rtmpClient.setOnGamePlayersSelected((players) -> this.gameStarted(players));
        this.messageHandler = new CelebrityMessageHandler(this, rtmpClient);


        this.rtmpClient.init();
    }

    private void gameStarted(List<PlayerInfo> players) {
        if (this.isHost) {
            this.messageHandler.setMeAsHost();
            this.gameState = new CelebrityGameState(this.messageHandler, players);
            this.messageHandler.setGameState(gameState);
        }

        showMiddleMessage(String.format(
                "Game started%nPlayers:%n%n" +
                players.stream()
                        .map(p -> String.format("(id=%s,displayName=%s", p.getId(), p.getDisplayName()))
                        .collect(Collectors.joining("\n"))
                )
        );
    }

    private void showWaitingForPlayers() {
        showMiddleMessage(R.string.waiting_for_players);
    }

    public void btn1Click(View view) {

    }

    public void btn2Click(View view) {
        this.isHost = true;
        this.rtmpClient.openSelectPlayers();
    }

    public void btn3Click(View view) {
        this.rtmpClient.waitForInvitation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: requestCode="+requestCode + ", resultCode=" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        rtmpClient.onActivityResult(requestCode, resultCode, data);
    }

    private void alert(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Message")
                .setMessage(msg)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    private void showMiddleMessage(int resId) {
        tvMiddle.setText(resId);
    }

    private void showMiddleMessage(String msg) {
        tvMiddle.setText(msg);
    }

    private void changeReadyStatus(boolean ready) {
        this.messageHandler.sendMsgImReady(ready);
    }

    public void gameSetupUpdated(GameSetupDTO gameSetup) {
        showMiddleMessage("gameSetupUpdated: " + gameSetup + (
                this.gameState == null ? "\n-" : "\n\n\n" + this.gameState.getGameSetupInfo()
                )) ;
    }

    public void gameSetupComplete(GameSetupDTO gameSetup) {
        Log.d(TAG, "gameSetupComplete: " + gameSetup);
    }
}
