package ru.job4j.tracker.action;

import ru.job4j.tracker.input.Input;
import ru.job4j.tracker.store.MemTracker;
import ru.job4j.tracker.store.SqlTracker;
import ru.job4j.tracker.store.Store;

import java.sql.SQLException;

public interface UserAction {
    String name();
    boolean execute(Input input, Store tracker) throws SQLException;
}
