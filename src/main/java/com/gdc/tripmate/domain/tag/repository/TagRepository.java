package com.gdc.tripmate.domain.tag.repository;

import com.gdc.tripmate.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT t FROM Tag t WHERE t.category = :category")
    List<Tag> findAllByCategory(String category);
    
    @Query("SELECT t FROM Tag t JOIN UserTag ut ON t.id = ut.tag.id GROUP BY t.id ORDER BY COUNT(ut.id) DESC LIMIT 50")
    List<Tag> findMostPopularTags();
}