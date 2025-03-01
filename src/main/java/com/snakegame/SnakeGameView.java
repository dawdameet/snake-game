package com.snakegame;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Route;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Push
@Route("")
@BodySize(height = "100vh", width = "100vw")
public class SnakeGameView extends Div implements KeyNotifier {
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 20;

    private LinkedList<int[]> snake = new LinkedList<>();
    private int[] food;
    private String direction = "RIGHT";
    private boolean running = true;
    private UI ui;

    public SnakeGameView() {
        setWidth("400px");
        setHeight("400px");
        getStyle().set("border", "3px solid black");
        getStyle().set("position", "absolute");
        getStyle().set("left", "50%");
        getStyle().set("top", "50%");
        getStyle().set("transform", "translate(-50%, -50%)");
        getStyle().set("background", "black");

        snake.add(new int[]{5, 5});
        spawnFood();

        // Handle key presses
        addKeyPressListener(Key.ARROW_UP, e -> changeDirection("UP"));
        addKeyPressListener(Key.ARROW_DOWN, e -> changeDirection("DOWN"));
        addKeyPressListener(Key.ARROW_LEFT, e -> changeDirection("LEFT"));
        addKeyPressListener(Key.ARROW_RIGHT, e -> changeDirection("RIGHT"));

        // Start game loop
        ui = UI.getCurrent();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::gameLoop);
    }

    private void gameLoop() {
        while (running) {
            try {
                Thread.sleep(150);
                moveSnake();
                ui.access(this::render);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveSnake() {
        int[] head = snake.getFirst();
        int[] newHead = head.clone();

        switch (direction) {
            case "UP":
                newHead[1]--;
                break;
            case "DOWN":
                newHead[1]++;
                break;
            case "LEFT":
                newHead[0]--;
                break;
            case "RIGHT":
                newHead[0]++;
                break;
        }

        // Check for collision (wall or self)
        if (newHead[0] < 0 || newHead[0] >= GRID_SIZE || newHead[1] < 0 || newHead[1] >= GRID_SIZE || checkCollision(newHead)) {
            running = false;
            return;
        }

        // Check if snake eats food
        if (newHead[0] == food[0] && newHead[1] == food[1]) {
            spawnFood();
        } else {
            snake.removeLast(); // Remove tail if no food eaten
        }

        snake.addFirst(newHead);
    }

    private boolean checkCollision(int[] head) {
        return snake.stream().anyMatch(s -> s[0] == head[0] && s[1] == head[1]);
    }

    private void spawnFood() {
        Random random = new Random();
        food = new int[]{random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)};
    }

    private void changeDirection(String newDirection) {
        if ((direction.equals("UP") && newDirection.equals("DOWN")) ||
            (direction.equals("DOWN") && newDirection.equals("UP")) ||
            (direction.equals("LEFT") && newDirection.equals("RIGHT")) ||
            (direction.equals("RIGHT") && newDirection.equals("LEFT"))) {
            return;
        }
        direction = newDirection;
    }

    private void render() {
        getElement().removeAllChildren();

        // Draw Snake
        for (int[] segment : snake) {
            Div snakePart = new Div();
            snakePart.setWidth(CELL_SIZE + "px");
            snakePart.setHeight(CELL_SIZE + "px");
            snakePart.getStyle().set("position", "absolute");
            snakePart.getStyle().set("left", (segment[0] * CELL_SIZE) + "px");
            snakePart.getStyle().set("top", (segment[1] * CELL_SIZE) + "px");
            snakePart.getStyle().set("background", "lime");
            add(snakePart);
        }

        // Draw Food
        Div foodDiv = new Div();
        foodDiv.setWidth(CELL_SIZE + "px");
        foodDiv.setHeight(CELL_SIZE + "px");
        foodDiv.getStyle().set("position", "absolute");
        foodDiv.getStyle().set("left", (food[0] * CELL_SIZE) + "px");
        foodDiv.getStyle().set("top", (food[1] * CELL_SIZE) + "px");
        foodDiv.getStyle().set("background", "red");
        add(foodDiv);
    }
}
