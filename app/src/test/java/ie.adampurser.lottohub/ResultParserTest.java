package ie.adampurser.lottohub;


import android.util.Log;

import junit.framework.Assert;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResultParserTest {

    private static final String LOG_ERROR = "ResultParserTest.error";
    private static final String EURO_MILLIONS_FILE = "mock_euro_millions_result_response";


    ResultParser mResultParser;
    Document mDocument;
    Result mResult;

    @Before
    public void init() {
        mResultParser = new ResultParser();
        try {
            mDocument = Jsoup.parse(getMockResponse(EURO_MILLIONS_FILE));
        } catch (Exception e) {
            Log.e(LOG_ERROR, e.getMessage());
        }
    }

    @Test
    public void ParseResultTest_ReturnsTrue() {
        Elements result = mDocument.getAllElements().select(ResultParser.SELECTOR_RESULTS);
        mResult = mResultParser.getResult(result);
        Assert.assertTrue(mResult != null);
    }


    private String getMockResponse(String fileName) throws IOException, JSONException {
        InputStream in;
        BufferedReader reader;
        StringBuilder responseBuilder;
        String line;
        in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        reader = new BufferedReader(new InputStreamReader(in));
        responseBuilder = new StringBuilder();
        while((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        return responseBuilder.toString();
    }
}
