package app.controllers;

import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import app.models.Model;
import app.services.CrudService;

@RestController
public abstract class CrudController<M extends Model, S extends CrudService<M, ? extends CrudRepository<M, Long>>> {

    S service;

    public abstract void setService(S service);

    public abstract boolean isAuthorized(Long entityId, S service);

    @GetMapping("/create")
    public M create(final M object) {

        if (isAuthorized(object.getId(), service)) {
            return service.save(object);
        }
        logUnauthorizedAccess();
        return null;
    }

    @PostMapping("/update")
    public M update(final M object) {

        if (isAuthorized(object.getId(), service)) {
            return service.update(object);
        }
        logUnauthorizedAccess();
        return null;
    }

    @PostMapping("/delete")
    public boolean delete(final Long id) {

        if (isAuthorized(id, service)) {
            return service.delete(id);
        }
        logUnauthorizedAccess();
        return false;
    }

    @GetMapping("/get")
    public @ResponseBody M get(final Long id) {

        if (isAuthorized(id, service)) {
            return service.get(id);
        }
        logUnauthorizedAccess();
        return null;
    }

    @GetMapping("/json")
    public @ResponseBody Iterable<M> json(final ModelMap map) {

        return service.getAll();
    }

    private void logUnauthorizedAccess() {

        System.out.println("!!UN-AUTHORIZED ACCESS DETECTED!!");
    }
}
