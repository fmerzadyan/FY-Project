package com.merzadyan;

import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateCategoriser {
    private static final Logger l = Logger.getLogger(DateCategoriser.class.getName());
    
    private static final LocalDate DEFAULT_START_DATE = LocalDate.parse("2018-03-01");
    private LocalDate startDate;
    private List<LocalDate> dates;
    
    public class Data {
        public int nthWeek;
        public LocalDate startDate,
                startDateOfWeek,
                endDateOfWeek;
        
        public Data(int nthWeek, LocalDate startDate, LocalDate startDateOfWeek, LocalDate endDateOfWeek) {
            this.nthWeek = nthWeek;
            this.startDate = startDate;
            this.startDateOfWeek = startDateOfWeek;
            this.endDateOfWeek = endDateOfWeek;
        }
    }
    
    /**
     * @param startDate is 2018-03-01 (default date) if param is null.
     */
    public DateCategoriser(LocalDate startDate) {
        this.startDate = startDate == null ? DEFAULT_START_DATE : startDate;
        
        // Gets the date of weeks between 2018-03-01 and the next 3 months.
        // LocalDate start = LocalDate.parse("2018-03-01"),
        LocalDate endDate = this.startDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        // Skip 7 days (1 week)
        dates = Stream.iterate(this.startDate, date -> date.plusWeeks(1))
                .limit(ChronoUnit.WEEKS.between(this.startDate, endDate))
                .collect(Collectors.toList());
        l.debug(dates.size());
        l.debug(dates);
    }
    
    public Data nthDetails(LocalDate extractedDate) {
        int nthWeek = 0;
        LocalDate startDateOfWeek = null, endDateOfWeek = null;
        // LocalDate extractedDate = LocalDate.parse("2018-03-03");
        for (int i = 0; i < dates.size(); i++) {
            if (i == dates.size() - 1) {
                break;
            }
            
            LocalDate currentDate = dates.get(i);
            LocalDate nextDate = dates.get(i + 1);
            if (extractedDate.isBefore(nextDate) && extractedDate.isAfter(currentDate)) {
                // nthWeek starts from 1 whereas i starts from 0 thus add 1.
                nthWeek = i + 1;
                startDateOfWeek = currentDate;
                endDateOfWeek = nextDate;
                break;
            }
        }
        
        l.debug("startOfCalendar: " + startDate + " nthWeek: " + nthWeek + " startDateOfWeek: " + startDateOfWeek +
                " endDateOfWeek: " + endDateOfWeek);
        return new Data(nthWeek, startDate, startDateOfWeek, endDateOfWeek);
    }
    
    /**
     * Extracts the intervals between the date elements in date member variable.
     *
     * @return
     */
    public String[] extractIntervals() {
        String[] intervals = new String[dates.size() - 1];
        for (int i = 0; i < dates.size(); i++) {
            if (i == dates.size() - 1) {
                break;
            }
            
            intervals[i] = dates.get(i) + " to " + dates.get(i + 1);
        }
        return intervals;
    }
    
    public static LocalDate getDefaultStartDate() {
        return DEFAULT_START_DATE;
    }
    
    public List<LocalDate> getDates() {
        return dates;
    }
}
