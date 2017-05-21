package api.controllers;

import java.util.List;

import api.dao.ForumDAO;
import api.dao.PostDAO;
import api.dao.ThreadDAO;
import api.dao.UserDAO;
import api.models.Forum;
import api.models.Post;
import api.models.PostDetails;
import api.models.PostUpdate;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/post")
public class PostController {
    private final PostDAO postDAO;
    private final UserDAO userDAO;
    private final ThreadDAO threadDAO;
    private final ForumDAO forumDAO;

    @Autowired
    PostController(PostDAO postDAO, UserDAO userDAO, ThreadDAO threadDAO, ForumDAO forumDAO) {
        this.postDAO = postDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
        this.forumDAO = forumDAO;
    }

    @Nullable
    private Post getPostDetails(final Integer id) {
        Post post;
        try {
            post = postDAO.getById(id);
        } catch(EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return post;
    }

    @GetMapping(path = "/{id}/details")
    public ResponseEntity getIdDetails(@PathVariable(name = "id") Integer id,
                                       @RequestParam(name = "related", required = false) List<String> related) {
        System.out.println("( get) post/{id}/details: " + id);

        Post post = getPostDetails(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        PostDetails postDetails = new PostDetails();
        postDetails.setPost(post);

        if (related != null) {
            for (String item : related) {

                switch (item) {
                    case "user":
                        postDetails.setAuthor(userDAO.getProfile(post.getAuthor()));
                        break;
                    case "thread":
                        postDetails.setThread(threadDAO.getByIdJoinAll(post.getThread()));
                        break;
                    case "forum":
                        Forum forum = forumDAO.getBySlugJoinUser(post.getForum());

                        postDetails.setForum(forum);
                        break;
                    default:
                        System.out.println("RELATED ITEM ERROR");
                        return ResponseEntity.notFound().build();
                }
            }
        }

        return ResponseEntity.ok(postDetails);
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity setIdDetails(@PathVariable(name = "id") int id,
                                       @RequestBody PostUpdate postUpdate) {
        System.out.println("(post) post/{id}/details: " + id);

        Post post = getPostDetails(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        if (postUpdate.getMessage() != null) {
            postDAO.update(post, postUpdate);
        }

        return ResponseEntity.ok(post);
    }
}
