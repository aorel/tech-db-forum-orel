package api.controllers;

import api.dao.ForumDAO;
import api.dao.ThreadDAO;
import api.dao.UserDAO;
import api.models.Forum;
import api.models.Thread;
import api.models.User;
import org.jetbrains.annotations.Nullable;
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


    @Nullable
    private Forum getBySlug(final String slug) {
        Forum forum;
        try {
            forum = forumDAO.getBySlug(slug);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return forum;
    }

    @PostMapping(path = "/create")
    public ResponseEntity create(@RequestBody Forum newForum) {

        User user;
        try {
            user = userDAO.getProfile(newForum.getUser());
        } catch (DataAccessException e) {
            return ResponseEntity.notFound().build();
        }

        newForum.setUserId(user.getId());
        if (!newForum.getUser().equals(user.getNickname())) {
            newForum.setUser(user.getNickname());
        }

        try {
            forumDAO.create(newForum);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();

            Forum forum = getBySlug(newForum.getSlug());
            if (forum == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(forum);
        } catch (DataAccessException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(newForum);
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity slugDetails(@PathVariable(name = "slug") final String slug) {
        Forum forum = getBySlug(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        forumDAO.getCountPosts(forum);
        forumDAO.getCountThreads(forum);

        return ResponseEntity.ok(forum);
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity slugCreate(@PathVariable(name = "slug") final String slug,
                                     @RequestBody Thread thread) {
        if(thread.getSlug() == null){
            thread.setSlug(slug);
        }

        User user;
        try {
            user = userDAO.getProfile(thread.getAuthor());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        Forum forum = getBySlug(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        thread.setUserId(user.getId());
        thread.setForumId(forum.getId());
        thread.setForum(forum.getSlug());

        try {
            int newId = threadDAO.create(thread);
            thread.setId(newId);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();

            Thread duplicatedThread = threadDAO.getBySlugJoinAll(thread.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicatedThread);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        if (thread.getForum().equals(thread.getSlug())) {
            thread.setSlug(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }


    @GetMapping(path = "/{slug}/users")
    public ResponseEntity slugUsers(@PathVariable(name = "slug") final String slug,
                                    @RequestParam(name = "limit", required = false) final Integer limit,
                                    @RequestParam(name = "since", required = false) final String since,
                                    @RequestParam(name = "desc", required = false) final Boolean desc) {

        Forum forum = getBySlug(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        List<User> users = userDAO.getForumUsers(slug, limit, since, desc);

        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity slugThreads(@PathVariable(name = "slug") final String slug,
                                      @RequestParam(name = "limit", required = false) final Integer limit,
                                      @RequestParam(name = "since", required = false) final String since,
                                      @RequestParam(name = "desc", required = false) final Boolean desc) {
        final List<Forum> forumDuplicates;
        try {
            forumDuplicates = forumDAO.getDuplicates(slug);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        if (forumDuplicates.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        final List<Thread> threadDuplicates;
        try {
            threadDuplicates = threadDAO.getByForumSlug(slug, limit, since, desc);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(threadDuplicates);
    }
}
