package main.dto;

import service.GetBeans;
import model.Site;
import service.DBMethods;
import service.ParseSiteOrPage;


import java.util.ArrayList;
import java.util.List;

public class DtoAllStatistics {

    private DBMethods dbMethods = new GetBeans().getDbMethods();
    private DtoTotalStatistic total;
    private List<DtoSiteDetailed> detailed;


    public DtoAllStatistics() {
        this.total = new DtoTotalStatistic(dbMethods.getSitesCount(),dbMethods.findPagesCount(),
                dbMethods.findLemmasCount(), ParseSiteOrPage.getIndexing());
        this.detailed = getDetailed();

    }

    public List<DtoSiteDetailed> getDetailed() {
        List<DtoSiteDetailed> list = new ArrayList<>();
        List<Site> sitesFromDB = dbMethods.getAllSites();
        for (Site site : sitesFromDB) {
           list.add(new DtoSiteDetailed(site));
        }
        return list;
    }


    public DtoTotalStatistic getTotal() {
        return total;
    }

    public void setTotal(DtoTotalStatistic total) {
        this.total = total;
    }

    public void setDetailed(List<DtoSiteDetailed> detailed) {
        this.detailed = detailed;
    }


}
