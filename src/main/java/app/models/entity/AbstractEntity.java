package app.models.entity;

import java.time.Instant;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public abstract class AbstractEntity {

    @GeneratedValue
    @Id
    private Long id;

    private Instant creationTime;

    private Instant lastModified;

    @PrePersist
    void createdAt() {

        final Instant i = Instant.now();
        creationTime = i;
        lastModified = i;
    }

    @PreUpdate
    void lastModified() {

        setLastModified(Instant.now());
    }

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public Instant getCreationTime() {

        return creationTime;
    }

    public void setCreationTime(final Instant creationTime) {

        this.creationTime = creationTime;
    }

    public Instant getLastModified() {

        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {

        this.lastModified = lastModified;
    }

}