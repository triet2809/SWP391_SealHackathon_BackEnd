package Group1.Topic4.repository;

import Group1.Topic4.entity.AccountStatus;
import Group1.Topic4.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID>{
    Users findByEmail(String email);
    List<Users> findByStatus(AccountStatus status);}
