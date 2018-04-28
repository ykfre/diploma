package ddejonge.bandana.tournament;

import ddejonge.bandana.exampleAgents.MyBot;

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
        if(playerName.contains("MyBot")) {
            coallitionNum = MyBot.COALLITION_NUM;
            for (int i = 0; i < coallitionNum; i++) {
                String prefixPlayerName = "MyBot ";
                sum += newResult.getRank("'"+prefixPlayerName + (i + 7 - MyBot.COALLITION_NUM)+ "'");
            }
        }
        else {
            coallitionNum = 7 - MyBot.COALLITION_NUM;
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
        return "Avg Rank";
    }

    @Override
    public String getScoreString(String playerName) {

        long total = Math.round(this.getTotalScore(playerName));

        return "" + total;

    }

}

