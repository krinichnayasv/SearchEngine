package repositories;

import model.Lemma;
import model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {


    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.site = :site")
    Lemma findLemmaByUniqueKey(String lemma, Site site);

    List<Lemma> findLemmasByLemma(String lemma);

    @Query("SELECT l FROM Lemma l WHERE l.site = :site")
    List<Lemma> findLemmasBySite(Site site);

    @Modifying
    @Query(value = "INSERT INTO Lemma (id, frequency, lemma, site) VALUES(:id,:frequency,:lemma,:site)"
            , nativeQuery = true)
    int insertLemmaForNewSite(int id, int frequency, String lemma, Site site);

    @Query("SELECT count(l) FROM Lemma l")
    long findLemmasCount();

    @Query("SELECT count(l) FROM Lemma l WHERE l.site = :site")
    long findLemmasCountBySite(Site site);

}
