package org.openmrs.module.chartsearch;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Eli on 16/03/14.
 */

public class GeneratingJson {


    public static String generateJson() {

        JSONObject jsonToReturn = new JSONObject();                     //returning this object
        JSONArray arr_of_groups = new JSONArray();

        Set<Set<Obs>> setOfObsGroups = generateObsGroupFromSearchResults();
        for (Set<Obs> obsGrpSet : setOfObsGroups) {           //for each obs group we go through it's obs
            JSONArray arr_of_obs = new JSONArray();        //array of all the obs in a given obs group
            JSONObject jsonObs = null;
            JSONObject jsonGrp = new JSONObject();
            for (Obs obs : obsGrpSet) {                       //for each obs in a group we create the single obs and add it to the obs array
                if (obs != null) {
                    jsonObs = createJsonObservation(obs);
                }
                arr_of_obs.add(jsonObs);
            }
            int obsGrpId = -1;                                         //init the obs grp id to -1, if there is a grp in
            if (obsGrpSet.iterator().hasNext()) {                         //check inside of group an individual obs, if available, what its grp id
                Obs obsFromObsGrp = obsGrpSet.iterator().next();
                Obs obsGrp = obsFromObsGrp.getObsGroup();
                obsGrpId = obsGrp.getObsId();

                jsonGrp.put("group_Id", obsGrpId);
                jsonGrp.put("group_name", obsGrp.getConcept().getDisplayString());

                Date obsDate = obsGrp.getObsDatetime() == null ? new Date() : obsGrp.getObsDatetime();
                SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy");
                String obsDateStr = formatDateJava.format(obsDate);

                jsonGrp.put("last_taken_date", obsDateStr);
                jsonGrp.put("observations", arr_of_obs);
                arr_of_groups.add(jsonGrp);
            }
        }


        jsonToReturn.put("obs_groups", arr_of_groups); //add the obs groups array to the json


        JSONObject jsonObs = null;
        JSONArray arr_of_obs = new JSONArray();
        for (Obs obsSingle : generateObsSinglesFromSearchResults()) {
            if (obsSingle != null) {
                jsonObs = createJsonObservation(obsSingle);
            }
            arr_of_obs.add(jsonObs);
        }
        jsonToReturn.put("obs_singles", arr_of_obs);

        return jsonToReturn.toString();
    }


    private static JSONObject createJsonObservation(Obs obs) {
        JSONObject jsonObs = new JSONObject();
        jsonObs.put("observation_id", obs.getObsId());
        jsonObs.put("concept_name", obs.getConcept().getDisplayString());

        Date obsDate = obs.getObsDatetime() == null ? new Date() : obs.getObsDatetime();

        SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy");
        String obsDateStr = formatDateJava.format(obsDate);

        jsonObs.put("date", obsDateStr);

        if (obs.getConcept().getDatatype().isNumeric()) { // ADD MORE DATATYPES
            jsonObs.put("value_type", obs.getConcept().getDatatype().getName());

            ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());
            jsonObs.put("units_of_measurement", conceptNumeric.getUnits());
            jsonObs.put("absolute_high", conceptNumeric.getHiAbsolute());
            jsonObs.put("absolute_low", conceptNumeric.getLowAbsolute());
            jsonObs.put("critical_high", conceptNumeric.getHiCritical());
            jsonObs.put("critical_low", conceptNumeric.getLowCritical());
            jsonObs.put("normal_high", conceptNumeric.getHiNormal());
            jsonObs.put("normal_low", conceptNumeric.getLowNormal());

        }
        jsonObs.put("value", obs.getValueAsString(Context.getLocale()));
        jsonObs.put("location", obs.getLocation().getDisplayString());
        return jsonObs;
    }


    public static Set<Set<Obs>> generateObsGroupFromSearchResults() {
        Set<Set<Obs>> obsGroups = new HashSet<Set<Obs>>();

        SearchAPI searchAPI = SearchAPI.getInstance();
        List<ChartListItem> searchResultsList= searchAPI.getResults();
        for (ChartListItem item : searchResultsList) {               //for each item in results we classify it by its obsGroup, and add all of the group.
            if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                int itemObsId = ((ObsItem) item).getObsId();


                // System.out.println("OUTSIDE" + Context.getObsService().getObs(itemObsId).getConcept().getDisplayString());
                Obs obsGrp = Context.getObsService().getObs(itemObsId).getObsGroup();
                if (obsGrp != null) {
                    // System.out.println("INSIDE OF IF" + Context.getObsService().getObs(itemObsId).getConcept().getDisplayString());
                    int groupId = obsGrp.getId();


                    Set<Obs> obsGroup = obsGrp.getGroupMembers();

                    boolean found = false;                                      //if found == ture then we don't need to add the group.
                    for (Set<Obs> grp : obsGroups) {
                        Obs ob = new Obs(-1);

                        if (grp.iterator().hasNext()) {
                            ob = grp.iterator().next();
                        }
                        if (ob.getObsGroup() != null && ob.getObsGroup().getId() != null) {
                            if (ob.getObsGroup().getId() == groupId) {
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        obsGroups.add(obsGroup);
                    }
                }
            }
        }

        return obsGroups;
    }

    public static Set<Obs> generateObsSinglesFromSearchResults() {
        SearchAPI searchAPI = SearchAPI.getInstance();
        Set<Obs> obsSingles = new HashSet<Obs>();
        for (ChartListItem item : searchAPI.getResults()) {
            if (item != null && item instanceof ObsItem && ((ObsItem) item).getObsId() != null) {
                int itemObsId = ((ObsItem) item).getObsId();

                    Obs obs = Context.getObsService().getObs(itemObsId);
                if (obs != null && obs.getObsGroup() == null && !obs.isObsGrouping() ) {
                    obsSingles.add(Context.getObsService().getObs(itemObsId));
                }
            }


        }
        return obsSingles;
    }
}