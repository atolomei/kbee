package com.novamens.solr.sync;

import org.apache.solr.common.util.NamedList;

import java.util.*;

public class SolrConfigDifHelper {

    public static List<Map<String, Object>> getAddedEntriesMatchingKey(String key, List<Map<String, Object>> entriesOrg, List<Map<String, Object>> entriesDst) {
        List<Map<String, Object>> addedEntries = new ArrayList<>();
        for(Map<String, Object> orgMap: entriesOrg){
            Object orgKeyValue = orgMap.get(key);
            assert orgKeyValue!= null;

            Map<String, Object> dstMap = entriesDst.stream()
                    .filter(dst -> orgKeyValue.equals(dst.get(key)))
                    .findAny().orElse(null);
            if( dstMap == null){
                addedEntries.add(orgMap);
            }
        }
        return addedEntries;
    }

    public static List<Map<String, Object>> getAddedEntriesMatchingFull(List<Map<String, Object>> entriesOrg, List<Map<String, Object>> entriesDst) {
        List<Map<String, Object>> addedEntries = new ArrayList<>();
        for(Map<String, Object> orgMap: entriesOrg){
            boolean hasMatching = false;
            for (Map<String, Object> dstMap : entriesDst) {
                if(equalsEntryValues(orgMap, dstMap)){
                    hasMatching = true;
                    break;
                }
            }
            if(!hasMatching)
                addedEntries.add(orgMap);

        }
        return addedEntries;
    }



    public static List<Map<String, Object>> getModifiedOrgEntries(String key, List<Map<String, Object>> entriesOrg, List<Map<String, Object>> entriesDst) {
        List<Map<String, Object>> modifiedEntries = new ArrayList<>();
        for(Map<String, Object> orgMap: entriesOrg){
            Object orgKeyValue = orgMap.get(key);
            assert orgKeyValue!= null;

            Map<String, Object> dstMap = entriesDst.stream()
                    .filter(dst -> orgKeyValue.equals(dst.get(key)))
                    .findAny().orElse(null);

            if( dstMap != null && !equalsEntryValues(orgMap,dstMap)){
                modifiedEntries.add(orgMap);
            }
        }
        return modifiedEntries;
    }


    public static boolean equalsEntryValues(Object orgValue, Object dstValue) {
        if( orgValue == null && dstValue == null)
            return true;
        else if (orgValue == null ||  dstValue == null)
            return false;
        else if(orgValue instanceof Map) {
            if (!(dstValue instanceof Map))
                return false;
            else
                return equalsEntriesMap((Map) orgValue, (Map) dstValue);
        }else if(orgValue instanceof List){
            if( !(dstValue instanceof List) )
                return false;
            return equalsEntriesList((List)orgValue,(List)dstValue);
        }else
            return orgValue.equals(dstValue);
    }





    private static boolean equalsEntriesMap(Map<String, Object> entryOrg, Map<String, Object> entryDst) {
        boolean equals = true;

        if( entryOrg == null && entryDst == null)
            return true;
        else if (entryOrg == null ||  entryDst == null)
            return false;

        if(entryOrg.size() != entryDst.size())
            return false;

        for (Map.Entry<String, Object> orgEntry : entryOrg.entrySet()) {
            Object orgValue = orgEntry.getValue();
            Object dstValue = entryDst.get(orgEntry.getKey());

            equals &= equalsEntryValues(orgValue, dstValue);
            if(!equals)
                break;
        }
        return equals;
    }

    private static boolean equalsEntriesList(List orgValue, List dstValue) {
        if( orgValue == null &&   dstValue == null)
            return true;
        else if (orgValue == null || dstValue == null)
            return false;

        if(orgValue.size() != dstValue.size())
            return false;



        Iterator orgIt = orgValue.iterator();
        Iterator dstIt = dstValue.iterator();

        boolean equals = true;
        while(equals && orgIt.hasNext()){
            Object tmpOrgItm = orgIt.next();
            Object dstOrgItm = dstIt.next();

            equals &= equalsEntryValues(tmpOrgItm, dstOrgItm);
        }

        return equals;
    }

    public static String getEntryDesc(int leftPads, Object entry){

        String leftPad = String.join("", Collections.nCopies(leftPads, "\t"));
        return getEntryDesc(leftPad, entry).toString();
    }

    private static StringBuilder getEntryDesc(String tabs, Object entry){
        StringBuilder sb = new StringBuilder();
        if( entry == null)
            sb.append("NULL");
        else if(entry instanceof Map) {
            sb.append("\n").append(tabs).append("{");
            Set<Map.Entry<?, ?>> entrySet = ((Map) entry).entrySet();
            boolean first = true;

            for (Map.Entry<?, ?> entrySetVal : entrySet) {
                if(!first)
                    sb.append(", ");
                else
                    first = false;
                sb.append("\n").append(tabs+"\t");
                sb.append("\"").append(entrySetVal.getKey()).append("\": ").
                append(getEntryDesc(tabs+"\t",entrySetVal.getValue()));

            }
            sb.append("\n").append(tabs).append("}");


        }else if(entry instanceof List){
            sb.append("\n").append(tabs).append("[");
            boolean first = true;
            for (Object listEntry : ((List) entry)) {
                if(!first)
                    sb.append(", ");
                else
                    first = false;
                sb.append(getEntryDesc(tabs+"\t",listEntry));
            }
            sb.append("\n").append(tabs).append("]");
        }else if(entry instanceof String)
            sb.append("\"").append(entry.toString()).append("\"");
        else
            sb.append(entry);

        return sb;
    }

    public static NamedList toNamedList(Map<String,Object> map){
        NamedList namedList = new NamedList();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object resultValue = auxObjToNamedList(entry.getValue());
            namedList.add(entry.getKey(), resultValue);
        }
        return namedList;
    }



    private static List<Object> toNamedList(List<Object> list){
        List<Object> result = new ArrayList<>();
        for (Object value : list) {
            Object resultValue=toNamedList((Map<String, Object>) value);
            result.add(resultValue);
        }
        return result;
    }

    private static Object auxObjToNamedList(Object value) {
        Object resultValue;
        if(value instanceof Map){
            resultValue=toNamedList((Map<String, Object>) value);
        }else if(value instanceof List){
            resultValue=toNamedList((List) value);
        }else{
            resultValue= value;
        }
        return resultValue;
    }


}
