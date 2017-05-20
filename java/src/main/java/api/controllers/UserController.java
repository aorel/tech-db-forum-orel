package api.controllers;

import api.dao.UserDAO;
import api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserDAO userDAO;

    @Autowired
    UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostMapping(path = "/{nickname}/create")
    public ResponseEntity create(@PathVariable(name = "nickname") final String nickname,
                                 @RequestBody User newUser) {
        newUser.setNickname(nickname);

        try {
            userDAO.create(newUser);
        } catch (DuplicateKeyException e) {
            List<User> duplicates = userDAO.getDuplicates(newUser);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicates);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping(path = "/{nickname}/profile")
    public ResponseEntity getProfile(@PathVariable(name = "nickname") final String nickname) {

        User user;
        try {
            user = userDAO.getProfile(nickname);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping(path = "/{nickname}/profile")
    public ResponseEntity setProfile(@PathVariable(name = "nickname") final String nickname,
                                     @RequestBody User updateUser) {
        updateUser.setNickname(nickname);

        try {
            userDAO.setProfile(updateUser);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        return getProfile(nickname);
    }
}
