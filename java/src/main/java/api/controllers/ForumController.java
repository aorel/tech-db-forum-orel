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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/forum")
public class ForumController {
    private final ForumDAO forumDAO;
    private final ThreadDAO threadDAO;
    private final UserDAO userDAO;

    @Autowired
    ForumController(ForumDAO forumDAO, ThreadDAO threadDAO, UserDAO userDAO) {
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.userDAO = userDAO;
    }

    @Nullable
    private Forum getBySlug(final String slug) {
        Forum forum;
        try {
            forum = forumDAO.getBySlug(slug);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return forum;
    }

    @Nullable
    private Forum getBySlugJoinUser(final String slug) {
        Forum forum;
        try {
            forum = forumDAO.getBySlugJoinUser(slug);
        } catch (EmptyResultDataAccessException e) {
            return null;
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
            Forum forum = getBySlugJoinUser(newForum.getSlug());
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
        Forum forum = getBySlugJoinUser(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        System.out.println("( get) forum/" + slug +"/details" +
                " [id=" + forum.getId() + "]");
        return ResponseEntity.ok(forum);
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity slugCreate(@PathVariable(name = "slug") final String slug,
                                     @RequestBody Thread thread) {

        User user;
        try {
            user = userDAO.getProfile(thread.getAuthor());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        Forum forum = getBySlugJoinUser(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        thread.setUserId(user.getId());
        thread.setForumId(forum.getId());
        thread.setForum(forum.getSlug());

        try {
            threadDAO.create(thread);
        } catch (DuplicateKeyException e) {
            Thread duplicatedThread = threadDAO.getBySlugJoinAll(thread.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicatedThread);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }


    @GetMapping(path = "/{slug}/users")
    public ResponseEntity slugUsers(@PathVariable(name = "slug") final String slug,
                                    @RequestParam(name = "limit", required = false) final Integer limit,
                                    @RequestParam(name = "since", required = false) final String since,
                                    @RequestParam(name = "desc", required = false) final Boolean desc) {

        Forum forum = getBySlugJoinUser(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        List<User> users = userDAO.getForumUsers(forum, limit, since, desc);

        System.out.println("( get) forum/" + slug +"/users" +
                " [id=" + forum.getId() + "]");
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity slugThreads(@PathVariable(name = "slug") final String slug,
                                      @RequestParam(name = "limit", required = false) final Integer limit,
                                      @RequestParam(name = "since", required = false) final String since,
                                      @RequestParam(name = "desc", required = false) final Boolean desc) {
        final Forum forum = getBySlug(slug);
        if (forum == null) {
            return ResponseEntity.notFound().build();
        }

        final List<Thread> threads;
        try {
            threads = threadDAO.getByForum(forum, limit, since, desc);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        System.out.println("( get) forum/" + slug +"/threads" +
                " [len=" + threads.size() + "]");
        return ResponseEntity.ok(threads);
    }
}
