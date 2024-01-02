package com.mahitotsu.cyclops.webapp.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
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
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@MappedSuperclass
@EqualsAndHashCode
@ToString
@EntityListeners({ AuditingEntityListener.class })
public abstract class AbstractEntityBase implements Serializable {

    private static final Random RANDOM = new Random();

    @Getter
    @Id
    @Column(name = "_id", unique = true, nullable = false, updatable = false)
    @NotNull
    private UUID id;

    @Getter
    @Version
    @Column(name = "_version", nullable = false)
    @NotNull
    @Min(0)
    private Long version;

    @Getter
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "_created_date", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdDateTime;

    @Getter
    @CreatedBy
    @Column(name = "_created_by", nullable = false, updatable = false)
    @NotNull
    private String createdBy;

    @Getter
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "_last_modified_date", nullable = false)
    @NotNull
    private LocalDateTime lastModifiedDateTime;

    @Getter
    @LastModifiedBy
    @Column(name = "_last_modified_by", nullable = false, updatable = false)
    @NotNull
    private String lastModifiedBy;

    @AssertTrue
    private boolean isValid() {
        return (this.createdDateTime != null && this.lastModifiedDateTime != null)
                ? this.createdDateTime.equals(this.lastModifiedDateTime)
                        || this.createdDateTime.isBefore(this.lastModifiedDateTime)
                : true;
    }

    @PrePersist
    private void onPersist() {
        this.id = new UUID(System.nanoTime(), RANDOM.nextLong());
    }
}
