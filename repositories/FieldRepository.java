package repositories;

import model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {

    @Query("SELECT f.weight FROM Field f WHERE f.name = :name")
    float findByNameReturnWeight(String name);

    Field findByName(String name);
}
