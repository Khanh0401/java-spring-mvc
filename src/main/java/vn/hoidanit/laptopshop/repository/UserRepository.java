package vn.hoidanit.laptopshop.repository;

import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.repository.CrudRepository;
import java.util.List;

//crud: create read update delete
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    void deleteById(long id);

    List<User> findByEmail(String Email);

    List<User> findAll();

    User findOneById(long id);

    boolean existsByEmail(String email);

    User findOneByEmail(String email);
}
