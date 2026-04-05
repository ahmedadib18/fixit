package com.fixit.fixit.repository;

import com.fixit.fixit.entity.HelperCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HelperCategoryRepository extends JpaRepository<HelperCategory, Long> {

    List<HelperCategory> findByHelperId(Long helperId);

    void deleteByHelperIdAndIdNotIn(Long helperId, List<Long> ids);
}