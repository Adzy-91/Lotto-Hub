package ie.adampurser.lottohub;


import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static ie.adampurser.lottohub.TicketJSONSerializer.LOG_TAG_DEBUG;

public class ResultParser {

    // a list of the selector statements to extract the winning info from the document
    public static final String SELECTOR_RESULTS = "DrawResult";
    private static final String SELECTOR_DRAW_TYPE = "DrawName";
    private static final String SELECTOR_MATCH = "Match";
    private static final String SELECTOR_WINNERS = "Winners";
    private static final String SELECTOR_PRIZE = "Prize";
    private static final String SELECTOR_WINNING_NUMS = "DrawNumber:has(Type:contains(Standard)) Number";
    private static final String SELECTOR_BONUS_NUMS_EURO = "DrawNumber:has(Type:contains(LuckyStar)) Number";
    private static final String SELECTOR_BONUS_NUMS = "DrawNumber:has(Type:contains(Bonus)) Number";
    private static final String SELECTOR_JACKPOT = "TopPrize";
    private static final String SELECTOR_DATE = "DrawDate";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT_TIME = "Ka";
    private static final String EURO_MILLIONS_DOC_NAME = "EuroMillions";
    private static final String EURO_MILLIONS_PLUS_DOC_NAME = "Plus";

    // root elements to be parsed
    private Elements mElements;


    public ResultParser() {}

    /* return all results in the form of a hashmap, with the key being the draw type */
    public HashMap<String, Result> getResultsMap(Elements elements) {

        HashMap<String, Result> allResults = new HashMap<>();

        // get the separate results
        Elements separateResults = elements.select(SELECTOR_RESULTS);

        // insert all results into hashmap
        Result result;
        for(Element element: separateResults) {

            result = new Result(getResult(element.getAllElements()));

            allResults.put(result.getDrawType(), result);
        }

        return allResults;
    }


    public Result getResult(Elements elements) {

        mElements = elements;

        Result result = new Result();

        result.setDrawType(getDrawType());
        result.setMatches(getMatches());
        result.setWinners(getWinners());
        result.setPrizes(getPrizes());
        result.setWinningNums(getWinningNums());
        result.setBonusNums(getBonusNums(result.getDrawType()));
        result.setDate(getDate());
        result.setJackpot(getJackpot());

        return result;
    }

    private String getDrawType() {
        String drawType = mElements.select(SELECTOR_DRAW_TYPE).text();

        // assign euro millions its correct name
        switch (drawType) {
            case EURO_MILLIONS_DOC_NAME:
                drawType = DrawType.EURO_MILLIONS;
                break;
            case EURO_MILLIONS_PLUS_DOC_NAME:
                drawType = DrawType.EURO_MILLIONS_PLUS;
                break;
        }

        return drawType;
    }

    private String[] getMatches() {
        return elementsToStringArray(mElements.select(SELECTOR_MATCH));
    }

    private String[] getWinners() {
        return elementsToStringArray(mElements.select(SELECTOR_WINNERS));
    }

    private String[] getPrizes() {

        String [] prizes = elementsToStringArray(mElements.select(SELECTOR_PRIZE));

        // format prizes with commas
        for(int i = 0; i < prizes.length; i++) {
            prizes[i] = NumberFormat.getInstance(Locale.ENGLISH).format(Integer.valueOf(prizes[i]));
        }

        return prizes;
    }

    private String[] getWinningNums() {
        return elementsToStringArray(mElements.select(SELECTOR_WINNING_NUMS));
    }

    /* we need to specify the draw type here to accommodate euro millions lucky stars */
    private String[] getBonusNums(String drawType) {
        if(drawType.equals(DrawType.EURO_MILLIONS)) {
            return elementsToStringArray(mElements.select(SELECTOR_BONUS_NUMS_EURO));
        } else {
            return elementsToStringArray(mElements.select(SELECTOR_BONUS_NUMS));
        }
    }

    private String getJackpot() {
        return mElements.select(SELECTOR_JACKPOT).text();
    }

    private Date getDate() {
        String date = mElements.select(SELECTOR_DATE).text();

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        try {
            return format.parse(date);
        }catch(Exception e) {
            Log.e(LOG_TAG_DEBUG, e.getMessage());
        }

        return null;
    }

    private String[] elementsToStringArray(Elements elements) {
        String [] elementValues = new String[elements.size()];

        for(int i = 0; i < elementValues.length; i++) {
            elementValues[i] = elements.get(i).text();
        }

        return elementValues;
    }
}
