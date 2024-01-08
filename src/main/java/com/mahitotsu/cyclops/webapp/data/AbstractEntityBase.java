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
import org.springframework.util.Assert;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class })
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AbstractEntityBase implements Serializable {

    private static final Random generator = new Random();

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @NotNull
    private UUID id;

    @Version
    @Column(nullable = false)
    @NotNull
    private Long version;

    @CreatedBy
    @Column(nullable = true, updatable = false)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdDateTime;

    @LastModifiedBy
    @Column(nullable = true)
    private String lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(nullable = false)
    @NotNull
    private LocalDateTime lastModifiedDateTime;

    @PrePersist
    @PreUpdate
    private void preSave() {
        if (this.id == null) {
            this.id = new UUID(System.nanoTime(), AbstractEntityBase.generator.nextLong());
        }
        if (this.createdDateTime != null && this.lastModifiedDateTime != null) {
            Assert.isTrue(
                    this.lastModifiedDateTime.equals(this.createdDateTime)
                            || this.lastModifiedDateTime.isAfter(this.createdDateTime),
                    "The last modified timestamp must be equal to or after to the created timestamp.");
        }
    }
}
