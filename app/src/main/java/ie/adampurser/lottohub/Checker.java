package ie.adampurser.lottohub;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Checker {

    public Checker() {}

    public ArrayList<Prize> getPrizes(TicketLine line, Result resultToCheck) {

        ArrayList<Prize> prizes = new ArrayList<>();
        Prize prize;
        ArrayList<String> winningNumMatches;
        ArrayList<String> bonusNumMatches;

        // raffle checker needs to be implemented. Skip for now
        if(resultToCheck.getDrawType().equals(DrawType.LOTTO_PLUS_RAFFLE)) {
            return prizes;
        }

        winningNumMatches = getWinningNumMatchesForSingleDraw(line, resultToCheck);
        bonusNumMatches = getBonusNumMatchesForSingleDraw(line, resultToCheck);
        if(lineIsWinner(resultToCheck.getDrawType(), winningNumMatches.size(), bonusNumMatches.size())) {
            prize = getPrizeForSingleDraw(
                        line,
                        getWinningNumMatchesForSingleDraw(line, resultToCheck),
                        getBonusNumMatchesForSingleDraw(line, resultToCheck),
                        resultToCheck
                    );

            if (prize != null) {
                prizes.add(prize);
            }
        }

        return prizes;
    }

    public ArrayList<String> getWinningNumMatchesForSingleDraw(TicketLine line, Result result) {
        String[] winningNums = result.getWinningNums();
        LinkedList<String> lineWinningNums = line.getNums();

        ArrayList<String> matches = new ArrayList<>();
        for(int i = 0; i < lineWinningNums.size(); i++) {
            if(lineWinningNums.contains(winningNums[i])) {
                matches.add(winningNums[i]);
            }
        }

        return matches;
    }

    public ArrayList<String> getBonusNumMatchesForSingleDraw(TicketLine line, Result result) {
        String[] drawBonusNum = result.getBonusNums();

        ArrayList<String> bonusMatches = new ArrayList<>(1);
        if(line.contains(drawBonusNum[0])) {
            bonusMatches.add(drawBonusNum[0]);
        }

        return bonusMatches;
    }

    public abstract Prize getPrizeForSingleDraw(
            TicketLine line,
            ArrayList<String> winningNumMatches,
            ArrayList<String> bonusNumMatches,
            Result result
    );

    public abstract boolean lineIsWinner(String drawTitle, int winningNumMatches, int bonusNumMatches);
}
