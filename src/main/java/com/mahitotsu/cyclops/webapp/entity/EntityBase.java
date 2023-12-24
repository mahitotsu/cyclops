package com.mahitotsu.cyclops.webapp.entity;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;

@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class })
@EqualsAndHashCode
public abstract class EntityBase {

    static private final Random RANDOM = new Random();

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private LocalDateTime createdTimestamp;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_timestamp", nullable = false)
    private LocalDateTime lastModifiedTimestamp;

    public UUID getId() {
        return this.id;
    }

    public Long getVersion() {
        return this.version;
    }

    public LocalDateTime getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    public LocalDateTime getLastModifiedTimepstamp() {
        return this.lastModifiedTimestamp;
    }

    @PrePersist
    private void assignNewId() {
        this.id = new UUID(System.nanoTime(), RANDOM.nextLong());
    }
}
