package ie.adampurser.lottohub;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

public class ResultHelper {

    /* return list of given draw types from hashmap */
    public static Result[] getResults(String[] drawTypes, HashMap<String, Result> allResults) {
        Result[] results = new Result[drawTypes.length];

        for(int i = 0; i < results.length; i++) {
            results[i] = allResults.get(drawTypes[i]);
        }

        return results;
    }

    public static Result[] getCompleteResult(String primaryDrawType, HashMap<String, Result> allResults) {
        String[] drawTypes = DrawType.getAssociatedDrawTypes(primaryDrawType);

        return getResults(drawTypes, allResults);
    }

    public static JSONArray getJSONArray(String[] strings) {
        if(strings == null) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        for(String s: strings) {
            jsonArray.put(s);
        }

        return jsonArray;
    }

    public static String[] getStringArray(JSONArray jsonArray) throws JSONException {

        if(jsonArray == null) {
            return null;
        }

        String[] strings = new String[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++) {
            strings[i] = jsonArray.getString(i);
        }

        return strings;
    }
}
