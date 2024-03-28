package _0.playground.func.uri.mailto;

import java.util.Map;

import _0.playground.func.Func;

public class Impl extends Func<String> {

	public Impl(String key, Map<String, Object> val) {
		super(key, val);
	}

	@Override
	public String cast() {
		String key = key();
		return key.startsWith("mailto:") ? key.substring("mailto:".length()) : null;
	}

}
