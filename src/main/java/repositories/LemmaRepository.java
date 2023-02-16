package repositories;

import model.Lemma;
import model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    List<Lemma> findLemmasByLemma(String lemma);

    @Query("SELECT l FROM Lemma l WHERE l.site = :site AND l.lemma = :lemma")
    List<Lemma> findLemmaBySiteAndLemma(Site site, String lemma);

    @Query("SELECT count(l) FROM Lemma l")
    long findLemmasCount();

    @Query("SELECT count(l) FROM Lemma l WHERE l.site = :site")
    long findLemmasCountBySite(Site site);

}
