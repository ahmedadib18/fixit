package com.fixit.fixit.repository;

import com.fixit.fixit.entity.Helper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface HelperRepository extends JpaRepository<Helper, Long> {

    Optional<Helper> findByUserId(Long userId);

    List<Helper> findByIsAvailableTrue();

    List<Helper> findByUserCityId(Long cityId);


    @Query("SELECT DISTINCT h FROM Helper h " +
            "LEFT JOIN h.categories hc " +
            "LEFT JOIN hc.category c " +
            "JOIN h.user u " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:language IS NULL OR :language = '' OR LOWER(h.languagesSpoken) LIKE LOWER(CONCAT('%', :language, '%'))) " +
            "AND (:maxPrice IS NULL OR hc.hourlyRate IS NULL OR hc.hourlyRate <= :maxPrice) " +
            "AND (:availableNow IS NULL OR :availableNow = false OR h.isAvailable = true) " +
            "AND (:cityId IS NULL OR u.city.id = :cityId)")
    List<Helper> searchHelpers(
            @Param("categoryId") Long categoryId,
            @Param("language") String language,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("availableNow") Boolean availableNow,
            @Param("cityId") Long cityId);

    @Query("SELECT h FROM Helper h " +
            "JOIN FETCH h.user u " +
            "WHERE h.id = :helperId")
    Optional<Helper> findByIdWithUser(@Param("helperId") Long helperId);
}