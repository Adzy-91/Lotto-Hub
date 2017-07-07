package ie.adampurser.lottohub;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class EuroMillionsCheckerTest {

    /*
     * information contained within this test result file
     * winning numbers: 31 36 38 47 49
     * bonus numbers: 08 11
     */
    private static final String MOCK_RESULT_FILE = "MockEuroMillionsResult.json";

    // mock ticket files
    private static final String TICKET_MATCH_5_PLUS_2 = "MockEMTicket_5+2.json";
    private static final String TICKET_MATCH_5_PLUS_1 = "MockEMTicket_5+1.json";
    private static final String TICKET_MATCH_5 = "MockEMTicket_5.json";
    private static final String TICKET_MATCH_4_PLUS_2 = "MockEMTicket_4+2.json";
    private static final String TICKET_MATCH_4_PLUS_1 = "MockEMTicket_4+1.json";
    private static final String TICKET_MATCH_4 = "MockEMTicket_4.json";
    private static final String TICKET_MATCH_3_PLUS_2 = "MockEMTicket_3+2.json";
    private static final String TICKET_MATCH_3_PLUS_1 = "MockEMTicket_3+1.json";
    private static final String TICKET_MATCH_3 = "MockEMTicket_3.json";
    private static final String TICKET_MATCH_2_PLUS_2 = "MockEMTicket_2+2.json";
    private static final String TICKET_MATCH_2_PLUS_1 = "MockEMTicket_2+1.json";
    private static final String TICKET_MATCH_2 = "MockEMTicket_2.json";
    private static final String TICKET_MATCH_1_PLUS_2 = "MockEMTicket_1+2.json";
    private static final String TICKET_MATCH_1_PLUS_1 = "MockEMTicket_1+1.json";
    private static final String TICKET_MATCH_1 = "MockEMTicket_1.json";
    private static final String TICKET_MATCH_0 = "MockEMTicket_0.json";


    private Ticket mMockTicket;

    private EuroMillionsChecker mChecker;

    private Result mMockEuroMillionsResult;


    @Before
    public void initMocks() {
        try {
            mMockEuroMillionsResult = getMockResult(MOCK_RESULT_FILE);
        }catch (Exception e) {
            e.printStackTrace();
        }
        mChecker = new EuroMillionsChecker();
    }

    /**
     * load up a test result and return it as a draw object
     */
    private Result getMockResult(String fileName) throws IOException, JSONException {
        InputStream in;
        BufferedReader reader;
        StringBuilder jsonString;
        String line;
        in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        reader = new BufferedReader(new InputStreamReader(in));
        jsonString = new StringBuilder();
        while((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        return new Result(new JSONObject(jsonString.toString()));
    }

    /**
     * load up a test ticket and return it as a ticket object
     */
    private Ticket getMockTicket(String fileName) {
        InputStream in;
        BufferedReader reader;
        StringBuilder jsonString;
        String line;
        try {
            in = this.getClass().getClassLoader().getResourceAsStream(fileName);
            reader = new BufferedReader(new InputStreamReader(in));
            jsonString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return new Ticket(new JSONObject(jsonString.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isMatch(int winningNumMatches, int bonusNumMatches) {
        int winningNumMatches0 = mChecker.getWinningNumMatchesForSingleDraw(
                mMockTicket.getLine(0),
                mMockEuroMillionsResult).size();
        int bonusNumMatches0 = mChecker.getBonusNumMatchesForSingleDraw(
                mMockTicket.getLine(0),
                mMockEuroMillionsResult).size();

        return winningNumMatches0 == winningNumMatches && bonusNumMatches0 == bonusNumMatches;
    }

    @Test
    public void checkTicket_match5Plus2_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_5_PLUS_2);
        Assert.assertTrue(isMatch(5,2) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 5,2));
    }

    @Test
    public void checkTicket_match5Plus1_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_5_PLUS_1);
        Assert.assertTrue(isMatch(5,1)&& mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 5,1));
    }

    @Test
    public void checkTicket_match5_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_5);
        Assert.assertTrue(isMatch(5,0) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 5,0));
    }

    @Test
    public void checkTicket_match4Plus2_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_4_PLUS_2);
        Assert.assertTrue(isMatch(4,2) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 4,2));
    }

    @Test
    public void checkTicket_match4Plus1_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_4_PLUS_1);
        Assert.assertTrue(isMatch(4,1) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 4,1));
    }

    @Test
    public void checkTicket_match4_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_4);
        Assert.assertTrue(isMatch(4,0) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 4,0));
    }

    @Test
    public void checkTicket_match3Plus2_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_3_PLUS_2);
        Assert.assertTrue(isMatch(3,2) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 3,2));
    }

    @Test
    public void checkTicket_match3Plus1_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_3_PLUS_1);
        Assert.assertTrue(isMatch(3,1) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 3,1));
    }

    @Test
    public void checkTicket_match3_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_3);
        Assert.assertTrue(isMatch(3,0) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 3,0));
    }

    @Test
    public void checkTicket_match2Plus2_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_2_PLUS_2);
        Assert.assertTrue(isMatch(2,2) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 2,2));
    }

    @Test
    public void checkTicket_match2Plus1_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_2_PLUS_1);
        Assert.assertTrue(isMatch(2,1) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 2,1));
    }

    @Test
    public void checkTicket_match2_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_2);
        Assert.assertTrue(isMatch(2,0) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 2,0));
    }

    @Test
    public void checkTicket_match1Plus2_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_1_PLUS_2);
        Assert.assertTrue(isMatch(1,2) && mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 1,2));
    }

    @Test
    public void checkTicket_match1Plus1_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_1_PLUS_1);
        Assert.assertTrue(isMatch(1,1) && !mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 1,1));
    }

    @Test
    public void checkTicket_match1_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_1);
        Assert.assertTrue(isMatch(1,0) && !mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 1,0));
    }

    @Test
    public void checkTicket_match0_ReturnsTrue() {
        mMockTicket = getMockTicket(TICKET_MATCH_0);
        Assert.assertTrue(isMatch(0,0) && !mChecker.lineIsWinner(DrawType.EURO_MILLIONS, 0,0));
    }
}
