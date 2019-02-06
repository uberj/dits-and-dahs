package com.example.uberj.test1;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class TestObserver<T> implements Observer<T> {
    private final List<T> valueHistory = new ArrayList<>();
    private final List<Observer<T>> childObservers = new ArrayList<>();
    private CountDownLatch valueLatch = new CountDownLatch(1);

    private TestObserver() {
    }

    @Override
    public void onChanged(@Nullable T value) {
        valueHistory.add(value);
        valueLatch.countDown();
        for (Observer<T> childObserver : childObservers) {
            childObserver.onChanged(value);
        }
    }

    /**
     * Returns a last received value. Fails if no value was received yet.
     *
     * @return a last received value
     */
    public T value() {
        assertHasValue();
        return valueHistory.get(valueHistory.size() - 1);
    }

    /**
     * Returns a unmodifiable list of received values.
     *
     * @return a list of received values
     */
    public List<T> valueHistory() {
        return Collections.unmodifiableList(valueHistory);
    }

    /**
     * Assert that this TestObserver received at least one value.
     *
     * @return this
     */
    public TestObserver<T> assertHasValue() {
        if (valueHistory.isEmpty()) {
            throw fail("Observer never received any value");
        }

        return this;
    }

    /**
     * Assert that this TestObserver never received any value.
     *
     * @return this
     */
    public TestObserver<T> assertNoValue() {
        if (!valueHistory.isEmpty()) {
            throw fail("Expected no value, but received: " + value());
        }

        return this;
    }

    /**
     * Assert that this TestObserver received the specified number of values.
     *
     * @param expectedSize the expected number of received values
     * @return this
     */
    public TestObserver<T> assertHistorySize(int expectedSize) {
        int size = valueHistory.size();
        if (size != expectedSize) {
            throw fail("History size differ; Expected: " + expectedSize + ", Actual: " + size);
        }
        return this;
    }

    /**
     * Assert that this TestObserver last received value is equal to
     * the given value.
     *
     * @param expected the value to expect being equal to last value, can be null
     * @return this
     */
    public TestObserver<T> assertValue(T expected) {
        T value = value();

        if (expected == null && value == null) {
            return this;
        }

        if (!value.equals(expected)) {
            throw fail("Expected: " + valueAndClass(expected) + ", Actual: " + valueAndClass(value));
        }

        return this;
    }

    /**
     * Asserts that for this TestObserver last received value
     * the provided predicate returns true.
     *
     * @param valuePredicate the predicate that receives the observed value
     *                       and should return true for the expected value.
     * @return this
     */
    public TestObserver<T> assertValue(Function<T, Boolean> valuePredicate) {
        T value = value();

        if (!valuePredicate.apply(value)) {
            throw fail("Predicate was false. See stack trace location for reason");
        }

        return this;
    }

    /**
     * Asserts that this TestObserver did not receive any value for which
     * the provided predicate returns true.
     *
     * @param valuePredicate the predicate that receives the observed values
     *                       and should return true for the value not supposed to be received.
     * @return this
     */
    public TestObserver<T> assertNever(Function<T, Boolean> valuePredicate) {
        int size = valueHistory.size();
        for (int valueIndex = 0; valueIndex < size; valueIndex++) {
            T value = this.valueHistory.get(valueIndex);
            if (valuePredicate.apply(value)) {
                throw fail("Value at position " + valueIndex + " matches predicate "
                        + valuePredicate.toString() + ", which was not expected.");
            }
        }

        return this;
    }

    /**
     * Allows assertion of some mapped value extracted from originally observed values.
     * History of observed values is retained.
     * <p>
     * This can became useful when you want to perform assertions on some complex structure and
     * you want to assert only on one field.
     *
     * @param mapper Function to map originally observed value.
     * @param <N>    Type of mapper.
     * @return TestObserver for mapped value
     */
    public <N> TestObserver<N> map(Function<T, N> mapper) {
        TestObserver<N> newObserver = create();
        // We want the history match the current one
        for (T value : valueHistory) {
            newObserver.onChanged(mapper.apply(value));
        }

        childObservers.add(value -> newObserver.onChanged(mapper.apply(value)));
        return newObserver;
    }

    /**
     * Awaits until this TestObserver has any value.
     * <p>
     * If this TestObserver has already value then this method returns immediately.
     *
     * @return this
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public TestObserver<T> awaitValue() throws InterruptedException {
        valueLatch.await();
        return this;
    }

    /**
     * Awaits the specified amount of time or until this TestObserver has any value.
     * <p>
     * If this TestObserver has already value then this method returns immediately.
     *
     * @return this
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public TestObserver<T> awaitValue(long timeout, TimeUnit timeUnit) throws InterruptedException {
        valueLatch.await(timeout, timeUnit);
        return this;
    }

    /**
     * Awaits until this TestObserver receives next value.
     * <p>
     * If this TestObserver has already value then it awaits for another one.
     *
     * @return this
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public TestObserver<T> awaitNextValue() throws InterruptedException {
        return withNewLatch().awaitValue();
    }


    /**
     * Awaits the specified amount of time or until this TestObserver receives next value.
     * <p>
     * If this TestObserver has already value then it awaits for another one.
     *
     * @return this
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public TestObserver<T> awaitNextValue(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return withNewLatch().awaitValue(timeout, timeUnit);
    }

    private TestObserver<T> withNewLatch() {
        valueLatch = new CountDownLatch(1);
        return this;
    }

    private AssertionError fail(String message) {
        return new AssertionError(message);
    }

    private static String valueAndClass(Object value) {
        if (value != null) {
            return value + " (class: " + value.getClass().getSimpleName() + ")";
        }
        return "null";
    }

    public static <T> TestObserver<T> create() {
        return new TestObserver<>();
    }

    public static <T> TestObserver<T> test(LiveData<T> liveData) {
        TestObserver<T> observer = new TestObserver<>();
        liveData.observeForever(observer);
        return observer;
    }
}

