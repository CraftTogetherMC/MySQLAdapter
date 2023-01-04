package de.crafttogether;

public interface Callback<E extends Throwable, V> {
    void call(E exception, V result);
}