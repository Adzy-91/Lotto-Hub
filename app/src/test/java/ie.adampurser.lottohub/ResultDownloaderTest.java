package ie.adampurser.lottohub;


import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ResultDownloaderTest {

    Elements mElements;

    @Test
    public void getLatestResultLotto_ReturnsTrue() {
        try {
            mElements = ResultDownloader.getLatestResult(DrawType.LOTTO, 1);
            Assert.assertTrue(true);
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void getLatestResultDailyMillion_ReturnsTrue() {
        try {
            mElements = ResultDownloader.getLatestResult(DrawType.DAILY_MILLION, 1);
            Assert.assertTrue(true);
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void getLatestResultEuroMillions_ReturnsTrue() {
        try {
            mElements = ResultDownloader.getLatestResult(DrawType.EURO_MILLIONS, 1);
            Assert.assertTrue(true);
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }
}
