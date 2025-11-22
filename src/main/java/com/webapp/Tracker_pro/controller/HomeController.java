package com.webapp.Tracker_pro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling home page redirection
 */
@Controller
public class HomeController {

    /**
     * Redirect root URL to index.html
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}
