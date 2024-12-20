package com.xyz.urlshortener.repositories;

import com.xyz.urlshortener.entities.Url;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Url url WHERE url.expiresAt < :now")
    void deleteExpiredUrls(LocalDateTime now);
}
