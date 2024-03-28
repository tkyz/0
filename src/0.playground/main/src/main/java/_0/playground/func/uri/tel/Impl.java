package _0.playground.func.uri.tel;

import java.util.Map;

import _0.playground.func.Func;

public class Impl extends Func<String> {

	public Impl(final String key, final Map<String, Object> val) {
		super(key, val);
	}

	@Override
	public String cast() {
		String key = key();
		return key.startsWith("tel:") ? key.substring("tel:".length()) : null;
	}

}
