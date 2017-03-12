package api.controllers;

import api.dao.ThreadDAO;
import api.models.Post;
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

    @PostMapping(path = "/{slug_or_id}/create")
    public ResponseEntity slugCreate(@PathVariable(name = "slug") final String slug,
                              @RequestBody List<Post> body) {
        try {
            // threadDAO.create(...);
        } catch (DuplicateKeyException e) {

        }

        return ResponseEntity.status(HttpStatus.CREATED).body("{}");
    }

    @GetMapping(path = "/{slug_or_id}/details")
    public ResponseEntity getSlugDetails(@PathVariable(name = "slug") final String slug) {
        return ResponseEntity.ok("{}");
    }

    @PostMapping(path = "/{slug_or_id}/details")
    public ResponseEntity setSlugDetails(@PathVariable(name = "slug_or_id") final String  slug_or_id,
                                       @RequestBody ThreadUpdate body){
        return ResponseEntity.ok("{}");
    }

    @GetMapping(path = "/{slug_or_id}/posts")
    public ResponseEntity slugPosts(@PathVariable(name = "slug_or_id)") final String slug_or_id,
                              @RequestParam(name = "limit", required = false) final Integer limit,
                              @RequestParam(name = "marker", required = false) final String marker,
                              @RequestParam(name = "sort", required = false) final String sort,
                              @RequestParam(name = "desc", required = false) final Boolean desc) {
        return ResponseEntity.ok("{}");
    }

    @PostMapping(path = "/{slug_or_id}/vote")
    public ResponseEntity slugVote(@PathVariable(name = "slug_or_id") final String slug_or_id,
                             @RequestBody ThreadVote body) {
        return ResponseEntity.ok("{}");
    }
}
