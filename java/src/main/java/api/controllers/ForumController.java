package api.controllers;

import api.dao.ForumDAO;
import api.dao.impl.ForumDAOImpl;
import api.models.Forum;
import api.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/forum")
public class ForumController {
    private ForumDAO forumDAO = new ForumDAOImpl();


    @PostMapping(path = "/create")
    public ResponseEntity create(@RequestBody Forum newForum) {

        try {
            forumDAO.create(newForum);
        } catch (DuplicateKeyException e) {
            List<Forum> duplicates = forumDAO.getDuplicates(newForum);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Forum.toJSON(duplicates));

        } catch (DataAccessException e) {

        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Forum.toJSON(newForum));
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity slugDetails(@PathVariable(name = "slug") String slug) {



        return ResponseEntity.status(HttpStatus.OK).body("{}");
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
