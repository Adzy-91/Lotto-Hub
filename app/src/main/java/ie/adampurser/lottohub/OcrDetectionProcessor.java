package ie.adampurser.lottohub;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ie.adampurser.lottohub.camera.GraphicOverlay;

/**
 * A Processor which receives detected TextBlocks from the ticket and builds up the ticket object
 * to be checked.
 */
public class OcrDetectionProcessor implements Detector.Processor<TextBlock> {
    private static final String LOG_TAG = "LOG.OCR";

    // the minimum number of times a ticket component
    // needs to be detected before it is added to the ticket object
    private static final int MIN_CONF = 2;

    // used to determine the number of lines on a ticket
    private static final int MIN_CONF_BLOCK_SIZE = 4;

    // regular expressions that define patterns on a lotto ticket
    public static final String LOTTO_NUM_REGEX = "(?:0[1-9]|[1-3][0-9]|4[0-7])";
    public static final String LOTTO_LINE_REGEX = LOTTO_NUM_REGEX + "{6}";
    public static final String DAILY_MILLION_NUM_REGEX = "(?:0[1-9]|[1-3][0-9])";
    public static final String DAILY_MILLION_LINE_REGEX = DAILY_MILLION_NUM_REGEX + "{6}";
    public static final String EURO_MILLIONS_NUM_REGEX = "(?:0[1-9]|[1-4][0-9]|50)";
    public static final String EURO_MILLIONS_BONUS_NUM_REGEX = "(?:0[1-9]|1[0-2])";
    public static final String EURO_MILLIONS_LINE_REGEX = EURO_MILLIONS_NUM_REGEX + "{5}"
            + EURO_MILLIONS_BONUS_NUM_REGEX + "{2}";
    private static final String MONTH_REGEX = "(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    public static final String DATE_REGEX_1 = "([0-3][0-9]" + MONTH_REGEX + "[0-9][0-9][0-9][0-9])"; // capture group
    public static final String DATE_REGEX_2 = "([0-3][0-9]/[0-1][0-9]/[1-9][1-9])";
    public static final String DATE_REGEX = DATE_REGEX_1 + "|" + DATE_REGEX_2;

    // the date format found on a lotto ticket
    public static final String DATE_FORMAT_1 = "ddMMMyyyy";
    public static final String DATE_FORMAT_2 = "dd/mm/yy";

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private OcrDetectionProgress mDetectionProgress;
    private HashMap<String, Integer> mCandidateTicketComponents;

    // the numbers in a ticket line e.g. 01 is one number
    private int mLineSize;

    private int mBlockSizeDetectionCount = 0;
    private int mCurrentBlockSize = 0;
    private boolean mBlockSizeDetected = false;
    private boolean mLinesDetected = false;
    private boolean mDateDetected = false;
    private String mDrawType;
    // the ticket object that will be built up with various ocr detections
    private Ticket mTicket;
    private SimpleDateFormat mDateFormat;
    private Pattern mLinePattern;
    private Matcher mLineMatcher;
    private Pattern mDatePattern;
    private Matcher mDateMatcher;


    /**
     * Constructor for OcrDetectionProcessor. Ticket line and date format are set according to
     * the draw type passed in.
     *
     * @param progress interface containing OCR callback functions implemented in OcrCaptureActivity.
     * @param graphicOverlay the graphic overlay which will highlight OCR detections on screen.
     * @param drawType the draw type of the ticket being checked.
     */
    OcrDetectionProcessor(OcrDetectionProgress progress,
                          GraphicOverlay<OcrGraphic> graphicOverlay,
                          String drawType) {
        mDetectionProgress = progress;
        mGraphicOverlay = graphicOverlay;
        mCandidateTicketComponents = new HashMap<>();
        mDrawType = drawType;
        switch (mDrawType) {
            case DrawType.LOTTO:
                mLineSize = 6;
                mLinePattern = Pattern.compile(LOTTO_LINE_REGEX);
                break;
            case DrawType.DAILY_MILLION:
                mLineSize = 6;
                mLinePattern = Pattern.compile(DAILY_MILLION_LINE_REGEX);
                break;
            case DrawType.EURO_MILLIONS:
                mLineSize = 7;
                mLinePattern = Pattern.compile(EURO_MILLIONS_LINE_REGEX);
        }
        mTicket = new Ticket(mDrawType);
        mDatePattern = Pattern.compile(DATE_REGEX);
    }

    /**
     * Called by the detector to deliver detection results.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();

        SparseArray<TextBlock> items = detections.getDetectedItems();

        for (int i = 0; i < items.size(); ++i) {

            TextBlock block = items.valueAt(i);

            if(!mLinesDetected) {
                detectTicketLines(block);
            }
            if(!mDateDetected) {
                detectDate(block);
            }
        }
    }

    /**
     * This function attempts to detect draw dates on a ticket by matching various detections
     * against a date regex.
     *
     * @param block block object containing various OCR detections.
     */
    private void detectDate(TextBlock block) {
        String lineValue;
        boolean minConfSatisfied = false;

        Line line;
        for(int i = 0; i < block.getComponents().size(); i++) {
            line = (Line) block.getComponents().get(i);

            // remove all spaces
            lineValue = line.getValue().replaceAll(" ", "");

            mDateMatcher = mDatePattern.matcher(lineValue);

            // returns true if a date pattern is matched
            if(mDateMatcher.find()) {
                try {

                    if(mDateMatcher.group(1) != null) {
                        mDateFormat = new SimpleDateFormat(DATE_FORMAT_1, Locale.ENGLISH);
                    }
                    else {
                        mDateFormat = new SimpleDateFormat(DATE_FORMAT_2, Locale.ENGLISH);
                    }
                    // parse the first date
                    Date firstDrawDate = mDateFormat.parse(
                            lineValue.substring(mDateMatcher.start(), mDateMatcher.end())
                    );

                    // check if there's a second date for multiple draws and attempt to parse it
                    Date lastDrawDate = null;
                    if(mDateMatcher.find()) {
                        lastDrawDate = mDateFormat.parse(
                                lineValue.substring(mDateMatcher.start(), mDateMatcher.end()));
                    }

                    // check if we need to add two dates
                    if(lastDrawDate != null) {
                        minConfSatisfied = addCandidateTicketComponent(firstDrawDate.toString() +
                                lastDrawDate.toString(), block);
                    }
                    else {
                        minConfSatisfied = addCandidateTicketComponent(firstDrawDate.toString(), block);
                    }

                    // if min conf is satisfied add the appropriate range of dates to the ticket
                    if(minConfSatisfied) {

                        // add the first date to the ticket
                        mTicket.addDate(firstDrawDate);

                        // check if there's multiple draw dates
                        if(lastDrawDate != null) {

                            // create a new date object with the current date
                            Date currentDate = new Date();

                            // a date object to hold the range of draw dates to be added
                            // to the ticket
                            Date tempDate = Result.getNextDrawDate(firstDrawDate, mDrawType);

                            // add the range of dates to the ticket making sure not
                            // to go past the current date
                            while(tempDate.before(currentDate) && !tempDate.after(lastDrawDate)) {

                                mTicket.addDate(tempDate);

                                // get the next draw date
                                tempDate = Result.getNextDrawDate(tempDate, mDrawType);
                            }
                        }
                    }
                }catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // date has been detected so update various variables
            if(minConfSatisfied) {
                mDateDetected = true;
                mDetectionProgress.updateDetection(OcrDetectionProgress.Detection.DATE_DETECTED);
                if(ticketDetected()) {
                    mDetectionProgress.finishDetection(mTicket);
                }
            }
        }
    }

    /**
     * This function attempts to detect lottery lines on a ticket by matching various detections
     * against a line regex.
     *
     * @param block object containing various OCR detections.
     */
    private void detectTicketLines(TextBlock block) {
        for(int j = 0; j < block.getComponents().size(); j++) {

            Line line  = (Line) block.getComponents().get(j);
            String lineValue;
            // check if line contains the min num of elements
            if(line.getComponents().size() >= mLineSize) {

                lineValue  = getLineValue((Line) block.getComponents().get(j));

                lineValue = lineValue.replaceAll("\\W", "");

                mLineMatcher = mLinePattern.matcher(lineValue);

                // check the string value of the line against regex and if it's a match
                // add it to the hashmap
                if(mLineMatcher.find()) {

                    if(!mBlockSizeDetected) {
                        // if the number of lines on a ticket have been detected
                        if(block.getComponents().size() == mCurrentBlockSize) {
                            mBlockSizeDetectionCount++;
                            if(mBlockSizeDetectionCount == MIN_CONF_BLOCK_SIZE) {
                                mBlockSizeDetected = true;
                            }
                        }
                        else {
                            mBlockSizeDetectionCount = 1;
                            mCurrentBlockSize = block.getComponents().size();
                        }
                    }

                    // add candidate line and get the boolean indicating if the minimum
                    // confidence has been met for this component

                    boolean minConfSatisfied = addCandidateTicketComponent(mLineMatcher.group(), block);

                    if(minConfSatisfied) {
                        mTicket.addLine(getTicketLine(mLineMatcher.group(), mDrawType));
                    }

                    if(mBlockSizeDetected && mTicket.size() == mCurrentBlockSize) {
                        mLinesDetected = true;
                        mDetectionProgress.updateDetection(OcrDetectionProgress.Detection.LINES_DETECTED);

                        // check have all components been detected
                        if(ticketDetected()) {
                            mDetectionProgress.finishDetection(mTicket);
                        }
                    }
                }
            }
        }
    }

    /**
     * Add a candidate ticket component to the HashMap.
     *
     * @param componentValue the value of the component being added to the HashMap.
     * @param block the block which will be highlighted on screen to indicate a detection.
     * @return boolean indicating if minimum confidence has been met for the added component
     */
    private boolean addCandidateTicketComponent(String componentValue, TextBlock block) {

        // add onscreen border around the block containing the componentValue being added
        OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, block);
        mGraphicOverlay.add(graphic);

        // if we've never seen this componentValue add it with a count of 1
        if(!mCandidateTicketComponents.containsKey(componentValue)) {
            mCandidateTicketComponents.put(componentValue, 1);
            Log.i(LOG_TAG, "new candidate componentValue detected");
        }
        // if we've seen the componentValue before but it hasn't met min confidence yet
        else if(mCandidateTicketComponents.get(componentValue) < MIN_CONF) {

            Log.i(LOG_TAG, "old candidate componentValue detected");

            // get the current count of this componentValue
            int count = mCandidateTicketComponents.get(componentValue);

            // increment the count as we've now seen it one more time
            mCandidateTicketComponents.put(componentValue, count + 1);

            // if we've reached min conf for this componentValue then return true indicating that
            // it should be added to the ticket
            if(count + 1 == MIN_CONF) {
                return true;
            }
        }
        else {
            Log.i(LOG_TAG, "Nothing new detected");
        }

        return false;
    }

    /**
     * returns true if all necessary parts of the ticket have been detected
     */
    private boolean ticketDetected() {
        return mLinesDetected && mDateDetected;
    }

    private void printLine(String line) {
        System.out.print("Line: ");
        for(int i = 0; i < line.length(); i+=2) {
            System.out.print(line.substring(i, i+2) + " ");
        }
        System.out.println("");
    }

    private String getLineValue(Line line) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < line.getComponents().size(); i++) {
            builder.append(line.getComponents().get(i).getValue());
        }

        return builder.toString();
    }

    private TicketLine getTicketLine(String lineValue, String drawType) {
        System.out.println("creating ticket line from: " + lineValue);
        TicketLine line = new TicketLine(drawType);
        for(int i = 0; i < line.getCapacity()*2; i+=2) {
            line.add(lineValue.substring(i, i+2));
        }

        return line;
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
