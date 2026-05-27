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

    public Class<E> getType() {
        return type;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean add(E e) {
        return delegate.add(e);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    public static String toString(MetaHashSet<Route> collection) {
        StringBuilder out = new StringBuilder();
        out.append("[ ");
        for (Route route : collection) {
            out.append("\n");
            out.append(route.toString(1));
            out.append(",");
        }
        out.deleteCharAt(out.length() - 1);
        out.append("\n]");
        return out.toString();
    }
    public static int[] compareTo(Collection<Route> collection, Route route) {
        int[] vals = new int[collection.size()];
        int i = 0;
        for (Route route1 : collection) {
            vals[i++] = route.compareTo(route1);
        }
        return vals;
    }
}

