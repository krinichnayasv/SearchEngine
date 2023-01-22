package repositories;

import model.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

        @Query("SELECT ind FROM Index ind WHERE ind.pageId = :pageId")
        List<Index> findByPageId(int pageId);

}
