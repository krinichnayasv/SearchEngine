package service;

import model.Index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalculateIndexForLemmaOnPage {

 private static DBMethods dbMethods = SpringContext.getBean(DBMethods.class);
 private static String titleText;
 private static String bodyText;
 private static int page_id;


 public CalculateIndexForLemmaOnPage () {

 }



 public static  List<Index> getIndexForLemmas (String titleText, String bodyText, int page_id, int siteId)
         throws IOException {

     float[] WEIGHT = {dbMethods.getWeightByName("title"), dbMethods.getWeightByName("body")};
    HashMap<String, Integer> lemmasTitle = new Morphology().getLemmas(titleText);
    HashMap<String, Integer> lemmasBody = new Morphology().getLemmas(bodyText);
    List<Index> indexList = new ArrayList<>();
    float rank = 0;
    for(String lemma : lemmasBody.keySet()) {
        if (lemmasTitle.get(lemma) == null) {
           rank = (lemmasBody.get(lemma) * WEIGHT[1]);
         } else {
             rank = (lemmasBody.get(lemma) * WEIGHT[1]) + (lemmasTitle.get(lemma) * WEIGHT[0]);
             lemmasTitle.remove(lemma);
         }
         indexList.add(new Index(page_id, dbMethods.getLemmaIdByUK(lemma, siteId).getId(), rank));
      }
     for(String lemma : lemmasTitle.keySet()) {
         indexList.add(new Index(page_id, dbMethods.getLemmaIdByUK(lemma, siteId).getId(), lemmasTitle.get(lemma) * WEIGHT[0]));
     }
      return indexList;
    }


    public static  String getIndexes (String titleText, String bodyText, int pageId, int siteId) throws IOException {

        float[] WEIGHT = {dbMethods.getWeightByName("title"), dbMethods.getWeightByName("body")};
        HashMap<String, Integer> lemmasTitle = new Morphology().getLemmas(titleText);
        HashMap<String, Integer> lemmasBody = new Morphology().getLemmas(bodyText);
        StringBuilder insertQuery = new StringBuilder();
        float rank = 0;
        for(String lemma : lemmasBody.keySet()) {
            if (lemmasTitle.get(lemma) == null) {
                rank = lemmasBody.get(lemma) * WEIGHT[1];
            } else {
                rank = (lemmasBody.get(lemma) * WEIGHT[1]) + (lemmasTitle.get(lemma) * WEIGHT[0]);
                lemmasTitle.remove(lemma);
            }
        insertQuery.append((insertQuery.length()==0 ? "" : ",") + "('" + dbMethods.getLemmaIdByUK(lemma, siteId).getId()
                                                                    + "', '" + pageId + "', '" + rank + "')");
            }
        for(String lemma : lemmasTitle.keySet()) {
            rank = lemmasTitle.get(lemma) * WEIGHT[0];
            insertQuery.append((insertQuery.length()==0 ? "" : ",") + "('" + dbMethods.getLemmaIdByUK(lemma, siteId).getId()
                    + "', '" + pageId + "', '" + rank + "')");
        }
        return insertQuery.toString();
    }

}
