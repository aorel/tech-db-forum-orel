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
    public ResponseEntity slugCreate(@PathVariable(name = "slug") String slug,
                              @RequestBody List<Post> body) {
        try {
            // threadDAO.create(...);
        } catch (DuplicateKeyException e) {

        }

        return ResponseEntity.status(HttpStatus.CREATED).body("{}");
    }

    @GetMapping(path = "/{slug_or_id}/details")
    public ResponseEntity getSlugDetails(@PathVariable(name = "slug") String slug) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }

    @PostMapping(path = "/{slug_or_id}/details")
    public ResponseEntity setSlugDetails(@PathVariable(name = "slug_or_id") String  slug_or_id,
                                       @RequestBody ThreadUpdate body){
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }

    @GetMapping(path = "/{slug_or_id}/posts")
    public ResponseEntity slugPosts(@PathVariable(name = "slug_or_id)") String slug_or_id,
                              @RequestParam(name = "limit", required = false) int limit,
                              @RequestParam(name = "marker", required = false) String marker,
                              @RequestParam(name = "sort", required = false) String sort,
                              @RequestParam(name = "desc", required = false) boolean desc) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }

    @PostMapping(path = "/{slug_or_id}/vote")
    public ResponseEntity slugVote(@PathVariable(name = "slug_or_id") String slug_or_id,
                             @RequestBody ThreadVote body) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }
}
