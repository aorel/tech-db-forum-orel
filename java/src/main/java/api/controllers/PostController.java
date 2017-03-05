package api.controllers;

import java.util.List;

import api.models.PostUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/post")
public class PostController {

    @GetMapping(path = "/{id}/details")
    public ResponseEntity getIdDetails(@PathVariable(name = "id") int id,
                                       @RequestParam(name = "related", required = false) List<String> related) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity setIdDetails(@PathVariable(name = "id") int id,
                              @RequestBody PostUpdate post) {
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }
}
