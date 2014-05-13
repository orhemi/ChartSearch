package org.openmrs.module.chartsearch.synonyms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

/**
 * Created by Eli on 21/04/14.
 */
public class SynonymGroups {
    private int counter;
    private Vector<SynonymGroup> synonymGroupsHolder;
    private static SynonymGroups instance;

    private SynonymGroups() {

        counter = 0;
        synonymGroupsHolder = new Vector<SynonymGroup>();
    }

    public static SynonymGroups getInstance() {
        if (instance == null) {
            instance = new SynonymGroups();
        }
        return instance;
    }

    public int getCounter() {
        return counter;
    }

    public int getNumberOfGroups() {
        return synonymGroupsHolder.size();
    }

    public SynonymGroup isSynonymContainedInGroup(String syn) {

        System.out.println("Entering check if syn '"+ syn +"' is contained in other group");

        for (SynonymGroup grp : synonymGroupsHolder) {

            System.out.println("Checking if contained in group: "+ grp.toString());

            HashSet<String> synSet = grp.getSynonyms();
            for(String synInGrp : synSet){


                if(synInGrp.equals(syn)){
                    System.out.println("phrase '"+syn+"' is contained in a group: "+grp.getGroupName());
                    return grp;
                }
            }
        }
        System.out.println("phrase '"+ syn + "' is NOT contained in a group");
        return null;
    }

    public SynonymGroup isSynFromGroupContainedInOtherGroup(SynonymGroup checkGroup) {
        for (SynonymGroup grp : synonymGroupsHolder) {
            HashSet<String> intersection = new HashSet<String>((Collection<? extends String>) checkGroup.getSynonyms()); // use the copy constructor
            intersection.retainAll((Collection<?>) grp.getSynonyms());
            if (!intersection.isEmpty()) {
                System.out.println("Synonym group is contained in another group: " + grp.getGroupName());
                return grp;
            }
        }
        System.out.println("Synonym group is NOT contained in another group");
        return null;
    }

    public void addSynonymGroup(SynonymGroup newGroup) {
        if (isSynFromGroupContainedInOtherGroup(newGroup) == null) {
            synonymGroupsHolder.add(newGroup);
            counter++;
        }
        System.out.println("Synonym group added to Group Holder");
    }

    public void mergeSynonymGroups(SynonymGroup grpToMerge) {
        SynonymGroup grp = isSynFromGroupContainedInOtherGroup(grpToMerge);
        if (!grp.equals(null)) {
            grp.merge(grpToMerge);

        }
    }

    public SynonymGroup getSynonymGroupByName(String name) {
        for (SynonymGroup grp : synonymGroupsHolder) {
            if (grp.getGroupName().equals(name)) {
                return grp;
            }
        }
        return null;
    }

    public SynonymGroup getSynonymGroupBySynonym(String syn) {
        for (SynonymGroup grp : synonymGroupsHolder) {
            if (grp.getSynonyms().contains(syn)) {
                return grp;
            }
        }
        return null;
    }

    public boolean deleteSynonymGroupByName(String name) {
        for (SynonymGroup grp : synonymGroupsHolder) {
            if (grp.getGroupName().equals(name)) {
                synonymGroupsHolder.remove(grp);

                return true;
            }
        }
        return false;
    }

    public Vector<SynonymGroup> getSynonymGroupsHolder() {
        return synonymGroupsHolder;
    }

    @Override
    public String toString() {
        {
            String str = "";
            for (SynonymGroup grp : synonymGroupsHolder) {
                str += grp.toString() + '\n';
            }
            return str;
        }

    }
}
