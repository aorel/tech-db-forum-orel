package api.controllers;

import api.dao.ServiceDAO;
import api.models.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/service")
public class ServiceController {

    @Autowired
    private ServiceDAO serviceDAO;

    @PostMapping(path = "/clear")
    public ResponseEntity clear() {
        try{
            serviceDAO.clear();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/status")
    public ResponseEntity status() {
        Service service = new Service();
        service.setUser(serviceDAO.getCountUsers());
        service.setForum(serviceDAO.getCountForums());
        service.setThread(serviceDAO.getCountThreads());
        service.setPost(serviceDAO.getCountPost());

        return ResponseEntity.ok(service);
    }
}
