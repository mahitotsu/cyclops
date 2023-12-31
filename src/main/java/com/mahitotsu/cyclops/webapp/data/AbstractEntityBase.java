package com.mahitotsu.cyclops.webapp.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.function.Supplier;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@MappedSuperclass
@EqualsAndHashCode
@ToString
@EntityListeners({ AuditingEntityListener.class })
public abstract class AbstractEntityBase<ID extends Serializable> implements Serializable {

    protected abstract Supplier<ID> idSupplier();

    @Getter
    @Id
    @Column(name = "_id", unique = true, nullable = false, updatable = false)
    private ID id;

    @Getter
    @Version
    @Column(name = "_version", nullable = false)
    private Long version;

    @Getter
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "_created_date", nullable = false, updatable = false)
    private LocalDateTime createdDateTime;

    @Getter
    @CreatedBy
    @Column(name = "_created_by", nullable = false, updatable = false)
    private String createdBy;

    @Getter
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "_last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDateTime;

    @Getter
    @LastModifiedBy
    @Column(name = "_last_modified_by", nullable = false, updatable = false)
    private String lastModifiedBy;

    @PrePersist
    private void onPersist() {
        this.id = this.idSupplier().get();
    }
}
