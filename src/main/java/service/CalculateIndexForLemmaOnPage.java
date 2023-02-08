package service;

import java.io.IOException;
import java.util.HashMap;


public class CalculateIndexForLemmaOnPage {

 private static DBMethods dbMethods = SpringContext.getBean(DBMethods.class);

 public CalculateIndexForLemmaOnPage () {

 }

    public static  String getIndexes (String titleText, String bodyText, int pageId, int siteId) throws IOException {

        float[] weight = {dbMethods.getWeightByName("title"), dbMethods.getWeightByName("body")};
        HashMap<String, Integer> lemmasTitle = new Morphology().getLemmas(titleText);
        HashMap<String, Integer> lemmasBody = new Morphology().getLemmas(bodyText);
        StringBuilder insertQuery = new StringBuilder();
        float rank = 0;
        for (String lemma : lemmasBody.keySet()) {
            if (lemmasTitle.get(lemma) == null) {
                rank = lemmasBody.get(lemma) * weight[1];
            } else {
                rank = (lemmasBody.get(lemma) * weight[1]) + (lemmasTitle.get(lemma) * weight[0]);
                lemmasTitle.remove(lemma);
            }
            insertQuery.append((insertQuery.length() == 0 ? "" : ",") + "('" + dbMethods.getLemmaIdByUK(lemma, siteId).getId()
                    + "', '" + pageId + "', '" + rank + "')");
        }
        for (String lemma : lemmasTitle.keySet()) {
            rank = lemmasTitle.get(lemma) * weight[0];
            insertQuery.append((insertQuery.length() == 0 ? "" : ",") + "('" + dbMethods.getLemmaIdByUK(lemma, siteId).getId()
                    + "', '" + pageId + "', '" + rank + "')");
        }
        return insertQuery.toString();
    }

}
