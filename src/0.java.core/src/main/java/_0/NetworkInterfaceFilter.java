package _0;

import java.net.NetworkInterface;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class NetworkInterfaceFilter implements Function<NetworkInterface, Boolean>, Predicate<NetworkInterface> {

	@SuppressWarnings("serial")
	private static final Set<String> prefixes = new HashSet<String>() {
		{
			add("en"); // eth
			add("sl"); // slip
			add("wl"); // wlan
			add("ww"); // wwan
		}
	};

	@Override
	public Boolean apply(final NetworkInterface nif) {
		return Boolean.valueOf(test(nif));
	}

	@Override
	public boolean test(final NetworkInterface nif) {
		return nif.getName().matches("^(" + String.join("|", prefixes) + ")p[0-9]+s[0-9a-f]+$");
	}

}
