package br.com.cwust.celebrity.engine;

import br.com.cwust.celebrity.dto.PlayerDTO;
import br.com.cwust.rtmp.PlayerInfo;

public class Player {
    private String playerId;
    private String name;
    private Team team;
    private boolean ready;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public static Player fromPlayerInfo(PlayerInfo playerInfo) {
        Player player = new Player();

        player.setPlayerId(playerInfo.getId());
        player.setName(playerInfo.getDisplayName());

        return player;
    }

    public PlayerDTO convertToPlayerDTO() {
        PlayerDTO playerDTO = new PlayerDTO();

        playerDTO.setPlayerId(this.playerId);
        playerDTO.setName(this.name);
        playerDTO.setReady(this.ready);

        return playerDTO;
    }

}
