package ru.job4j.tracker.action;

import ru.job4j.tracker.input.Input;
import ru.job4j.tracker.store.SqlTracker;

public class ExitAction implements UserAction {
    @Override
    public String name() {
        return "=== Exit ====";
    }

    @Override
    public boolean execute(Input input, SqlTracker tracker) {
        return false;
    }
}
