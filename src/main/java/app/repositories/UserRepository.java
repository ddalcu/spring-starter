package app.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.models.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findOneByUserName(String name);
    User findOneByEmail(String email);
    User findOneByToken(String token);
}