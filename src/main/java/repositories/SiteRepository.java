package repositories;

import model.Site;
import model.SiteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    @Transactional
    @Query("SELECT s FROM Site s WHERE s.url = :url")
    Site findSiteByUrl(String url);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE Site set statusTime = ?1, status = ?2, lastError = ?3 WHERE id = ?4")
    int updateSiteOnIndexing(long statusTime, SiteStatus status, String lastError, int id);


    }
