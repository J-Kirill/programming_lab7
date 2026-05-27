package common;

import common.stored.Route;

import java.time.ZonedDateTime;
import java.util.*;

public class MetaHashSet<E> implements Set<E> {
    private final HashSet<E> delegate;
    private final Class<E> type;
    private final ZonedDateTime createdAt;

    public MetaHashSet(Class<E> type) {
        this.delegate = new HashSet<>();
        this.type = type;
        this.createdAt = ZonedDateTime.now();
    }

    public MetaHashSet(Class<E> type, int initialCapacity) {
        this.delegate = new HashSet<>(initialCapacity);
        this.type = type;
        this.createdAt = ZonedDateTime.now();
    }

    public MetaHashSet(Class<E> type, Collection<? extends E> c) {
        this.delegate = new HashSet<>(c);
        this.type = type;
        this.createdAt = ZonedDateTime.now();
    }

    public synchronized Class<E> getType() {
        return type;
    }

    public synchronized ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public synchronized boolean add(E e) {
        return delegate.add(e);
    }

    @Override
    public synchronized int size() {
        return delegate.size();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        return delegate.addAll(c);
    }

    @Override
    public synchronized boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return new HashSet<>(delegate).iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    public synchronized String toString() {
        StringBuilder out = new StringBuilder();
        out.append("[ ");
        for (Object object : this) {
            Route route = (Route) object;
            out.append("\n");
            out.append(route.toString(1));
            out.append(",");
        }
        out.deleteCharAt(out.length() - 1);
        out.append("\n]");
        return out.toString();
    }
    public static int[] compareTo(MetaHashSet<Route> collection, Route route) {
        synchronized (collection) {
            int[] vals = new int[collection.delegate.size()];
            int i = 0;
            for (Route route1 : collection.delegate) {
                vals[i++] = route.compareTo(route1);
            }
            return vals;
        }
    }
}

