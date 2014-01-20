/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (props, at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.opentripplanner.routing.graph.Graph;

/**
 * Represents a day of transit services. 
 * Intended for quickly checking whether a service is running during path searches.
 * 
 * @author andrewbyrd
 *
 */
public class ServiceDay implements Serializable {
    private static final long serialVersionUID = -1206371243806996680L;

    protected long midnight;
    protected BitSet serviceIdsRunning;
    
    /* 
     * make a ServiceDay including the given time's day's starting second and a set of 
     * serviceIds running on that day.
     */
    public ServiceDay(Graph graph, long time, CalendarService cs, String agencyId) {
        TimeZone timeZone = cs.getTimeZoneForAgencyId(agencyId);
        GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.setTime(new Date(time * 1000));

        ServiceDate sd = new ServiceDate(calendar);
        Date d = sd.getAsDate(timeZone);
        this.midnight = d.getTime() / 1000;
        serviceIdsRunning = new BitSet(cs.getServiceIds().size());
        
        ServiceIdToNumberService service = graph.getService(ServiceIdToNumberService.class);
        for (AgencyAndId serviceId : cs.getServiceIdsOnDate(sd)) {
            int n = service.getNumber(serviceId);
            if (n < 0)
                continue;
            serviceIdsRunning.set(n);
        }
    }

    /* 
     * Does the given serviceId run on this ServiceDay?
     */
    public boolean serviceIdRunning(int serviceId) {
        return this.serviceIdsRunning.get(serviceId);
    }

    /* 
     * Return number of seconds after midnight on this ServiceDay
     * for the given time.
     * 
     * Note that the parameter and the return value are in seconds since the epoch
     * 
     * Return value may be negative, indicating that the time is 
     * before this ServiceDay.
     */
    public int secondsSinceMidnight(long time) {
        return (int) (time - this.midnight);
    }
    
    /* 
     * Return number of seconds since the epoch
     * based on the given number of seconds after midnight on this ServiceDay
     * 
     * Input value may be negative, indicating that the time is 
     * before this ServiceDay.
     */
    public long time(int secondsSinceMidnight) {
        return this.midnight + secondsSinceMidnight;
    }
    
    public String toString() {
        return Long.toString(this.midnight) + Arrays.asList(serviceIdsRunning);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof ServiceDay)) return false;
        ServiceDay other = (ServiceDay) o;
        return other.midnight == midnight;
    }
    
    @Override
    public int hashCode() {
        return (int) midnight;
    }

//    public static ServiceDay universalService(Graph graph) {
//        //FIXME: assumes all service is in the same tz
//        final String agencyId = graph.getAgencyIds().iterator().next();
//        ServiceDay universal = new ServiceDay(graph, 0, graph.getCalendarService(), agencyId);
//
//        return universal;
//    }
    
    /** Used in RAPTOR graph analysis to board everything. */
    public static class UniversalService extends ServiceDay {
        
        private static final long serialVersionUID = 1L;

        public UniversalService(Graph graph) {
            super(graph, 0, graph.getCalendarService(), graph.getAgencyIds().iterator().next());
        }

        @Override
        public boolean serviceIdRunning(int serviceId) { return true; }
        
    }
    
}
