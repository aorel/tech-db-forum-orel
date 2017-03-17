package api.controllers;

import java.util.List;

import api.dao.PostDAO;
import api.models.Post;
import api.models.PostDetails;
import api.models.PostUpdate;
import com.sun.istack.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/post")
public class PostController {
    @Autowired
    private PostDAO postDAO;

    @Nullable
    private Post getPostDetails(final Integer id) {
        /*Thread thread;
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
        return thread;*/
        Post post;
        try {
            post = postDAO.getById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return post;
    }

    @GetMapping(path = "/{id}/details")
    public ResponseEntity getIdDetails(@PathVariable(name = "id") Integer id,
                                       @RequestParam(name = "related", required = false) List<String> related) {
        Post post = getPostDetails(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        PostDetails postDetails = new PostDetails();
        postDetails.setPost(post);

        return ResponseEntity.ok(postDetails);
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity setIdDetails(@PathVariable(name = "id") int id,
                                       @RequestBody PostUpdate postUpdate) {
        Post post = getPostDetails(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        if (postUpdate.getMessage() == null) {
            System.out.println("PostUpdate empty message");
            return ResponseEntity.notFound().build();
        }

        postDAO.update(post, postUpdate);

        return ResponseEntity.ok(post);
    }
}
