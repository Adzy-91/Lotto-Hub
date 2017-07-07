package ie.adampurser.lottohub;

import java.util.ArrayList;
import java.util.LinkedList;

public class EuroMillionsChecker extends Checker {

    public EuroMillionsChecker() {}

    @Override
    public ArrayList<String> getWinningNumMatchesForSingleDraw(TicketLine line, Result result) {
        LinkedList<String> ticketWinningNums = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            ticketWinningNums.add(line.getNum(i));
        }
        String[] drawWinningNums = result.getWinningNums();

        ArrayList<String> matches = new ArrayList<>();
        for(int i = 0; i < ticketWinningNums.size(); i++) {
            if(ticketWinningNums.contains(drawWinningNums[i])) {
                matches.add(drawWinningNums[i]);
            }
        }

        return matches;
    }

    @Override
    public ArrayList<String> getBonusNumMatchesForSingleDraw(TicketLine line, Result result) {
        if(result.getDrawType().equals(DrawType.EURO_MILLIONS_PLUS)) {
            return new ArrayList<>();
        }
        LinkedList<String> ticketBonusNums = new LinkedList<>();
        ticketBonusNums.add(line.getNum(6));
        ticketBonusNums.add(line.getNum(5));
        String[] drawBonusNums = result.getBonusNums();

        ArrayList<String> matches = new ArrayList<>(2);
        for(int i = 0; i < ticketBonusNums.size(); i++) {
            if(ticketBonusNums.contains(drawBonusNums[i])) {
                matches.add(drawBonusNums[i]);
            }
        }

        return matches;
    }

    @Override
    public Prize getPrizeForSingleDraw(TicketLine line, ArrayList<String> winningNumMatches,
                                       ArrayList<String> bonusNumMatches, Result result) {
        String[] prizes = result.getPrizes();
        String prize = null;

        // Plus
        if(result.getDrawType().equals(DrawType.EURO_MILLIONS_PLUS)) {
            switch (winningNumMatches.size()) {
                case 5:
                    prize = prizes[0];
                    break;
                case 4:
                    prize = prizes[1];
                    break;
                case 3:
                    prize = prizes[2];
            }
        }

        // Two stars
        else if(bonusNumMatches.size() == 2) {
            switch (winningNumMatches.size()) {
                case 5:
                    prize = prizes[0];
                    break;
                case 4:
                    prize = prizes[3];
                    break;
                case 3:
                    prize = prizes[6];
                    break;
                case 2:
                    prize = prizes[7];
                    break;
                case 1:
                    prize = prizes[10];
                    break;
            }
        }
        // One star
        else if(bonusNumMatches.size() == 1) {
            switch (winningNumMatches.size()) {
                case 5:
                    prize = prizes[1];
                    break;
                case 4:
                    prize = prizes[4];
                    break;
                case 3:
                    prize = prizes[8];
                    break;
                case 2:
                    prize = prizes[11];
                    break;
            }
        }
        // No stars
        else {
            switch (winningNumMatches.size()) {
                case 5:
                    prize = prizes[2];
                    break;
                case 4:
                    prize = prizes[5];
                    break;
                case 3:
                    prize = prizes[9];
                    break;
                case 2:
                    prize = prizes[12];
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
        if(drawTitle.equals(DrawType.EURO_MILLIONS_PLUS)) {
            return winningNumMatches >= 3;
        }
        else {
            return ((winningNumMatches >= 1 && bonusNumMatches == 2) || winningNumMatches >= 2);
        }
    }
}
