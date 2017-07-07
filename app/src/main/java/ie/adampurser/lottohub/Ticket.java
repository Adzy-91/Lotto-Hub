package ie.adampurser.lottohub;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class Ticket implements Serializable{
    private static final String JSON_LINES = "lines";
    private static final String JSON_DRAW_TITLE = "drawTitle";

    private LinkedList<TicketLine> mLines;
    private ArrayList<Date> mDates;
    private String mDrawType;

    public Ticket(String drawType) {
        mDrawType = drawType;
        mLines = new LinkedList<>();
        mDates = new ArrayList<>();
    }

    public Ticket(String drawType, Date date) {
        mDates = new ArrayList<>();
        mDates.add(date);
        mDrawType = drawType;
        mLines = new LinkedList<>();
    }

    public Ticket(String drawType, LinkedList<TicketLine> lines, Date date) {
        mDrawType = drawType;
        mLines = lines;
        mDates = new ArrayList<>();
        mDates.add(date);
    }

    public Ticket(JSONObject json) throws JSONException {
        mDrawType = json.getString(JSON_DRAW_TITLE);
        mDates = new ArrayList<>();
        mLines = new LinkedList<>();
        JSONArray jsonArray = json.getJSONArray(JSON_LINES);
        for(int i = 0; i < jsonArray.length(); i++) {
            mLines.add(new TicketLine(jsonArray.getJSONObject(i)));
        }
    }

    public JSONObject toJson() throws JSONException{
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(JSON_DRAW_TITLE, mDrawType);

        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < mLines.size(); i++) {
            jsonArray.put(mLines.get(i).toJson());
        }
        jsonObject.put(JSON_LINES, jsonArray);

        return jsonObject;
    }

    public Ticket(Ticket ticket) {
        mDates = ticket.getDates();
        mDrawType = ticket.getDrawType();
        mLines = ticket.getLines();
    }

    public LinkedList<TicketLine> getCopyOfLines() {
        LinkedList<TicketLine> lines = new LinkedList<>();
        lines.addAll(mLines);
        return lines;
    }

    public void clearLines() {
        mLines.clear();
    }

    public void setLines(LinkedList<TicketLine> lines) {
        mLines = lines;
    }

    public ArrayList<Date> getDates() {
        return mDates;
    }

    public void setDates(ArrayList<Date> dates) {
        mDates = dates;
    }

    public String getDrawType() {
        return mDrawType;
    }

    public void addLine(int position, TicketLine line) {
        mLines.add(position, line);
        updateLineInfo();
    }

    public void addLine(TicketLine line) {
        mLines.add(line);
        updateLineInfo();
    }

    public void removeLastLine() {
        mLines.removeLast();
        updateLineInfo();
    }

    public void removeLine(int index) {
        mLines.remove(index);
        updateLineInfo();
    }

    public void addDate(Date date) {
        mDates.add(date);
    }

    public TicketLine getCurrentLine() {
        return mLines.getLast();
    }

    public TicketLine getLine(int position) {
        return mLines.get(position);
    }

    public LinkedList<TicketLine> getLines() {
        return mLines;
    }

    public int size() {
        return mLines.size();
    }

    private void updateLineInfo() {
        for(int i = 0; i < mLines.size(); i++) {
            mLines.get(i).setLetter(String.valueOf((char)(97 + i)));
            mLines.get(i).setLinePosition(i);
        }
    }
}
