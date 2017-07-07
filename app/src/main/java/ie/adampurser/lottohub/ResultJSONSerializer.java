package ie.adampurser.lottohub;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class ResultJSONSerializer {
    private static final String TAG_DEBUG = "JSONSerializer.DEBUG";


    public static final String FILENAME_RESULTS_LOTTO = "lottoResults.json";
    public static final String FILENAME_RESULTS_DAILY = "dailyMillionResults.json";
    public static final String FILENAME_RESULTS_EURO = "euroMillionsResults.json";

    private Context mContext;

    public ResultJSONSerializer(Context context) {
        mContext = context;
    }

    public void saveResult(Result[] completeResult)
            throws JSONException, IOException {

        writeJSONArray(resultsToJSONArray(completeResult), getFileName(
                completeResult[0].getDrawType()));
    }

    private JSONArray resultsToJSONArray(Result[] completeResult)
            throws JSONException {

        JSONArray jsonArray = new JSONArray();
        for(Result result : completeResult) {
            jsonArray.put(result.toJSON());
        }

        return jsonArray;
    }

    private void writeJSONArray(JSONArray jsonArray, String fileName)
            throws JSONException, IOException{

        Writer writer = null;
        OutputStream out;

        try {
            out = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(jsonArray.toString());

        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    public Result[] loadCompleteResult(String drawType) throws IOException, JSONException{
        ArrayList<Result> completeResult = new ArrayList<>();

        BufferedReader reader;
        InputStream in;
        StringBuilder jsonString;
        String line;
        JSONArray jsonArray;
        in = mContext.openFileInput(getFileName(drawType));
        reader = new BufferedReader(new InputStreamReader(in));
        jsonString = new StringBuilder();
        while((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        jsonArray = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
        for(int i = 0; i < jsonArray.length(); i++) {
            completeResult.add(new Result(jsonArray.getJSONObject(i)));
        }

        reader.close();

        return completeResult.toArray(new Result[completeResult.size()]);
    }

    private String getFileName(String drawType) {
        switch (drawType) {
            case DrawType.LOTTO:
                return FILENAME_RESULTS_LOTTO;
            case DrawType.DAILY_MILLION:
                return FILENAME_RESULTS_DAILY;
            case DrawType.EURO_MILLIONS:
                return FILENAME_RESULTS_EURO;
            default:
                return null;
        }
    }
}
