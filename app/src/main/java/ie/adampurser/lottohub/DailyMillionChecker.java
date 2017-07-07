package ie.adampurser.lottohub;

import java.util.ArrayList;

public class DailyMillionChecker extends Checker {

    public DailyMillionChecker() {}

    @Override
    public Prize getPrizeForSingleDraw(
            TicketLine line,
            ArrayList<String> winningNumMatches,
            ArrayList<String> bonusNumMatches,
            Result result) {

        String[] prizes = result.getPrizes();
        String prize = null;
        // With bonus
        if(bonusNumMatches.size() > 0) {
            switch (winningNumMatches.size()) {
                case 5:
                    prize = prizes[1];
                    break;
                case 4:
                    prize = prizes[3];
                    break;
                case 3:
                    prize = prizes[5];
                    break;
            }
        }
        // No bonus
        else {
            switch (winningNumMatches.size()) {
                case 6:
                    prize = prizes[0];
                    break;
                case 5:
                    prize = prizes[2];
                    break;
                case 4:
                    prize = prizes[4];
                    break;
                case 3:
                    prize = prizes[6];
                    break;
            }
        }

        if(prize != null) {
            return new Prize(result, winningNumMatches, bonusNumMatches, line.getLetter(), prize);
        }

        return null;
    }

    @Override
    public boolean lineIsWinner(String drawTitle, int winningNumMatches, int bonusNumMatches) {
        return winningNumMatches >= 3;
    }
}
