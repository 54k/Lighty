package io.lighty.network.buffer;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArrayByteBufferPool implements ByteBufferPool {

	public final static ByteBufferPool DEFAULT = new ArrayByteBufferPool();

	private final static int DEFAULT_MIN_SIZE = 64;
	private final static int DEFAULT_STEP_SIZE = 1024;
	private final static int DEFAULT_MAX_SIZE = 65536;

	private final int minSize;
	private final Bucket[] directBuckets;
	private final Bucket[] heapBuckets;
	private final int increment;
	private int bucketsCount;

	public ArrayByteBufferPool() {
		this(DEFAULT_MIN_SIZE, DEFAULT_STEP_SIZE, DEFAULT_MAX_SIZE);
	}

	public ArrayByteBufferPool(int minSize, int increment, int maxSize) {
		if (minSize >= increment) {
			throw new IllegalArgumentException("minSize >= increment");
		}
		if ((maxSize % increment) != 0 || increment >= maxSize) {
			throw new IllegalArgumentException("increment must be a divisor of maxSize");
		}

		this.minSize = minSize;
		this.increment = increment;

		bucketsCount = maxSize / increment;

		directBuckets = new Bucket[bucketsCount];
		heapBuckets = new Bucket[bucketsCount];

		int size = 0;
		for (int i = 0; i < bucketsCount; i++) {
			size += this.increment;
			directBuckets[i] = new Bucket(size);
			heapBuckets[i] = new Bucket(size);
		}
	}

	@Override
	public ByteBuffer acquire(int size, boolean direct) {
		Bucket bucket = bucketFor(size, direct);
		ByteBuffer buffer = bucket == null ? null : bucket.queue.poll();

		if (buffer == null) {
			int capacity = bucket == null ? size : bucket.size;
			buffer = direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
		}

		return buffer;
	}

	@Override
	public DynamicByteBuffer acquireDynamic(int size, boolean direct) {
		return DynamicByteBuffer.allocate(new AllocatorWrapper(this, direct), size);
	}

	@Override
	public void release(ByteBuffer buffer) {
		if (buffer != null) {
			Bucket bucket = bucketFor(buffer.capacity(), buffer.isDirect());
			if (bucket != null) {
				buffer.clear();
				bucket.queue.offer(buffer);
			}
		}
	}

	@Override
	public void release(DynamicByteBuffer buffer) {
		buffer.release();
	}

	@Override
	public void clear() {
		for (int i = 0; i < bucketsCount; i++) {
			directBuckets[i].queue.clear();
			heapBuckets[i].queue.clear();
		}
	}

	private Bucket bucketFor(int size, boolean direct) {
		if (size <= minSize) {
			return null;
		}
		int b = (size - 1) / increment;
		if (b >= this.directBuckets.length) {
			return null;
		}

		return direct ? this.directBuckets[b] : heapBuckets[b];
	}

	private static final class Bucket {
		public final int size;
		public final Queue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();

		public Bucket(int size) {
			this.size = size;
		}
	}

	private static final class AllocatorWrapper implements ByteBufferAllocator {

		private ByteBufferPool pool;
		private boolean direct;

		private AllocatorWrapper(ByteBufferPool pool, boolean direct) {
			this.pool = pool;
			this.direct = direct;
		}

		@Override
		public ByteBuffer allocate(int capacity) {
			return pool.acquire(capacity, direct);
		}

		@Override
		public void release(ByteBuffer buffer) {
			pool.release(buffer);
		}
	}
}
