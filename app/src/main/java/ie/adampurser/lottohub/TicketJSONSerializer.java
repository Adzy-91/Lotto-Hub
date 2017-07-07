package ie.adampurser.lottohub;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class TicketJSONSerializer {
    public static final String LOG_TAG_DEBUG = "SavedNumbersJSON.DEBUG";

    private static final String FILENAME_LOTTO = "SavedTicketsLotto.json";
    private static final String FILENAME_DAILY = "SavedTicketsDaily.json";
    private static final String FILENAME_EURO = "SavedTicketsEuro.json";

    private static final int MAX_NUM_OF_SAVED_TICKETS = 20;

    private Context mContext;

    public TicketJSONSerializer(Context context) {
        mContext = context;
    }

    public boolean saveTickets(Ticket[] tickets, String drawType) throws JSONException, IOException{
        if(tickets.length > MAX_NUM_OF_SAVED_TICKETS) {
            Log.d(LOG_TAG_DEBUG, "Number of tickets exceeds max amount");
            return false;
        }

        JSONArray jsonArray = new JSONArray();
        for(Ticket t: tickets) {
            try {
                jsonArray.put(t.toJson());
            }catch (Exception e) {
                Log.d(LOG_TAG_DEBUG, e.getMessage());
            }
        }

        saveJsonArray(jsonArray, drawType);

        return true;
    }

    public void saveTicket(Ticket ticket, String drawType) throws JSONException, IOException {
        ArrayList<Ticket> savedTickets = loadTickets(drawType);

        if(savedTickets.size() == MAX_NUM_OF_SAVED_TICKETS) {
            Toast.makeText(mContext, R.string.toast_max_num_of_saved_tickets,
                    Toast.LENGTH_LONG).show();
        }
        else {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(ticket.toJson());

            for (Ticket t : savedTickets) {
                jsonArray.put(t.toJson());
            }

            saveJsonArray(jsonArray, drawType);
        }
    }

    public boolean removeTicket(int position, String drawType) throws JSONException, IOException{
        ArrayList<Ticket> loadedTickets = loadTickets(drawType);

        if(position >= loadedTickets.size()) {
            Log.e(LOG_TAG_DEBUG, "position is out of bounds");
            return false;
        }

        loadedTickets.remove(position);

        JSONArray jsonArray = new JSONArray();
        for(Ticket t: loadedTickets) {
            jsonArray.put(t.toJson());
        }

        saveJsonArray(jsonArray, drawType);
        return true;
    }

    private void saveJsonArray(JSONArray jsonArray, String drawType) throws JSONException, IOException {
        Writer writer = null;
        OutputStream out;

        try {
            out = mContext.openFileOutput(getFileName(drawType), Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(jsonArray.toString());
        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    public ArrayList<Ticket> loadTickets(String drawType) {
        BufferedReader reader;
        InputStream in;
        StringBuilder jsonString;
        String line;
        JSONArray jsonArray;
        ArrayList<Ticket> tickets = new ArrayList<>(MAX_NUM_OF_SAVED_TICKETS);

        try {
            in = mContext.openFileInput(getFileName(drawType));
            reader = new BufferedReader(new InputStreamReader(in));
            jsonString = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            jsonArray = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            for (int i = 0; i < jsonArray.length(); i++) {
                tickets.add(new Ticket(jsonArray.getJSONObject(i)));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG_DEBUG, "Failed to load tickets");
        }

        return tickets;
    }

    private String getFileName(String drawType) {
        switch (drawType) {
            case DrawType.LOTTO:
                return FILENAME_LOTTO;
            case DrawType.DAILY_MILLION:
                return FILENAME_DAILY;
            default:
                return FILENAME_EURO;
        }
    }
}
