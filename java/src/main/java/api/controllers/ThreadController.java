package api.controllers;

import api.dao.PostDAO;
import api.dao.ThreadDAO;
import api.models.Post;
import api.models.Thread;
import api.models.ThreadUpdate;
import api.models.ThreadVote;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/thread")
public class ThreadController {
    @Autowired
    private ThreadDAO threadDAO;
    @Autowired
    private PostDAO postDAO;


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

        // get thread id
        // postDAO create post with tread id
        try {
            postDAO.create(thread, posts);
        } catch (DuplicateKeyException e) {

        }

        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity getSlugDetails(@PathVariable(name = "slug") final String slug) {
        return ResponseEntity.ok("{}");
    }

    @PostMapping(path = "/{slug}/details")
    public ResponseEntity setSlugDetails(@PathVariable(name = "slug") final String slug_or_id,
                                         @RequestBody ThreadUpdate body) {
        return ResponseEntity.ok("{}");
    }

    @GetMapping(path = "/{slug}/posts")
    public ResponseEntity slugPosts(@PathVariable(name = "slug)") final String slug_or_id,
                                    @RequestParam(name = "limit", required = false) final Integer limit,
                                    @RequestParam(name = "marker", required = false) final String marker,
                                    @RequestParam(name = "sort", required = false) final String sort,
                                    @RequestParam(name = "desc", required = false) final Boolean desc) {
        return ResponseEntity.ok("{}");
    }

    @PostMapping(path = "/{slug}/vote")
    public ResponseEntity slugVote(@PathVariable(name = "slug") final String slug_or_id,
                                   @RequestBody ThreadVote body) {
        return ResponseEntity.ok("{}");
    }
}
