package utils;

import java.util.HashSet;

public class ConcurrentHashSet<K> extends HashSet<K> {

	@Override
	public boolean add(K e) {
		synchronized (this) {

			return super.add(e);
		}
	}

	@Override
	public void clear() {
		synchronized (this) {
			super.clear();
		}
	}

	@Override
	public int size() {
		synchronized (this) {
			return super.size();
		}
	}

}
