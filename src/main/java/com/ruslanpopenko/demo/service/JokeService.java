package com.ruslanpopenko.demo.service;

import com.ruslanpopenko.demo.entity.JokeEntity;

import java.util.List;

public interface JokeService {

    List<JokeEntity> getJokes(int count);

}
