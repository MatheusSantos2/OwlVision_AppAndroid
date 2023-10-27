package Models;

import java.util.ArrayList;

public class CircularBuffer<T> {
    private Object[] buffer;
    private int capacity;
    private int readIndex;
    private int writeIndex;
    private int size;

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new Object[capacity];
        readIndex = 0;
        writeIndex = 0;
        size = 0;
    }

    public void add(T item) {
        buffer[writeIndex] = item;
        writeIndex = (writeIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        } else {
            readIndex = (readIndex + 1) % capacity;
        }
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index");
        }
        int actualIndex = (readIndex + index) % capacity;
        return (T) buffer[actualIndex];
    }

    public int getSize() {
        return size;
    }

    public ArrayList<T> getRecent(int count) {
        if (count < 0 || count > size) {
            throw new IllegalArgumentException("Invalid count");
        }
        ArrayList<T> recentItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = (writeIndex - count + i + capacity) % capacity;
            recentItems.add((T) buffer[index]);
        }
        return recentItems;
    }

}