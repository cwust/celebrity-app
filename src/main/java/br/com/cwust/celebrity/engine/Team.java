package br.com.cwust.celebrity.engine;

import java.util.LinkedHashSet;
import java.util.Set;

import br.com.cwust.celebrity.dto.PlayerDTO;
import br.com.cwust.celebrity.dto.TeamDTO;

public class Team {
    private int teamNumber;
    private Set<Player> players;

    public Team(int teamNumber) {
        this.teamNumber = teamNumber;
        this.players = new LinkedHashSet<>(); //to keep order
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        boolean removed = this.getPlayers().remove(player);
        if (!removed) {
            throw new CelebrityEngineException("Could not remove player %s from team %d", player.getPlayerId(),
                    this.getTeamNumber());
        }
    }

    public void addPlayer(Player player) {
        boolean added = this.getPlayers().add(player);
        if (!added) {
            throw new CelebrityEngineException("Player %s was already added to team %d", player.getPlayerId(),
                    this.getTeamNumber());
        }
    }

    public TeamDTO convertToTeamDTO() {
        TeamDTO teamInfo = new TeamDTO();
        teamInfo.setTeamNumber(this.teamNumber);

        teamInfo.setPlayers(this.getPlayers().stream()
                .map(Player::convertToPlayerDTO)
                .toArray(PlayerDTO[]::new));

        return teamInfo;
    }}
