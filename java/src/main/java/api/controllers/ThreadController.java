package api.controllers;

import api.dao.PostDAO;
import api.dao.ThreadDAO;
import api.dao.ThreadVoteDAO;
import api.models.*;

import java.util.List;

import api.models.Thread;
import com.sun.istack.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Nullable
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
    public ResponseEntity slugCreate(@PathVariable(name = "slugOrId") final String slugOrId,
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

        List<Integer> children = postDAO.getChildren(thread.getId());
        for (Post post : posts) {
            if (post.getParent() != null &&
                    post.getParent() != 0 &&
                    !children.contains(post.getParent())) {
                System.out.println("No post parent");
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        try {
            postDAO.create(thread, posts);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (DataIntegrityViolationException e){
            //user not found
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @GetMapping(path = "/{slugOrId}/details")
    public ResponseEntity getSlugDetails(@PathVariable(name = "slugOrId") final String slugOrId) {
        Thread thread = getThreadDetails(slugOrId);
        if (thread == null) {
            return ResponseEntity.notFound().build();
        }

        threadDAO.getCountVotes(thread);

        return ResponseEntity.ok(thread);

    }

    @PostMapping(path = "/{slugOrId}/details")
    public ResponseEntity setSlugDetails(@PathVariable(name = "slugOrId") final String slugOrId,
                                         @RequestBody ThreadUpdate threadUpdate) {
        Thread thread = getThreadDetails(slugOrId);
        if (thread == null) {
            return ResponseEntity.notFound().build();
        }

        if (threadUpdate.getTitle() == null) {
            System.out.println("ThreadUpdate empty title");
            return ResponseEntity.notFound().build();
        }
        if (threadUpdate.getMessage() == null) {
            System.out.println("ThreadUpdate empty message");
            return ResponseEntity.notFound().build();
        }

        threadDAO.update(thread, threadUpdate);

        return ResponseEntity.ok(thread);
    }

    @GetMapping(path = "/{slugOrId}/posts")
    public ResponseEntity slugPosts(@PathVariable(name = "slugOrId") final String slugOrId,
                                    @RequestParam(name = "limit", required = false, defaultValue = "0") final Integer limit,
                                    @RequestParam(name = "marker", required = false, defaultValue = "0") final String marker,
                                    @RequestParam(name = "sort", required = false, defaultValue = "flat") final String sort,
                                    @RequestParam(name = "desc", required = false, defaultValue = "false") final Boolean desc) {

        Thread thread = getThreadDetails(slugOrId);
        if (thread == null) {
            return ResponseEntity.notFound().build();
        }

        Integer offset = 0;
        if (marker.matches("\\d+")) {
            offset = Integer.parseInt(marker);
        } else {
            System.out.println("MARKER ERROR");
            return ResponseEntity.notFound().build();
        }

        Posts posts = new Posts();
        Integer size;
        try {
            switch (sort) {
                case "flat":
                    posts.setPosts(postDAO.getPostsFlat(thread, limit, offset, desc));
                    size = posts.getPosts().size();
                    break;
                case "tree":
                    posts.setPosts(postDAO.getPostsTree(thread, limit, offset, desc));
                    size = posts.getPosts().size();
                    break;
                case "parent_tree":
                    List<Integer> parents = postDAO.getParents(thread, limit, offset, desc);
                    size = parents.size();
                    posts.setPosts(postDAO.getPostsParentTree(thread, desc, parents));
                    break;
                default:
                    System.out.println("SORT ERROR");
                    return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        offset += size;
        posts.setMarker(offset.toString());
        return ResponseEntity.ok(posts);
    }

    @PostMapping(path = "/{slugOrId}/vote")
    public ResponseEntity slugVote(@PathVariable(name = "slugOrId") final String slugOrId,
                                   @RequestBody ThreadVote vote) {
        Thread thread = getThreadDetails(slugOrId);
        if (thread == null) {
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
