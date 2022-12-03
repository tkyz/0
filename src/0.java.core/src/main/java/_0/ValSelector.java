package _0;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ValSelector {

	private Object root   = null;

	private Object select = null;

	private ValSelector() {
	}

	public static ValSelector of(List<?> list) {

		ValSelector selector = new ValSelector();
		selector.root = list;
		selector.reset();

		return selector;

	}

	public static ValSelector of(Map<?, ?> map) {

		ValSelector selector = new ValSelector();
		selector.root = map;
		selector.reset();

		return selector;

	}

	public ValSelector of() {

		ValSelector selector = new ValSelector();
		selector.root = this.select;
		selector.reset();

		reset();

		return selector;

	}

	public void reset() {
		select = root;
	}

	public Set<?> keys() {
		return ((Map<?, ?>)select).keySet();
	}

	public int size() {
		return ((List<?>)select).size();
	}

	public ValSelector get(Object... keys) {

		for (Object key : keys) {

			if (null == select) {
				break;
			}

			if (select instanceof Map) {
				select = ((Map<?, ?>)select).get(key);

			} else if (select instanceof List) {
				select = ((List<?>)select).get(((Integer)key).intValue());

			} else {
				throw new IllegalArgumentException(String.valueOf(key));
			}

		}

		return this;

	}

	public <T> T val() {

		@SuppressWarnings("unchecked")
		T val = (T)select;

		reset();

		return val;

	}

	public static <T> T val(List<?> list, Object... keys) {
		return of(list).get(keys).val();
	}

	public static <T> T val(Map<?, ?> map, Object... keys) {
		return of(map).get(keys).val();
	}

	@Override
	public String toString() {
		return select.toString();
	}

}
