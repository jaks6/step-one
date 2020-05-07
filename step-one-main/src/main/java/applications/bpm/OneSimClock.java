package applications.bpm;

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import core.SimClock;
import org.flowable.common.engine.impl.runtime.Clock;
import org.flowable.common.engine.impl.util.TimeZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Joram Barrez
 */
public class OneSimClock implements Clock {
    private static OneSimClock mInstance;
    Logger log = LoggerFactory.getLogger(this.getClass());


    private TimeZone timeZone;
    protected static volatile Calendar CURRENT_TIME;

    public OneSimClock() {
    }

    public OneSimClock(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public static Clock getInstance() {
        if (mInstance == null){
            mInstance = new OneSimClock();
        }
        return mInstance;
    }

    @Override
    public void setCurrentTime(Date currentTime) {
        log.error("Overriding SimClock!!! (setCurrentTime())");
        Calendar time = null;

        if (currentTime != null) {
            time = (timeZone == null) ? new GregorianCalendar() : new GregorianCalendar(timeZone);
            time.setTime(currentTime);
        }

        setCurrentCalendar(time);
    }

    @Override
    public void setCurrentCalendar(Calendar currentTime) {
        CURRENT_TIME = currentTime;
    }

    @Override
    public void reset() {
        CURRENT_TIME = null;
    }

    @Override
    public Date getCurrentTime() {

        if (CURRENT_TIME == null){
            // Milliseconds since ePoch
            return new Date((long) SimClock.getTime() * 1000L); //TODO: see if CURRENT_TIME can be set and re-used, reset() every simulation update
        }
        return CURRENT_TIME.getTime();
    }

    @Override
    public Calendar getCurrentCalendar() {
        // Milliseconds since ePoch


        if (CURRENT_TIME == null) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(new Date((long) SimClock.getTime() * 1000L));
            //return (timeZone == null) ? new GregorianCalendar() : new GregorianCalendar(timeZone);
            return calendar;
        }

        return (Calendar) CURRENT_TIME.clone();
    }

    @Override
    public Calendar getCurrentCalendar(TimeZone timeZone) {
        return TimeZoneUtil.convertToTimeZone(getCurrentCalendar(), timeZone);
    }

    @Override
    public TimeZone getCurrentTimeZone() {
        return getCurrentCalendar().getTimeZone();
    }

}
