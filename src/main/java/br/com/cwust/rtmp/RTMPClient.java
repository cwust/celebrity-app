package br.com.cwust.rtmp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import br.com.cwust.celebrity.CelebrityCodes;
import br.com.cwust.celebrity.CelebrityException;

public class RTMPClient implements CelebrityCodes {

    private static final String TAG = "cwust.celebrity.RTMPClient";

    public static final int MIN_TOTAL_PLAYERS = 2;
    public static final int MAX_TOTAL_PLAYERS = 8;

    private GoogleSignInAccount signInAccount;
    private RealTimeMultiplayerClient rtmpClient;
    private InvitationsClient invitationsClient;
    private PlayersClient playersClient;
    private String myPlayerId;
    private String myParticipantId;
    private String roomId;
    private List<Participant> participants;

    private Activity activity;
    private Map<Integer, BiConsumer<Integer, Intent>> mapActivityResultHandlers;

    private Runnable onSelectPlayersClosedCallback;
    private Consumer<List<PlayerInfo>> onGamePlayersSelectedCallback;
    private RTMPMessageHandler messageHandler;

    private RoomUpdateCallback roomUpdateCallback = new RoomUpdateCallback() {

        @Override
        public void onRoomCreated(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomCreated: ");
        }

        @Override
        public void onJoinedRoom(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onJoinedRoom: ");
        }

        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            Log.d(TAG, "onLeftRoom: ");
        }

        @Override
        public void onRoomConnected(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomConnected: ");

            List<PlayerInfo> players = room.getParticipants().stream()
                    .map(RTMPClient.this::participantToCelebrityPlayerInfo)
                    .collect(Collectors.toList());

            onGamePlayersSelectedCallback.accept(players);
        }
    };

    private RoomStatusUpdateCallback roomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            Log.d(TAG, "onRoomConnecting: ");
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            Log.d(TAG, "onRoomAutoMatching: ");
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeerInvitedToRoom: ");
        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeerDeclined: ");
        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeerJoined: ");
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeerLeft: ");
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d(TAG, "onConnectedToRoom: ");
            participants = room.getParticipants();
            myParticipantId = room.getParticipantId(myPlayerId);
            if (roomId == null) {
                roomId = room.getRoomId();
            }
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            Log.d(TAG, "onDisconnectedFromRoom: ");
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeersConnected: ");
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            Log.d(TAG, "onPeersDisconnected: ");
        }

        @Override
        public void onP2PConnected(@NonNull String s) {
            Log.d(TAG, "onP2PConnected: ");
        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {
            Log.d(TAG, "onP2PDisconnected: ");
        }
    };

    private OnRealTimeMessageReceivedListener rtMessageReceivedListener = (realTimeMessage) -> {
        Log.d(TAG, "onRealTimeMessageReceived: ");
        byte[] bytes = realTimeMessage.getMessageData();
        Log.d(TAG, "Message received: " + bytes);
        try {
            processMessageBytes(bytes, realTimeMessage.getSenderParticipantId());
        } catch (Exception e) {
            throw new CelebrityException(e, "Error receiving message");
        }
    };

    private void processMessageBytes(byte [] bytes, String senderId) throws IOException, ClassNotFoundException {
        RTMPMessage message = RTMPMessage.fromBytes(bytes);
        message.setSenderId(senderId);
        this.messageHandler.handleMessage(message);
    }

    private InvitationCallback invitationCallback = new InvitationCallback() {
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            Log.d(TAG, "onInvitationReceived: ");
        }

        @Override
        public void onInvitationRemoved(@NonNull String s) {
            Log.d(TAG, "onInvitationRemoved: ");
        }
    };

    public RTMPClient(Activity activity) {
        this.activity = activity;
        this.initActivityResultHandlers();
    }

    public void init() {
        Log.d(TAG, "init: ");
        this.startSignIn();
    }

    private void initActivityResultHandlers() {
        this.mapActivityResultHandlers = new HashMap<>();

        this.mapActivityResultHandlers.put(RC_SIGN_IN, this::onSignInResult);
        this.mapActivityResultHandlers.put(RC_SELECT_PLAYERS, this::onSelectPlayersResult);
        this.mapActivityResultHandlers.put(RC_INVITATION_INBOX, this::onInvitationInboxResult);
    }

    private void startSignIn() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        activity.startActivityForResult(signInClient.getSignInIntent(), RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        BiConsumer<Integer, Intent> handler = this.mapActivityResultHandlers.get(requestCode);
        if (handler == null) {
            throw new CelebrityException("Could not find activityResult handler for requestCode %d", requestCode);
        } else {
            handler.accept(resultCode, intent);
        }
    }

    private void onSignInResult(int resultCode, Intent intent) {
        Log.d(TAG, "onSignInResult: ");
        Task<GoogleSignInAccount> task =
                GoogleSignIn.getSignedInAccountFromIntent(intent);

        try {
            this.signInAccount = task.getResult(ApiException.class);
            onConnected();
        } catch (Exception e) {
            throw new CelebrityException(e, "Error retrieving sign in account");
        }
    }

    private void onConnected() {
        this.rtmpClient = Games.getRealTimeMultiplayerClient(activity, this.signInAccount);
        this.invitationsClient = Games.getInvitationsClient(activity, this.signInAccount);
        this.playersClient = Games.getPlayersClient(activity, this.signInAccount);

        this.invitationsClient.registerInvitationCallback(this.invitationCallback);

        this.playersClient.getCurrentPlayer()
                .addOnSuccessListener(new OnSuccessListener<Player>() {
                    @Override
                    public void onSuccess(Player player) {
                        myPlayerId = player.getPlayerId();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "getCurrentPlayer onFailure: ");
                    }
                });


        Games.getGamesClient(activity, signInAccount)
                .getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                Log.d(TAG, "onConnected: connection hint has a room invite!");
                                //acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "getActivationHint onFailure: ", e);
                    }
                });
    }

    private void onSelectPlayersResult(int resultCode, Intent intent) {
        Log.d(TAG, "onSelectPlayersResult: start");

        final ArrayList<String> invitees = intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        RoomConfig roomConfig = RoomConfig.builder(roomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(rtMessageReceivedListener)
                .setRoomStatusUpdateCallback(roomStatusUpdateCallback)
                /*.setAutoMatchCriteria(null)*/.build();
        rtmpClient.create(roomConfig);
        Log.d(TAG, "Room created, waiting for it to be ready...");

        onSelectPlayersClosedCallback.run();
    }

    private void onInvitationInboxResult(int resultCode, Intent intent) {
        Invitation invitation = intent.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
        if (invitation != null) {
            RoomConfig roomConfig = RoomConfig.builder(roomUpdateCallback)
                    .setInvitationIdToAccept(invitation.getInvitationId())
                    .setOnMessageReceivedListener(rtMessageReceivedListener)
                    .setRoomStatusUpdateCallback(roomStatusUpdateCallback)
                    .build();

            rtmpClient.join(roomConfig)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Room Joined Successfully!");
                        }
                    });
        }
    }

    public void openSelectPlayers() {
        this.rtmpClient.getSelectOpponentsIntent(MIN_TOTAL_PLAYERS - 1, MAX_TOTAL_PLAYERS - 1, false)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        activity.startActivityForResult(intent, RC_SELECT_PLAYERS);
                    }
                });
    }

    public void waitForInvitation() {
        this.invitationsClient.registerInvitationCallback(invitationCallback);

        this.invitationsClient.getInvitationInboxIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        activity.startActivityForResult(intent, RC_INVITATION_INBOX);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "waitForInvitation failure: ", e);

            }
        });
    }

    public void setOnSelectPlayersClosed(Runnable callback) {
        this.onSelectPlayersClosedCallback = callback;
    }

    public void setOnGamePlayersSelected(Consumer<List<PlayerInfo>> callback) {
        this.onGamePlayersSelectedCallback = callback;
    }

    public void sendMessage(int messageCode, Serializable payload, String participantId) {
        RTMPMessage message = new RTMPMessage(messageCode, payload);
        try {
            byte[] bytes = message.toBytes();
            sendReliableMessage(bytes, participantId);
        } catch (Exception e) {
            throw new CelebrityException(e, "Error sending message");
        }
    }

    private void sendReliableMessage(byte[] bytes, String participantId) {
        if (participantId == myParticipantId) {
            try {
                processMessageBytes(bytes, participantId);
            } catch (Exception e) {
                throw new CelebrityException(e, "Error processing own message");
            }
        } else {
            rtmpClient.sendReliableMessage(bytes,
                    roomId, participantId, (statusCode, tokenId, recipientParticipantId) -> {
                        Log.d(TAG, "RealTime message sent");
                        Log.d(TAG, "  statusCode: " + statusCode);
                        Log.d(TAG, "  tokenId: " + tokenId);
                        Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                    }).addOnSuccessListener((tokenId) -> Log.d(TAG, "Created a reliable message with tokenId: " + tokenId));
        }
    }

    public void broadcastMessage(int messageCode, Serializable payload) {
        RTMPMessage message = new RTMPMessage(messageCode, payload);
        try {
            byte[] bytes = message.toBytes();

            participants.stream()
                    .filter(p -> p.getStatus() == Participant.STATUS_JOINED)
                    .forEach(p -> sendReliableMessage(bytes, p.getParticipantId()));
        } catch (Exception e) {
            throw new CelebrityException(e, "Error sending message");
        }
    }

    private PlayerInfo participantToCelebrityPlayerInfo(Participant participant) {
        PlayerInfo player = new PlayerInfo();
        player.setId(participant.getParticipantId());
        player.setDisplayName(participant.getDisplayName());
        return player;
    }

    public String getMyParticipantId() {
        return myParticipantId;
    }

    public void setMessageHandler(RTMPMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
}
