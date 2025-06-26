package com.codingworld.service1.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apo")
public class DataControl {
@GetMapping("ll")
public String dta(){

    return "data";
}
}
