package kbee.api.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ApiSearch implements Serializable {
    private static final long serialVersionUID = 1L;

    private String site;
    private Integer page = 0;
    private Integer pageSize = 25;
    private Boolean facets = true;
    private String sort;
    private INode node;
    private Map<String, List<String>> filters;
    
    
    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
    
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }


    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getFacets() {
        return facets;
    }

    public void setFacets(Boolean fecets) {
        this.facets = fecets;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
    
    public INode getNode() {
        return node;
    }

    public void setNode(INode node) {
        this.node = node;
    }


    public Map<String, List<String>> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, List<String>> filters) {
        this.filters = filters;
    }
}