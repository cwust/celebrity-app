package br.com.cwust.celebrity.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.cwust.celebrity.CelebrityMessageHandler;
import br.com.cwust.celebrity.dto.GameSetupDTO;
import br.com.cwust.celebrity.dto.PlayerDTO;
import br.com.cwust.celebrity.dto.TeamDTO;
import br.com.cwust.rtmp.PlayerInfo;

public class CelebrityGameState {
    private CelebrityMessageHandler messageHandler;
    private Map<String, Player> playersMap;
    private List<Player> unassignedPlayers;
    private List<Team> teams;

    public CelebrityGameState(CelebrityMessageHandler messageHandler, List<PlayerInfo> playerInfos) {
        this.messageHandler = messageHandler;
        this.playersMap = playerInfos.stream()
                .map(Player::fromPlayerInfo)
                .collect(Collectors.toMap(
                        Player::getPlayerId,
                        Function.identity()));

        this.unassignedPlayers = new ArrayList<>(this.playersMap.values());
        this.teams = new ArrayList<>();
    }

    public Team createTeam() {
        Team team = new Team(this.teams.size() + 1);
        this.teams.add(team);
        return team;
    }

    public void chooseTeam(String playerId, int teamNumber) {
        Player player = this.getPlayer(playerId);

        if (player.getTeam() == null) {
            boolean removed = this.unassignedPlayers.remove(player);
            if (!removed) {
                throw new CelebrityEngineException("Could not choose team: player %s was not in unassignedPlayers", playerId);
            }
        } else {
            player.getTeam().removePlayer(player);
        }

        Team newTeam = this.teams.get(teamNumber - 1);
        newTeam.addPlayer(player);
        player.setTeam(newTeam);

        broadcastGameSetupUpdated();
    }


    private void broadcastGameSetupUpdated() {
        this.messageHandler.broadcastMsgGameSetupUpdated(this.getGameSetupInfo());
    }

    private Player getPlayer(String playerId) {
        Player player = this.playersMap.get(playerId);

        if (player == null) {
            throw new CelebrityEngineException("Could not find player %s", playerId);
        } else {
            return player;
        }
    }

    public void setPlayerReady(String playerId, Boolean ready) {
        Player player = this.getPlayer(playerId);
        player.setReady(ready);
        broadcastGameSetupUpdated();
    }

    public boolean isGameSetupComplete() {
        return false;
    }

    public GameSetupDTO getGameSetupInfo() {
        GameSetupDTO gameSetup = new GameSetupDTO();

        gameSetup.setUnassignedPlayers(this.unassignedPlayers.stream()
                .map(Player::convertToPlayerDTO)
                .toArray(PlayerDTO[]::new));

        gameSetup.setTeams(this.teams.stream()
                .map(Team::convertToTeamDTO)
                .toArray(TeamDTO[]::new));

        return gameSetup;

    }
}