package ie.adampurser.lottohub;


import junit.framework.Assert;

import org.junit.Test;

public class OcrDetectionProcessorTest {
    // correct lotto line format examples
    private static final String LOTTO_LINE_CORRECT_FORMAT_1 = "061522354202";
    private static final String LOTTO_LINE_CORRECT_FORMAT_2 = "403805081527";
    private static final String LOTTO_LINE_CORRECT_FORMAT_3 = "262139440119";
    private static final String LOTTO_LINE_CORRECT_FORMAT_4 = "010834221711";

    // incorrect lotto line format examples
    private static final String LOTTO_LINE_INCORRECT_FORMAT_1 = "61522354202"; // 1 digit number missing a leading 0
    private static final String LOTTO_LINE_INCORRECT_FORMAT_2 = "061522354248"; // 48 is out of bounds
    private static final String LOTTO_LINE_INCORRECT_FORMAT_3 = "1522354202"; // only contains 5 numbers
    private static final String LOTTO_LINE_INCORRECT_FORMAT_4 = "06152235420204"; // contains 7 numbers

    // correct euro millions format examples
    private static final String EURO_LINE_CORRECT_FORMAT_1 = "01455033270209";
    private static final String EURO_LINE_CORRECT_FORMAT_2 = "43201639480611";
    private static final String EURO_LINE_CORRECT_FORMAT_3 = "07112330401012";
    private static final String EURO_LINE_CORRECT_FORMAT_4 = "22260913420507";

    // incorrect euro millions format examples
    private static final String EURO_LINE_INCORRECT_FORMAT_1 = "01455133270209"; // out of bounds main nums
    private static final String EURO_LINE_INCORRECT_FORMAT_2 = "455033270209"; // 1 digit missing
    private static final String EURO_LINE_INCORRECT_FORMAT_3 = "0145503327209"; // digit missing leading 0
    private static final String EURO_LINE_INCORRECT_FORMAT_4 = "0145503327020948"; // contains 8 numbers
    private static final String EURO_LINE_INCORRECT_FORMAT_5 = "01455033270213"; // bonus num out of bounds

    // correct daily million format examples
    private static final String DAILY_LINE_CORRECT_FORMAT_1 = "150238391929";
    private static final String DAILY_LINE_CORRECT_FORMAT_2 = "010926321727";

    // incorrect daily million format examples
    private static final String DAILY_LINE_INCORRECT_FORMAT_1 = "000238391929"; // out of bounds
    private static final String DAILY_LINE_INCORRECT_FORMAT_2 = "010238391940"; // out of bounds
    private static final String DAILY_LINE_INCORRECT_FORMAT_3 = "0102383929"; // contains 5 numbers
    private static final String DAILY_LINE_INCORRECT_FORMAT_4 = "15023839192928"; // contains 7 numbers


    @Test
    public void LottoLineCorrectFormatTest_ReturnsTrue() {
        Assert.assertTrue(
                LOTTO_LINE_CORRECT_FORMAT_1.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
                && LOTTO_LINE_CORRECT_FORMAT_2.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
                && LOTTO_LINE_CORRECT_FORMAT_3.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
                && LOTTO_LINE_CORRECT_FORMAT_4.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
        );
    }

    @Test
    public void LottoLineIncorrectFormatTest_ReturnsTrue() {
        Assert.assertFalse(
                LOTTO_LINE_INCORRECT_FORMAT_1.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
                && LOTTO_LINE_INCORRECT_FORMAT_2.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
                && LOTTO_LINE_INCORRECT_FORMAT_3.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
                && LOTTO_LINE_INCORRECT_FORMAT_4.matches(OcrDetectionProcessor.LOTTO_LINE_REGEX)
        );
    }

    @Test
    public void EuroMillionsLineCorrectFormatTest_ReturnsTrue() {
        Assert.assertTrue(
                EURO_LINE_CORRECT_FORMAT_1.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_CORRECT_FORMAT_2.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_CORRECT_FORMAT_3.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_CORRECT_FORMAT_4.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
        );
    }

    @Test
    public void EuroMillionsLineIncorrectFormatTest_ReturnsTrue() {
        Assert.assertFalse(
                EURO_LINE_INCORRECT_FORMAT_1.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_INCORRECT_FORMAT_2.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_INCORRECT_FORMAT_3.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_INCORRECT_FORMAT_4.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
                && EURO_LINE_INCORRECT_FORMAT_5.matches(OcrDetectionProcessor.EURO_MILLIONS_LINE_REGEX)
        );
    }

    @Test
    public void DailyMillionsLineCorrectFormatTest_ReturnsTrue() {
        Assert.assertTrue(
                DAILY_LINE_CORRECT_FORMAT_1.matches(OcrDetectionProcessor.DAILY_MILLION_LINE_REGEX)
                && DAILY_LINE_CORRECT_FORMAT_2.matches(OcrDetectionProcessor.DAILY_MILLION_LINE_REGEX)
        );
    }

    @Test
    public void DailyMillionsLineIncorrectFormatTest_ReturnsTrue() {
        Assert.assertFalse(
                DAILY_LINE_INCORRECT_FORMAT_1.matches(OcrDetectionProcessor.DAILY_MILLION_LINE_REGEX)
                && DAILY_LINE_INCORRECT_FORMAT_2.matches(OcrDetectionProcessor.DAILY_MILLION_LINE_REGEX)
                && DAILY_LINE_INCORRECT_FORMAT_3.matches(OcrDetectionProcessor.DAILY_MILLION_LINE_REGEX)
                && DAILY_LINE_INCORRECT_FORMAT_4.matches(OcrDetectionProcessor.DAILY_MILLION_LINE_REGEX)
        );
    }
}
