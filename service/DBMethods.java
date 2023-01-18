package service;

import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
@Transactional
public class DBMethods {

    private final FieldRepository fieldRepository;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;


    @Autowired
    public DBMethods(FieldRepository fieldRepository, SiteRepository siteRepository,
                     PageRepository pageRepository, LemmaRepository lemmaRepository,
                     IndexRepository indexRepository) {
        this.fieldRepository = fieldRepository;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }


    public  float getWeightByName(String name) {
        return fieldRepository.findByName(name).getWeight();
    }

    public  void insertIntoSite(Site site) {
        siteRepository.save(site);
    }

    public int getSitesCount() {
        Iterable<Site> siteIterable = siteRepository.findAll();
        ArrayList<Site> sitesAll = new ArrayList<>();
        for(Site site : siteIterable) {
            sitesAll.add(site);
        }
        return sitesAll.size();
    }

    public List<Site> getAllSites() {
        Iterable<Site> siteIterable = siteRepository.findAll();
        ArrayList<Site> sitesAll = new ArrayList<>();
        for(Site site : siteIterable) {
            sitesAll.add(site);
        }
        return sitesAll;
    }

    public Site getSiteByUrl (String url) {
        return siteRepository.findSiteByUrl(url);
    }

    public boolean ifSiteExist(String url) {
        boolean result = false;
        Site site = siteRepository.findSiteByUrl(url);
        if (site != null) {
            result = true;
        }
        return result;
    }

    //   @Transactional
    public Site getSiteById (int id) {
        return siteRepository.getById(id);
    }


    public void deleteSite (int id) {
        siteRepository.deleteById(id);
    }

    public void deleteAllSites () {
        siteRepository.deleteAll();
    }

//    public void updateSiteOnIndexing(long statusTime, SiteStatus status, String lastError, int id){
//        siteRepository.updateSiteOnIndexing(statusTime, status, lastError, id);
//    }

    public Site updateSiteOnIndexing(Site site) {
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(System.currentTimeMillis());
        site.setLastError("");
        siteRepository.saveAndFlush(site);
        return site;
    }


    public  void insertIntoPage(Page page) {
        pageRepository.saveAndFlush(page);
    }

    public Page getPageByPath(String path, int siteId) {
        Page existPage = null;
        List<Page> pages = pageRepository.findPageByPath(path);
        for (Page page : pages) {
            if (page.getSite().getId() == siteId) {
                existPage = page;
            }
        }
        return  existPage;
    }

    public boolean ifPageExist(String path, int siteId) {
        boolean result = false;
        List<Page> pages = pageRepository.findPageByPath(path);
        for (Page page : pages) {
            if (page.getSite().getId() == siteId) {
                result = true;
            } else { }
        }
        return result;
    }

    public void deletePage(int id) {
        pageRepository.deleteById(id);
    }

    public long getPagesCount() {
        Iterable<Page> pageIterable = pageRepository.findAll();
        ArrayList<Page> pagesAll = new ArrayList<>();
        for(Page page : pageIterable) {
            pagesAll.add(page);
        }
        return pagesAll.size();
    }

    public long findPagesCount() {
        return pageRepository.findPagesCount();
    }

    public  int getStatusOfPage(int id) {
        return pageRepository.getById(id).getCode();
    }

    public  String getContentOfPage(int id) {
        return pageRepository.getById(id).getContent();
    }

    public  List<Page> getPagesBySite(Site site) {
        return pageRepository.findPagesBySite(site);
    }

    //  @Transactional
    public Page getPageById(int id) {
        return pageRepository.findById(id).get();
    }

    public  long getCountPagesBySite(Site site) {
        return pageRepository.findPagesCountBySite(site);
    }


    public  int getFrequencyLemmaByLemma(String lemma, int siteId) {
        int freq=0;
        List<Lemma> lemmas = lemmaRepository.findLemmasByLemma(lemma);
        for(Lemma lem : lemmas) {
            if(lem.getSite().getId() == siteId) {
                freq = lem.getFrequency();
            }
        }
        return freq;
    }

    public long getLemmasCount() {
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        ArrayList<Lemma> lemmasAll = new ArrayList<>();
        for(Lemma lemma : lemmaIterable) {
            lemmasAll.add(lemma);
        }
        return lemmasAll.size();
    }

    public long getLemmasCountBySite(Site site) {
        return lemmaRepository.findLemmasCountBySite(site);
    }

    public long findLemmasCount() {
        return lemmaRepository.findLemmasCount();
    }

    public List<Integer> getLemmasIdListByPage(Page page) {
        List<Index> indexList = indexRepository.findByPageId(page.getId());
        List<Integer> lemmasIdList = new ArrayList<>();
        for (Index index : indexList) {
            lemmasIdList.add(index.getLemmaId());
        }
        return lemmasIdList;
    }


    public  void insertIntoIndexAll(List<Index> indexList) {
        indexRepository.saveAll(indexList);
    }

    public boolean isIndexExistByPage(int pageId) {
        if(indexRepository.findByPageId(pageId) == null) {
            return false;
        } else {return true;}

    }

    //  @Transactional
    public void updateLemmaSetLessFrequency(List<Integer> lemmasIdList) {
        List<Lemma> lemmas = new ArrayList<>();
        for (Integer lemmaId : lemmasIdList) {
            Lemma lemma = lemmaRepository.getById(lemmaId);
            lemma.setFrequency(lemma.getFrequency() - 1);
            if(lemma.getFrequency() == 0) {
                lemmaRepository.deleteById(lemmaId);
            } else {
                lemmas.add(lemma);
            }
        }
        lemmaRepository.saveAllAndFlush(lemmas);
    }


    public long getCountLemmasBySiteId(Site site) {
        return  lemmaRepository.findLemmasBySite(site).size();
    }

    public Lemma getLemmaIdByUK(String lemma, int siteId) {
        Lemma existLemma = null;
        List<Lemma> lemmas = lemmaRepository.findLemmasByLemma(lemma);
        for (Lemma lem : lemmas) {
            if (lem.getSite().getId() == siteId) {
                existLemma = lem;
            } else { }
        }
        return existLemma;
    }

    public synchronized void insertOrUpdateIntoLemmaAll(HashMap<String, Integer> lemmas, Site site) {
        List<Lemma> lemmasList = new ArrayList<>();
        for(String lemma : lemmas.keySet() ) {
            List<Lemma> findLemmas = lemmaRepository.findLemmasByLemma(lemma);
            if (getLemmaIdByUK(lemma, site.getId()) != null) {
                Lemma existLemma = getLemmaIdByUK(lemma, site.getId());
                existLemma.setFrequency(getLemmaIdByUK(lemma, site.getId()).getFrequency() + 1);
                lemmaRepository.saveAndFlush(existLemma);
            }  else {
                lemmasList.add(new Lemma(lemma, 1, site));
            }
        }
        lemmaRepository.saveAllAndFlush(lemmasList);
    }

    //   @Transactional
    public synchronized void insertOrUpdateIntoLemmaAllJDBC(HashMap<String, Integer> lemmas, int siteId)
            throws SQLException {
        StringBuilder insertQuery = new StringBuilder();
        for(String lemma : lemmas.keySet() ) {
            insertQuery.append((insertQuery.length()==0 ? "" : ",") + "('" + lemma + "', '" + siteId + "', 1)");
        }
        DBConnection.executeMultiInsertIntoLemma(insertQuery.toString());
    }

    //   @Transactional
    public void insertIntoIndexJDBC(String query) throws SQLException {
        DBConnection.executeMultiInsertIntoIndex(query);
    }


}

