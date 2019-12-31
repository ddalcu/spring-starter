package app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.models.Sample;
import app.services.SampleService;

@RestController
@RequestMapping("/sample")
public class SampleController extends CrudController<Sample, SampleService> {

    @Autowired
    @Override
    public void setService(final SampleService service) {

        this.service = service;
    }

    @Override
    public boolean isAuthorized(final Long entityId, final SampleService service) {

        return true;
    }
}
