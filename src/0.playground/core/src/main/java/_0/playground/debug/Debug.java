package _0.playground.debug;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import _0.playground.core._0;

public final class Debug {

	public static void println() {
		println(false);
	}

	public static void println(final boolean all) {

		Comparator<Entry<?, ?>> sort = (o1, o2) -> _0.compare(o1.getKey().toString(), o2.getKey().toString());

		String path_separator = System.getProperty("path.separator");

		System.out.println("---");
		System.out.println(Debug.class.getName() + "." + _0.methodName() + ":");

		// env
		if (all) {

			Set<String> set = new HashSet<>();
			set.add("classpath");
			set.add("path");
			set.add("pathext");
			set.add("psmodulepath");
			set.add("session_manager");

			System.out.println("  env:");
			for (Entry<String, String> entry : System.getenv().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

				if (set.contains(entry.getKey().toLowerCase())) {

					String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
					System.out.println("    " + entry.getKey() + ":");
					for (String path : paths) {
						System.out.println("      - \"" + path + "\"");
					}

				} else {
					System.out.println("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
				}

			}

		}

		// prop
		if (all) {

			Set<String> set = new HashSet<>();
			set.add("java.class.path");
			set.add("java.library.path");

			System.out.println("  prop:");
			for (Entry<Object, Object> entry : System.getProperties().entrySet().stream().sorted(sort).collect(Collectors.toList())) {

				if (set.contains(entry.getKey().toString().toLowerCase())) {

					String[] paths = entry.getValue().toString().split(Pattern.quote(path_separator));
					System.out.println("    " + entry.getKey() + ":");
					for (String path : paths) {
						System.out.println("      - \"" + path + "\"");
					}

				} else {
					System.out.println("    " + entry.getKey() + ": \"" + entry.getValue() + "\"");
				}

			}

		}

		if (true) {

			Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

			List<Thread> keys = new LinkedList<>(map.keySet());
			Collections.sort(keys, (o1, o2) -> (int)(o1.threadId() - o2.threadId()));

			System.out.println("  threads:");
			for (Thread thread : keys) {

				List<StackTraceElement> stacks = new LinkedList<>();
				{

					StackTraceElement[] items = map.get(thread);
					for (StackTraceElement item : items) {

						if (!all && !item.toString().startsWith("app//_0.")) {
							continue;
						}

						stacks.add(item);

					}

					Collections.reverse(stacks);

				}

				if (_0.empty(stacks)) {
					continue;
				}

				System.out.println("    - id: "   + thread.threadId());
				System.out.println("      name: " + thread.getName());
				System.out.println("      stacktrace:");
				for (StackTraceElement stack : stacks) {
					System.out.println("        - " + stack);
				}

			}

		}

	}

}
