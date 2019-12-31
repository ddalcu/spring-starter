package app.services;

import org.springframework.data.repository.CrudRepository;

public abstract class CrudService<M extends app.models.Model, R extends CrudRepository<M, Long>> {

    R repo;

    public abstract void setRepo(R repo);

    /**
     * Define the parameters that you want to save to the DB when calling the update() method
     *
     * @param from
     *            source object
     * @param to
     *            DB object that gets saves, "return to" in this method
     * @return
     */
    public abstract M copy(M from, M to);

    public Iterable<M> getAll() {

        return this.repo.findAll();
    }

    /**
     * Mainly used to create a new entity however, can also be used to save something without using the update() method.
     *
     * @param model
     * @return saved entity model
     */
    public M save(final M model) {

        return this.repo.save(model);
    }

    public M get(final Long id) {

        return this.repo.findById(id).orElse(null);
    }

    public M update(final M model) {

        M updated = this.repo.findById(model.getId()).orElse(null);
        updated = copy(model, updated);
        return this.repo.save(updated);
    }

    public Boolean delete(final Long id) {

        this.repo.deleteById(id);
        return true;
    }
}
