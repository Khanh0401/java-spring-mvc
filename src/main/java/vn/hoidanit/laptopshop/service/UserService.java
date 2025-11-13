package vn.hoidanit.laptopshop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleSaveUser(User user) {
        User people = this.userRepository.save(user);
        System.out.println(people);
        return people;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public List<User> getAllUsersByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User getUserByID(long id) {
        return this.userRepository.findOneById(id);
    }

    public String handleHello() {
        return "Hello from service";
    }

    public void deleteById(long id) {
        this.userRepository.deleteById(id);
    }
}
