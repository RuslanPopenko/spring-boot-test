package com.ruslanpopenko.demo.controller;

import com.ruslanpopenko.demo.controller.model.JokeDto;
import com.ruslanpopenko.demo.entity.JokeEntity;
import com.ruslanpopenko.demo.service.JokeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController(value = "/joke")
@AllArgsConstructor
public class JokeController {

    private final JokeService jokeService;

    @GetMapping("/api/jokes")
    public List<JokeDto> getJokes(@RequestParam(defaultValue = "5") Integer count) {
        return mapJokes(jokeService.getJokes(count));
    }

    private List<JokeDto> mapJokes(final List<JokeEntity> jokes) {
        return jokes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private JokeDto toDto(JokeEntity jokeEntity) {
        return new JokeDto(
                jokeEntity.id(),
                jokeEntity.type(),
                jokeEntity.setup(),
                jokeEntity.punchline()
        );
    }


}
