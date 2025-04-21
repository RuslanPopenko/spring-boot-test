package com.ruslanpopenko.demo.service.impl;

import com.ruslanpopenko.demo.client.RandomJokeClient;
import com.ruslanpopenko.demo.client.model.RandomJoke;
import com.ruslanpopenko.demo.entity.JokeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JokeServiceImplTest {

    @Mock
    private RandomJokeClient randomJokeClient;

    @Mock
    private AsyncTaskExecutor asyncTaskExecutor;

    @Spy
    @InjectMocks
    private JokeServiceImpl jokeService;

    @BeforeEach
    void setUp() {
        // Override the @Lookup method to return our mocked executor
        doReturn(asyncTaskExecutor).when(jokeService).getLimitedTaskExecutor();
    }

    @Test
    void getJokes_whenCountIsOne_shouldReturnSingleJoke() {
        // Arrange
        RandomJoke mockJoke = new RandomJoke(1, "general", "setup", "punchline");
        when(randomJokeClient.getRandomJoke()).thenReturn(mockJoke);

        // Act
        List<JokeEntity> result = jokeService.getJokes(1);

        // Assert
        assertEquals(1, result.size());
        JokeEntity joke = result.get(0);
        assertEquals(1, joke.id());
        assertEquals("general", joke.type());
        assertEquals("setup", joke.setup());
        assertEquals("punchline", joke.punchline());

        // Verify
        verify(randomJokeClient, times(1)).getRandomJoke();
        verify(asyncTaskExecutor, never()).submitCompletable(any());
    }

    @Test
    void getJokes_whenCountIsGreaterThanOne_shouldReturnMultipleJokes() {
        // Arrange
        int count = 3;

        // Create test jokes
        RandomJoke joke1 = new RandomJoke(1, "general", "setup1", "punchline1");
        RandomJoke joke2 = new RandomJoke(2, "general", "setup2", "punchline2");
        RandomJoke joke3 = new RandomJoke(3, "general", "setup3", "punchline3");

        // Configure random joke client to return different jokes
        when(randomJokeClient.getRandomJoke())
                .thenReturn(joke1)
                .thenReturn(joke2)
                .thenReturn(joke3);

        // Configure task executor to execute the lambda and return completed futures
        when(asyncTaskExecutor.submitCompletable(any()))
                .thenAnswer(invocation -> {
                    Runnable supplier = invocation.getArgument(0);
                    return CompletableFuture.completedFuture(supplier.get());
                });

        // Act
        List<JokeEntity> result = jokeService.getJokes(count);

        // Assert
        assertEquals(count, result.size());
        verify(asyncTaskExecutor, times(count)).submitCompletable(any());

        // Verify joke content
        for (int i = 0; i < count; i++) {
            assertEquals(i + 1, result.get(i).id());
            assertEquals("setup" + (i + 1), result.get(i).setup());
            assertEquals("punchline" + (i + 1), result.get(i).punchline());
        }
    }

    @Test
    void getJokes_whenExecutorThrowsException_shouldPropagateException() {
        // Arrange
        CompletableFuture<JokeEntity> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Test exception"));

        when(asyncTaskExecutor.submitCompletable(any())).thenReturn(failedFuture);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jokeService.getJokes(2));
        verify(jokeService).getLimitedTaskExecutor();
    }

    @Test
    void getJokes_whenInterruptedExceptionOccurs_shouldWrapInRuntimeException() {
        // Arrange
        CompletableFuture<JokeEntity> future = spy(new CompletableFuture<>());
        when(asyncTaskExecutor.submitCompletable(any())).thenReturn(future);

        // Configure future.get() to throw InterruptedException
        doThrow(new InterruptedException("Test interruption")).when(future).get();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> jokeService.getJokes(2));
        assertTrue(exception.getCause() instanceof InterruptedException);
    }

    @Test
    void getJokes_whenExecutionExceptionOccurs_shouldWrapInRuntimeException() {
        // Arrange
        CompletableFuture<JokeEntity> future = spy(new CompletableFuture<>());
        when(asyncTaskExecutor.submitCompletable(any())).thenReturn(future);

        // Configure future.get() to throw ExecutionException
        doThrow(