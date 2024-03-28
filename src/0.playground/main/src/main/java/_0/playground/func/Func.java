package _0.playground.func;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Func<T> implements Callable<Void> {

	private static final Logger log = LoggerFactory.getLogger(Func.class);

	private String              key = null;
	private Map<String, Object> val = null;

	public Func(final String key, final Map<String, Object> val) {
		this.key = key;
		this.val = val;
	}

	protected String key() {
		return key;
	}

	protected Map<String, Object> val() {
		return val;
	}

	@SuppressWarnings("unchecked")
	protected T cast() {
		return (T)key;
	}

	@Override
	public Void call()
			throws Exception {

		log.debug("{}", key);

		return null;

	}

}
