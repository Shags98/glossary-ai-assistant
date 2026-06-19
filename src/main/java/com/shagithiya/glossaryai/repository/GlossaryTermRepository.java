package com.shagithiya.glossaryai.repository;

import com.shagithiya.glossaryai.model.GlossaryTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GlossaryTermRepository extends JpaRepository<GlossaryTerm, Long> {

    Optional<GlossaryTerm> findByEnglishTermIgnoreCase(String englishTerm);

    List<GlossaryTerm> findAllByOrderByCreatedAtDesc();
}
