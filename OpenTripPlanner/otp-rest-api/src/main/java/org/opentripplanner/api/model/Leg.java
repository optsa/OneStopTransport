/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.api.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.patch.Alert;
import org.opentripplanner.util.model.EncodedPolylineBean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

 /**
 * One leg of a trip -- that is, a temporally continuous piece of the journey that takes place on a
 * particular vehicle (or on foot).
 */

public class Leg {

    /**
     * The date and time this leg begins.
     */
    public Calendar startTime = null;
    
    /**
     * The date and time this leg ends.
     */
    public Calendar endTime = null;
    
    /**
     * The distance traveled while traversing the leg in meters.
     */
    public Double distance = null;

    /**
     * The mode (e.g., <code>Walk</code>) used when traversing this leg.
     */
    @XmlAttribute
    @JsonSerialize
    public String mode = TraverseMode.WALK.toString();

    /**
     * For transit legs, the route of the bus or train being used. For non-transit legs, the name of
     * the street being traversed.
     */
    @XmlAttribute
    @JsonSerialize
    public String route = "";

    @XmlAttribute
    @JsonSerialize
    public String agencyName;

    @XmlAttribute
    @JsonSerialize
    public String agencyUrl;

    @XmlAttribute
    @JsonSerialize
    public int agencyTimeZoneOffset;

    /**
     * For transit leg, the route's (background) color (if one exists). For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String routeColor = null;

    /**
     * For transit legs, the type of the route. Non transit -1
     * When 0-7: 0 Tram, 1 Subway, 2 Train, 3 Bus, 4 Ferry, 5 Cable Car, 6 Gondola, 7 Funicular
     * When equal or highter than 100, it is coded using the Hierarchical Vehicle Type (HVT) codes from the European TPEG standard
     * Also see http://groups.google.com/group/gtfs-changes/msg/ed917a69cf8c5bef
     */
    @XmlAttribute
    @JsonSerialize
    public Integer routeType = null;
    
    /**
     * For transit legs, the ID of the route.
     * For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String routeId = null;

    /**
     * For transit leg, the route's text color (if one exists). For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String routeTextColor = null;

    /**
     * For transit legs, if the rider should stay on the vehicle as it changes route names.
     */
    @XmlAttribute
    @JsonSerialize
    public Boolean interlineWithPreviousLeg;

    
    /**
     * For transit leg, the trip's short name (if one exists). For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String tripShortName = null;

    /**
     * For transit leg, the trip's block ID (if one exists). For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String tripBlockId = null;
    
    /**
     * For transit legs, the headsign of the bus or train being used. For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String headsign = null;

    /**
     * For transit legs, the ID of the transit agency that operates the service used for this leg.
     * For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String agencyId = null;
    
    /**
     * For transit legs, the ID of the trip.
     * For non-transit legs, null.
     */
    @XmlAttribute
    @JsonSerialize
    public String tripId = null;
    
    /**
     * The Place where the leg originates.
     */
    public Place from = null;
    
    /**
     * The Place where the leg begins.
     */
    public Place to = null;

    /**
     * For transit legs, intermediate stops between the Place where the leg originates and the Place where the leg ends.
     * For non-transit legs, null.
     * This field is optional i.e. it is always null unless "showIntermediateStops" parameter is set to "true" in the planner request.
     */
    @XmlElementWrapper(name = "intermediateStops")
    @JsonProperty(value="intermediateStops")
    public List<Place> stop;

    /**
     * The leg's geometry.
     */
    public EncodedPolylineBean legGeometry;

    /**
     * A series of turn by turn instructions used for walking, biking and driving. 
     */
    @XmlElementWrapper(name = "steps")
    @JsonProperty(value="steps")
    public List<WalkStep> walkSteps;

    /**
     * Deprecated field formerly used for notes -- will be removed.  See
     * alerts
     */
    @XmlElement
    @JsonSerialize
    private List<Note> notes;

    @XmlElement
    @JsonSerialize
    private List<Alert> alerts;

    @XmlAttribute
    @JsonSerialize
    public String routeShortName;

    @XmlAttribute
    @JsonSerialize
    public String routeLongName;

    @XmlAttribute
    @JsonSerialize
    public String boardRule;

    @XmlAttribute
    @JsonSerialize
    public String alightRule;

    @XmlAttribute
    @JsonSerialize
    public Boolean rentedBike;

    /**
     * bogus walk/bike/car legs are those that have 0.0 distance, 
     * and just one instruction
     * 
     * @return boolean true if the leg is bogus 
     */
    public boolean isBogusNonTransitLeg() {
        boolean retVal = false;
        if( (TraverseMode.WALK.toString().equals(this.mode) ||
             TraverseMode.CAR.toString().equals(this.mode) ||
             TraverseMode.BICYCLE.toString().equals(this.mode)) &&
            (this.walkSteps == null || this.walkSteps.size() <= 1) && 
            this.distance == 0) {
            retVal = true;
        }
        return retVal;
    }
    
    /** 
     * The leg's duration in milliseconds
     */
    @XmlElement
    @JsonSerialize
    public long getDuration() {
        return endTime.getTimeInMillis() - startTime.getTimeInMillis();
    }

    public void addAlert(Alert alert) {
        if (notes == null) {
            notes = new ArrayList<Note>();
        }
        if (alerts == null) {
            alerts = new ArrayList<Alert>();
        }
        String text = alert.alertHeaderText.getSomeTranslation();
        if (text == null) {
            text = alert.alertDescriptionText.getSomeTranslation();
        }
        if (text == null) {
            text = alert.alertUrl.getSomeTranslation();
        }
        Note note = new Note(text);
        if (!notes.contains(note)) {
            notes.add(note);
        }
        if (!alerts.contains(alert)) {
            alerts.add(alert);
        }
    }

    public void setTimeZone(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(startTime.getTime());
        startTime = calendar;
        calendar = Calendar.getInstance(timeZone);
        calendar.setTime(endTime.getTime());
        endTime = calendar;
        agencyTimeZoneOffset = timeZone.getOffset(startTime.getTimeInMillis());
    }
}
