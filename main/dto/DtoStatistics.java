package main.dto;

import service.ParseSiteOrPage;

public class DtoStatistics {

    private boolean result;
    private DtoAllStatistics statistics;

    public DtoStatistics () {
        this.result = true;
        this.statistics = new DtoAllStatistics();
    }


    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public DtoAllStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(DtoAllStatistics statistics) {
        this.statistics = statistics;
    }


}
