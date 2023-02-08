package repositories;

import model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    @Transactional
    @Query("SELECT s FROM Site s WHERE s.url = :url")
    Site findSiteByUrl(String url);

    }
