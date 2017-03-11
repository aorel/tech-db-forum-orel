package api.controllers;

import api.dao.ForumDAO;
import api.dao.ThreadDAO;
import api.dao.UserDAO;
import api.models.Forum;
import api.models.Thread;
import api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/forum")
public class ForumController {
    @Autowired
    private ForumDAO forumDAO;
    @Autowired
    private ThreadDAO threadDAO;
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

        System.out.println("create getNickname: " + user.getNickname());
        newForum.setUserId(user.getId());
        if (!newForum.getUser().equals(user.getNickname())) {
            System.out.println(" | " + newForum.getUser());
            newForum.setUser(user.getNickname());
        }

        try {
            forumDAO.create(newForum);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();

            Forum forum;

            try {
                forum = forumDAO.getSlug(newForum.getSlug());
            } catch (Exception ee) {
                ee.printStackTrace();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(forum);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(newForum);
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

        return ResponseEntity.ok(forum);
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity slugCreate(@PathVariable(name = "slug") String slug,
                                     @RequestBody Thread thread) {
        System.out.println("/{slug}/create");

        thread.setSlug(slug);

        Forum forum;
        try {
            forum = forumDAO.getSlug(slug);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }
        System.out.println("forum.getUserId()" + forum.getUserId());
        System.out.println("    forum.getId()" + forum.getId());

        thread.setUserId(forum.getUserId());
        thread.setForumId(forum.getId());
        System.out.println("Thread:" + thread);

        try {
            int newId = threadDAO.create(thread);
            thread.setId(newId);
        } catch (DuplicateKeyException e) {
            List<Thread> duplicates = threadDAO.getDuplicates(thread);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }


    @GetMapping(path = "/{slug}/users")
    public ResponseEntity slugUsers(@PathVariable(name = "slug") String slug,
                                    @RequestParam(name = "limit", required = false) int limit,
                                    @RequestParam(name = "since", required = false) String since,
                                    @RequestParam(name = "desc", required = false) boolean desc) {

        return ResponseEntity.ok("{}");
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity slugThreads(@PathVariable(name = "slug") String slug,
                                      @RequestParam(name = "limit", required = false) int limit,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "desc", required = false) boolean desc) {
        return ResponseEntity.ok("{}");
    }
}
