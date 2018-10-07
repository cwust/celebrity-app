package br.com.cwust.celebrity.dto;

import java.io.Serializable;
import java.util.Arrays;

public class GameSetupDTO implements Serializable {
    private static final long serialVersionUID = 361397605703759504L;

    private PlayerDTO[] unassignedPlayers;
    private TeamDTO[] teams;

    public PlayerDTO[] getUnassignedPlayers() {
        return unassignedPlayers;
    }

    public void setUnassignedPlayers(PlayerDTO[] unassignedPlayers) {
        this.unassignedPlayers = unassignedPlayers;
    }

    public TeamDTO[] getTeams() {
        return teams;
    }

    public void setTeams(TeamDTO[] teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        return "GameSetupDTO{" +
                "unassignedPlayers=" + Arrays.toString(unassignedPlayers) +
                ", teams=" + Arrays.toString(teams) +
                '}';
    }
}
