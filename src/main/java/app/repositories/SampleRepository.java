package app.repositories;

import org.springframework.data.repository.CrudRepository;

import app.models.Sample;

public interface SampleRepository extends CrudRepository<Sample, Long> {

}
