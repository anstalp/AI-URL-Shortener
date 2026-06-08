package com.anastasis.minilink.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Το JpaRepository μας δίνει έτοιμες μεθόδους όπως save(), findAll(), deleteById()
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {

    // Custom μέθοδος: "Βρες μου το UrlMapping αν σου δώσω το shortCode"
    // Η SQL γράφεται αυτόματα στο παρασκήνιο
    Optional<UrlMapping> findByShortCode(String shortCode);

    List<UrlMapping> getAllByClickCount(int clickCount);
}