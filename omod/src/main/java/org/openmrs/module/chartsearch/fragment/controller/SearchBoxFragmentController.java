package org.openmrs.module.chartsearch.fragment.controller;





import org.openmrs.api.context.Context;
import org.openmrs.module.chartsearch.ChartListItem;
import org.openmrs.module.chartsearch.ObsItem;
import org.openmrs.module.chartsearch.SearchAPI;
import org.openmrs.module.chartsearch.SearchPhrase;
import org.openmrs.module.chartsearch.solr.ChartSearchSearcher;
import org.openmrs.module.chartsearch.synonyms.SynonymGroup;
import org.openmrs.module.chartsearch.synonyms.SynonymGroups;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchBoxFragmentController {
    private ChartSearchSearcher searcher = getComponent(ChartSearchSearcher.class);

	public void get() {
	}






	public String post(PageModel model, @BindParams SearchPhrase searchPhrase ,@RequestParam("patientId") Integer patient) {

        SearchAPI searchAPI =SearchAPI.getInstance();
        model.addAttribute("patientID_from_get", patient);

        Integer length = Integer.valueOf(999999999);
        Integer start = Integer.valueOf(0);

        List<ChartListItem> items = new ArrayList<ChartListItem>();

        String synonyms=searchPhrase.getPhrase();
        SynonymGroup synGroup = SynonymGroups.isSynonymContainedInGroup(searchPhrase.getPhrase());
        if(!synGroup.equals(null)){
           for(String syn : (HashSet<String>)synGroup.getSynonyms()){
                synonyms+=" OR "+syn;
            }
        }

        try {
            items = searcher.getDocumentList(patient, synonyms, start, length); //the actual search
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(items == null){
           items = new ArrayList<ChartListItem>();
           ObsItem itemsIsNull = new ObsItem();
            itemsIsNull.setConceptName("items list returned from search is null");
            items.add(itemsIsNull);

        }

        searchAPI.setResults(items);


		return "redirect:chartsearch/chartsearch.page?patientId=" + patient;
	}


    private <T> T getComponent(Class<T> clazz) {
        List<T> list = Context.getRegisteredComponents(clazz);
        if (list == null || list.size() == 0)
            throw new RuntimeException("Cannot find component of " + clazz);
        return list.get(0);
    }





}