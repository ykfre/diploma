package ddejonge.bandana.tournament;

/**
* Created by idowe on 4/28/2018.
*/

public class AvgRankCalculator extends ScoreCalculator{


    public AvgRankCalculator() {
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
                sum += newResult.getRank("'"+prefixPlayerName + (i + 7 - Globals.COALLITION_NUM)+ "'");
            }
        }
        else {
            coallitionNum = 7 - Globals.COALLITION_NUM;
            for (int i = 0; i < coallitionNum; i++) {
                String prefixPlayerName = "NotNegotiator ";
                sum += newResult.getRank("'"+prefixPlayerName + i+"'");
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
        return "Avg Rank For Bot Type";
    }

    @Override
    public String getScoreString(String playerName) {

        return "" + this.getTotalScore(playerName);

    }

}

