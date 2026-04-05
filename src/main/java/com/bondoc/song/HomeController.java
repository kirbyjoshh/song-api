package com.bondoc.song;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Song API is running. Use /songs to access the endpoints.";
    }
}
