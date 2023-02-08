package repositories;

import model.Page;
import model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query("SELECT p FROM Page p WHERE p.site = :site")
    List<Page> findPagesBySite(Site site);

    @Query("SELECT p FROM Page p WHERE p.path = :path")
    List<Page> findPageByPath(String path);

    @Query("SELECT count(p) FROM Page p")
    long findPagesCount();

    @Query("SELECT count(p) FROM Page p WHERE p.site = :site")
    long findPagesCountBySite(Site site);

    @Query("SELECT p FROM Page p WHERE p.id = :id")
    Page findPageById(int id);

}
