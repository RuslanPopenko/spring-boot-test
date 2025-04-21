package com.ruslanpopenko.demo.client;

import com.ruslanpopenko.demo.client.model.RandomJoke;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "joke-client", url = "${client.joke.url}")
public interface RandomJokeClient {

    @GetMapping("/random_joke")
    RandomJoke getRandomJoke();

}
