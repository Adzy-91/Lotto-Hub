package ie.adampurser.lottohub;


public interface OcrDetectionProgress {

    enum Detection {
        DATE_DETECTED,
        LINES_DETECTED,
    }

    void updateDetection(Detection detection);
    void finishDetection(Ticket ticket);
}
