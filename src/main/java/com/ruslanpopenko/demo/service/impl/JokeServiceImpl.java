package com.ruslanpopenko.demo.service.impl;

import com.ruslanpopenko.demo.client.RandomJokeClient;
import com.ruslanpopenko.demo.client.model.RandomJoke;
import com.ruslanpopenko.demo.entity.JokeEntity;
import com.ruslanpopenko.demo.service.JokeService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class JokeServiceImpl implements JokeService {

    private final RandomJokeClient randomJokeClient;

    @Override
    public List<JokeEntity> getJokes(final int count) {
        if (count == 1) {
            return Collections.singletonList(
                    mapToEntity(randomJokeClient.getRandomJoke())
            );
        }
        final var collect = IntStream.range(0, count)
                .mapToObj(i -> getLimitedTaskExecutor().submitCompletable(() -> mapToEntity(randomJokeClient.getRandomJoke())))
                .collect(Collectors.toList());

        final var allTasks = CompletableFuture.allOf(collect.toArray(new CompletableFuture<?>[0]));

        allTasks.join();

        return collect.stream()
                .map(jokeEntityCompletableFuture -> {
                    try {
                        return jokeEntityCompletableFuture.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private JokeEntity mapToEntity(final RandomJoke randomJoke) {
        return new JokeEntity(
                randomJoke.id(),
                randomJoke.type(),
                randomJoke.setup(),
                randomJoke.punchline()
        );
    }

    @Lookup
    public AsyncTaskExecutor getLimitedTaskExecutor() {
        return null;
    }

}
