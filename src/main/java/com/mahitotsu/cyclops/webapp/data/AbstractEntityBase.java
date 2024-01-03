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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class })
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AbstractEntityBase implements Serializable {

    private static final Random RANDOM = new Random();

    @Id
    @Column(name = "_id", unique = true, nullable = false, updatable = false)
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private UUID id;

    @Version
    @Column(name = "_mod_count", nullable = false)
    @NotNull
    @Min(0)
    @Getter(AccessLevel.PROTECTED)
    private Long modCount;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "_created_date", nullable = false, updatable = false)
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "_created_by", nullable = false, updatable = false)
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private String createdBy;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "_last_modified_date", nullable = false)
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private LocalDateTime lastModifiedDate;

    @LastModifiedBy
    @Column(name = "_last_modified_by", nullable = false)
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private String lastModifiedBy;

    @AssertTrue
    private boolean isDateContextValid() {
        return (this.createdDate != null && this.lastModifiedDate != null)
                ? this.createdDate.equals(this.lastModifiedDate)
                        || this.createdDate.isBefore(this.lastModifiedDate)
                : true;
    }

    @PrePersist
    private void assignNewId() {
        this.id = new UUID(System.nanoTime(), RANDOM.nextLong());
    }
}
