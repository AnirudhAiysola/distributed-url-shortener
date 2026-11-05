package com.kyo.url_shortener.url;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;

@Entity
@Table(name = "urls")
@Getter                                             // getters for all fields
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA needs it; our code shouldn't use it
public class ShortUrl implements Persistable<String> {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "long_url", nullable = false)
    private String longUrl;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    @Getter(AccessLevel.NONE) // we write isNew() ourselves below
    private boolean isNew = true;

    public ShortUrl(String code, String longUrl, Instant expiresAt, Instant createdAt) {
        this.code = code;
        this.longUrl = longUrl;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public boolean isExpired(Instant now) {
        return (expiresAt != null && !now.isBefore(expiresAt));
    }

    @Override
    public String getId() {
        return code;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}