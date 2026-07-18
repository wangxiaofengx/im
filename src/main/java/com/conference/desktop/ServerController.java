package com.conference.desktop;

import com.conference.App;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

final class ServerController {
    enum State {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING,
        FAILED
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "video-conference-server-control");
        thread.setDaemon(false);
        return thread;
    });
    private final String[] applicationArguments;
    private final Consumer<State> stateListener;
    private final Consumer<String> errorListener;

    private volatile ConfigurableApplicationContext context;
    private volatile State state = State.STOPPED;

    ServerController(String[] applicationArguments, Consumer<State> stateListener,
                     Consumer<String> errorListener) {
        this.applicationArguments = Arrays.stream(applicationArguments)
                .filter(argument -> !argument.startsWith("--server.port="))
                .filter(argument -> !argument.equals("--console"))
                .toArray(String[]::new);
        this.stateListener = stateListener;
        this.errorListener = errorListener;
    }

    State getState() {
        return state;
    }

    void start(int port) {
        if (state != State.STOPPED && state != State.FAILED) {
            return;
        }
        changeState(State.STARTING);
        executor.submit(() -> {
            try {
                String[] args = Arrays.copyOf(applicationArguments, applicationArguments.length + 1);
                args[args.length - 1] = "--server.port=" + port;

                SpringApplicationBuilder builder = new SpringApplicationBuilder(App.class).headless(false);
                builder.application().setRegisterShutdownHook(false);
                context = builder.run(args);
                changeState(State.RUNNING);
            } catch (Exception exception) {
                context = null;
                changeState(State.FAILED);
                errorListener.accept(rootMessage(exception));
            }
        });
    }

    void stop() {
        if (state != State.RUNNING) {
            return;
        }
        changeState(State.STOPPING);
        executor.submit(() -> {
            ConfigurableApplicationContext current = context;
            context = null;
            try {
                if (current != null) {
                    current.close();
                }
            } finally {
                changeState(State.STOPPED);
            }
        });
    }

    void shutdown(Runnable completion) {
        executor.submit(() -> {
            ConfigurableApplicationContext current = context;
            context = null;
            if (current != null) {
                current.close();
            }
            executor.shutdown();
            completion.run();
        });
    }

    private void changeState(State newState) {
        state = newState;
        stateListener.accept(newState);
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
