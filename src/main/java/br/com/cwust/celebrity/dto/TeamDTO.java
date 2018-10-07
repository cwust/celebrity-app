package br.com.cwust.celebrity.dto;

import java.io.Serializable;
import java.util.Arrays;

public class TeamDTO  implements Serializable {
    private static final long serialVersionUID = -7204327900021108031L;

    private int teamNumber;
    private PlayerDTO[] players;

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public PlayerDTO[] getPlayers() {
        return players;
    }

    public void setPlayers(PlayerDTO[] players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "TeamDTO{" +
                "teamNumber=" + teamNumber +
                ", players=" + Arrays.toString(players) +
                '}';
    }
}
