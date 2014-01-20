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

package org.opentripplanner.updater;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.opentripplanner.routing.patch.Alert;
import org.opentripplanner.routing.patch.AlertPatch;
import org.opentripplanner.routing.patch.TimePeriod;
import org.opentripplanner.routing.patch.TranslatedString;
import org.opentripplanner.routing.services.PatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;

/**  
 * This presently only includes GTFS-Realtime Service Alert feeds; 
 * we hope to eventually include Trip Updates as well. 
 * @author novalis
 *
 */
public class UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(UpdateHandler.class);

    private String defaultAgencyId;

    private Set<String> patchIds = new HashSet<String>();

    private PatchService patchService;

    /** How long before the posted start of an event it should be displayed to users */
    private long earlyStart;

    public UpdateHandler() {
    }

    public void update(FeedMessage message) {
        patchService.expire(patchIds);
        patchIds.clear();

        for (FeedEntity entity : message.getEntityList()) {
            if (!entity.hasAlert()) {
                continue;
            }
            GtfsRealtime.Alert alert = entity.getAlert();
            String id = entity.getId();
            handleAlert(id, alert);
        }
    }

    private void handleAlert(String id, GtfsRealtime.Alert alert) {
        Alert alertText = new Alert();
        alertText.alertDescriptionText = deBuffer(alert.getDescriptionText());
        alertText.alertHeaderText = deBuffer(alert.getHeaderText());
        alertText.alertUrl = deBuffer(alert.getUrl());
        ArrayList<TimePeriod> periods        = new ArrayList<TimePeriod>();
        ArrayList<TimePeriod> displayPeriods = new ArrayList<TimePeriod>();
        long bestStartTime = Long.MAX_VALUE;
        for (TimeRange activePeriod : alert.getActivePeriodList()) {
            final long start = activePeriod.hasStart() ? activePeriod.getStart() - earlyStart : 0;
            final long realStart = activePeriod.hasStart() ? activePeriod.getStart() : 0;
            if (realStart > 0 && realStart < bestStartTime) {
                bestStartTime = realStart;
            }
            final long end = activePeriod.hasEnd() ? activePeriod.getEnd() : Long.MAX_VALUE;
            periods.add(new TimePeriod(realStart, end));
            if(earlyStart > 0 && start != realStart)
                displayPeriods.add(new TimePeriod(start, realStart));
        }
        if (bestStartTime != Long.MAX_VALUE) {
            alertText.effectiveStartDate = new Date(bestStartTime * 1000);
        }
        for (EntitySelector informed : alert.getInformedEntityList()) {
            String patchId = createId(id, informed);

            String routeId = null;
            if (informed.hasRouteId()) {
                routeId = informed.getRouteId();
            }
            // TODO: The other elements of a TripDescriptor are ignored...
            String tripId = null;
            if (informed.hasTrip() && informed.getTrip().hasTripId()) {
                tripId = informed.getTrip().getTripId();
            }
            String stopId = null;
            if (informed.hasStopId()) {
                stopId = informed.getStopId();
            }

            String agencyId = informed.getAgencyId();
            if (informed.hasAgencyId()) {
                agencyId = informed.getAgencyId().intern();
            } else {
                agencyId = defaultAgencyId;
            }
            if (agencyId == null) {
                log.error("Empty agency id (and no default set) in feed; other ids are route "
                        + routeId + " and stop " + stopId);
                continue;
            }

            AlertPatch patch = new AlertPatch();
            if (routeId != null) {
                patch.setRoute(new AgencyAndId(agencyId, routeId));
            }
            if (tripId != null) {
                patch.setTrip(new AgencyAndId(agencyId, tripId));
            }
            if (stopId != null) {
                patch.setStop(new AgencyAndId(agencyId, stopId));
            }
            if(agencyId != null && routeId == null && tripId == null && stopId == null) {
                patch.setAgencyId(agencyId);
            }
            patch.setCancelled(alert.getEffect() == GtfsRealtime.Alert.Effect.NO_SERVICE);
            patch.setTimePeriods(periods);
            patch.setDisplayTimePeriods(displayPeriods);
            patch.setAlert(alertText);

            patch.setId(patchId);
            patchIds.add(patchId);

            patchService.apply(patch);
        }
    }

    private String createId(String id, EntitySelector informed) {
        return id + " "
            + (informed.hasAgencyId  () ? informed.getAgencyId  () : " null ") + " "
            + (informed.hasRouteId   () ? informed.getRouteId   () : " null ") + " "
            + (informed.hasRouteType () ? informed.getRouteType () : " null ") + " "
            + (informed.hasStopId    () ? informed.getStopId    () : " null ") + " "
            + (informed.hasTrip() ? informed.getTrip().getTripId() : " null ");
    }

    /**
     * convert a protobuf TranslatedString to a OTP TranslatedString
     * 
     * @return
     */
    private TranslatedString deBuffer(GtfsRealtime.TranslatedString buffered) {
        TranslatedString result = new TranslatedString();
        for (Translation translation : buffered.getTranslationList()) {
            String language = translation.getLanguage();
            String string = translation.getText();
            result.addTranslation(language, string);
        }
        return result;
    }

    public void setDefaultAgencyId(String defaultAgencyId) {
        if(defaultAgencyId != null)
            this.defaultAgencyId = defaultAgencyId.intern();
    }

    public void setPatchService(PatchService patchService) {
        this.patchService = patchService;
    }

    public long getEarlyStart() {
        return earlyStart;
    }

    public void setEarlyStart(long earlyStart) {
        this.earlyStart = earlyStart;
    }

}
