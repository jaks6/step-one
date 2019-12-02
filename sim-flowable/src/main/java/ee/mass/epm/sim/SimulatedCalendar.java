package ee.mass.epm.sim;

import org.flowable.common.engine.impl.calendar.BusinessCalendar;

import java.util.Date;

public class SimulatedCalendar implements BusinessCalendar {
    @Override
    public Date resolveDuedate(String duedateDescription) {
        return null;
    }

    @Override
    public Date resolveDuedate(String duedateDescription, int maxIterations) {
        return null;
    }

    @Override
    public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate, Date newTimer) {
        return null;
    }

    @Override
    public Date resolveEndDate(String endDateString) {
        return null;
    }
}
