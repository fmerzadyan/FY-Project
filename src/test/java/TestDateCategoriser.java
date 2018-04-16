import com.merzadyan.stock.DateCategoriser;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;


public class TestDateCategoriser {
    // Date is 2018-03-01 by default.
    private static final DateCategoriser DATE_CATEGORISER = new DateCategoriser(null);
    
    @Test
    public void defaultDateShouldBe20180301() {
        Assert.assertEquals(DateCategoriser.getDefaultStartDate(), LocalDate.parse("2018-03-01"));
    }
    
    @Test
    public void delimiterShouldBeTo() {
        Assert.assertEquals("to", DateCategoriser.INTERVAL_DELIMITER);
    }
    
    @Test
    public void shouldBeOneWeekApart() {
        List<LocalDate> dates = DATE_CATEGORISER.getDates();
        for (int i = 0, datesSize = dates.size(); i < datesSize; i++) {
            if (i == dates.size() - 1) {
                break;
            }
            
            LocalDate currentDate = dates.get(i);
            Assert.assertEquals(currentDate.plusWeeks(1), dates.get(i + 1));
        }
    }
    
    @Test
    public void shouldReturnWeekIntervals() {
        String[] intervals = DATE_CATEGORISER.extractIntervals();
        Assert.assertEquals("2018-03-01 to 2018-03-08", intervals[0]);
        Assert.assertEquals("2018-03-08 to 2018-03-15", intervals[1]);
        Assert.assertEquals("2018-03-15 to 2018-03-22", intervals[2]);
        Assert.assertEquals("2018-03-22 to 2018-03-29", intervals[3]);
        Assert.assertEquals("2018-03-29 to 2018-04-05", intervals[4]);
        Assert.assertEquals("2018-04-05 to 2018-04-12", intervals[5]);
        Assert.assertEquals("2018-04-12 to 2018-04-19", intervals[6]);
    }
    
    @Test
    public void nthWeek() {
        LocalDate date = LocalDate.parse("2018-03-28"),
                startOfWeek = LocalDate.parse("2018-03-22"),
                endOfWeek = LocalDate.parse("2018-03-29");
        DateCategoriser.Data data = DATE_CATEGORISER.nthDetails(date);
        Assert.assertEquals(4, data.nthWeek);
        Assert.assertEquals(startOfWeek, data.startDateOfWeek);
        Assert.assertEquals(endOfWeek, data.endDateOfWeek);
    }
}
