package ie.adampurser.lottohub;

import java.util.ArrayList;
import java.util.Date;

public class Prize {
    private String[] mWinningNums;
    private String[] mBonusNums;
    private ArrayList<String> mMatchingNums;
    private ArrayList<String> mMatchingBonusNums;
    private String mLineLetter;
    private String mPrize;
    private String mDrawTitle;
    private Date mDate;

    public Prize(){}

    public Prize(Result result,
                 ArrayList<String> matchingWinningNums,
                 ArrayList<String> matchingBonusNums,
                 String lineLetter,
                 String prize) {

        mWinningNums = result.getWinningNums();
        mBonusNums = result.getBonusNums();
        mMatchingNums = matchingWinningNums;
        mMatchingBonusNums = matchingBonusNums;
        mLineLetter = lineLetter;
        mPrize = prize;
        mDrawTitle = result.getDrawType();
        mDate = result.getDate();
    }

    public String[] getWinningNums() {
        return mWinningNums;
    }

    public String[] getBonusNums() {
        return mBonusNums;
    }

    public String getDrawTitle() {
        return mDrawTitle;
    }

    public String getLineLetter() {
        return mLineLetter;
    }

    public void setPrize(String prize) {
        mPrize = prize;
    }

    public Date getDate() {
        return mDate;
    }

    public ArrayList<String> getMatchingNums() {
        return mMatchingNums;
    }

    public ArrayList<String> getMatchingBonusNums() {
        return mMatchingBonusNums;
    }

    public String getPrize() {
        return mPrize;
    }
}
