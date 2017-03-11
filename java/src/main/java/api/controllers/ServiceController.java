package api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/service")
public class ServiceController {

    @PostMapping(path = "/clear")
    public ResponseEntity clear() {
        return ResponseEntity.ok("{}");
    }

    @GetMapping(path = "/status")
    public ResponseEntity status() {
        return ResponseEntity.ok("{}");
    }
}
