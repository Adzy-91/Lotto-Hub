package ie.adampurser.lottohub;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class Result implements Serializable {
    private static final String LOG_TAG_DEBUG = "Draw.java";

    private static final String JSON_DRAW_TITLE = "title";
    private static final String JSON_DATE = "date";
    private static final String JSON_JACKPOT = "jackpot";
    private static final String JSON_WINNING_NUMS = "winningNums";
    private static final String JSON_BONUS_NUMS = "bonusNums";
    private static final String JSON_WINNING_INFO_MATCH = "match";
    private static final String JSON_WINNING_INFO_WINNERS = "winners";
    private static final String JSON_WINNING_INFO_PRIZE = "prize";

    private String mDrawType;
    private Date mDate;
    private String mJackpot;
    private String[] mWinningNums;
    private String[] mBonusNums;
    private String[] mWinners;
    private String[] mPrizes;
    private String[] mMatches;

    public Result(){}

    public Result(String drawType) {
        mDrawType = drawType;
    }

    public Result(Result result) {
        mDrawType = result.getDrawType();
        mDate = result.getDate();
        mJackpot = result.getJackpot();
        mWinningNums = result.getWinningNums();
        mBonusNums = result.getBonusNums();
        mWinners = result.getWinners();
        mPrizes = result.getPrizes();
        mMatches = result.getMatches();
    }

    public Result(JSONObject json) throws JSONException {
        mDrawType = json.getString(JSON_DRAW_TITLE);
        Long time = json.getLong(JSON_DATE);
        mDate = new Date(time);
        mJackpot = json.getString(JSON_JACKPOT);

        mWinningNums = ResultHelper.getStringArray(json.getJSONArray(JSON_WINNING_NUMS));
        if(!mDrawType.equals(DrawType.EURO_MILLIONS_PLUS)) {
            mBonusNums = ResultHelper.getStringArray(json.getJSONArray(JSON_BONUS_NUMS));
        }
        mMatches = ResultHelper.getStringArray(json.getJSONArray(JSON_WINNING_INFO_MATCH));
        mWinners = ResultHelper.getStringArray(json.getJSONArray(JSON_WINNING_INFO_WINNERS));
        mPrizes = ResultHelper.getStringArray(json.getJSONArray(JSON_WINNING_INFO_PRIZE));
    }


    public JSONObject toJSON() throws JSONException{
        JSONObject json = new JSONObject();

        json.put(JSON_DRAW_TITLE, mDrawType);
        json.put(JSON_DATE, mDate.getTime());
        json.put(JSON_JACKPOT, mJackpot);
        json.put(JSON_WINNING_NUMS, ResultHelper.getJSONArray(mWinningNums));
        if(mBonusNums != null) {
            json.put(JSON_BONUS_NUMS, ResultHelper.getJSONArray(mBonusNums));
        }
        json.put(JSON_WINNING_INFO_MATCH, ResultHelper.getJSONArray(mMatches));
        json.put(JSON_WINNING_INFO_WINNERS, ResultHelper.getJSONArray(mWinners));
        json.put(JSON_WINNING_INFO_PRIZE, ResultHelper.getJSONArray(mPrizes));

        return json;
    }

    public Date getNextDrawDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = 0;

        switch (mDrawType) {
            case DrawType.LOTTO:
                if(currentDay == 4) {
                    daysToAdd = 3;
                }
                else {
                    daysToAdd = 4;
                }
                break;
            case DrawType.DAILY_MILLION:
                daysToAdd = 1;
                break;
            case DrawType.EURO_MILLIONS:
                if(currentDay == 3) {
                    daysToAdd = 3;
                }
                else {
                    daysToAdd = 4;
                }
                break;
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

        return calendar.getTime();
    }

    public static Date getNextDrawDate(Date date, String drawType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = 0;

        switch (drawType) {
            case DrawType.LOTTO:
            case DrawType.LOTTO_PLUS_1:
            case DrawType.LOTTO_PLUS_2:
            case DrawType.LOTTO_PLUS_RAFFLE:
                if(currentDay == 4) {
                    daysToAdd = 3;
                }
                else {
                    daysToAdd = 4;
                }
                break;
            case DrawType.DAILY_MILLION:
            case DrawType.DAILY_MILLION_PLUS:
                daysToAdd = 1;
                break;
            case DrawType.EURO_MILLIONS:
            case DrawType.EURO_MILLIONS_PLUS:
                if(currentDay == 3) {
                    daysToAdd = 3;
                }
                else {
                    daysToAdd = 4;
                }
                break;
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

        return calendar.getTime();
    }

    public static Date getPreviousDrawDate(String drawType, Date currentDrawDate) {
        // set calendar to current draw date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDrawDate);

        // get the day draw took place on i.e. sun = 1, mon = 2, etc.
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        // days we need to take away to get previous draw day
        int daysToSubtract = 0;

        switch (drawType) {
            case DrawType.LOTTO:
            case DrawType.LOTTO_PLUS_1:
            case DrawType.LOTTO_PLUS_2:
            case DrawType.LOTTO_PLUS_RAFFLE:
                if(currentDay == 4) {
                    daysToSubtract = -4;
                }
                else {
                    daysToSubtract = -3;
                }
                break;
            case DrawType.DAILY_MILLION:
            case DrawType.DAILY_MILLION_PLUS:
                // if current draw time @2pm go to previous day
                if(calendar.get(Calendar.HOUR_OF_DAY) == 14) {
                    calendar.set(Calendar.HOUR_OF_DAY, 21);
                    daysToSubtract = -1;
                }
                // current time @9pm therefore return same date @2pm
                else {
                    calendar.set(Calendar.HOUR_OF_DAY, 14);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime();
                }
                break;
            case DrawType.EURO_MILLIONS:
            case DrawType.EURO_MILLIONS_PLUS:
                if(currentDay == 3) {
                    daysToSubtract = -4;
                }
                else {
                    daysToSubtract = -3;
                }
                break;
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToSubtract);

        return calendar.getTime();
    }

    public Date getPreviousDrawDate() {

        // set calendar to current draw date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);

        // get the day draw took place on i.e. sun = 1, mon = 2, etc.
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        // days we need to take away to get previous draw day
        int daysToSubtract = 0;

        switch (mDrawType) {
            case DrawType.LOTTO:
                if(currentDay == 4) {
                    daysToSubtract = -4;
                }
                else {
                    daysToSubtract = -3;
                }
                break;
            case DrawType.DAILY_MILLION:
                // if current draw time @2pm go to previous day
                if(calendar.get(Calendar.HOUR_OF_DAY) == 14) {
                    calendar.set(Calendar.HOUR_OF_DAY, 21);
                    daysToSubtract = -1;
                }
                // current time @9pm therefore return same date @2pm
                else {
                    calendar.set(Calendar.HOUR_OF_DAY, 14);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime();
                }
                break;
            case DrawType.EURO_MILLIONS:
                if(currentDay == 3) {
                    daysToSubtract = -4;
                }
                else {
                    daysToSubtract = -3;
                }
                break;
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToSubtract);

        return calendar.getTime();
    }

    public static boolean isDrawDay(String drawTitle, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        switch (drawTitle) {
            case DrawType.LOTTO:
                return day == 4 || day == 7;
            case DrawType.DAILY_MILLION:
                return true;
            case DrawType.EURO_MILLIONS:
                return day == 3 || day == 6;
            default:
                return false;
        }
    }

    public String getDrawType() {
        return mDrawType;
    }

    public Date getDate() {
        return mDate;
    }

    public String[] getMatches() {
        return mMatches;
    }

    public String[] getWinners() {return mWinners;}

    public String[] getPrizes() {
        return mPrizes;
    }

    public String[] getWinningNums() {
        return mWinningNums;
    }

    public String[] getBonusNums() {
        return mBonusNums;
    }

    public String getBonusNumber(int index) {
        if(index > mWinningNums.length - 1)
            return null;

        return mBonusNums[index];
    }

    public String getJackpot() {
       return mJackpot;
    }

    public String getFormatJackpot() {
        DecimalFormat df = new DecimalFormat("0 Million");

        return df.format(Integer.valueOf(mJackpot) / 1000000);
    }

    public void setDrawType(String drawType) {
        mDrawType = drawType;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void setJackpot(String jackpot) {
        mJackpot = jackpot;
    }

    public void setWinningNums(String[] winningNums) {
        mWinningNums = winningNums;
    }

    public void setBonusNums(String[] bonusNums) {
        mBonusNums = bonusNums;
    }

    public void setMatches(String[] matches) {
        mMatches = matches;
    }

    public void setWinners(String[] winners) {
        mWinners = winners;
    }

    public void setPrizes(String[] prizes) {
        mPrizes = prizes;
    }

    public static Date getLatestDrawDate(String drawType) {
        try {
            Elements elements = ResultDownloader.getLatestResult(drawType, 1);
            ResultParser parser = new ResultParser();
            return parser.getResult(elements).getDate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

