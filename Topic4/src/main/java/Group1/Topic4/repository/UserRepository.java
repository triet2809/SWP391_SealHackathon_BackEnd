package Group1.Topic4.repository;

import Group1.Topic4.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Integer>{
    Users findByEmail(String email);
}
