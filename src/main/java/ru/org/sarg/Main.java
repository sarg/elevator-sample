package ru.org.sarg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static Clock clock = new Clock();

    private static final String ANSI_CLEAR_SCREEN = "\u001B[2J";
    private static final String ANSI_MOVE_TOP_LEFT = "\u001B[0;0H";
    static volatile boolean exit = false;

    public static void main(String[] args) throws InterruptedException {
        Elevator elevator = new Elevator(2.0, 3.0, 10);

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        new Thread(() -> {
            while (!exit) {
                try {
                    String s = r.readLine();

                    if (s.startsWith("q")) {
                        exit = true;
                        System.out.println("exit");
                    }

                    try {
                        int i = Integer.parseInt(s);
                        elevator.onPressFloorButton(i);
                    } catch (NumberFormatException e) {
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        while (!exit) {
            System.out.print(ANSI_CLEAR_SCREEN);
            System.out.print(ANSI_MOVE_TOP_LEFT);

            elevator.draw();
            Thread.sleep(1000);
        }
    }

    public static class Clock {
        public long now() {
            return System.nanoTime();
        }

        public void schedule(long delay, Runnable task) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }

            task.run();
        }
    }

    public static class Elevator {
        public static class State {
            public State(Action action, int floor) {
                this.floor = floor;
                this.action = action;
            }


            enum Action {
                GOING_UP, GOING_DOWN, STAYING, OPENING, CLOSING, IDLE
            }

            public final int floor;
            public final Action action;
        }

        private final long[] cabinButtons;
        private final long[] floorButtons;
        private final double speed;
        private final double floorHeight;
        private final int floors;
        private State state;
        private State nextState;

        public boolean isPressed(int i, long now) {
            return floorButtons[i] > 0 && floorButtons[i] <= now;
        }

        public Elevator(double speed, double floorHeight, int floors) {
            this.speed = speed;
            this.floorHeight = floorHeight;
            this.floors = floors;
            this.floorButtons = new long[floors];
            this.cabinButtons = new long[floors];
            this.state = new State(State.Action.IDLE, 0);
            this.nextState = this.state;
        }

        public State getState() {
            return state;
        }

        public void onPressCabinButton(int floor) {
            cabinButtons[floor] = clock.now();
        }

        public void onPressFloorButton(int floor) {
            floorButtons[floor] = clock.now();
        }

        public void refresh() {
            state = nextState;

            long min = Long.MAX_VALUE;
            int minFloor = 0;
            for (int i = 0; i < floorButtons.length; i++) {
                if (floorButtons[i] > 0 && floorButtons[i] < min) {
                    min = floorButtons[i];
                    minFloor = i;
                }
            }
        }

        public void draw() {
            System.out.println("-= Elevator simulator =-");
            long now = clock.now();
            for (int i = floors-1; i >= 0; i--) {
                System.out.println(String.format(" %02d %1s |  %4s  |",
                        i,
                        isPressed(i, now) ? '*' : ' ',
                        state.floor == i ? "[:)]" : ""
                ));
            }
        }

    }
}
