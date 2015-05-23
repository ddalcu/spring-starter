package app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.models.Sample;
import app.repositories.SampleRepository;

@Service
public class SampleService extends CrudService<Sample, SampleRepository> {

    @Autowired
    @Override
    public void setRepo(SampleRepository repo) {
        this.repo = repo;
    }

    @Override
    public Sample copy(Sample from, Sample to) {
        to = from;
        return to;
    }

}
