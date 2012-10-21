package com.ekaqu.cunulus.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Test(groups = "Unit")
public class LongCounterTest {

  private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
      .setDaemon(true)
      .setNameFormat(LongCounterTest.class.getSimpleName())
      .build();
  private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2 + 1;
  private static final int EXECUTION_COUNT = 50000;  // 50000 is when you really see larger difference

  public void testGet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.get(), 5, "Get didn't return the expected value");
  }

  public void testSet() throws Exception {
    LongCounter counter = new LongCounter();
    counter.set(5);
    Assert.assertEquals(counter.get(), 5, "Get didn't return the expected value");
  }

  public void testLazySet() throws Exception {
    LongCounter counter = new LongCounter();
    counter.lazySet(5);
    Assert.assertEquals(counter.get(), 5, "Get didn't return the expected value");
  }

  public void testGetAndSet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.getAndSet(12), 5, "GetAndSet didn't return the expected value");
    Assert.assertEquals(counter.get(), 12, "Get didn't return the expected value");
  }

  public void testCompareAndSet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertFalse(counter.compareAndSet(0, 12), "Set should have been rejected");
    Assert.assertEquals(counter.get(), 5, "Get didn't return the expected value");
    Assert.assertTrue(counter.compareAndSet(5, 12), "Set should have been accepted");
    Assert.assertEquals(counter.get(), 12, "Get didn't return the expected value");
  }

  public void testWeakCompareAndSet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertFalse(counter.weakCompareAndSet(0, 12), "Set should have been rejected");
    Assert.assertEquals(counter.get(), 5, "Get didn't return the expected value");
    Assert.assertTrue(counter.weakCompareAndSet(5, 12), "Set should have been accepted");
    Assert.assertEquals(counter.get(), 12, "Get didn't return the expected value");
  }

  public void testGetAndIncrement() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.getAndIncrement(), 5, "Get and Increment didn't return expeced value");
    Assert.assertEquals(counter.get(), 6, "Get didn't return the expected value");
  }

  public void testGetAndDecrement() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.getAndDecrement(), 5, "Get and Decrement didn't return expeced value");
    Assert.assertEquals(counter.get(), 4, "Get didn't return the expected value");
  }

  public void testGetAndAdd() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.getAndAdd(5), 5, "Get and Decrement didn't return expeced value");
    Assert.assertEquals(counter.get(), 10, "Get didn't return the expected value");
  }

  public void testIncrementAndGet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.incrementAndGet(), 6, "Get and Increment didn't return expeced value");
    Assert.assertEquals(counter.get(), 6, "Get didn't return the expected value");
  }

  public void testDecrementAndGet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.decrementAndGet(), 4, "Get and Decrement didn't return expeced value");
    Assert.assertEquals(counter.get(), 4, "Get didn't return the expected value");
  }

  public void testAddAndGet() throws Exception {
    LongCounter counter = new LongCounter(5);
    Assert.assertEquals(counter.addAndGet(5), 10, "Get and Decrement didn't return expeced value");
    Assert.assertEquals(counter.get(), 10, "Get didn't return the expected value");
  }

  public void testToString() throws Exception {
    LongCounter counter = new LongCounter(12);
    Assert.assertEquals(counter.toString(), "12", "toString didn't give expected value");
  }

  public void testIntValue() throws Exception {
    LongCounter counter = new LongCounter(12);
    Assert.assertEquals(counter.intValue(), 12);
  }

  public void testLongValue() throws Exception {
    LongCounter counter = new LongCounter(12);
    Assert.assertEquals(counter.longValue(), 12l);
  }

  public void testFloatValue() throws Exception {
    LongCounter counter = new LongCounter(12);
    Assert.assertEquals(counter.floatValue(), 12f);
  }

  public void testDoubleValue() throws Exception {
    LongCounter counter = new LongCounter(12);
    Assert.assertEquals(counter.doubleValue(), 12d);
  }

  public void testAddAndGetWithMax() {
    LongCounter counter = new LongCounter(Long.MAX_VALUE);
    // 4 because +1 makes it roll over, so 4 remains
    Assert.assertEquals(counter.addAndGet(5), Long.MIN_VALUE + 4, "Get and Decrement didn't return expeced value");
  }

  public void testAddAndGetWithMin() {
    LongCounter counter = new LongCounter(Long.MIN_VALUE);
    // 4 because +1 makes it roll over, so 4 remains
    Assert.assertEquals(counter.addAndGet(-5), Long.MAX_VALUE - 4, "Get and Decrement didn't return expeced value");
  }

  @Test(groups = "Experiment", description = "Shows that the class isn't thread safe")
  public void concurrentIncrement() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT, THREAD_FACTORY);
    final LongCounter counter = new LongCounter();
    for(int i = 0; i < EXECUTION_COUNT; i++) { // 50000 is when you really see larger difference
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          counter.incrementAndGet();
        }
      });
    }
    executorService.shutdown();
    executorService.awaitTermination(20, TimeUnit.SECONDS);

    Assert.assertEquals(counter.get(), 500);
  }

  @Test(groups = "Experiment", description = "Shows that the class isn't thread safe")
  public void concurrentDecrement() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT, THREAD_FACTORY);
    final LongCounter counter = new LongCounter(500);
    for(int i = 0; i < EXECUTION_COUNT; i++) { // 50000 is when you really see larger difference
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          counter.decrementAndGet();
        }
      });
    }
    executorService.shutdown();
    executorService.awaitTermination(20, TimeUnit.SECONDS);

    Assert.assertEquals(counter.get(), 0);
  }

  @Test(groups = "Experiment", description = "Shows that the class isn't thread safe")
  public void concurrentEnvironment() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT, THREAD_FACTORY);
    final LongCounter counter = new LongCounter();
    for(int i = 0; i < EXECUTION_COUNT; i++) { // 50000 is when you really see larger difference
      final int finalI = i;
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          if(finalI % 2 == 0) {
            counter.incrementAndGet();
          } else {
            counter.decrementAndGet();
          }
        }
      });
    }
    executorService.shutdown();
    executorService.awaitTermination(20, TimeUnit.SECONDS);

    Assert.assertEquals(counter.get(), 0);
  }
}