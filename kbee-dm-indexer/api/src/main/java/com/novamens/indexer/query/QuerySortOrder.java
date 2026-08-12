package com.novamens.indexer.query;


public enum QuerySortOrder {
    RELEVANCE("relevance_desc","score, title_sort", false),
    ALPHA_ASC("alpha_asc","title_sort",true),
    ALPA_DESC("alpha_desc", "title_sort", false);

    private final String str;
    private final String solrSortFields;
    private final boolean solrSortAscending;

    private QuerySortOrder(String id, String solrSortFields, boolean solrSortAscending) {
        this.str = id;
        this.solrSortFields = solrSortFields;
        this.solrSortAscending = solrSortAscending;
    }

    public String getSolrSortFields() {
        return solrSortFields;
    }

    public boolean isSolrSortAscending() {
        return solrSortAscending;
    }

    public String getStr() {
        return str;
    }

    public static QuerySortOrder fromId(String id, QuerySortOrder def){
        QuerySortOrder querySortOrder = def;
        if (QuerySortOrder.ALPHA_ASC.getStr().equals(id)) {
            querySortOrder = QuerySortOrder.ALPHA_ASC;
        } else if (QuerySortOrder.ALPA_DESC.getStr().equals(id)) {
            querySortOrder = QuerySortOrder.ALPA_DESC;
        }else if (QuerySortOrder.RELEVANCE.getStr().equals(id)) {
            querySortOrder = QuerySortOrder.RELEVANCE;
        }
        return querySortOrder;
    }
}
