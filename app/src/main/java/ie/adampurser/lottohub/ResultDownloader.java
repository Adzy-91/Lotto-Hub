package ie.adampurser.lottohub;


import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

/*
 * A class to download an xml document containing the results for a draw.
 * This document will then be passed a ResultParser object to extract the results.
 */

public class ResultDownloader {

    public static final String ALL_DRAWS = "All";
    private static final String LOTTO = "Lotto";
    private static final String LOTTO_PLUS_1 = "LottoPlus1";
    private static final String LOTTO_PLUS_2 = "LottoPlus2";
    private static final String LOTTO_PLUS_RAFFLE = "LottoPlus_Raffle";
    private static final String DAILY_MILLION = "DailyMillion";
    private static final String DAILY_MILLION_PLUS = "DailyMillionPlus";
    private static final String EURO_MILLIONS = "EuroMillions";
    private static final String EURO_MILLIONS_PLUS = "EuroMillionsPlus";
    private static final String URL = "http://resultsservice.lottery.ie/resultsservice.asmx/";
    private static final String URL_LATEST_RESULT =  URL + "GetResults?";
    private static final String URL_RESULT_FOR_DATE = URL + "GetResultsForDate?";
    private static final String USER_AGENT = "Chrome";
    private static final String DATE_FORMAT_TIMEZONE = "%2B00:00";
    private static final String SELECTOR_DRAW_RESULT = "DrawResult";


    public static Elements getLatestResult(String drawType, int numOfDraws) throws IOException {

        if(!drawType.equals(ALL_DRAWS)) {
            drawType = getLottoAPIDrawType(drawType);
        }

        String url = URL_LATEST_RESULT + "drawType=" + drawType + "&lastNumberOfDraws=" + numOfDraws;

        return Jsoup.connect(url)
                .followRedirects(true)
                .timeout(5000)
                .userAgent(USER_AGENT)
                .get()
                .getAllElements();
    }

    public static Elements getResultForDate(String drawType, Date ticketDate,
                                            Date latestDrawDate) throws IOException {

        int numOfPreviousDraws = getNumOfPreviousDraws(drawType, ticketDate, latestDrawDate);

       return getLatestResult(drawType, numOfPreviousDraws).select(SELECTOR_DRAW_RESULT)
                .get(numOfPreviousDraws - 1).getAllElements();
    }

    private static int getNumOfPreviousDraws(String drawType, Date ticketDrawDate, Date latestDrawDate) {
        int previousDrawCount = 0;

        while(latestDrawDate.compareTo(ticketDrawDate) >= 0) {
            latestDrawDate = Result.getPreviousDrawDate(drawType, latestDrawDate);
            previousDrawCount++;
        }

        return previousDrawCount;
    }

    // return the string to be used to download a draw from the lotto API
    private static String getLottoAPIDrawType(String drawType) {
        switch(drawType) {
            case DrawType.LOTTO:
                return LOTTO;
            case DrawType.LOTTO_PLUS_1:
                return LOTTO_PLUS_1;
            case DrawType.LOTTO_PLUS_2:
                return LOTTO_PLUS_2;
            case DrawType.LOTTO_PLUS_RAFFLE:
                return LOTTO_PLUS_RAFFLE;
            case DrawType.DAILY_MILLION:
                return DAILY_MILLION;
            case DrawType.DAILY_MILLION_PLUS:
                return DAILY_MILLION_PLUS;
            case DrawType.EURO_MILLIONS:
                return EURO_MILLIONS;
            case DrawType.EURO_MILLIONS_PLUS:
                return EURO_MILLIONS_PLUS;
        }

        return null;
    }
}
