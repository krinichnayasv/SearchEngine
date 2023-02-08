package main.dto;

import service.ParseSiteOrPage;
import service.RelevancePage;
import service.SearchByQuery;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DtoResultSearch {

    private boolean result;
    private long count;
    private List<RelevancePage> data;
    private String message;
    private SearchByQuery searchByQuery;


    public DtoResultSearch (String site, String query) throws SQLException, IOException, ExecutionException, InterruptedException {
        this.searchByQuery = ParseSiteOrPage.searchByQuery(site, query);
        this.result = true;
        this.data = searchByQuery.getResultOfSearch();
        this.count = data.size();
        this.message = searchByQuery.getNewQuery() == null ? "Result: success"
                : "Запрос не дал результатов. Результативный запрос: \n" + searchByQuery.getNewQuery().toString();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<RelevancePage> getData() {
        return data;
    }

    public void setData(List<RelevancePage> data) {
        this.data = data;
    }

}