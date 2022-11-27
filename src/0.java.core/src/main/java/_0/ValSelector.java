package _0;

import java.util.List;
import java.util.Map;

public final class ValSelector {

	private Object root   = null;

	private Object select = null;

	private ValSelector() {
	}

	public static ValSelector of(List<?> list) {

		ValSelector instance = new ValSelector();
		instance.root = list;
		instance.reset();

		return instance;

	}

	public static ValSelector of(Map<?, ?> map) {

		ValSelector instance = new ValSelector();
		instance.root = map;
		instance.reset();

		return instance;

	}

	public void reset() {
		select = root;
	}

	public ValSelector get(Object key) {
		select = ((Map<?, ?>)select).get(key);
		return this;
	}

	public ValSelector get(int i) {
		select = ((List<?>)select).get(i);
		return this;
	}

	public <T> T val() {

		@SuppressWarnings("unchecked")
		T val = (T)select;

		reset();

		return val;

	}

	public static <T> T val(List<?> list, Object... keys) {

		ValSelector selector = ValSelector.of(list);
		for (Object key : keys) {
			selector = selector.get(key);
		}

		return selector.val();

	}

	public static <T> T val(Map<?, ?> map, Object... keys) {

		ValSelector selector = ValSelector.of(map);
		for (Object key : keys) {
			selector = selector.get(key);
		}

		return selector.val();

	}

}
