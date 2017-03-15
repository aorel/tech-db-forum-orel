package api.controllers;

import api.dao.PostDAO;
import api.dao.ThreadDAO;
import api.dao.ThreadVoteDAO;
import api.models.Post;
import api.models.Thread;
import api.models.ThreadUpdate;
import api.models.ThreadVote;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/thread")
public class ThreadController {
    @Autowired
    private ThreadDAO threadDAO;
    @Autowired
    private ThreadVoteDAO threadVoteDAO;
    @Autowired
    private PostDAO postDAO;

    private Thread getThreadDetails(final String slugOrId) {
        Thread thread;
        try {
            if (slugOrId.matches("\\d+")) {
                Integer id = Integer.parseInt(slugOrId);
                thread = threadDAO.getByIdJoinAll(id);
            } else {
                thread = threadDAO.getBySlugJoinAll(slugOrId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //return ResponseEntity.notFound().build();
            return null;
        }
        return thread;
    }


    @PostMapping(path = "/{slugOrId}/create")
    public ResponseEntity slugCreate1(@PathVariable(name = "slugOrId") final String slugOrId,
                                      @RequestBody List<Post> posts) {
        if (posts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Thread thread;
        try {
            if (slugOrId.matches("\\d+")) {
                Integer id = Integer.parseInt(slugOrId);
                thread = threadDAO.getByIdJoinForum(id);
            } else {
                thread = threadDAO.getBySlugJoinForum(slugOrId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        try {
            postDAO.create(thread, posts);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @GetMapping(path = "/{slugOrId}/details")
    public ResponseEntity getSlugDetails(@PathVariable(name = "slugOrId") final String slugOrId) {
        Thread thread = getThreadDetails(slugOrId);
        if(thread == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(thread);
        }
    }

    @PostMapping(path = "/{slug}/details")
    public ResponseEntity setSlugDetails(@PathVariable(name = "slug") final String slug_or_id,
                                         @RequestBody ThreadUpdate body) {
        return ResponseEntity.ok("{}");
    }

    @GetMapping(path = "/{slug}/posts")
    public ResponseEntity slugPosts(@PathVariable(name = "slug)") final String slug,
                                    @RequestParam(name = "limit", required = false) final Integer limit,
                                    @RequestParam(name = "marker", required = false) final String marker,
                                    @RequestParam(name = "sort", required = false) final String sort,
                                    @RequestParam(name = "desc", required = false) final Boolean desc) {
        return ResponseEntity.ok("{}");
    }

    @PostMapping(path = "/{slugOrId}/vote")
    public ResponseEntity slugVote(@PathVariable(name = "slugOrId") final String slugOrId,
                                   @RequestBody ThreadVote vote) {
        Thread thread = getThreadDetails(slugOrId);
        if(thread == null) {
            return ResponseEntity.notFound().build();
        }

        ThreadVote existingVote;
        try {
            existingVote = threadVoteDAO.get(thread, vote);
        } catch (EmptyResultDataAccessException e) {
            existingVote = null;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        try {
            if (existingVote == null) {
                threadVoteDAO.create(thread, vote);
            } else {
                vote.setId(existingVote.getId());
                threadVoteDAO.insert(vote);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        threadVoteDAO.count(thread);

        return ResponseEntity.ok(thread);
    }
}
