package io.lighty.network.buffer;

import java.nio.ByteBuffer;

public final class DynamicByteBuffer {

	private static final HeapByteBufferAllocator HEAP_BYTE_BUFFER_ALLOCATOR = new HeapByteBufferAllocator();
	private static final DirectByteBufferAllocator DIRECT_BYTE_BUFFER_ALLOCATOR = new DirectByteBufferAllocator();

	private ByteBuffer buffer;
	private ByteBufferAllocator allocator;
	private int mark = -1;

	private DynamicByteBuffer(ByteBufferAllocator allocator, int initialCapacity) {
		if (allocator == null) {
			throw new IllegalArgumentException("allocator");
		}
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity should not be negative");
		}
		this.allocator = allocator;
		buffer = allocator.allocate(initialCapacity);
	}

	private DynamicByteBuffer(ByteBufferAllocator allocator, ByteBuffer buffer) {
		if (allocator == null) {
			throw new IllegalArgumentException("allocator");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer");
		}
		this.allocator = allocator;
		this.buffer = buffer;
	}

	public static DynamicByteBuffer allocate(int capacity) {
		return new DynamicByteBuffer(HEAP_BYTE_BUFFER_ALLOCATOR, capacity);
	}

	public static DynamicByteBuffer allocateDirect(int capacity) {
		return new DynamicByteBuffer(DIRECT_BYTE_BUFFER_ALLOCATOR, capacity);
	}

	public static DynamicByteBuffer allocate(ByteBufferAllocator allocator, int capacity) {
		return new DynamicByteBuffer(allocator, capacity);
	}

	public static DynamicByteBuffer wrap(byte[] bytes) {
		return new DynamicByteBuffer(HEAP_BYTE_BUFFER_ALLOCATOR, ByteBuffer.wrap(bytes));
	}

	public byte get() {
		checkReleased();
		return buffer.get();
	}

	public DynamicByteBuffer put(byte b) {
		ensureCapacity(1);
		buffer.put(b);
		return this;
	}

	public byte get(int index) {
		checkReleased();
		return buffer.get(index);
	}

	public DynamicByteBuffer put(int index, byte b) {
		ensureCapacity(1);
		buffer.put(index, b);
		return this;
	}

	public DynamicByteBuffer get(byte[] dst, int offset, int length) {
		checkReleased();
		buffer.get(dst, offset, length);
		return this;
	}

	public DynamicByteBuffer get(byte[] dst) {
		checkReleased();
		buffer.get(dst);
		return this;
	}

	public DynamicByteBuffer put(ByteBuffer src) {
		ensureCapacity(src.remaining());
		buffer.put(src);
		return this;
	}

	public DynamicByteBuffer put(byte[] src, int offset, int length) {
		ensureCapacity(length);
		buffer.put(src, offset, length);
		return this;
	}

	public DynamicByteBuffer put(byte[] src) {
		ensureCapacity(src.length);
		buffer.put(src);
		return this;
	}

	public char getChar() {
		checkReleased();
		return buffer.getChar();
	}

	public DynamicByteBuffer putChar(char value) {
		ensureCapacity(2);
		buffer.putChar(value);
		return this;
	}

	public char getChar(int index) {
		checkReleased();
		return buffer.getChar(index);
	}

	public DynamicByteBuffer putChar(int index, char value) {
		ensureCapacity(2);
		buffer.putChar(index, value);
		return this;
	}

	public short getShort() {
		checkReleased();
		return buffer.getShort();
	}

	public DynamicByteBuffer putShort(short value) {
		ensureCapacity(2);
		buffer.putShort(value);
		return this;
	}

	public short getShort(int index) {
		checkReleased();
		return buffer.getShort(index);
	}

	public DynamicByteBuffer putShort(int index, short value) {
		buffer.putShort(index, value);
		return this;
	}

	public int getInt() {
		checkReleased();
		return buffer.getInt();
	}

	public DynamicByteBuffer putInt(int value) {
		ensureCapacity(4);
		buffer.putInt(value);
		return this;
	}

	public int getInt(int index) {
		checkReleased();
		return buffer.getInt(index);
	}

	public DynamicByteBuffer putInt(int index, int value) {
		ensureCapacity(4);
		buffer.putInt(index, value);
		return this;
	}

	public long getLong() {
		checkReleased();
		return buffer.getLong();
	}

	public DynamicByteBuffer putLong(long value) {
		ensureCapacity(8);
		buffer.putLong(value);
		return this;
	}

	public long getLong(int index) {
		checkReleased();
		return buffer.getLong(index);
	}

	public DynamicByteBuffer putLong(int index, long value) {
		ensureCapacity(8);
		buffer.putLong(index, value);
		return this;
	}

	public float getFloat() {
		checkReleased();
		return buffer.getFloat();
	}

	public DynamicByteBuffer putFloat(float value) {
		ensureCapacity(4);
		buffer.putFloat(value);
		return this;
	}

	public float getFloat(int index) {
		checkReleased();
		return buffer.getFloat(index);
	}

	public DynamicByteBuffer putFloat(int index, float value) {
		ensureCapacity(4);
		buffer.putFloat(index, value);
		return this;
	}

	public double getDouble() {
		checkReleased();
		return buffer.getDouble();
	}

	public DynamicByteBuffer putDouble(double value) {
		ensureCapacity(8);
		buffer.putDouble(value);
		return this;
	}

	public double getDouble(int index) {
		checkReleased();
		return buffer.getDouble(index);
	}

	public DynamicByteBuffer putDouble(int index, double value) {
		ensureCapacity(8);
		buffer.putDouble(index, value);
		return this;
	}

	public DynamicByteBuffer flip() {
		checkReleased();
		buffer.flip();
		return this;
	}

	public int remaining() {
		checkReleased();
		return buffer.remaining();
	}

	public int position() {
		checkReleased();
		return buffer.position();
	}

	public DynamicByteBuffer duplicate() {
		checkReleased();
		return new DynamicByteBuffer(allocator, buffer.duplicate());
	}

	public DynamicByteBuffer position(int newPosition) {
		checkReleased();
		buffer.position(newPosition);
		return this;
	}

	public DynamicByteBuffer compact() {
		checkReleased();
		buffer.compact();
		return this;
	}

	public byte[] array() {
		checkReleased();
		return buffer.array();
	}

	public boolean hasRemaining() {
		return buffer.hasRemaining();
	}

	public boolean isDirect() {
		checkReleased();
		return buffer.isDirect();
	}

	public int limit() {
		checkReleased();
		return buffer.limit();
	}

	public DynamicByteBuffer limit(int newLimit) {
		checkReleased();
		buffer.limit(newLimit);
		return this;
	}

	public int capacity() {
		checkReleased();
		return buffer.capacity();
	}

	public DynamicByteBuffer mark() {
		checkReleased();
		buffer.mark();
		mark = buffer.position();
		return this;
	}

	public DynamicByteBuffer reset() {
		checkReleased();
		buffer.reset();
		return this;
	}

	public DynamicByteBuffer clear() {
		checkReleased();
		buffer.clear();
		mark = -1;
		return this;
	}

	public void release() {
		checkReleased();
		allocator.release(buffer);
		buffer = null;
		allocator = null;
	}

	public boolean isReleased() {
		return buffer == null;
	}

	public ByteBuffer unwrap() {
		checkReleased();
		return buffer;
	}

	private void checkReleased() {
		if (isReleased()) {
			throw new IllegalStateException("Buffer was released.");
		}
	}

	public void ensureCapacity(int capacity) {
		checkReleased();
		if (buffer.remaining() < capacity) {
			expandBufferBy(capacity);
		}
	}

	private void expandBufferBy(int capacity) {
		int oldPosition = buffer.position();
		int newCapacity = oldPosition + capacity;
		ByteBuffer newBuffer = allocator.allocate(newCapacity);

		buffer.flip();
		newBuffer.put(buffer);
		allocator.release(buffer);

		if (mark > -1) {
			newBuffer.position(mark);
			newBuffer.mark();
		}

		newBuffer.position(oldPosition);
		buffer = newBuffer;
	}

	private static final class HeapByteBufferAllocator implements ByteBufferAllocator {

		@Override
		public ByteBuffer allocate(int capacity) {
			return ByteBuffer.allocate(capacity);
		}

		@Override
		public void release(ByteBuffer buffer) {
			buffer.clear();
		}
	}

	private static final class DirectByteBufferAllocator implements ByteBufferAllocator {

		@Override
		public ByteBuffer allocate(int capacity) {
			return ByteBuffer.allocateDirect(capacity);
		}

		@Override
		public void release(ByteBuffer buffer) {
			buffer.clear();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DynamicByteBuffer that = (DynamicByteBuffer) o;

		return !(buffer != null ? !buffer.equals(that.buffer) : that.buffer != null);
	}

	@Override
	public int hashCode() {
		return buffer != null ? buffer.hashCode() : 0;
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName() + "[buffer=" + buffer + ']';
	}
}
