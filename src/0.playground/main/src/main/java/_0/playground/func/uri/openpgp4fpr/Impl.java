package _0.playground.func.uri.openpgp4fpr;

import java.util.Map;

import _0.playground.func.Func;

public class Impl extends Func<String> {

	public Impl(final String key, final Map<String, Object> val) {
		super(key, val);
	}

	@Override
	public String cast() {
		String key = key();
		return key.startsWith("openpgp4fpr:") ? key.substring("openpgp4fpr:".length()) : null;
	}

}
