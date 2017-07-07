package ie.adampurser.lottohub;

import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.LinkedList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class TicketLine implements Serializable{
    public static final String JSON_LETTER = "letter";
    private static final String JSON_NUMS = "nums";
    private static final String JSON_POSITION = "linePosition";
    private static final String JSON_CAPACITY = "capacity";

    private String mLetter;
    private int mLinePosition;
    private int mCapacity;
    private LinkedList<String> mNums;


    TicketLine(String drawType) {
        mNums = new LinkedList<>();
        switch (drawType) {
            case DrawType.EURO_MILLIONS:
                mCapacity = 7;
                break;
            default:
                mCapacity = 6;
        }
    }

    TicketLine(Line line) {
        mNums = new LinkedList<>();
        for(Text text: line.getComponents()) {
            mNums.add(text.getValue());
        }
        mLetter = "";
        mCapacity = line.getComponents().size();
    }

    public TicketLine(JSONObject json) throws JSONException {
        mLetter = json.getString(JSON_LETTER);
        mCapacity = json.getInt(JSON_CAPACITY);
        mLinePosition = json.getInt(JSON_POSITION);

        mNums = new LinkedList<>();
        JSONArray jsonArray = json.getJSONArray(JSON_NUMS);
        for(int i = 0; i < jsonArray.length(); i++) {
            mNums.add(jsonArray.getString(i));
        }

    }

    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();

        json.put(JSON_LETTER, mLetter);
        json.put(JSON_CAPACITY, mCapacity);
        json.put(JSON_POSITION, mLinePosition);

        JSONArray jsonArray = new JSONArray();
        for(String s: mNums) {
            jsonArray.put(s);
        }
        json.put(JSON_NUMS, jsonArray);

        return json;
    }

    public int getCurrentSize() {
        return mNums.size();
    }

    public int getLinePosition() {
        return mLinePosition;
    }

    public void setLinePosition(int linePosition) {
        mLinePosition = linePosition;
    }

    public boolean isCompleted(){
        return mNums.size() == mCapacity;
    }

    // Add a number to this ticket line
    public void add(String num) {

        // Check for leading zero
        if(num.charAt(0) == '0') {
            num = String.valueOf(num.charAt(1));
        }

        mNums.add(num);
    }

    public String getLetter() {
        return mLetter;
    }

    public void setLetter(String letter) {
        mLetter = letter;
    }

    public void removeFirstOccurrence(String lineNum) {
        mNums.removeFirstOccurrence(lineNum);
    }

    public void remove(int position) {
        mNums.remove(position);
    }

    public boolean contains(String lineNum) {
        return mNums.contains(lineNum);
    }

    public LinkedList<String> getNums() {
        return mNums;
    }

    // Returns the line number at the specified index
    public String getNum(int linePosition) {
        if(linePosition >= mNums.size()) {
            return null;
        }
        return mNums.get(linePosition);
    }

    // Returns the max amount of numbers this line can have
    public int getCapacity() {
        return mCapacity;
    }

    public void printLine() {
        for (String ticketNum: mNums) {
            System.out.print(ticketNum + " ");
        }
    }
}
