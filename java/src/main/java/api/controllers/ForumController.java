package api.controllers;

import api.dao.ForumDAO;
import api.dao.UserDAO;
import api.models.Forum;
import api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/forum")
public class ForumController {
    @Autowired
    private ForumDAO forumDAO;
    @Autowired
    private UserDAO userDAO;


    @PostMapping(path = "/create")
    public ResponseEntity create(@RequestBody Forum newForum) {

        User user;
        try {
            user = userDAO.getProfile(newForum.getUser());
        } catch (DataAccessException e) {
            System.out.println("DataAccessException");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        newForum.setUserId(user.getId());

        try {
            forumDAO.create(newForum);
        } catch (DuplicateKeyException e) {
            Forum forum = forumDAO.getSlug(newForum.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Forum.toJSON(forum));
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Forum.toJSON(newForum));
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity slugDetails(@PathVariable(name = "slug") String slug) {

        Forum forum;
        try {
            forum = forumDAO.getSlug(slug);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }

        return ResponseEntity.status(HttpStatus.OK).body(Forum.toJSON(forum));
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity slugCreate(@RequestBody Thread body) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }


    @GetMapping(path = "/{slug}/users")
    public ResponseEntity slugUsers(@PathVariable(name = "slug") String slug,
                                    @RequestParam(name = "limit", required = false) int limit,
                                    @RequestParam(name = "since", required = false) String since,
                                    @RequestParam(name = "desc", required = false) boolean desc) {

        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity slugThreads(@PathVariable(name = "slug") String slug,
                                      @RequestParam(name = "limit", required = false) int limit,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "desc", required = false) boolean desc) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }
}
