package ddejonge.bandana.tournament;

import static ddejonge.bandana.tournament.Globals.COALLITION_NUM;

/**
* Created by idowe on 4/28/2018.
*/

public class AvgScCalculator extends ScoreCalculator{


    public AvgScCalculator() {
        super(true);
    }


    @Override
    public double calculateGameScore(GameResult newResult, String playerName) {
        double sum = 0;
        int coallitionNum = 0;
        if(playerName.contains("CoallitionBot")) {
            coallitionNum = Globals.COALLITION_NUM;
            for (int i = 0; i < coallitionNum; i++) {
                String prefixPlayerName = "CoallitionBot ";
                sum += newResult.getNumSupplyCenters("'"+prefixPlayerName + (i + 7 - Globals.COALLITION_NUM)+ "'");
            }
        }
        else {
            coallitionNum = 7 - Globals.COALLITION_NUM;
            for (int i = 0; i < coallitionNum; i++) {
                String prefixPlayerName = "NotNegotiator ";
                sum += newResult.getNumSupplyCenters("'"+prefixPlayerName + i+"'");
            }
        }
        return sum/coallitionNum;
    }

    @Override
    public double getTournamentScore(String playerName) {
        return  this.getAverageScore(playerName);
    }



    @Override
    public String getScoreSystemName() {
        return "Avg Sc For Bot Type";
    }

    @Override
    public String getScoreString(String playerName) {

        return "" + this.getTotalScore(playerName);

    }

}

